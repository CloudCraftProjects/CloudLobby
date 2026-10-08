package dev.booky.cloudlobby.listeners;

import dev.booky.cloudlobby.CloudLobbyConfig.ActionConfig;
import dev.booky.cloudlobby.CloudLobbyConfig.MenuItemConfig;
import dev.booky.cloudlobby.CloudLobbyManager;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jspecify.annotations.NullMarked;

import java.util.Map;

@NullMarked
public final class MenuItemListener implements Listener {

    private static final Component NEUTRAL_PARENT = Component.text()
            .color(NamedTextColor.WHITE)
            .decoration(TextDecoration.ITALIC, false)
            .asComponent();

    private final CloudLobbyManager manager;
    private final NamespacedKey itemKey;

    public MenuItemListener(CloudLobbyManager manager) {
        this.manager = manager;
        this.itemKey = new NamespacedKey(manager.getPlugin(), "menu-item");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        for (Map.Entry<String, MenuItemConfig> entry : this.manager.getConfig().getMenuItems().entrySet()) {
            MenuItemConfig config = entry.getValue();
            if (!config.isEnabled() || !this.canUse(player, config)) {
                continue;
            }
            int slot = config.getSlot();
            if (slot >= 0 && slot <= 8) {
                ItemStack stack = this.createStack(entry.getKey(), config);
                inventory.setItem(slot, stack);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }
        String itemId = item.getPersistentDataContainer().get(this.itemKey, PersistentDataType.STRING);
        if (itemId == null) {
            return;
        }

        MenuItemConfig config = this.manager.getConfig().getMenuItems().get(itemId);
        if (config == null || !config.isEnabled()) {
            return;
        }

        Player player = event.getPlayer();
        if (!this.canUse(player, config)) {
            return;
        }

        ActionConfig action = this.getAction(player, config);
        String command = renderCommand(action.getCommand(), player, itemId, config.getSlot());
        if (command.isEmpty()) {
            return;
        }

        event.setUseItemInHand(Event.Result.DENY);

        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        if (action.isAsConsole()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else {
            Bukkit.dispatchCommand(player, command);
        }
    }

    private boolean canUse(Player player, MenuItemConfig config) {
        String permission = config.getPermission();
        return permission == null || player.hasPermission(permission);
    }

    private ActionConfig getAction(Player player, MenuItemConfig config) {
        ActionConfig bedrockAction = config.getBedrockAction();
        if (bedrockAction != null && isBedrockPlayer(player)) {
            return bedrockAction;
        }
        return config.getAction();
    }

    private static boolean isBedrockPlayer(Player player) {
        return Bukkit.getPluginManager().isPluginEnabled("floodgate")
                && FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
    }

    private static String renderCommand(String command, Player player, String itemId, int slot) {
        return command
                .replace("{player}", player.getName())
                .replace("{uuid}", player.getUniqueId().toString())
                .replace("{item}", itemId)
                .replace("{slot}", Integer.toString(slot));
    }

    private ItemStack createStack(String itemId, MenuItemConfig config) {
        ItemStack stack = Registry.ITEM.getOrThrow(config.getMaterial()).createItemStack();
        MiniMessage mm = MiniMessage.miniMessage();
        if (!config.getName().isEmpty()) {
            stack.setData(DataComponentTypes.CUSTOM_NAME, NEUTRAL_PARENT
                    .append(mm.deserialize(config.getName())));
        }
        if (!config.getLore().isEmpty()) {
            ItemLore.Builder lore = ItemLore.lore();
            for (String line : config.getLore()) {
                lore.addLine(NEUTRAL_PARENT.append(mm.deserialize(line)));
            }
            stack.setData(DataComponentTypes.LORE, lore);
        }
        stack.editPersistentDataContainer(container ->
                container.set(this.itemKey, PersistentDataType.STRING, itemId));
        return stack;
    }
}
