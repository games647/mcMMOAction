package com.github.games647.mcmmoaction;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.github.games647.mcmmoaction.config.Configuration;
import com.github.games647.mcmmoaction.progress.ExperienceListener;
import com.github.games647.mcmmoaction.redirect.RedirectListener;
import com.github.games647.mcmmoaction.refresh.RefreshManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.bukkit.plugin.java.JavaPlugin;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class mcMMOAction extends JavaPlugin {

    private static final String PROGRESS_FILE_NAME = "disabled-progress.txt";
    private static final String ACTIONBAR_FILE_NAME = "disabled-action.txt";
    private static final int SOUND_TIMEOUT = 2;

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

        TimeoutManager timeoutManager = new TimeoutManager(Duration.ofSeconds(SOUND_TIMEOUT));
        RefreshManager refreshManager = new RefreshManager(this, timeoutManager, configuration.getAppearanceTime());
        getServer().getPluginManager().registerEvents(timeoutManager, this);
        getServer().getPluginManager().registerEvents(refreshManager, this);

        if (configuration.isProgressEnabled()) {
            getServer().getPluginManager().registerEvents(new ExperienceListener(this, refreshManager), this);
        }

        getCommand(getName().toLowerCase()).setExecutor(new ToggleCommand(this));

        //the event could and should be executed async, but if we try to use it with other sync listeners
        //the sending order gets mixed up
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new RedirectListener(this, refreshManager, configuration.getMessages()));
        protocolManager.addPacketListener(refreshManager);

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
        if (Files.notExists(file)) {
            return new HashSet<>();
        }

        try (Stream<String> lines = Files.lines(file)) {
            return lines.map(UUID::fromString).collect(toSet());
        } catch (IOException ioEx) {
            getLogger().log(Level.WARNING, "Failed to load disabled list", ioEx);
        }

        return new HashSet<>();
    }

    private void saveDisabled(String fileName, Collection<UUID> disabledLst) {
        Path file = getDataFolder().toPath().resolve(fileName);
        try {
            Path dataFolder = file.getParent();
            if (Files.notExists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }

            List<String> progressLst = disabledLst
                    .parallelStream()
                    .map(Object::toString)
                    .collect(toList());
            Files.write(file, progressLst, StandardOpenOption.CREATE);
        } catch (IOException ioEx) {
            getLogger().log(Level.WARNING, "Failed to save disabled list", ioEx);
        }
    }

    public Collection<UUID> getActionBarDisabled() {
        return actionBarDisabled;
    }

    public Collection<UUID> getProgressBarDisabled() {
        return progressBarDisabled;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public boolean supportsChatTypeEnum() {
        return MinecraftVersion.getCurrentVersion().compareTo(explorationUpdate) > 0;
    }
}
