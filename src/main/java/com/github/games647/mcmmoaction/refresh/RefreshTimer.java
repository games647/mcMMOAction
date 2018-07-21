package com.github.games647.mcmmoaction.refresh;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RefreshTimer extends BukkitRunnable {

    private final RefreshManager refreshManager;

    private final Player player;
    private final String message;

    private int remainingRefresh;

    public RefreshTimer(RefreshManager refreshManager, Player player, String message, int remainingRefresh) {
        this.refreshManager = refreshManager;

        this.player = player;
        this.message = message;

        this.remainingRefresh = remainingRefresh;
    }

    @Override
    public void run() {
        refreshManager.sendMessagePacket(player, message);

        remainingRefresh--;
        if (remainingRefresh <= 0) {
            refreshManager.cancelTimer(player.getUniqueId());
        }
    }
}
