package tk.booky.cloudlobby.listeners;
// Created by booky10 in Lobby (13:48 12.09.21)

import org.bukkit.GameMode;
import org.bukkit.Tag;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import tk.booky.cloudlobby.utils.CloudLobbyManager;

public record MiscListener(CloudLobbyManager manager) implements Listener {

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (event.getTarget() != null && event.getTarget().getType() == EntityType.PLAYER) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            if (event.getRightClicked().getType() != EntityType.MINECART) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(true);
        } else if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            switch (event.getAction()) {
                case RIGHT_CLICK_BLOCK -> {
                    if (event.getClickedBlock() == null || !Tag.BUTTONS.isTagged(event.getClickedBlock().getType())) {
                        event.setCancelled(true);
                    }
                }
                case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK, RIGHT_CLICK_AIR -> event.setCancelled(true);
            }
        }
    }
}
