package com.github.games647.mcmmoaction;

import java.util.Set;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ToggleCommand implements CommandExecutor {

    private final mcMMOAction plugin;

    public ToggleCommand(mcMMOAction plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = ((Player) sender);

            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("progress")) {
                    toggleProgress(player);
                } else {
                    sendLocaleMessage(sender, "unknown-argument");
                }
            } else {
                toggle(player, plugin.getActionBarDisabled(), "toggle-actionbar", "toggle-chat");
            }
        } else {
            sendLocaleMessage(sender, "no-console");
        }

        return true;
    }

    private void toggleProgress(Player player) {
        if (!plugin.getConfiguration().isProgressEnabled()) {
            sendLocaleMessage(player, "progress-global-disabled");
            return;
        }

        toggle(player, plugin.getProgressBarDisabled(), "progress-enable", "progress-disable");
    }

    private void toggle(Player player, Set<UUID> disabledLst, String enableKey, String disabledKey) {
        UUID uniqueId = player.getUniqueId();
        if (disabledLst.contains(uniqueId)) {
            disabledLst.remove(uniqueId);
            sendLocaleMessage(player, enableKey);
        } else {
            disabledLst.add(uniqueId);
            sendLocaleMessage(player, disabledKey);
        }
    }

    private void sendLocaleMessage(CommandSender sender, String key) {
        String message = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(key));
        sender.sendMessage(message);
    }
}
