package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.InvadedLandsEvents.listeners.EventLeaveEvent;
import me.nicbo.InvadedLandsEvents.scoreboard.EventScoreboard;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import me.nicbo.InvadedLandsEvents.utils.ItemUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * KOTH Event:
 * All players get a kit
 * One player can be capturing the zone at a time
 * Once a player reaches the max points they win
 *
 * @author Nicbo
 * @since 2020-05-11
 */

public final class KOTH extends InvadedEvent {
    private ProtectedRegion region;

    private List<Location> locations;
    private ItemStack[] armour;
    private ItemStack[] kit;

    private Set<Player> respawningPlayers;

    private HashMap<Player, Integer> points;
    private List<Player> playersInRegion;
    private BukkitRunnable regionChecker;
    private BukkitRunnable incrementPoints;

    private Player capturing;
    private Player leader;

    private final int WIN_POINTS;

    private final String CAPTURING;
    private final String CAPTURING_POINTS;
    private final String LOST;

    public KOTH() {
        super("King Of The Hill", "koth");

        String regionName = eventConfig.getString("safe-region");
        try {
            this.region = regionManager.getRegion(regionName);
        } catch (NullPointerException npe) {
            logger.severe("Waterdrop region '" + regionName + "' does not exist");
        }

        this.locations = new ArrayList<>();

        for (int i = 1; i < 5; i++) {
            this.locations.add(ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-" + i), eventWorld));
        }

        this.armour = new ItemStack[] {
                ItemUtils.addEnchant(new ItemStack(Material.IRON_BOOTS, 1), Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                ItemUtils.addEnchant(new ItemStack(Material.IRON_LEGGINGS, 1), Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                ItemUtils.addEnchant(new ItemStack(Material.IRON_CHESTPLATE, 1), Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                ItemUtils.addEnchant(new ItemStack(Material.IRON_HELMET, 1), Enchantment.PROTECTION_ENVIRONMENTAL, 2)
        };

        this.kit = new ItemStack[] {
                ItemUtils.addEnchant(new ItemStack(Material.IRON_SWORD, 1), Enchantment.DAMAGE_ALL, 1),
                new ItemStack(Material.BOW, 1),
                new ItemStack(Material.GOLDEN_APPLE, 10),
                new ItemStack(Material.ARROW, 32)
        };

        this.respawningPlayers = new HashSet<>();

        this.points = new HashMap<>();
        this.playersInRegion = new ArrayList<>();

        this.WIN_POINTS = eventConfig.getInt("int-win-points");

        this.CAPTURING = getEventMessage("CAPTURING");
        this.CAPTURING_POINTS = getEventMessage("CAPTURING_POINTS");
        this.LOST = getEventMessage("LOST");
    }

    @Override
    public void init() {
        this.regionChecker = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : players) {
                    if (EventUtils.isLocInRegion(player.getLocation(), region))
                        playersInRegion.add(player);
                    else
                        playersInRegion.remove(player);
                }

                if (capturing == null)
                    setCapturing(GeneralUtils.getRandom(playersInRegion));
            }
        };

        this.incrementPoints = new BukkitRunnable() {
            @Override
            public void run() {
                if (capturing != null) {
                    points.put(capturing, points.get(capturing) + 1);
                    if (points.get(capturing) % 5 == 0)
                        EventUtils.broadcastEventMessage(CAPTURING_POINTS.replace("{player}", capturing.getName())
                                .replace("{points}", String.valueOf(points.get(capturing))));

                    if (points.get(capturing) >= points.get(leader))
                        leader = capturing;

                    if (points.get(capturing) == WIN_POINTS)
                        playerWon(capturing);
                }
            }
        };
    }

    @Override
    public void start() {
        regionChecker.runTaskTimerAsynchronously(plugin, 0, 5);
        incrementPoints.runTaskTimerAsynchronously(plugin, 0, 20);

        for (Player player : players) {
            points.put(player, 0);
            giveKit(player);
            player.teleport(getRandomLocation());
        }
    }

    @Override
    public void over() {
        regionChecker.cancel();
        incrementPoints.cancel();
    }

    private void setCapturing(Player player) {
        if (this.capturing != null)
            EventUtils.broadcastEventMessage(LOST.replace("{player}", this.capturing.getName()));

        if (player != null)
            EventUtils.broadcastEventMessage(CAPTURING.replace("{player}", player.getName()));

        this.capturing = player;
    }

    private void giveKit(Player player) {
        player.getInventory().setArmorContents(armour);
        player.getInventory().setArmorContents(kit);
    }

    private Location getRandomLocation() {
        return GeneralUtils.getRandom(locations);
    }

    @EventHandler
    public void playerHurt(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (blockListener(player))
                return;

            if (event.getFinalDamage() >= player.getHealth()) { // Damage will kill player
                capturing = GeneralUtils.getRandom(playersInRegion);
                respawningPlayers.add(player);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.spigot().respawn(), 1);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (respawningPlayers.contains(player)) {
            giveKit(player);
            event.setRespawnLocation(getRandomLocation());
            respawningPlayers.remove(player);
        }
    }

    @EventHandler
    public void onLeave(EventLeaveEvent event) {
        Player player = event.getPlayer();
        if (blockListener(player))
            playersInRegion.remove(player);
    }

    public final class KOTHSB extends EventScoreboard {
        public KOTHSB() {
            super();
        }

        @Override
        public void refresh() {

        }
    }
}
