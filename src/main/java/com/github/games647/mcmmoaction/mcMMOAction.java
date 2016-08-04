package com.github.games647.mcmmoaction;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.gmail.nossr50.datatypes.skills.AbilityType;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.datatypes.skills.ToolType;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.StringUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class mcMMOAction extends JavaPlugin {

    private static final String BUNDLE_ROOT = "com.gmail.nossr50.locale.locale";
    private static final String NOTIFCATION_IDENTIFIER = "**";

    //compile the pattern just once - remove the comma so it also detect numbers like (10,000)
    private final Pattern numberRemover = Pattern.compile("[,0-9]");
    //create a immutable set in order to be thread-safe and faster than normal sets
    private ImmutableSet<String> localizedMessages;

    private final Set<UUID> disabledActionBar = Sets.newHashSet();

    //notification sound
    private boolean soundEnabled;
    private Sound sound;
    private float volume;
    private float pitch;

    @Override
    public void onEnable() {
        loadConfig();

        loadAllMessages();

        getServer().getPluginManager().registerEvents(new QuitListener(this), this);
        getCommand("mmoaction").setExecutor(new ToggleCommand(this));

        //the event could and should be executed async, but if we try to use it with other sync listeners
        //the sending order gets mixed up
//        AsynchronousManager asyncManager = ProtocolLibrary.getProtocolManager().getAsynchronousManager();
//        asyncManager.registerAsyncHandler(new MessageListener(this)).start();
        ProtocolManager protManager = ProtocolLibrary.getProtocolManager();
        protManager.addPacketListener(new MessageListener(this));
    }

    public Set<UUID> getDisabledActionBar() {
        return disabledActionBar;
    }

    //this method has to be thread-safe
    public boolean isMcmmoMessage(String plainText) {
        //remove the numbers to match the string easier
        String cleanedMessage = numberRemover.matcher(plainText).replaceAll("");
        return localizedMessages.contains(cleanedMessage);
    }

    public void playNotificationSound(Player player) {
        if (soundEnabled && sound != null) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    private void loadAllMessages() {
        Set<String> messages = Sets.newHashSet();

        loadingByIdentifier(messages);

        for (SkillType skillType : SkillType.values()) {
            if (!skillType.isChildSkill()) {
                String messageKey = StringUtils.getCapitalized(skillType.toString()) + ".Skillup";
                String localizedMessage = getLocalizedMessage(messageKey);
                addOrRemove(messages, localizedMessage, getConfig().getBoolean("ignore.levelup"));
            }

            AbilityType ability = skillType.getAbility();
            if (ability != null) {
                String abilityOn = ChatColor.stripColor(ability.getAbilityOn());
                String abilityOff = ChatColor.stripColor(ability.getAbilityOff());
                addOrRemove(messages, abilityOn, getConfig().getBoolean("ignore.ability"));
                addOrRemove(messages, abilityOff, getConfig().getBoolean("ignore.ability"));
            }

            ToolType tool = skillType.getTool();
            if (tool != null) {
                addOrRemove(messages, ChatColor.stripColor(tool.getRaiseTool()), getConfig().getBoolean("ignore.tool"));
                addOrRemove(messages, ChatColor.stripColor(tool.getLowerTool()), getConfig().getBoolean("ignore.tool"));
            }
        }

        //messages that cannot be retrieved dynmaically because the message key isn't in (or equal as)
        //the enum getSkillAbilities() - SecondaryAbilities
        messages.add(getLocalizedMessage("Axes.Combat.SS.Struck"));

        messages.add(getLocalizedMessage("Axes.Combat.CriticalHit"));
        messages.add(getLocalizedMessage("Axes.Combat.CritStruck"));

        messages.add(getLocalizedMessage("Swords.Combat.Counter.Hit"));
        messages.add(getLocalizedMessage("Swords.Combat.Bleeding"));
        messages.add(getLocalizedMessage("Swords.Combat.Bleeding.Started"));
        messages.add(getLocalizedMessage("Swords.Combat.Bleeding.Stopped"));

        //hardcore messages
        boolean hardcoreIgnore = getConfig().getBoolean("ignore.hardcore");
        addOrRemove(messages, getLocalizedMessage("Hardcore.DeathStatLoss.PlayerDeath"), hardcoreIgnore);
        addOrRemove(messages, getLocalizedMessage("Hardcore.Vampirism.Killer.Failure"), hardcoreIgnore);
        addOrRemove(messages, getLocalizedMessage("Hardcore.Vampirism.Killer.Success"), hardcoreIgnore);
        addOrRemove(messages, getLocalizedMessage("Hardcore.Vampirism.Victim.Failure"), hardcoreIgnore);
        addOrRemove(messages, getLocalizedMessage("Hardcore.Vampirism.Victim.Success"), hardcoreIgnore);

        //general message
        addOrRemove(messages, getLocalizedMessage("Skills.TooTired"), getConfig().getBoolean("ignore.tooTired"));

        //explicit added messages
        for (String key : getConfig().getStringList("others")) {
            messages.add(getLocalizedMessage(key));
        }

        //explicit ignored messages
        for (String key : getConfig().getStringList("ignore.others")) {
            messages.remove(getLocalizedMessage(key));
        }

        localizedMessages = ImmutableSet.copyOf(messages);
    }

    private void addOrRemove(Set<String> messages, String message, boolean ignore) {
        if (ignore) {
            messages.remove(message);
        } else {
            messages.add(message);
        }
    }

    private void loadingByIdentifier(Set<String> builder) {
        ClassLoader classLoader = mcMMO.p.getClass().getClassLoader();
        ResourceBundle enBundle = ResourceBundle.getBundle(BUNDLE_ROOT, Locale.US, classLoader);
        for (Enumeration<String> enumeration = enBundle.getKeys(); enumeration.hasMoreElements();) {
            String key = enumeration.nextElement();
            String localizedMessage = getLocalizedMessage(key);
            if (localizedMessage.endsWith(NOTIFCATION_IDENTIFIER)) {
                builder.add(localizedMessage);
            }
        }
    }

    private String getLocalizedMessage(String key) {
        //if the message has less arguments they will be just ignored
        String localizedMessage = LocaleLoader.getString(key, 0, 0, 0, 0);
        //strip color to match faster and easier
        String plainMessageText = ChatColor.stripColor(localizedMessage);
        //remove all numbers in order to match it with the sent message in general
        return numberRemover.matcher(plainMessageText).replaceAll("");
    }

    private void loadConfig() {
        saveDefaultConfig();
        String configCategory = "notification-sound";

        soundEnabled = getConfig().getBoolean(configCategory + ".enabled");
        if (soundEnabled) {
            volume = (float) getConfig().getDouble(configCategory + ".volume");
            pitch = (float) getConfig().getDouble(configCategory + ".pitch");

            String soundType = getConfig().getString(configCategory + ".type");
            try {
                sound = Sound.valueOf(soundType.toUpperCase());
            } catch (IllegalStateException illegalStateException) {
                getLogger().log(Level.WARNING, "Failed to load the sound type", illegalStateException);
                sound = null;
            }
        }
    }
}
