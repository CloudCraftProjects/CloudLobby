package dev.booky.cloudlobby.commands;
// Created by booky10 in Lobby (14:27 12.09.21)

import dev.booky.cloudlobby.commands.pvpbox.PvpBoxSubCommand;
import dev.booky.cloudlobby.utils.CloudLobbyManager;
import dev.jorel.commandapi.CommandAPICommand;

public class CloudLobbyRootCommand extends CommandAPICommand {

    public CloudLobbyRootCommand(CloudLobbyManager manager) {
        super("cloudlobby");

        withAliases("cl");
        withPermission("cloudlobby.command");

        withSubcommand(new PvpBoxSubCommand(manager));
    }
}
