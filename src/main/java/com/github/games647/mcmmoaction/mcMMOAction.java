package com.github.games647.mcmmoaction;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.gmail.nossr50.datatypes.skills.AbilityType;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.datatypes.skills.ToolType;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.util.StringUtils;
import com.google.common.collect.ImmutableList;

import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.plugin.java.JavaPlugin;

public class mcMMOAction extends JavaPlugin {

    //compile the pattern just once
    private final Pattern numberRemover = Pattern.compile("[0-9]");
    //create a immutable list in order to be thread-safe and faster than normal lists
    private ImmutableList<String> localizedMessages;

    @Override
    public void onEnable() {
        loadLocales();

        AsynchronousManager asyncManager = ProtocolLibrary.getProtocolManager().getAsynchronousManager();
        asyncManager.registerAsyncHandler(new MessageListener(this)).start();
    }

    //this method has to be thread-safe
    public boolean isMcmmoMessage(String plainText) {
        String cleanedMessage = numberRemover.matcher(plainText).replaceAll("");
        return localizedMessages.contains(cleanedMessage);
    }

    private void loadLocales() {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        for (SkillType skillType : SkillType.values()) {
            String messageKey = StringUtils.getCapitalized(skillType.toString()) + ".Skillup";
            builder.add(getLocalizedMessage(messageKey, 0, 0));

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

        builder.add(getLocalizedMessage("Skills.TooTired", 0));

        localizedMessages = builder.build();
    }

    private String getLocalizedMessage(String key, Object... messageArgs) {
        String localizedMessage = LocaleLoader.getString(key, messageArgs);
        //strip color to match faster and easier
        String plainMessageText = ChatColor.stripColor(localizedMessage);
        //remove all numbers in order to match it with the sent message in general
        return numberRemover.matcher(plainMessageText).replaceAll("");
    }
}
