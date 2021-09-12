package tk.booky.cloudlobby.commands.pvpbox;
// Created by booky10 in Lobby (17:57 12.09.21)

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandExecutor;
import org.bukkit.command.CommandSender;
import tk.booky.cloudlobby.utils.CloudLobbyManager;

public class GetPvpBoxExitDelaySubCommand extends CommandAPICommand implements CommandExecutor {

    private final CloudLobbyManager manager;

    public GetPvpBoxExitDelaySubCommand(CloudLobbyManager manager) {
        super("exitDelay");
        this.manager = manager;

        withArguments(new LiteralArgument("get"));
        withPermission("cloudlobby.command.pvpbox.exit-delay.get").executes(this);
    }

    @Override
    public void run(CommandSender sender, Object[] args) throws WrapperCommandSyntaxException {
        long milliseconds = manager.config().pvpBoxExitDelay(), ticks = milliseconds / 50, seconds = milliseconds / 1000;

        if (milliseconds == 0) {
            manager.fail("The pvp box exit delay is currently disabled.");
        } else {
            manager.message(sender, "The pvp box exit delay is currently at " + milliseconds + " milliseconds (" + ticks + " ticks, " + seconds + " seconds).");
        }
    }
}
