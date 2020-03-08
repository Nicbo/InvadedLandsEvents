package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Spleef extends InvadedEvent {
    private boolean matchCountdown;
    private BukkitRunnable heightCheck;
    private ProtectedRegion region;
    private int minY;

    private ItemStack shovel;

    public Spleef(EventsMain plugin) {
        super("Spleef", "spleef", plugin);
    }

    @Override
    public void init(EventsMain plugin) {
        this.heightCheck = new BukkitRunnable() {
            private List<Player> toLose = new ArrayList<>();

            @Override
            public void run() {
                for (Player player : players) {
                    if (player.getLocation().getY() < minY - 1) {
                        toLose.add(player);
                    }
                }
                toLose.forEach(player -> loseEvent(player));
                toLose.clear();
            }
        };

        initPlayerCheck();


        RegionManager regionManager = plugin.getWorldGuardPlugin().getRegionManager(eventWorld);
        String regionName = eventConfig.getString("region");

        try {
            region = regionManager.getRegion(regionName);
        } catch (NullPointerException npe) {
            logger.severe("Spleef region '" + regionName + "' does not exist");
        }

        this.shovel = new ItemStack(Material.DIAMOND_SPADE);
        ItemMeta itemMeta = shovel.getItemMeta();
        itemMeta.addEnchant(Enchantment.DIG_SPEED, 5, true);
        this.shovel.setItemMeta(itemMeta);

        BlockVector pos1 = ConfigUtils.blockVectorFromConfig(eventConfig.getConfigurationSection("snow-position-1"));
        BlockVector pos2 = ConfigUtils.blockVectorFromConfig(eventConfig.getConfigurationSection("snow-position-2"));
        buildSnow(pos1, pos2);
    }

    @Override
    public void start() {
        started = true;
        clearPlayers();
        tpPlayers();
        startRunnables();
        startMatchCountdown();
        players.forEach(player -> player.getInventory().setItem(0, shovel));
    }

    @Override
    public void stop() {
        started = false;
        cancelRunnables();
        spawnTpPlayers();
        clearLists();
        plugin.getManagerHandler().getEventManager().setEventRunning(false);
    }

    private void startRunnables() {
        heightCheck.runTaskTimerAsynchronously(plugin, 0, 1);
        playerCheck.runTaskTimerAsynchronously(plugin, 0, 1);
    }

    private void cancelRunnables() {
        heightCheck.cancel();
        playerCheck.cancel();
    }

    private void clearLists() {
        players.clear();
        spectators.clear();
    }

    private void buildSnow(BlockVector pos1, BlockVector pos2) {
        int minX = (int) Math.min(pos1.getX(), pos2.getX());
        this.minY = (int) Math.min(pos1.getY(), pos2.getY());
        int minZ = (int) Math.min(pos1.getZ(), pos2.getZ());
        int maxX = (int) Math.max(pos1.getX(), pos2.getX());
        int maxY = (int) Math.max(pos1.getY(), pos2.getY());
        int maxZ = (int) Math.max(pos1.getZ(), pos2.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    eventWorld.getBlockAt(x, y, z).setType(Material.SNOW_BLOCK);
                }
            }
        }
    }

    private void tpPlayers() {
        Location start1 = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-1"), eventWorld);
        Location start2 = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-2"), eventWorld);

        for (int i = 0; i < players.size(); i++) {
            players.get(i).teleport(i % 2 == 0 ? start1 : start2);
        }
    }

    private void startMatchCountdown() {
        matchCountdown = true;
        new BukkitRunnable() {
            private int timer = 5;

            @Override
            public void run() {
                if (!matchCountdown) {
                    this.cancel();
                    return;
                }

                if (timer == 1) {
                    matchCountdown = false;
                    this.cancel();
                }
                players.forEach(player -> player.sendMessage(ChatColor.YELLOW + "You can break blocks in " + ChatColor.GOLD + timer));
                timer--;
            }

        }.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    @EventHandler
    public void snowBreak(BlockBreakEvent event) {
        if (blockEvent(event.getPlayer()))
            return;

        if (matchCountdown) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();
        Location loc = block.getLocation();
        if (region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()) && event.getBlock().getType() == Material.SNOW_BLOCK) {
            block.setType(Material.AIR);
            event.getPlayer().getInventory().addItem(new ItemStack(Material.SNOW_BALL, 4));
        }
    }

    @EventHandler
    public void snowBallHitSnow(ProjectileHitEvent event) {
        ProjectileSource shooter = event.getEntity().getShooter();
        if (!(shooter instanceof Player) || blockEvent((Player) shooter))
            return;

        Entity entity = event.getEntity();

        if (entity instanceof Snowball) {
            Location loc = entity.getLocation();
            Vector vec = entity.getVelocity();
            Location loc2 = new Location(loc.getWorld(), loc.getX() + vec.getX(), loc.getY() + vec.getY(), loc.getZ() + vec.getZ());
            if (loc2.getBlock().getType() == Material.SNOW_BLOCK) {
                loc2.getBlock().setType(Material.AIR);
            }
        }
    }
}
