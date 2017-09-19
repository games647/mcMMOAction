package com.github.games647.mcmmoaction;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.github.games647.mcmmoaction.listener.MessageListener;
import com.github.games647.mcmmoaction.listener.PlayerListener;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class mcMMOAction extends JavaPlugin {

    private final Set<UUID> disabledActionBar = Sets.newHashSet();

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
    }

    public Set<UUID> getDisabledActionBar() {
        return disabledActionBar;
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
}
