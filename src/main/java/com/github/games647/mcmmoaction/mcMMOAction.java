package com.github.games647.mcmmoaction;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.github.games647.mcmmoaction.listener.MessageListener;
import com.github.games647.mcmmoaction.listener.PlayerListener;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static com.comphenix.protocol.PacketType.Play.Server.CHAT;

public class mcMMOAction extends JavaPlugin {

    private static final String PROGRESS_FILE_NAME = "disabled-progress.txt";
    private static final String ACTIONBAR_FILE_NAME = "disabled-action.txt";

    private final MinecraftVersion currentVersion = MinecraftVersion.getCurrentVersion();
    //in comparison to the ProtocolLib variant this includes the build number
    private final MinecraftVersion explorationUpdate = new MinecraftVersion(1, 11, 2);

    private Set<UUID> actionBarDisabled;
    private Set<UUID> progressBarDisabled;
    private Configuration configuration;

    @Override
    public void onEnable() {
        configuration = new Configuration(this);
        configuration.saveDefault();
        configuration.load();

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getCommand("mmoaction").setExecutor(new ToggleCommand(this));

        //the event could and should be executed async, but if we try to use it with other sync listeners
        //the sending order gets mixed up
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new MessageListener(this, configuration.getMessages()));

        //load disabled lists
        actionBarDisabled = loadDisabled(ACTIONBAR_FILE_NAME);
        progressBarDisabled = loadDisabled(PROGRESS_FILE_NAME);
    }

    @Override
    public void onDisable() {
        saveDisabled(ACTIONBAR_FILE_NAME, actionBarDisabled);
        saveDisabled(PROGRESS_FILE_NAME, progressBarDisabled);
    }

    private Set<UUID> loadDisabled(String fileName) {
        Path file = getDataFolder().toPath().resolve(fileName);
        if (Files.exists(file)) {
            try {
                return Files.readAllLines(file).stream().map(UUID::fromString).collect(Collectors.toSet());
            } catch (IOException ioEx) {
                getLogger().log(Level.WARNING, "Failed to load disabled list", ioEx);
            }
        }

        return Sets.newHashSet();
    }

    private void saveDisabled(String fileName, Collection<UUID> disabledLst) {
        Path file = getDataFolder().toPath().resolve(fileName);
        try {
            List<String> progressLst = disabledLst.stream().map(Object::toString).collect(Collectors.toList());
            Files.write(file, progressLst, StandardOpenOption.CREATE);
        } catch (IOException ioEx) {
            getLogger().log(Level.WARNING, "Failed to save disabled list", ioEx);
        }
    }

    public Set<UUID> getActionBarDisabled() {
        return actionBarDisabled;
    }

    public Set<UUID> getProgressBarDisabled() {
        return progressBarDisabled;
    }

    public boolean isProgressEnabled(UUID user) {
        return configuration.isProgressEnabled() && !progressBarDisabled.contains(user);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void playNotificationSound(Player player) {
        Sound sound = configuration.getSoundType();
        if (sound != null) {
            float volume = configuration.getVolume();
            float pitch = configuration.getPitch();
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    /**
     * Sends the action bar message using packets in order to be compatible with 1.8
     *
     * @param receiver the receiver of this message
     * @param message  the message content
     */
    public void sendActionMessage(Player receiver, String message) {
        if (supportsChatTypeEnum()) {
            //the API for this action bar message is available and we could use it
            receiver.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
            return;
        }

        PacketContainer chatPacket = new PacketContainer(CHAT);
        chatPacket.getChatComponents().write(0, WrappedChatComponent.fromText(message));
        chatPacket.getBytes().write(0, ChatType.GAME_INFO.getId());

        //ignore our own packets
        chatPacket.addMetadata(getName(), true);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, chatPacket);
        } catch (InvocationTargetException invokeEx) {
            getLogger().log(Level.WARNING, "Failed to send action bar message", invokeEx);
        }
    }

    public boolean supportsChatTypeEnum() {
        return currentVersion.compareTo(explorationUpdate) > 0;
    }
}
