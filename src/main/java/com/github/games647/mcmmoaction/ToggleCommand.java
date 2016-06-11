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
            UUID uniqueId = ((Player) sender).getUniqueId();

            Set<UUID> disabledActionBar = plugin.getDisabledActionBar();
            if (disabledActionBar.contains(uniqueId)) {
                disabledActionBar.remove(uniqueId);
                sendLocaleMessage(sender, "toggle-chat");
            } else {
                disabledActionBar.add(uniqueId);
                sendLocaleMessage(sender, "toggle-actionbar");
            }
        } else {
            sendLocaleMessage(sender, "no-console");
        }

        return true;
    }

    private void sendLocaleMessage(CommandSender sender, String key) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(key)));
    }
}
