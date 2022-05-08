package dev.booky.cloudlobby.commands.pvpbox;
// Created by booky10 in Lobby (17:53 12.09.21)

import dev.booky.cloudlobby.utils.CloudLobbyManager;
import dev.jorel.commandapi.CommandAPICommand;

public class PvpBoxSubCommand extends CommandAPICommand {

    public PvpBoxSubCommand(CloudLobbyManager manager) {
        super("pvpBox");

        withPermission("cloudlobby.command.pvpbox");

        withSubcommand(new GetPvpBoxExitDelaySubCommand(manager));
        withSubcommand(new SetPvpBoxExitDelaySubCommand(manager));
        withSubcommand(new GetPvpBoxRespawnSubCommand(manager));
        withSubcommand(new SetPvpBoxRespawnSubCommand(manager));
        withSubcommand(new GetBoxSubCommand(manager));
        withSubcommand(new SetBoxSubCommand(manager));
    }
}
