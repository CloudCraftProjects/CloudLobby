package dev.booky.cloudlobby.listeners;
// Created by booky10 in Lobby (13:48 12.09.21)

import dev.booky.cloudlobby.CloudLobbyManager;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import static net.kyori.adventure.text.Component.translatable;

public final class PvPListener implements Listener {

    private final CloudLobbyManager manager;

    public PvPListener(CloudLobbyManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onAttack(PrePlayerAttackEntityEvent event) {
        Player attacker = event.getPlayer();
        if (attacker.getGameMode() != GameMode.ADVENTURE) {
            return;
        }

        if (!(event.getAttacked() instanceof Player attacked)) {
            // instantly cancel if not attacking player
            event.setCancelled(true);
            return;
        }

        // cancel if either attacker or attacked is not in pvp box
        if (!this.manager.isPvpBox(attacker)
                || !this.manager.isPvpBox(attacked)) {
            event.setCancelled(true);
            return;
        }

        // attacker and attacked are both in pvp box
        this.manager.resetExitCooldown(attacker.getUniqueId());
        this.manager.resetExitCooldown(attacked.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.getGameMode() != GameMode.ADVENTURE) {
            return;
        }

        if (!this.manager.isPvpBox(player)) {
            event.setCancelled(true);
            return;
        }

        if (event.getCause() == DamageCause.FALL) {
            // cancel fall damage in pvp box
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        this.manager.removeExitCooldown(event.getPlayer().getUniqueId());
        event.getEntity().sendActionBar(translatable("cl.pvp-box.died"));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Location respawnLoc = this.manager.getConfig().getPvpBox().getRespawnLocation();
        if (respawnLoc != null) {
            event.setRespawnLocation(respawnLoc);
        }
    }
}
