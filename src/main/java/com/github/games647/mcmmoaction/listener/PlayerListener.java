package com.github.games647.mcmmoaction.listener;

import com.github.games647.mcmmoaction.mcMMOAction;
import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static net.md_5.bungee.api.ChatMessageType.ACTION_BAR;

public class PlayerListener implements Listener {

    private final mcMMOAction plugin;

    public PlayerListener(mcMMOAction plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent quitEvent) {
        Player player = quitEvent.getPlayer();
        plugin.getDisabledActionBar().remove(player.getUniqueId());
    }

    @EventHandler
    public void onExperienceGain(McMMOPlayerXpGainEvent experienceEvent) {
        if (plugin.getConfiguration().isProgressEnabled()) {
            String message = plugin.getConfig().getString("progress-msg");
            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);

            Player player = experienceEvent.getPlayer();
            coloredMessage = replaceVariables(experienceEvent, coloredMessage, player);

            player.spigot().sendMessage(ACTION_BAR, TextComponent.fromLegacyText(coloredMessage));
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
