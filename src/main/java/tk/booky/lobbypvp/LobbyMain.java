package tk.booky.lobbypvp;

import me.rockyhawk.commandpanels.api.PanelOpenedEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class LobbyMain extends JavaPlugin implements Listener {

    private static final BoundingBox box = new BoundingBox(946, 81, -256, 977, 92, -287);
    private static final Map<UUID, Map<Block, Integer>> blockSchedulers = new HashMap<>();
    private static final Map<UUID, Long> lastDamage = new HashMap<>();
    private static BlockData barrier, air;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        Block zero = Bukkit.getWorlds().get(0).getBlockAt(0, 0, 0);

        zero.setType(Material.BARRIER);
        barrier = zero.getBlockData().clone();

        zero.setType(Material.AIR);
        air = zero.getBlockData().clone();
    }

    @Override
    public void onDisable() {
        blockSchedulers.clear();
        lastDamage.clear();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntityType().equals(EntityType.PLAYER) && !event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
            Player player = (Player) event.getEntity();
            Location location = player.getLocation();

            if (box.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
                if (event instanceof EntityDamageByEntityEvent entityEvent) {
                    if (entityEvent.getDamager().getType().equals(EntityType.PLAYER)) {
                        Player damager = (Player) entityEvent.getDamager();
                        Location attackerLocation = damager.getLocation();

                        if (box.contains(attackerLocation.getBlockX(), attackerLocation.getBlockY(), attackerLocation.getBlockZ())) {
                            lastDamage.put(player.getUniqueId(), System.currentTimeMillis());
                            lastDamage.put(damager.getUniqueId(), System.currentTimeMillis());
                            return;
                        }
                    } else {
                        lastDamage.put(player.getUniqueId(), System.currentTimeMillis());
                        return;
                    }
                } else {
                    lastDamage.put(player.getUniqueId(), System.currentTimeMillis());
                    return;
                }
            }
        }

        event.setCancelled(true);
        event.setDamage(0D);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event instanceof PlayerTeleportEvent teleportEvent) {
            if (teleportEvent.getCause().equals(PlayerTeleportEvent.TeleportCause.COMMAND)) return;
        }

        Player player = event.getPlayer();
        Location to = event.getTo().clone(), from = event.getFrom().clone();

        if (box.contains(to.getBlockX(), to.getBlockY(), to.getBlockZ())) {
            if (box.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ())) return;
            if (player.getGameMode().equals(GameMode.ADVENTURE) && System.currentTimeMillis() - lastDamage.getOrDefault(player.getUniqueId(), 0L) < 5000) return;

            player.setAllowFlight(false);
            player.sendActionBar(Component.text("§cDu hast die PvP Arena betreten!"));
            return;
        }

        if (!box.contains(from.getBlockX(), from.getBlockY(), from.getBlockZ())) return;

        if (!player.getGameMode().equals(GameMode.ADVENTURE) || System.currentTimeMillis() - lastDamage.getOrDefault(player.getUniqueId(), 0L) > 5000) {
            player.sendActionBar(Component.text("§aDu hast die PvP Arena verlassen!"));
            player.setAllowFlight(true);
        } else {
            event.setCancelled(true);
            player.sendActionBar(Component.text("§cDu kannst jetzt nicht raus!"));

            player.sendBlockChange(to, barrier);
            player.sendBlockChange(to.clone().add(0, 1, 0), barrier);

            Map<Block, Integer> innerMap = blockSchedulers.getOrDefault(player.getUniqueId(), new HashMap<>());
            if (innerMap.containsKey(to.getBlock()))
                Bukkit.getScheduler().cancelTask(innerMap.get(to.getBlock()));

            innerMap.put(to.getBlock(), Bukkit.getScheduler().runTaskLater(this, () -> {
                player.sendBlockChange(to, air);
                player.sendBlockChange(to.clone().add(0, 1, 0), air);
            }, 60).getTaskId());

            blockSchedulers.put(player.getUniqueId(), innerMap);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setFoodLevel(20);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        lastDamage.put(event.getEntity().getUniqueId(), 0L);
        event.getEntity().sendActionBar(Component.text("§cDu bist gestorben!"));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(new Location(event.getRespawnLocation().getWorld(), 961.5, 93.125, -271.5, -45, 8));
    }

    @EventHandler
    public void onPanelOpened(PanelOpenedEvent event) {
        if (!event.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) return;
        if (System.currentTimeMillis() - lastDamage.getOrDefault(event.getPlayer().getUniqueId(), 0L) > 5000) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (!event.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) return;
        event.setCancelled(true);

        if (event.getRightClicked().getType().equals(EntityType.MINECART)) {
            Minecart minecart = (Minecart) event.getRightClicked();
            minecart.addPassenger(event.getPlayer());
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!event.getPlayer().getGameMode().equals(GameMode.ADVENTURE)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().setAllowFlight(true);
        event.joinMessage(Component.text(""));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(Component.text(""));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) {
                    break;
                } else if(event.getClickedBlock() != null) {
                    if (event.getClickedBlock().getX() == 1019) {
                        if(event.getClickedBlock().getY() == 80) {
                            if (event.getClickedBlock().getZ() == -211) {
                                break;
                            }
                        }
                    }
                }
            case PHYSICAL:
                event.setCancelled(true);
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onFlyToggle(PlayerToggleFlightEvent event) {
        if (event.getPlayer().getGameMode().equals(GameMode.CREATIVE)) return;

        Player player = event.getPlayer();
        event.setCancelled(true);

        Vector vector = player.getLocation().getDirection().multiply(1.8);
        vector.setY(vector.getY() > 1 ? vector.getY() : 1);
        player.setVelocity(vector);
        player.setAllowFlight(false);

        AtomicInteger count = new AtomicInteger();
        new BukkitRunnable() {
            @Override
            @SuppressWarnings("deprecation")
            public void run() {
                if (player.isOnline()) {
                    if (player.isOnGround() || count.incrementAndGet() > 100) {
                        if (!box.contains(player.getLocation().toVector())) {
                            player.setAllowFlight(true);
                        }
                    } else {
                        return;
                    }
                }
                cancel();
            }
        }.runTaskTimer(this, 10, 1);

        Location location = player.getLocation().clone().subtract(0, 0.5, 0);
        player.spawnParticle(Particle.CLOUD, location, 15, 0, 0, 0, 0.1f);
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST_FAR, 1f, 0.75f);
    }
}
