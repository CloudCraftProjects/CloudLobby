package dev.booky.cloudlobby;
// Created by booky10 in Lobby (14:13 12.09.21)

import dev.booky.cloudcore.util.BlockBBox;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.Location;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.concurrent.TimeUnit;

@NullUnmarked
@ConfigSerializable
public final class CloudLobbyConfig {

    private PvpBoxConfig pvpBox = new PvpBoxConfig();

    @ConfigSerializable
    public static final class PvpBoxConfig {

        private BlockBBox box = null;
        private Location respawnLocation = null;
        private long exitDelayMillis = TimeUnit.SECONDS.toMillis(5L);

        public PvpBoxConfig() {
        }

        public BlockBBox getBox() {
            return this.box;
        }

        public void setBox(BlockBBox box) {
            this.box = box;
        }

        public Location getRespawnLocation() {
            return this.respawnLocation;
        }

        public void setRespawnLocation(Location respawnLocation) {
            this.respawnLocation = respawnLocation;
        }

        public long getExitDelayMillis() {
            return this.exitDelayMillis;
        }

        public void setExitDelayMillis(long exitDelayMillis) {
            this.exitDelayMillis = exitDelayMillis;
        }
    }

    private JumpConfig jump = new JumpConfig();

    @ConfigSerializable
    public static final class JumpConfig {

        private @Nullable BlockPosition startPos = null;
        private @Nullable Location respawnLocation = null;
        private List<BlockBBox> boundingBoxes = List.of();
        private List<BlockPattern> blockPatterns = List.of(
                new BlockPattern(-1, "XXX--\n--XX-\n---XX\n----X\n----X"),
                new BlockPattern(0, "XX---\nXXXX-\n--XX-\n---XX\n---XX"),
                new BlockPattern(1, "-----\nXXX--\n--XX-\n---X-\n---X-")
        );
        private float viewRange = 45f;
        private double maxDistance = 8d;

        @ConfigSerializable
        public record BlockPattern(int offset, String pattern) {
        }

        private JumpConfig() {
        }

        public @Nullable BlockPosition getStartPos() {
            return this.startPos;
        }

        public void setStartPos(@Nullable BlockPosition startPos) {
            this.startPos = startPos;
        }

        public @Nullable Location getRespawnLocation() {
            return this.respawnLocation;
        }

        public void setRespawnLocation(@Nullable Location respawnLocation) {
            this.respawnLocation = respawnLocation;
        }

        public List<BlockBBox> getBoundingBoxes() {
            return this.boundingBoxes;
        }

        public void setBoundingBoxes(List<BlockBBox> boundingBoxes) {
            this.boundingBoxes = boundingBoxes;
        }

        public List<BlockPattern> getBlockPatterns() {
            return this.blockPatterns;
        }

        public void setBlockPatterns(List<BlockPattern> blockPatterns) {
            this.blockPatterns = blockPatterns;
        }

        public float getViewRange() {
            return this.viewRange;
        }

        public void setViewRange(float viewRange) {
            this.viewRange = viewRange;
        }

        public double getMaxDistance() {
            return this.maxDistance;
        }

        public void setMaxDistance(double maxDistance) {
            this.maxDistance = maxDistance;
        }
    }

    private CloudLobbyConfig() {
    }

    public PvpBoxConfig getPvpBox() {
        return this.pvpBox;
    }

    public JumpConfig getJump() {
        return this.jump;
    }
}
