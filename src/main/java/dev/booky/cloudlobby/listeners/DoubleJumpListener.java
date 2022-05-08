package dev.booky.cloudlobby.listeners;
// Created by booky10 in Lobby (13:49 12.09.21)

import dev.booky.cloudlobby.utils.CloudLobbyManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static org.bukkit.Particle.CLOUD;
import static org.bukkit.Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR;

public record DoubleJumpListener(CloudLobbyManager manager) implements Listener {

    @EventHandler
    public void onFlyToggle(PlayerToggleFlightEvent event) {
        if (!event.isFlying()) return;

        switch (event.getPlayer().getGameMode()) {
            case ADVENTURE, SURVIVAL -> {
                event.getPlayer().setVelocity(event.getPlayer().getLocation().getDirection().multiply(1.8));

                new BukkitRunnable() {
                    private int count = 0;

                    @Override
                    @SuppressWarnings("deprecation")
                    public void run() {
                        if (event.getPlayer().isOnline()) {
                            if (event.getPlayer().isOnGround() || ++count > 100) {
                                if (!manager.config().pvpBoxBox().contains(event.getPlayer().getLocation().toVector())) {
                                    event.getPlayer().setAllowFlight(true);
                                }
                            } else {
                                return;
                            }
                        }
                        cancel();
                    }
                }.runTaskTimer(manager.plugin(), 10, 2);
                Location location = event.getPlayer().getLocation().subtract(0, 0.5, 0);

                event.setCancelled(true);
                event.getPlayer().setAllowFlight(false);
                event.getPlayer().spawnParticle(CLOUD, location, 15, 0, 0, 0, 0.1f);
                event.getPlayer().playSound(location, ENTITY_FIREWORK_ROCKET_BLAST_FAR, 1f, 0.75f);
            }
        }
    }
}
