package dev.booky.cloudlobby.listeners;
// Created by booky10 in Lobby (13:49 12.09.21)

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import dev.booky.cloudlobby.CloudLobbyManager;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class DoubleJumpListener implements Listener {

    private static final int MIN_COOLDOWN = Ticks.TICKS_PER_SECOND / 2;
    private static final int MAX_COOLDOWN = Ticks.TICKS_PER_SECOND * 5;

    private final CloudLobbyManager manager;

    public DoubleJumpListener(CloudLobbyManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            event.getPlayer().setAllowFlight(true);
        }
    }

    @EventHandler
    public void onPostRespawn(PlayerPostRespawnEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.ADVENTURE) {
            event.getPlayer().setAllowFlight(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGamemodeChange(PlayerGameModeChangeEvent event) {
        if (event.getCause() == PlayerGameModeChangeEvent.Cause.DEFAULT_GAMEMODE) {
            return; // causes errors as far as I know
        }

        if (event.getNewGameMode() == GameMode.ADVENTURE) {
            // delay by one tick, no post event is present here
            Bukkit.getScheduler().runTask(this.manager.getPlugin(),
                    () -> event.getPlayer().setAllowFlight(true));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFlyToggle(PlayerToggleFlightEvent event) {
        if (!event.isFlying()) {
            return; // if a player somehow got into flying mode, don't do this
        }

        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.ADVENTURE) {
            return; // only allow in adventure
        }
        event.setCancelled(true);

        // boost velocity
        Vector boostDir = player.getLocation().getDirection().multiply(1.8d);
        player.setVelocity(boostDir); // no api for adding to player's velocity sadly

        // boost effects
        Location particleLoc = player.getLocation().subtract(0, 0.5, 0);
        Particle.CLOUD.builder().source(player).location(particleLoc).extra(0.1d).count(15).spawn();
        player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR, 1f, 0.75f);

        // cooldown mechanic
        player.setAllowFlight(false);
        new BukkitRunnable() {
            private int ticks = 0;

            // on ground check is deprecated...
            // should be fixed by anticheat
            @SuppressWarnings("deprecation")
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }

                // shorten cooldown if player is on ground before
                if (!player.isOnGround() && this.ticks++ < MAX_COOLDOWN) {
                    return; // not on ground or cooldown not done yet
                }

                // don't give players fly in pvp zone
                if (!DoubleJumpListener.this.manager.isPvpBox(player)) {
                    player.setAllowFlight(true);
                }
                this.cancel();
            }
        }.runTaskTimer(this.manager.getPlugin(), MIN_COOLDOWN, 2);
    }
}
