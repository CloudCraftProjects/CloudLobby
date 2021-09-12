package tk.booky.cloudlobby;
// Created by booky10 in Lobby (14:23 12.09.21)

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import tk.booky.cloudlobby.commands.CloudLobbyRootCommand;
import tk.booky.cloudlobby.listeners.DoubleJumpListener;
import tk.booky.cloudlobby.listeners.JoinQuitListener;
import tk.booky.cloudlobby.listeners.MiscListener;
import tk.booky.cloudlobby.listeners.MoveListener;
import tk.booky.cloudlobby.listeners.PvPListener;
import tk.booky.cloudlobby.utils.CloudLobbyConfig;
import tk.booky.cloudlobby.utils.CloudLobbyManager;

import java.io.File;

public class CloudLobbyMain extends JavaPlugin {

    private CloudLobbyConfig configuration;
    private CommandAPICommand command;
    private CloudLobbyManager manager;

    @Override
    public void onLoad() {
        configuration = new CloudLobbyConfig(new File(getDataFolder(), "config.yml"));
        manager = new CloudLobbyManager(configuration, this);
        (command = new CloudLobbyRootCommand(manager)).register();
    }

    @Override
    public void onEnable() {
        manager.loadOverworld();

        if (configuration.reloadConfiguration().pvpBoxRespawn() == null) {
            configuration.pvpBoxRespawn(manager.overworld().getSpawnLocation());
        }
        configuration.saveConfiguration();

        Bukkit.getPluginManager().registerEvents(new DoubleJumpListener(manager), this);
        Bukkit.getPluginManager().registerEvents(new JoinQuitListener(manager), this);
        Bukkit.getPluginManager().registerEvents(new MiscListener(manager), this);
        Bukkit.getPluginManager().registerEvents(new MoveListener(manager), this);
        Bukkit.getPluginManager().registerEvents(new PvPListener(manager), this);
    }

    @Override
    public void onDisable() {
        configuration.saveConfiguration();
        CommandAPI.unregister(command.getName(), true);
    }

    public CloudLobbyConfig configuration() {
        return configuration;
    }

    public CommandAPICommand command() {
        return command;
    }

    public CloudLobbyManager manager() {
        return manager;
    }
}
