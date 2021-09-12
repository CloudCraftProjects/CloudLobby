package tk.booky.cloudlobby.utils;
// Created by booky10 in Lobby (14:00 12.09.21)

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class CloudLobbyManager {

    private static final Component PREFIX = text()
        .append(text('[', GRAY))
        .append(text('C', WHITE, BOLD))
        .append(text('L', AQUA, BOLD))
        .append(text(']', GRAY))
        .append(space()).build();

    private final Map<UUID, Long> lastDamages = new HashMap<>();
    private final CloudLobbyConfig config;
    private final Plugin plugin;
    private World overworld;

    public CloudLobbyManager(CloudLobbyConfig config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation") // it's "just" unsafe ¯\_(ツ)_/¯
    public void fail(String message) throws WrapperCommandSyntaxException {
        String prefixed = Bukkit.getUnsafe().legacyComponentSerializer().serialize(prefix(text(message, RED)));
        throw new WrapperCommandSyntaxException(new SimpleCommandExceptionType(new LiteralMessage(prefixed)).create());
    }

    @SuppressWarnings("deprecation") // it's "just" unsafe ¯\_(ツ)_/¯
    public void fail(Component component) throws WrapperCommandSyntaxException {
        String message = Bukkit.getUnsafe().legacyComponentSerializer().serialize(prefix(component.color(RED)));
        throw new WrapperCommandSyntaxException(new SimpleCommandExceptionType(new LiteralMessage(message)).create());
    }

    public void message(Audience audience, String message) {
        audience.sendMessage(Identity.nil(), prefix(text(message, GREEN)), MessageType.SYSTEM);
    }

    public void message(Audience audience, Component component) {
        audience.sendMessage(Identity.nil(), prefix(component.color(GREEN)), MessageType.SYSTEM);
    }

    public Component prefix(String message) {
        return PREFIX.append(text(message));
    }

    public Component prefix(Component component) {
        return PREFIX.append(component);
    }

    public World loadOverworld() {
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                return overworld = world;
            }
        }

        throw new IllegalStateException("No overworld could be found!");
    }

    public long getRemainingCooldown(UUID uuid) {
        return (System.currentTimeMillis() - lastDamages.getOrDefault(uuid, 0L) - config().pvpBoxExitDelay()) * -1;
    }

    public boolean hasExitCooldown(UUID uuid) {
        return System.currentTimeMillis() - lastDamages.getOrDefault(uuid, 0L) < config().pvpBoxExitDelay();
    }

    public Map<UUID, Long> lastDamages() {
        return lastDamages;
    }

    public CloudLobbyConfig config() {
        return config;
    }

    public World overworld() {
        return overworld;
    }

    public Plugin plugin() {
        return plugin;
    }
}
