package dev.booky.cloudlobby.utils;
// Created by booky10 in Lobby (14:13 12.09.21)

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.io.IOException;

public class CloudLobbyConfig {

    private final File configurationFile;
    private BoundingBox pvpBoxBox = new BoundingBox(0, 0, 0, 0, 0, 0);
    private Location pvpBoxRespawn = null;
    private long pvpBoxExitDelay = 5000;

    public CloudLobbyConfig(File configurationFile) {
        this.configurationFile = configurationFile;
    }

    public CloudLobbyConfig reloadConfiguration() {
        if (!configurationFile.exists()) {
            return saveConfiguration();
        } else {
            FileConfiguration configuration = YamlConfiguration.loadConfiguration(configurationFile);

            pvpBoxBox = configuration.getObject("pvp-box.box", BoundingBox.class, pvpBoxBox);
            pvpBoxExitDelay = configuration.getLong("pvp-box.exit-delay", pvpBoxExitDelay);
            pvpBoxRespawn = configuration.getLocation("pvp-box.respawn", pvpBoxRespawn);

            return this;
        }
    }

    public CloudLobbyConfig saveConfiguration() {
        try {
            FileConfiguration configuration = new YamlConfiguration();

            configuration.set("pvp-box.exit-delay", pvpBoxExitDelay);
            configuration.set("pvp-box.respawn", pvpBoxRespawn);
            configuration.set("pvp-box.box", pvpBoxBox);

            configuration.save(configurationFile);
            return this;
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public File configurationFile() {
        return configurationFile;
    }

    public BoundingBox pvpBoxBox() {
        return pvpBoxBox;
    }

    public void pvpBoxBox(BoundingBox pvpBoxBox) {
        this.pvpBoxBox = pvpBoxBox;
        saveConfiguration();
    }

    public Location pvpBoxRespawn() {
        return pvpBoxRespawn;
    }

    public void pvpBoxRespawn(Location pvpBoxRespawn) {
        this.pvpBoxRespawn = pvpBoxRespawn;
        saveConfiguration();
    }

    public long pvpBoxExitDelay() {
        return pvpBoxExitDelay;
    }

    public void pvpBoxExitDelay(long pvpBoxExitDelay) {
        this.pvpBoxExitDelay = pvpBoxExitDelay;
        saveConfiguration();
    }
}
