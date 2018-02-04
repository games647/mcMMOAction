package com.github.games647.mcmmoaction.listener;

import com.github.games647.mcmmoaction.mcMMOAction;
import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import static java.lang.String.valueOf;

public class ExperienceGainListener implements Listener {

    private final Map<String, BiFunction<Player, String, String>> replacers = new HashMap<>();
    private final Pattern variablePattern;
    private final mcMMOAction plugin;

    public ExperienceGainListener(mcMMOAction plugin) {
        this.plugin = plugin;

        replacers.put("{skill-type}", (player, skill) -> skill);
        replacers.put("{exp}}", (player, skill) -> valueOf(ExperienceAPI.getXP(player, skill)));
        replacers.put("{exp-remaining}", (player, skill) -> valueOf(ExperienceAPI.getXPRemaining(player, skill)));
        replacers.put("{current-lvl}", (player, skill) -> valueOf(ExperienceAPI.getLevel(player, skill)));
        replacers.put("{next-lvl}", (player, skill) -> valueOf(ExperienceAPI.getLevel(player, skill) + 1));

        StringBuilder builder = new StringBuilder();
        Iterator<String> iterator = replacers.keySet().iterator();
        while (iterator.hasNext()) {
            String var = iterator.next();
            builder.append('(').append(Pattern.quote(var)).append(')');
            if (iterator.hasNext()) {
                builder.append('|');
            }
        }

        variablePattern = Pattern.compile(builder.toString());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExperienceGain(McMMOPlayerXpGainEvent experienceEvent) {
        Player player = experienceEvent.getPlayer();
        if (plugin.isProgressEnabled(player) && !plugin.isDisabledProgress(experienceEvent.getSkill())) {
            String message = plugin.getConfig().getString("progress-msg");
            String coloredMessage = ChatColor.translateAlternateColorCodes('&', message);

            coloredMessage = replaceVariables(experienceEvent, coloredMessage);
            plugin.sendActionMessage(player, coloredMessage);
        }
    }

    private String replaceVariables(McMMOPlayerXpGainEvent experienceEvent, String template) {
        Player player = experienceEvent.getPlayer();
        SkillType skill = experienceEvent.getSkill();

        //StringBuilder is only compatible with Java 9+
        StringBuffer buffer = new StringBuffer();

        Matcher matcher = variablePattern.matcher(template);
        while (matcher.find()) {
            matcher.appendReplacement(buffer, replacers.get(matcher.group()).apply(player, skill.getName()));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
