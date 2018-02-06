package com.github.games647.mcmmoaction.progress;

import com.github.games647.mcmmoaction.mcMMOAction;
import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import static java.lang.String.valueOf;

public class ExperienceGainListener implements Listener {

    private final mcMMOAction plugin;
    private final MessageFormatter<Player, String> formatter = new MessageFormatter<>();

    public ExperienceGainListener(mcMMOAction plugin) {
        this.plugin = plugin;

        formatter.addReplacer("skill-type", (player, skill) -> skill);
        formatter.addReplacer("exp", (player, skill) -> valueOf(ExperienceAPI.getXP(player, skill)));
        formatter.addReplacer("exp-remaining", (player, skill) -> valueOf(ExperienceAPI.getXPRemaining(player, skill)));
        formatter.addReplacer("current-lvl", (player, skill) -> valueOf(ExperienceAPI.getLevel(player, skill)));
        formatter.addReplacer("next-lvl", (player, skill) -> valueOf(ExperienceAPI.getLevel(player, skill) + 1));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExperienceGain(McMMOPlayerXpGainEvent experienceEvent) {
        Player player = experienceEvent.getPlayer();
        if (plugin.isProgressEnabled(player) && !plugin.isDisabledProgress(experienceEvent.getSkill())) {
            String template = plugin.getConfig().getString("progress-msg");
            String message = replaceVariables(experienceEvent, template);
            plugin.sendActionMessage(player, message);
        }
    }

    private String replaceVariables(McMMOPlayerXpGainEvent experienceEvent, String template) {
        Player player = experienceEvent.getPlayer();
        SkillType skill = experienceEvent.getSkill();

        String coloredMessage = ChatColor.translateAlternateColorCodes('&', template);
        return formatter.replace(player, skill.getName(), coloredMessage);
    }
}