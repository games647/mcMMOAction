package com.github.games647.mcmmoaction.listener;

import com.github.games647.mcmmoaction.mcMMOAction;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
        plugin.getDisabledActionBar().remove(player.getUniqueId());
    }
}
