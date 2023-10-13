package dev.booky.cloudlobby.listeners;
// Created by booky10 in Lobby (13:48 12.09.21)

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public final class MiscListener implements Listener {

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player target
                && target.getGameMode() == GameMode.ADVENTURE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getEntity().getGameMode() == GameMode.ADVENTURE) {
            event.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.ADVENTURE) {
            return;
        }
        if (!(event.getRightClicked() instanceof RideableMinecart)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.ADVENTURE) {
            return;
        }

        if (event.getAction() == Action.PHYSICAL) {
            // always cancel farmland interactions
            event.setCancelled(true);
            return;
        }

        // allow buttons to be clicked
        if (event.getClickedBlock() != null) {
            Material clickedType = event.getClickedBlock().getType();
            if (Tag.BUTTONS.isTagged(clickedType)) {
                return;
            }
        }

        event.setCancelled(true);
    }
}
