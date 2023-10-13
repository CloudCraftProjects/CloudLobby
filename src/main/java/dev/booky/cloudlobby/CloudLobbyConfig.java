package dev.booky.cloudlobby;
// Created by booky10 in Lobby (14:13 12.09.21)

import dev.booky.cloudcore.util.BlockBBox;
import org.bukkit.Location;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.concurrent.TimeUnit;

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

    private CloudLobbyConfig() {
    }

    public PvpBoxConfig getPvpBox() {
        return this.pvpBox;
    }
}
