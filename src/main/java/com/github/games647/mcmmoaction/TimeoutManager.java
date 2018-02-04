package com.github.games647.mcmmoaction;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class TimeoutManager implements Listener {

    private final Map<UUID, Instant> lastNotifications = new HashMap<>();

    @EventHandler
    public void onQuit(PlayerQuitEvent quitEvent) {
        UUID uniqueId = quitEvent.getPlayer().getUniqueId();
        lastNotifications.remove(uniqueId);
    }

    public boolean isAllowed(Player player) {
        UUID uniqueId = player.getUniqueId();

        Instant now = Instant.now();
        Instant lastNotification = lastNotifications.get(uniqueId);

        //update the current time
        lastNotifications.put(uniqueId, now);
        return lastNotification == null || Duration.between(lastNotification, now).getSeconds() > 2;
    }
}
