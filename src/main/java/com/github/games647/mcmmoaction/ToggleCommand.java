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
                sender.sendMessage(ChatColor.DARK_GREEN + "Notifications will be displayed in chat");
            } else {
                disabledActionBar.add(uniqueId);
                sender.sendMessage(ChatColor.DARK_GREEN + "You'll see notification in the action bar");
            }
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Only players can see the actionbar");
        }

        return true;
    }
}
