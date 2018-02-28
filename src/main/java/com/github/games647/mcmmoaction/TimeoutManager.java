package com.github.games647.mcmmoaction;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class TimeoutManager implements Listener {

    private static final int TIMEOUT = 2;

    private final Map<UUID, Instant> lastNotifications = new ConcurrentHashMap<>();

    @EventHandler
    public void onQuit(PlayerQuitEvent quitEvent) {
        UUID uniqueId = quitEvent.getPlayer().getUniqueId();
        lastNotifications.remove(uniqueId);
    }

    public boolean isAllowed(UUID uniqueId) {
        Instant now = Instant.now();
        Instant lastNotification = lastNotifications.get(uniqueId);

        if (lastNotification == null || Duration.between(lastNotification, now).getSeconds() >= TIMEOUT) {
            //update the current time
            lastNotifications.put(uniqueId, now);
            return true;
        }

        return false;
    }
}
