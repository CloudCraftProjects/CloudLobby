package tk.booky.cloudlobby.listeners;
// Created by booky10 in Lobby (13:49 12.09.21)

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import tk.booky.cloudlobby.utils.CloudLobbyManager;

public record JoinQuitListener(CloudLobbyManager manager) implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().setAllowFlight(true);
        event.joinMessage(null);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
    }
}
