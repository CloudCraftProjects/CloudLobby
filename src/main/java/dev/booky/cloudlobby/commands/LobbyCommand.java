package dev.booky.cloudlobby.commands;
// Created by booky10 in Lobby (14:27 12.09.21)

import com.google.common.collect.Sets;
import dev.booky.cloudlobby.CloudLobbyManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.wrappers.NativeProxyCommandSender;

import java.util.Set;

import static net.kyori.adventure.text.Component.translatable;

public final class LobbyCommand {

    private static final String MAIN_LABEL = "cloudlobby";
    private static final Set<String> ALIASES = Set.of("cl");
    private static final Set<String> ALL_LABELS = Sets.union(Set.of(MAIN_LABEL), ALIASES);

    private final CloudLobbyManager manager;

    public LobbyCommand(CloudLobbyManager manager) {
        this.manager = manager;
    }

    public void register() {
        this.unregister();

        new CommandTree(MAIN_LABEL)
                .withAliases(ALIASES.toArray(new String[0]))
                .withPermission("cloudlobby.command")
                .then(new LiteralArgument("reloadConfig")
                        .executesNative(this::reloadConfig))
                .register();
    }

    public void unregister() {
        for (String label : ALL_LABELS) {
            CommandAPI.unregister(label, true);
        }
    }

    private void reloadConfig(NativeProxyCommandSender sender, CommandArguments args) {
        this.manager.reloadConfig();
        this.manager.message(sender, translatable("cl.command.reload-config"));
    }
}
