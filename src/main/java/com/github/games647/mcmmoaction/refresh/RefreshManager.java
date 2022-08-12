package com.github.games647.mcmmoaction.refresh;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.github.games647.mcmmoaction.MessageListener;
import com.github.games647.mcmmoaction.TimeoutManager;
import com.github.games647.mcmmoaction.config.SoundConfig;
import com.github.games647.mcmmoaction.mcMMOAction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static com.comphenix.protocol.PacketType.Play.Server.CHAT;

public class RefreshManager extends MessageListener implements Listener {

    public static final int DEFAULT_DISAPPEAR_TIME = 2;

    private final TimeoutManager timeoutManager;
    private final Map<UUID, Integer> runningTimers = new HashMap<>();
    private final int appearanceTime;

    public RefreshManager(mcMMOAction plugin, TimeoutManager timeoutManager, int appearanceTime) {
        super(plugin, params().types(CHAT).listenerPriority(ListenerPriority.HIGH));
        this.timeoutManager = timeoutManager;
        this.appearanceTime = appearanceTime;
    }

    @Override
    public void onPacketSending(PacketEvent packetEvent) {
        PacketContainer packet = packetEvent.getPacket();
        if (packetEvent.isCancelled() || isOurPacket(packet)) {
            return;
        }

        Player player = packetEvent.getPlayer();
        ChatType chatType = readChatPosition(packet);
        if (chatType == ChatType.GAME_INFO) {
            // new action message - we should stop refreshing our messages to not override it
            cancelTimer(player.getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent quitEvent) {
        //player left stop timer
        Player player = quitEvent.getPlayer();
        cancelTimer(player.getUniqueId());
    }

    protected void cancelTimer(UUID uuid) {
        Integer taskId = runningTimers.remove(uuid);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    /**
     * Sends the action bar message using packets in order to be compatible with 1.8
     *
     * @param receiver the receiver of this message
     * @param message  the message content
     */
    public void sendActionMessage(Player receiver, String message) {
        //cancel previous timer
        cancelTimer(receiver.getUniqueId());

        playNotificationSound(receiver);
        sendMessagePacket(receiver, message);

        if (appearanceTime > 2) {
            int refreshes = Math.floorDiv(appearanceTime - DEFAULT_DISAPPEAR_TIME, DEFAULT_DISAPPEAR_TIME) + 1;
            RefreshTimer timer = new RefreshTimer(this, receiver, message, refreshes);

            int delay = appearanceTime % DEFAULT_DISAPPEAR_TIME;
            int taskId = timer.runTaskTimer(plugin, delay * 20L - 10L, DEFAULT_DISAPPEAR_TIME * 20L).getTaskId();
            runningTimers.put(receiver.getUniqueId(), taskId);
        }
    }

    public void playNotificationSound(Player player) {
        SoundConfig soundConfig = plugin.getConfiguration().getSoundConfig();

        Sound sound = soundConfig.getSound();
        if (sound != null && timeoutManager.isAllowed(player.getUniqueId())) {
            float volume = soundConfig.getVolume();
            float pitch = soundConfig.getPitch();
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }


    protected void sendMessagePacket(Player receiver, String message) {
        PacketContainer chatPacket = new PacketContainer(CHAT);
        chatPacket.getChatComponents().write(0, WrappedChatComponent.fromText(message));
        writeChatPosition(chatPacket);

        // ignore our own packets
        chatPacket.setMeta(plugin.getName(), true);
        chatPacket.getUUIDs().writeSafely(0, new UUID(0, 0));
        ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, chatPacket);
    }

    private void writeChatPosition(PacketContainer packet) {
        ChatType infoPosition = ChatType.GAME_INFO;
        if (plugin.supportsChatTypeEnum()) {
            packet.getChatTypes().write(0, infoPosition);
        } else {
            packet.getBytes().write(0, infoPosition.getId());
        }
    }
}
