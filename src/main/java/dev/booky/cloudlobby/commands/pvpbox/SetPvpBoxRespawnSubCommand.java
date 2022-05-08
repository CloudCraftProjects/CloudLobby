package dev.booky.cloudlobby.commands.pvpbox;
// Created by booky10 in Lobby (17:57 12.09.21)

import dev.booky.cloudlobby.utils.CloudLobbyManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.AngleArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class SetPvpBoxRespawnSubCommand extends CommandAPICommand implements CommandExecutor {

    private final CloudLobbyManager manager;

    public SetPvpBoxRespawnSubCommand(CloudLobbyManager manager) {
        super("respawn");
        this.manager = manager;

        withArguments(
            new LiteralArgument("set"),
            new LocationArgument("location", LocationType.PRECISE_POSITION),
            new AngleArgument("yaw"),
            new AngleArgument("pitch"));

        withPermission("cloudlobby.command.pvpbox.respawn.set").executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) {
        Location location = (Location) args[0];
        location.setYaw((float) args[1]);
        location.setPitch((float) args[2]);

        manager.config().pvpBoxRespawn(location);
        manager.message(sender, "The respawn location has been updated");
    }
}
