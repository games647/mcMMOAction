package com.github.games647.mcmmoaction;

import com.comphenix.protocol.AsynchronousManager;
import com.comphenix.protocol.ProtocolLibrary;
import com.gmail.nossr50.datatypes.skills.AbilityType;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.datatypes.skills.ToolType;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.util.StringUtils;
import com.google.common.collect.ImmutableSet;

import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.plugin.java.JavaPlugin;

public class mcMMOAction extends JavaPlugin {

    //compile the pattern just once
    private final Pattern numberRemover = Pattern.compile("[0-9]");
    //create a immutable set in order to be thread-safe and faster than normal sets
    private ImmutableSet<String> localizedMessages;

    @Override
    public void onEnable() {
        loadAllMessages();

        AsynchronousManager asyncManager = ProtocolLibrary.getProtocolManager().getAsynchronousManager();
        asyncManager.registerAsyncHandler(new MessageListener(this)).start();
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

        //messages that cannot be retrieved dynmaically because the message key isn't in the enum getSkillAbilities()
        builder.add(getLocalizedMessage("Herbalism.Ability.ShroomThumb.Fail"));
        builder.add(getLocalizedMessage("Herbalism.Ability.GTh.Fail"));
        builder.add(getLocalizedMessage("Herbalism.Ability.GTh"));
        builder.add(getLocalizedMessage("Mining.Blast.Boom"));
        builder.add(getLocalizedMessage("Acrobatics.Roll.Text"));
        builder.add(getLocalizedMessage("Acrobatics.Ability.Proc"));
        builder.add(getLocalizedMessage("Acrobatics.Combat.Proc"));
        builder.add(getLocalizedMessage("Swords.Combat.Bleeding"));
        builder.add(getLocalizedMessage("Swords.Combat.Bleeding.Stopped"));
        builder.add(getLocalizedMessage("Swords.Combat.Countered"));

        //non skill type specific messages
        builder.add(getLocalizedMessage("Combat.ArrowDeflect"));
        builder.add(getLocalizedMessage("Combat.BeastLore"));
        builder.add(getLocalizedMessage("Combat.Gore"));
        builder.add(getLocalizedMessage("Combat.StruckByGore"));
        builder.add(getLocalizedMessage("Item.ChimaeraWing.Fail"));
        builder.add(getLocalizedMessage("Item.ChimaeraWing.Pass"));

        //general messages
        builder.add(getLocalizedMessage("Ability.Generic.Refresh"));
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
