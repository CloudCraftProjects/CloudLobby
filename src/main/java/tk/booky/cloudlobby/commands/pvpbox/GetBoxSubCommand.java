package tk.booky.cloudlobby.commands.pvpbox;
// Created by booky10 in Lobby (17:59 12.09.21)

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.BoundingBox;
import tk.booky.cloudlobby.utils.CloudLobbyManager;

public class GetBoxSubCommand extends CommandAPICommand implements CommandExecutor {

    private final CloudLobbyManager manager;

    public GetBoxSubCommand(CloudLobbyManager manager) {
        super("box");
        this.manager = manager;

        withArguments(new LiteralArgument("get"));
        withPermission("cloudlobby.command.pvpbox.box.get").executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        BoundingBox box = manager.config().pvpBoxBox();

        if (box.getMin().equals(box.getMax())) {
            manager.fail("No pvp box is set currently.");
        } else {
            manager.message(sender, String.format("The current pvp box reaches from %s %s %s to %s %s %s.",
                box.getMinX(), box.getMinY(), box.getMinZ(),
                box.getMaxX(), box.getMaxY(), box.getMaxZ()
            ));
        }
    }
}
