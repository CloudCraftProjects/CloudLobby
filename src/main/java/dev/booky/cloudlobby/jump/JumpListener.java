package dev.booky.cloudlobby.jump;
// Created by booky10 in CloudLobby (12:42 AM 09.09.2026)

import dev.booky.cloudlobby.CloudLobbyConfig;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
public class JumpListener implements Listener {

    private final JumpManager manager;

    public JumpListener(JumpManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL
                || event.getClickedBlock() == null
                || event.getPlayer().isInsideVehicle()) {
            return;
        }
        BlockPosition startPos = this.manager.getManager().getConfig().getJump().getStartPos();
        Block block = event.getClickedBlock();
        if (startPos != null
                && startPos.blockX() == block.getX()
                && startPos.blockY() == block.getY()
                && startPos.blockZ() == block.getZ()) {
            this.manager.startJumping(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedPosition()) {
            return;
        }
        Player player = event.getPlayer();
        JumpInstance instance = this.manager.getInstance(player);
        if (instance == null) {
            return;
        }
        List<BlockPosition> blocks = instance.getBlocks();
        if (blocks.size() < 2) {
            return; // safeguard
        }
        BlockPosition nextBlockPos = blocks.get(1);
        BoundingBox nextBlockBBox = new BoundingBox(
                nextBlockPos.x(), nextBlockPos.y(), nextBlockPos.z(),
                nextBlockPos.x() + 1d, nextBlockPos.y() + 1d, nextBlockPos.z() + 1d
        );

        // check if player will land on target block; build bbox manually to use event.getTo() instead of current location
        Location to = event.getTo();
        BoundingBox playerBox = BoundingBox.of(to, player.getWidth() / 2d, 2d, player.getWidth() / 2d)
                .shift(0d, -0.8d, 0d);
        if (playerBox.overlaps(nextBlockBBox)) {
            instance.advance();
            return;
        }

        // check if player fell off
        double minY = to.getY();
        for (BlockPosition block : blocks) {
            if (block.y() < minY) {
                minY = block.y();
            }
        }
        if (to.getY() + 1e-6d < minY) {
            this.manager.stopJumping(player); // player fell
            return;
        }

        // ensure player doesn't move too far away from jump blocks
        double minDistSq = Double.MAX_VALUE;
        for (BlockPosition block : blocks) {
            double distSq = NumberConversions.square(block.x() - to.getX())
                    + NumberConversions.square(block.y() - to.getY())
                    + NumberConversions.square(block.z() - to.getZ());
            if (distSq < minDistSq) {
                minDistSq = distSq;
            }
        }
        CloudLobbyConfig.JumpConfig config = this.manager.getManager().getConfig().getJump();
        if (minDistSq > NumberConversions.square(config.getMaxDistance() + 1e-6d)) {
            this.manager.stopJumping(player); // too far away
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        this.manager.stopJumping(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        this.manager.stopJumping(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        this.manager.stopJumping(event.getPlayer());
    }
}
