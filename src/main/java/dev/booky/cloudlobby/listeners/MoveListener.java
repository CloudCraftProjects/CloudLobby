package dev.booky.cloudlobby.listeners;
// Created by booky10 in Lobby (13:48 12.09.21)

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.booky.cloudlobby.utils.CloudLobbyManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.BlockVector;

import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static org.bukkit.Bukkit.createBlockData;
import static org.bukkit.Bukkit.getScheduler;
import static org.bukkit.Material.AIR;
import static org.bukkit.Material.BARRIER;

public class MoveListener implements Listener {

    private static final BlockData BARRIER_DATA = createBlockData(BARRIER), AIR_DATA = createBlockData(AIR);
    private final Multimap<UUID, BlockVector> blocked = HashMultimap.create();
    private final CloudLobbyManager manager;

    public MoveListener(CloudLobbyManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != TeleportCause.SPECTATE && manager.config().pvpBoxBox().contains(event.getFrom().toVector())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.hasChangedBlock()) return;
        if (event.getPlayer().getGameMode() != GameMode.ADVENTURE) return;

        if (manager.config().pvpBoxBox().contains(event.getTo().toVector())) {
            if (manager.config().pvpBoxBox().contains(event.getFrom().toVector())) return;
            if (manager.hasExitCooldown(event.getPlayer().getUniqueId())) return;

            event.getPlayer().setAllowFlight(false);
            event.getPlayer().sendActionBar(text("You have entered the pvp arena", RED));
            return;
        }

        if (!manager.config().pvpBoxBox().contains(event.getFrom().toVector())) return;
        long cooldown = manager.getRemainingCooldown(event.getPlayer().getUniqueId());
        if (cooldown <= 0) {
            event.getPlayer().sendActionBar(text("You have left the pvp arena", GREEN));
            event.getPlayer().setAllowFlight(true);
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendActionBar(text("You can't leave now", RED));

        BlockVector bottomVector = event.getTo().toVector().toBlockVector();
        if (blocked.containsEntry(event.getPlayer().getUniqueId(), bottomVector)) return;

        blocked.put(event.getPlayer().getUniqueId(), bottomVector);
        Location topLocation = event.getTo().clone().add(0, 1, 0);

        event.getPlayer().sendBlockChange(event.getTo(), BARRIER_DATA);
        event.getPlayer().sendBlockChange(topLocation, BARRIER_DATA);

        getScheduler().runTaskLater(manager.plugin(), () -> {
            if (event.getPlayer().isOnline()) {
                event.getPlayer().sendBlockChange(event.getTo(), AIR_DATA);
                event.getPlayer().sendBlockChange(topLocation, AIR_DATA);
            }

            blocked.remove(event.getPlayer().getUniqueId(), bottomVector);
        }, cooldown / 50 + 5);
    }
}
