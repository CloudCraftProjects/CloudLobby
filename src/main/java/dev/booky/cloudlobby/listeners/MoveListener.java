package dev.booky.cloudlobby.listeners;
// Created by booky10 in Lobby (13:48 12.09.21)

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Scheduler;
import dev.booky.cloudlobby.CloudLobbyManager;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.Position;
import net.kyori.adventure.util.Ticks;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.checkerframework.checker.index.qual.NonNegative;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.Component.translatable;

public class MoveListener implements Listener {

    private final CloudLobbyManager manager;

    private final Cache<ExitBlockKey, Boolean> exitBlockCache = Caffeine.newBuilder()
            .<ExitBlockKey, Boolean>expireAfter(new Expiry<>() {
                private long getExpiry(ExitBlockKey key) {
                    long cooldownMillis = MoveListener.this.manager.getRemainingExitCooldown(key.player().getUniqueId());
                    long clampedCooldownMillis = Math.max(Ticks.SINGLE_TICK_DURATION_MS, cooldownMillis);
                    return TimeUnit.MILLISECONDS.toNanos(clampedCooldownMillis);
                }

                @Override
                public long expireAfterCreate(ExitBlockKey key, Boolean value, long currentTime) {
                    return this.getExpiry(key);
                }

                @Override
                public long expireAfterUpdate(ExitBlockKey key, Boolean value, long currentTime, @NonNegative long currentDuration) {
                    return this.getExpiry(key);
                }

                @Override
                public long expireAfterRead(ExitBlockKey key, Boolean value, long currentTime, @NonNegative long currentDuration) {
                    return this.getExpiry(key);
                }
            })
            .scheduler(Scheduler.systemScheduler())
            .evictionListener((key, value, cause) -> {
                if (key != null) {
                    key.clearBarrier();
                }
            })
            .build();

    public MoveListener(CloudLobbyManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() == TeleportCause.ENDER_PEARL
                && this.manager.isPvpBox(event.getFrom())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.ADVENTURE) {
            return;
        }

        // check for pvp-box enter
        if (this.manager.isPvpBox(event.getTo())) {
            if (this.manager.isPvpBox(event.getFrom())) {
                return; // didn't change state
            }
            // this message gets spammed if player tries to leave
            // the box while still cooling down
            if (this.manager.hasExitCooldown(player.getUniqueId())) {
                return;
            }
            this.onPvpBoxEnter(player);
            return;
        }

        if (!this.manager.isPvpBox(event.getFrom())) {
            return; // didn't change state
        }

        boolean cancelEvent = this.onPvpBoxLeave(player, event.getTo());
        if (cancelEvent) {
            event.setCancelled(true);
        }
    }

    private void onPvpBoxEnter(Player player) {
        player.setAllowFlight(false); // disable double-jump
        player.sendActionBar(translatable("cl.pvp-box.enter"));
    }

    /**
     * @return cancel move event
     */
    private boolean onPvpBoxLeave(Player player, Location to) {
        long cooldown = this.manager.getRemainingExitCooldown(player.getUniqueId());
        if (cooldown <= 0) {
            // no cooldown, let them leave
            player.sendActionBar(translatable("cl.pvp-box.leave"));
            player.setAllowFlight(true); // enable double-jump
            return false;
        }

        player.sendActionBar(translatable("cl.pvp-box.leave-cooldown"));

        BlockPosition bottomPos = to.toBlock();
        BlockPosition topPos = bottomPos.offset(BlockFace.UP);

        // this cache places blocks on write and removes them on expiry
        ExitBlockKey blockKey = new ExitBlockKey(player, List.of(bottomPos, topPos));
        this.exitBlockCache.get(blockKey, key -> {
            // only send barrier packets if didn't exist before
            key.createBarrier();
            return true;
        });

        return true; // teleport back!
    }

    private record ExitBlockKey(Player player, List<BlockPosition> blocks) {

        private static final BlockData BARRIER_DATA = Material.BARRIER.createBlockData();
        private static final BlockData AIR_DATA = Material.AIR.createBlockData();

        public void createBarrier() {
            this.sendUpdates(BARRIER_DATA);
        }

        public void clearBarrier() {
            this.sendUpdates(AIR_DATA);
        }

        private void sendUpdates(BlockData data) {
            if (!this.player.isOnline()) {
                return; // player went offline
            }

            Map<Position, BlockData> updates = new HashMap<>(this.blocks.size());
            for (BlockPosition position : this.blocks) {
                updates.put(position, data);
            }
            this.player.sendMultiBlockChange(updates);
        }
    }
}
