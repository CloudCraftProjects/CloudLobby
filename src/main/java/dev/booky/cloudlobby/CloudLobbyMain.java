package dev.booky.cloudlobby;
// Created by booky10 in Lobby (14:23 12.09.21)

import dev.booky.cloudcore.i18n.CloudTranslator;
import dev.booky.cloudlobby.commands.LobbyCommand;
import dev.booky.cloudlobby.listeners.DoubleJumpListener;
import dev.booky.cloudlobby.listeners.JoinQuitMessageListener;
import dev.booky.cloudlobby.listeners.MiscListener;
import dev.booky.cloudlobby.listeners.MoveListener;
import dev.booky.cloudlobby.listeners.PvPListener;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Locale;

public final class CloudLobbyMain extends JavaPlugin {

    private CloudTranslator i18nLoader;
    private CloudLobbyManager manager;
    private LobbyCommand command;

    @Override
    public void onLoad() {
        this.i18nLoader = new CloudTranslator(this.getClassLoader(),
                new NamespacedKey(this, "i18n"),
                Locale.ENGLISH, Locale.GERMAN);
        this.i18nLoader.load();

        this.manager = new CloudLobbyManager(this, this.getDataFolder().toPath());
    }

    @Override
    public void onEnable() {
        // config can't be loaded before enabling the plugin, as
        // worlds aren't loaded yet and required for this
        this.manager.reloadConfig();

        this.command = new LobbyCommand(this.manager);
        this.command.register();

        Bukkit.getPluginManager().registerEvents(new DoubleJumpListener(this.manager), this);
        Bukkit.getPluginManager().registerEvents(new JoinQuitMessageListener(), this);
        Bukkit.getPluginManager().registerEvents(new MiscListener(), this);
        Bukkit.getPluginManager().registerEvents(new MoveListener(this.manager), this);
        Bukkit.getPluginManager().registerEvents(new PvPListener(this.manager), this);

        Bukkit.getServicesManager().register(CloudLobbyManager.class, this.manager, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        if (this.command != null) {
            this.command.unregister();
        }
        if (this.i18nLoader != null) {
            this.i18nLoader.unload();
        }
    }
}
