package tk.booky.cloudlobby.commands.pvpbox;
// Created by booky10 in Lobby (17:59 12.09.21)

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.LocationArgument;
import dev.jorel.commandapi.arguments.LocationType;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import tk.booky.cloudlobby.utils.CloudLobbyManager;

public class SetBoxSubCommand extends CommandAPICommand implements CommandExecutor {

    private final CloudLobbyManager manager;

    public SetBoxSubCommand(CloudLobbyManager manager) {
        super("box");
        this.manager = manager;

        withArguments(
            new LiteralArgument("set"),
            new LocationArgument("pos1", LocationType.PRECISE_POSITION),
            new LocationArgument("pos2", LocationType.PRECISE_POSITION)
        );

        withPermission("cloudlobby.command.pvpbox.box.set").executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) {
        Vector position1 = ((Location) args[0]).toVector();
        Vector position2 = ((Location) args[1]).toVector();

        if (position1.equals(position2)) {
            manager.config().pvpBoxBox(new BoundingBox());
            manager.message(sender, "The pvp box has been disabled.");
        } else {
            manager.config().pvpBoxBox(BoundingBox.of(position1, position2));
            manager.message(sender, "The pvp box has been updated.");
        }
    }
}
