package com.github.games647.mcmmoaction.config;

import com.github.games647.mcmmoaction.mcMMOAction;
import com.google.common.base.Enums;
import com.google.common.base.Optional;

import java.util.logging.Level;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

public class SoundConfig {

    private final mcMMOAction plugin;

    //notification sound
    private Sound sound;
    private float volume;
    private float pitch;

    public SoundConfig(mcMMOAction plugin) {
        this.plugin = plugin;
    }

    public void load(ConfigurationSection section) {
        if (section.getBoolean("enabled")) {
            volume = (float) section.getDouble("volume");
            pitch = (float) section.getDouble("pitch");

            String soundType = section.getString("type");
            Optional<Sound> sound = Enums.getIfPresent(Sound.class, soundType.toUpperCase());
            if (sound.isPresent()) {
                this.sound = sound.get();
            } else {
                plugin.getLogger().log(Level.WARNING, "Failed to load the sound type");
            }
        }
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }
}
