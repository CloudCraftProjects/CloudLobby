package dev.booky.cloudlobby.jump;
// Created by booky10 in CloudLobby (12:32 AM 09.09.2026)

import dev.booky.cloudcore.util.BlockBBox;
import dev.booky.cloudlobby.CloudLobbyConfig;
import dev.booky.cloudlobby.CloudLobbyManager;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.NumberConversions;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.management.PlatformLoggingMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@NullMarked
public class JumpManager {

    private final CloudLobbyManager manager;
    private final NamespacedKey highscoreKey;

    private final BlockGenerator blockGenerator = new BlockGenerator();
    private BlockBoxSupplier boxSupplier = new BlockBoxSupplier(List.of());

    private final Map<UUID, JumpInstance> instances = new ConcurrentHashMap<>();

    public JumpManager(CloudLobbyManager manager) {
        this.manager = manager;
        this.highscoreKey = new NamespacedKey(manager.getPlugin(), "jump/highscore");

        manager.addConfigReloadHook(() -> {
            CloudLobbyConfig.JumpConfig config = this.manager.getConfig().getJump();
            this.blockGenerator.parse(config.getBlockPatterns());
            this.boxSupplier = new BlockBoxSupplier(config.getBoundingBoxes());
        });

        Bukkit.getPluginManager().registerEvents(new JumpListener(this), manager.getPlugin());
    }

    public List<JumpInstance> getNearbyInstances(World world, BlockPosition position, double distanceThreshold) {
        double distThresholSq = distanceThreshold * distanceThreshold;
        List<JumpInstance> instances = new ArrayList<>(3);
        for (JumpInstance inst : this.instances.values()) {
            if (world != inst.getWorld()) {
                continue;
            }
            // compare each block against target position
            for (BlockPosition block : inst.getBlocks()) {
                double distSq = NumberConversions.square(block.blockX() - position.blockX())
                        + NumberConversions.square(block.blockY() - position.blockY())
                        + NumberConversions.square(block.blockZ() - position.blockZ());
                if (distSq <= distThresholSq) {
                    instances.add(inst);
                }
            }
        }
        return instances;
    }

    public int getHighscore(Player player) {
        return player.getPersistentDataContainer().getOrDefault(this.highscoreKey, PersistentDataType.INTEGER, 0);
    }

    public void setHighscore(Player player, int highscore) {
        player.getPersistentDataContainer().set(this.highscoreKey, PersistentDataType.INTEGER, highscore);
    }

    public @Nullable JumpInstance getInstance(Player player) {
        return this.instances.get(player.getUniqueId());
    }

    public void startJumping(Player player) {
        this.instances.computeIfAbsent(player.getUniqueId(), __ -> {
            JumpInstance instance = new JumpInstance(this, player);
            instance.start();
            return instance;
        });
    }

    public void stopJumping(Player player) {
        JumpInstance instance = this.instances.remove(player.getUniqueId());
        if (instance != null) {
            instance.stop();
        }
    }

    public boolean isInBox(BlockPosition position) {
        for (BlockBBox box : this.manager.getConfig().getJump().getBoundingBoxes()) {
            if (box.contains(position.blockX(), position.blockY(), position.blockZ())) {
                return true;
            }
        }
        return false;
    }

    public BlockPosition getRandomStartBlock() {
        return this.boxSupplier.getRandom(ThreadLocalRandom.current());
    }

    public CloudLobbyManager getManager() {
        return this.manager;
    }

    public BlockGenerator getBlockGenerator() {
        return this.blockGenerator;
    }
}
