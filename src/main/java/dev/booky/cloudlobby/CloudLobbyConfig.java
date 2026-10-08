package dev.booky.cloudlobby;
// Created by booky10 in Lobby (14:13 12.09.21)

import dev.booky.cloudcore.util.BlockBBox;
import io.papermc.paper.math.BlockPosition;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;
import java.util.Map;
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

    @Comment("""
            Hotbar items which are given to players on join and execute a command
            action when right-clicked. The item is identified by the map key.
            The action command supports the placeholders {player}, {uuid},
            {item} (the item id) and {slot}.""")
    private Map<String, MenuItemConfig> menuItems = createDefaultMenuItems();

    @ConfigSerializable
    public static final class MenuItemConfig {

        private boolean enabled = true;
        private int slot = 4;
        private NamespacedKey material = NamespacedKey.minecraft("compass");
        private String name = "";
        private List<String> lore = List.of();
        private @Nullable String permission = null;
        private ActionConfig action = new ActionConfig();
        private @Nullable ActionConfig bedrockAction = null;

        private MenuItemConfig() {
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getSlot() {
            return this.slot;
        }

        public void setSlot(int slot) {
            this.slot = slot;
        }

        public NamespacedKey getMaterial() {
            return this.material;
        }

        public void setMaterial(NamespacedKey material) {
            this.material = material;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getLore() {
            return this.lore;
        }

        public void setLore(List<String> lore) {
            this.lore = lore;
        }

        public @Nullable String getPermission() {
            return this.permission;
        }

        public void setPermission(@Nullable String permission) {
            this.permission = permission;
        }

        public ActionConfig getAction() {
            return this.action;
        }

        public void setAction(ActionConfig action) {
            this.action = action;
        }

        public @Nullable ActionConfig getBedrockAction() {
            return this.bedrockAction;
        }

        public void setBedrockAction(@Nullable ActionConfig bedrockAction) {
            this.bedrockAction = bedrockAction;
        }
    }

    @ConfigSerializable
    public static final class ActionConfig {

        private String command = "";
        private boolean asConsole = true;

        private ActionConfig() {
        }

        public ActionConfig(String command, boolean asConsole) {
            this.command = command;
            this.asConsole = asConsole;
        }

        public String getCommand() {
            return this.command;
        }

        public void setCommand(String command) {
            this.command = command;
        }

        public boolean isAsConsole() {
            return this.asConsole;
        }

        public void setAsConsole(boolean asConsole) {
            this.asConsole = asConsole;
        }
    }

    private static Map<String, MenuItemConfig> createDefaultMenuItems() {
        MenuItemConfig navigator = new MenuItemConfig();
        navigator.setName("<gold>Navigator </gold><c:#cccccc>(<key:key.use>)");
        navigator.setAction(new ActionConfig("panels open navigator {player}", true));
        navigator.setBedrockAction(new ActionConfig("panels open navigator_bedrock {player}", true));
        return Map.of("navigator", navigator);
    }

    private CloudLobbyConfig() {
    }

    public PvpBoxConfig getPvpBox() {
        return this.pvpBox;
    }

    public JumpConfig getJump() {
        return this.jump;
    }

    public Map<String, MenuItemConfig> getMenuItems() {
        return this.menuItems;
    }
}
