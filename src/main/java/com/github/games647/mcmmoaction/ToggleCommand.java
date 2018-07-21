package com.github.games647.mcmmoaction;

import java.util.Collection;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class ToggleCommand implements CommandExecutor {

    private final mcMMOAction plugin;

    ToggleCommand(mcMMOAction plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = ((Player) sender);

            if (args.length > 0) {
                if ("progress".equalsIgnoreCase(args[0])) {
                    toggleProgress(player);
                    return true;
                }

                sendLocaleMessage(sender, "unknown-argument");
                return true;
            }

            toggle(player, plugin.getActionBarDisabled(), "toggle-actionbar", "toggle-chat");
            return true;
        }

        sendLocaleMessage(sender, "no-console");
        return true;
    }

    private void toggleProgress(Player player) {
        if (!plugin.getConfiguration().isProgressEnabled()) {
            sendLocaleMessage(player, "progress-global-disabled");
            return;
        }

        toggle(player, plugin.getProgressBarDisabled(), "progress-enable", "progress-disable");
    }

    private void toggle(Player player, Collection<UUID> disabledLst, String enableKey, String disabledKey) {
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
