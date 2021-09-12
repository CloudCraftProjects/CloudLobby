package tk.booky.cloudlobby.listeners;
// Created by booky10 in Lobby (13:48 12.09.21)

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import tk.booky.cloudlobby.utils.CloudLobbyManager;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public record PvPListener(CloudLobbyManager manager) implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType() == EntityType.PLAYER && event.getCause() != DamageCause.FALL && event.getFinalDamage() > 0) {
            if (manager.config().pvpBoxBox().contains(event.getEntity().getLocation().toVector())) {
                if (event instanceof EntityDamageByEntityEvent byEntityEvent) {
                    if (byEntityEvent.getDamager().getType() == EntityType.PLAYER) {
                        if (manager.config().pvpBoxBox().contains(byEntityEvent.getDamager().getLocation().toVector())) {
                            manager.lastDamages().put(event.getEntity().getUniqueId(), System.currentTimeMillis());
                            manager.lastDamages().put(byEntityEvent.getDamager().getUniqueId(), System.currentTimeMillis());
                            return;
                        }
                    } else {
                        manager.lastDamages().put(event.getEntity().getUniqueId(), System.currentTimeMillis());
                        return;
                    }
                } else {
                    manager.lastDamages().put(event.getEntity().getUniqueId(), System.currentTimeMillis());
                    return;
                }
            }
        }

        event.setCancelled(true);
        event.setDamage(0);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        manager.lastDamages().put(event.getEntity().getUniqueId(), 0L);
        event.getEntity().sendActionBar(text("You have died.", RED));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(manager.config().pvpBoxRespawn());
        event.getPlayer().setAllowFlight(true);
    }
}
