package dev.booky.cloudlobby.commands;
// Created by booky10 in CloudLobby (12:38 AM 09.09.2026)

import com.google.common.collect.Sets;
import dev.booky.cloudlobby.CloudLobbyManager;
import dev.booky.cloudlobby.jump.JumpManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.CommandArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

@NullMarked
public final class JumpCommand {

    private static final String MAIN_LABEL = "jump";
    private static final Set<String> ALIASES = Set.of("jnr");
    private static final Set<String> ALL_LABELS = Sets.union(Set.of(MAIN_LABEL), ALIASES);

    private final JumpManager manager;

    public JumpCommand(JumpManager manager) {
        this.manager = manager;
    }

    public void register() {
        this.unregister();

        new CommandTree(MAIN_LABEL)
                .withAliases(ALIASES.toArray(new String[0]))
                .withPermission("cloudlobby.command.jump")
                .executesNative(this::executeJumping)
                .register();
    }

    public void unregister() {
        for (String label : ALL_LABELS) {
            CommandAPI.unregister(label, true);
        }
    }

    private void executeJumping(NativeProxyCommandSender sender, CommandArguments args) {
        if (sender.getCallee() instanceof Player player) {
            this.manager.startJumping(player);
        }
    }
}
