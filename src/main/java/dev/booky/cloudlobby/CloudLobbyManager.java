package dev.booky.cloudlobby;
// Created by booky10 in Lobby (14:00 12.09.21)

import dev.booky.cloudcore.config.ConfigLoader;
import dev.booky.cloudcore.util.BlockBBox;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public final class CloudLobbyManager {

    private static final Component PREFIX = text()
            .append(text('[', GRAY))
            .append(text('C', WHITE, BOLD))
            .append(text('L', AQUA, BOLD))
            .append(text(']', GRAY))
            .appendSpace()
            .build();

    private final Map<UUID, Long> lastDamage = new HashMap<>();

    private final Path configPath;
    private CloudLobbyConfig config;

    private final Plugin plugin;

    public CloudLobbyManager(Plugin plugin, Path dataDir) {
        this.plugin = plugin;

        this.configPath = dataDir.resolve("config.yml");
        this.reloadConfig();
    }

    public void reloadConfig() {
        this.config = ConfigLoader.loadObject(this.configPath, CloudLobbyConfig.class);
    }

    public WrapperCommandSyntaxException fail(String message) {
        return this.fail(text(message));
    }

    public WrapperCommandSyntaxException fail(Component message) {
        Component coloredMsg = text().append(message).color(RED).build();
        Component prefixedMsg = PREFIX.append(coloredMsg);
        return CommandAPIBukkit.failWithAdventureComponent(prefixedMsg);
    }

    public void message(Audience audience, String message) {
        this.message(audience, text(message));
    }

    public void message(Audience audience, Component message) {
        Component coloredMsg = text().append(message).color(GREEN).build();
        Component prefixedMsg = PREFIX.append(coloredMsg);
        audience.sendMessage(prefixedMsg);
    }

    public boolean isPvpBox(Entity entity) {
        return this.isPvpBox(entity.getLocation());
    }

    public boolean isPvpBox(Location loc) {
        BlockBBox box = this.config.getPvpBox().getBox();
        return box.contains(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public long getRemainingExitCooldown(UUID playerId) {
        long lastDamage = this.lastDamage.getOrDefault(playerId, 0L);
        long cooldownExpiry = lastDamage + this.config.getPvpBox().getExitDelayMillis();
        long remainingCooldown = cooldownExpiry - System.currentTimeMillis();
        return Math.max(0L, remainingCooldown);
    }

    public boolean hasExitCooldown(UUID playerId) {
        return this.getRemainingExitCooldown(playerId) > 0L;
    }

    public void resetExitCooldown(UUID playerId) {
        this.lastDamage.put(playerId, System.currentTimeMillis());
    }

    public void removeExitCooldown(UUID playerId) {
        this.lastDamage.remove(playerId);
    }

    public CloudLobbyConfig getConfig() {
        return this.config;
    }

    public Plugin getPlugin() {
        return this.plugin;
    }
}
