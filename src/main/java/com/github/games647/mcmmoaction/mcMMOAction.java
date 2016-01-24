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

import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.plugin.java.JavaPlugin;

public class mcMMOAction extends JavaPlugin {

    private static final String BUNDLE_ROOT = "com.gmail.nossr50.locale.locale";
    private static final String NOTIFCATION_IDENTIFIER = "**";

    //compile the pattern just once
    private final Pattern numberRemover = Pattern.compile("[0-9]");
    //create a immutable set in order to be thread-safe and faster than normal sets
    private ImmutableSet<String> localizedMessages;

    @Override
    public void onEnable() {
        loadAllMessages();

        //the event could and should be executed async, but if we try to use it with other sync listeners
        //the sending order gets mixed up
//        AsynchronousManager asyncManager = ProtocolLibrary.getProtocolManager().getAsynchronousManager();
//        asyncManager.registerAsyncHandler(new MessageListener(this)).start();
        ProtocolManager protManager = ProtocolLibrary.getProtocolManager();
        protManager.addPacketListener(new MessageListener(this));
    }

    //this method has to be thread-safe
    public boolean isMcmmoMessage(String plainText) {
        //remove the numbers to match the string easier
        String cleanedMessage = numberRemover.matcher(plainText).replaceAll("");
        return localizedMessages.contains(cleanedMessage);
    }

    private void loadAllMessages() {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (SkillType skillType : SkillType.values()) {
            if (!skillType.isChildSkill()) {
                String messageKey = StringUtils.getCapitalized(skillType.toString()) + ".Skillup";
                builder.add(getLocalizedMessage(messageKey, 0, 0));
            }

            AbilityType ability = skillType.getAbility();
            if (ability != null) {
                builder.add(ChatColor.stripColor(ability.getAbilityOn()));
                builder.add(ChatColor.stripColor(ability.getAbilityOff()));
            }

            ToolType tool = skillType.getTool();
            if (tool != null) {
                builder.add(ChatColor.stripColor(tool.getRaiseTool()));
                builder.add(ChatColor.stripColor(tool.getLowerTool()));
            }
        }

        //messages that cannot be retrieved dynmaically because the message key isn't in (or equal as)
        //the enum getSkillAbilities() - SecondaryAbilities
        builder.add(getLocalizedMessage("Axes.Combat.SS.Struck"));

        builder.add(getLocalizedMessage("Axes.Combat.CriticalHit"));
        builder.add(getLocalizedMessage("Axes.Combat.CritStruck"));

        builder.add(getLocalizedMessage("Swords.Combat.Bleeding"));
        builder.add(getLocalizedMessage("Swords.Combat.Bleeding.Stopped"));

        //general message
        builder.add(getLocalizedMessage("Skills.TooTired", 0));

        loadingByIdentifier(builder);

        localizedMessages = builder.build();
    }

    private void loadingByIdentifier(ImmutableSet.Builder<String> builder) {
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

    private String getLocalizedMessage(String key, Object... messageArgs) {
        String localizedMessage = LocaleLoader.getString(key, messageArgs);
        //strip color to match faster and easier
        String plainMessageText = ChatColor.stripColor(localizedMessage);
        //remove all numbers in order to match it with the sent message in general
        return numberRemover.matcher(plainMessageText).replaceAll("");
    }
}
