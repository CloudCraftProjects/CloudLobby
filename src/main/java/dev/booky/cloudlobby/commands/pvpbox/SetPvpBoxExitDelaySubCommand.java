package dev.booky.cloudlobby.commands.pvpbox;
// Created by booky10 in Lobby (17:57 12.09.21)

import dev.booky.cloudlobby.utils.CloudLobbyManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LongArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SetPvpBoxExitDelaySubCommand extends CommandAPICommand implements CommandExecutor {

    private final CloudLobbyManager manager;

    public SetPvpBoxExitDelaySubCommand(CloudLobbyManager manager) {
        super("exitDelay");
        this.manager = manager;

        withArguments(
            new LiteralArgument("set"),
            new LongArgument("milliseconds", 0));

        withPermission("cloudlobby.command.pvpbox.exit-delay.set").executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) {
        long milliseconds = (long) args[0];
        manager.config().pvpBoxExitDelay((long) args[0]);

        if (milliseconds == 0) {
            manager.message(sender, "The pvp box exit delay has been disabled");
            return;
        }

        manager.message(sender, "The pvp box exit delay has been updated");
    }
}
