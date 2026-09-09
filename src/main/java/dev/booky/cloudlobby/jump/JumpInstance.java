package dev.booky.cloudlobby.jump;
// Created by booky10 in CloudLobby (12:31 AM 09.09.2026)

import io.papermc.paper.entity.TeleportFlag;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.NumberConversions;
import org.joml.Math;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@NullMarked
public class JumpInstance {

    private static final double NEARBY_THRESHOLD = 7d;

    private final JumpManager manager;

    private final Player player;
    private final World world;
    private final JumpMaterial material;

    private final List<BlockPosition> blocks = new LinkedList<>();

    private boolean started = false;
    private boolean stopped = false;

    private final int highscore;
    private int score = 0;

    private double previousJumpDistance = -1;

    private @Nullable ScheduledTask actionbarTask;
    private WeakReference<@Nullable Entity> glowingEntity = new WeakReference<>(null);

    private boolean wasAllowFlight;

    public JumpInstance(JumpManager manager, Player player) {
        this.manager = manager;
        this.player = player;
        this.world = player.getWorld();

        this.material = JumpMaterial.provideRandom(ThreadLocalRandom.current());
        this.highscore = manager.getHighscore(player);

        this.generateBlock(); // start
        this.generateBlock(); // next
        this.generateBlock(); // preview
    }

    public void generateBlock() {
        if (this.blocks.isEmpty()) {
            // initial start block
            int i = 0;
            while (true) {
                BlockPosition block = this.manager.getRandomStartBlock();
                if (i++ == BlockGenerator.MAX_TRIES || this.isValidBlock(block)) {
                    this.placeBlock(block);
                    return;
                }
            }
        }

        // generate next block based on player view angle + valid-ness
        ThreadLocalRandom random = ThreadLocalRandom.current();
        float angle = (float) (Math.toRadians(this.player.getYaw()) + Math.PI / 2d);
        float viewRange = Math.toRadians(this.manager.getManager().getConfig().getJump().getViewRange());
        this.placeBlock(this.manager.getBlockGenerator().getRandomBlock(
                this.blocks.getLast(), random,
                this.previousJumpDistance,
                angle - viewRange,
                angle + viewRange,
                this::isValidBlock
        ));
    }

    private boolean isValidBlock(BlockPosition position) {
        if (!this.manager.isInBox(position)) {
            return false;
        }
        // check if air is above
        Block block = this.world.getBlockAt(position.blockX(), position.blockY(), position.blockZ());
        if (!block.isEmpty()
                || !block.getRelative(0, 1, 0).isEmpty()
                || !block.getRelative(0, 2, 0).isEmpty()
                || !block.getRelative(0, 3, 0).isEmpty()) {
            return false;
        }
        List<JumpInstance> instances = this.manager.getNearbyInstances(this.world, position, NEARBY_THRESHOLD);
        return instances.isEmpty() || instances.size() == 1 && instances.getFirst() == this;
    }

    private void placeBlock(BlockPosition pos) {
        if (!this.blocks.isEmpty()) {
            BlockPosition prev = this.blocks.getLast();
            double horizontal = Math.sqrt(NumberConversions.square(prev.blockX() - pos.blockX())
                    + NumberConversions.square(prev.blockZ() - pos.blockZ()));
            this.previousJumpDistance = Math.max(
                    horizontal + (pos.blockY() - prev.blockY()) * BlockGenerator.GRAVITY_FACTOR, 0.1);
        }
        Block block = this.world.getBlockAt(pos.blockX(), pos.blockY(), pos.blockZ());
        block.setType(this.blocks.isEmpty() ? this.material.concrete() : this.material.glass(), false);
        block.getRelative(0, 1, 0).setType(Material.LIGHT, false);
        this.blocks.addLast(pos);
    }

    private void destroyBlock(boolean update) {
        if (this.blocks.isEmpty()) {
            return; // silently ignore
        }
        BlockPosition pos = this.blocks.removeFirst();
        Block block = this.world.getBlockAt(pos.blockX(), pos.blockY(), pos.blockZ());
        block.setType(Material.AIR, false);
        block.getRelative(0, 1, 0).setType(Material.AIR, false);

        // update material of new block at head
        if (update && !this.blocks.isEmpty()) {
            BlockPosition headPos = this.blocks.getFirst();
            Block headBlock = this.world.getBlockAt(headPos.blockX(), headPos.blockY(), headPos.blockZ());
            headBlock.setType(this.material.concrete(), false);
        }
    }

    public void updateActionbar() {
        this.player.sendActionBar(Component.translatable("cl.jump.actionbar",
                TranslationArgument.numeric(this.score),
                TranslationArgument.numeric(this.highscore)));
    }

    public void advance() {
        this.destroyBlock(true);
        this.generateBlock();
        this.updateGlowing();

        this.score++;
        this.updateActionbar();

        this.player.playSound(this.player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 2f);
    }

    public boolean hasStarted() {
        return this.started;
    }

    void start() {
        this.updateGlowing();

        Location spawnPos = this.blocks.getFirst().toLocation(this.world);
        spawnPos.add(0.5d, 1.025d, 0.5d);
        spawnPos.setYaw(this.player.getYaw());
        spawnPos.setPitch(this.player.getPitch());

        if (this.player.getAllowFlight()) {
            this.wasAllowFlight = true;
            this.player.setAllowFlight(false);
        }

        this.player.teleportAsync(spawnPos, PlayerTeleportEvent.TeleportCause.PLUGIN,
                        TeleportFlag.Relative.VELOCITY_ROTATION)
                .thenRun(() -> {
                    if (!this.player.isConnected() || this.stopped) {
                        return;
                    }
                    this.player.playSound(this.player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);

                    this.updateActionbar();
                    this.actionbarTask = this.player.getScheduler().runAtFixedRate(this.manager.getManager().getPlugin(),
                            __ -> this.updateActionbar(), null,
                            Ticks.TICKS_PER_SECOND * 2, Ticks.TICKS_PER_SECOND * 2);

                    this.started = true;
                });
    }

    void stop() {
        this.stopped = true;
        boolean connected = this.player.isConnected();

        // destroy remaining blocks
        for (int i = 0, count = this.blocks.size(); i < count; i++) {
            this.destroyBlock(false);
        }
        this.updateGlowing();

        // send user feedback
        if (this.score > this.highscore) {
            this.manager.setHighscore(this.player, this.score);
            if (connected) {
                this.player.playSound(this.player, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1f, 1f);
                this.player.sendMessage(Component.translatable("cl.jump.highscore",
                        TranslationArgument.numeric(this.score)));
            }
        } else {
            if (connected) {
                this.player.playSound(this.player, Sound.BLOCK_ANVIL_DESTROY, 1f, 1f);
                this.player.sendMessage(Component.translatable("cl.jump.no-highscore",
                        TranslationArgument.numeric(this.score)));
            }
        }

        if (this.actionbarTask != null) {
            this.actionbarTask.cancel();
            this.actionbarTask = null;
        }

        // reset
        if (connected) {
            this.player.sendActionBar(Component.empty());

            Location respawnLoc = this.manager.getManager().getConfig().getJump().getRespawnLocation();
            if (respawnLoc != null) {
                this.player.teleportAsync(respawnLoc);
            }

            if (this.wasAllowFlight) {
                this.wasAllowFlight = false;
                this.player.setAllowFlight(true);
            }
        }
    }

    private void updateGlowing() {
        Entity existing = this.glowingEntity.get();
        if (existing != null) {
            existing.remove();
        }
        // we need at least the next block to show glowing effect
        if (this.blocks.size() < 2) {
            return;
        }
        // spawn slime at size 2, size is exactly 1x1x1
        Location spawnLoc = this.blocks.get(1).toLocation(this.world);
        spawnLoc.add(0.5d, 0d, 0.5d);
        this.world.spawn(spawnLoc, Slime.class, false, entity -> {
            // only visible to jumping player
            entity.setVisibleByDefault(false);
            this.player.showEntity(this.manager.getManager().getPlugin(), entity);

            // discard once unloaded
            entity.setPersistent(false);

            entity.setWander(false);
            entity.setGravity(false);
            entity.setInvulnerable(true);
            entity.setInvisible(true);
            entity.setAI(false);
            entity.setSize(2); // 1x1x1 bbox
            entity.setGlowing(true);

            this.glowingEntity = new WeakReference<>(entity);
        });
    }

    public World getWorld() {
        return this.world;
    }

    public List<BlockPosition> getBlocks() {
        return this.blocks;
    }
}
