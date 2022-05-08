package dev.booky.cloudlobby.commands.pvpbox;
// Created by booky10 in Lobby (17:57 12.09.21)

import dev.booky.cloudlobby.utils.CloudLobbyManager;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

public class GetPvpBoxRespawnSubCommand extends CommandAPICommand implements CommandExecutor {

    private final CloudLobbyManager manager;

    public GetPvpBoxRespawnSubCommand(CloudLobbyManager manager) {
        super("respawn");
        this.manager = manager;

        withArguments(new LiteralArgument("get"));
        withPermission("cloudlobby.command.pvpbox.respawn.get").executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) {
        Location respawnLocation = manager.config().pvpBoxRespawn();
        manager.message(sender, String.format("The respawn location is currently in world %s at %s %s %s with yaw %s and pitch %s",
            respawnLocation.getWorld().getName(),
            respawnLocation.getX(), respawnLocation.getY(), respawnLocation.getZ(),
            respawnLocation.getYaw(), respawnLocation.getPitch()));
    }
}
