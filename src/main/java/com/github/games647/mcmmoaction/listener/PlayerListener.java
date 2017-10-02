package com.github.games647.mcmmoaction.listener;

import com.github.games647.mcmmoaction.mcMMOAction;
import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final mcMMOAction plugin;

    public PlayerListener(mcMMOAction plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        Player player = quitEvent.getPlayer();
        plugin.getActionBarDisabled().remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExperienceGain(McMMOPlayerXpGainEvent experienceEvent) {
        Player player = experienceEvent.getPlayer();
        if (plugin.isProgressEnabled(player.getUniqueId())) {
            String message = plugin.getConfig().getString("progress-msg");
            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);

            coloredMessage = replaceVariables(experienceEvent, coloredMessage, player);
            plugin.sendActionMessage(player, coloredMessage);
        }
    }

    private String replaceVariables(McMMOPlayerXpGainEvent experienceEvent, String template, Player player) {
        SkillType skill = experienceEvent.getSkill();

        String skillName = skill.getName();
        int xpToNextLevel = ExperienceAPI.getXPToNextLevel(player, skillName);
        int xp = ExperienceAPI.getXP(player, skillName);
        int level = ExperienceAPI.getLevel(player, skillName);
        return template.replace("{skill-type}", skillName)
                .replace("{exp}", String.valueOf(xp))
                .replace("{exp-remaining}", String.valueOf(xpToNextLevel))
                .replace("{current-lvl}", String.valueOf(level))
                .replace("{next-lvl}", String.valueOf(level + 1));
    }
}
