package tk.booky.cloudlobby.commands;
// Created by booky10 in Lobby (14:27 12.09.21)

import dev.jorel.commandapi.CommandAPICommand;
import tk.booky.cloudlobby.commands.pvpbox.PvpBoxSubCommand;
import tk.booky.cloudlobby.utils.CloudLobbyManager;

public class CloudLobbyRootCommand extends CommandAPICommand {

    public CloudLobbyRootCommand(CloudLobbyManager manager) {
        super("cloudlobby");

        withAliases("cl");
        withPermission("cloudlobby.command");

        withSubcommand(new PvpBoxSubCommand(manager));
    }
}
