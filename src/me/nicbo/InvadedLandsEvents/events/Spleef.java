package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Spleef extends InvadedEvent {
    private BukkitRunnable heightCheck;
    private ProtectedRegion region;
    private int minY;

    public Spleef(EventsMain plugin) {
        super("Spleef", plugin);
        this.heightCheck = new BukkitRunnable() {
            @Override
            public void run() {
                List<Player> toLose = new ArrayList<>();
                for (Player player : players) {
                    if (player.getLocation().getY() < minY - 1) {
                        toLose.add(player);
                    }
                }
                toLose.forEach(player -> loseEvent(player));
            }
        };
    }

    @Override
    public void init(EventsMain plugin) {
        RegionManager regionManager = plugin.getWorldGuardPlugin().getRegionManager(ConfigUtils.getEventWorld());
        String regionName = eventConfig.getString("region");

        try {
            region = regionManager.getRegion(regionName);
        } catch (NullPointerException npe) {
            log.severe("Spleef region '" + regionName + "' does not exist");
        }

        BlockVector pos1 = ConfigUtils.blockVectorFromConfig(eventConfig.getConfigurationSection("snow-position-1"));
        BlockVector pos2 = ConfigUtils.blockVectorFromConfig(eventConfig.getConfigurationSection("snow-position-2"));
        buildSnow(pos1, pos2);
    }

    @Override
    public void start() {
        started = true;
        tpPlayers();
        players.forEach(player -> player.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SPADE, 1)));
        heightCheck.runTaskTimerAsynchronously(plugin, 0, 1);
        playerCheck.runTaskTimerAsynchronously(plugin, 0, 1);
    }

    @Override
    public void stop() {
        started = false;
        heightCheck.cancel();
        playerCheck.cancel();
        spawnTpPlayers();
        EventManager.setEventRunning(false);
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
                    ConfigUtils.getEventWorld().getBlockAt(x, y, z).setType(Material.SNOW_BLOCK);
                    System.out.println("block snow now " + x + " " + y  + " " + z);
                }
            }
        }
    }

    private void tpPlayers() {
        for (int i = 0; i < players.size(); i++) {
            Location start = ConfigUtils.locFromConfig(eventConfig.getConfigurationSection("start-location-" + (i % 2 == 0 ? 1 : 2)));
            players.get(i).teleport(start);
        }
    }

    @EventHandler
    public void snowClick(BlockDamageEvent event) {
        if (!started || !players.contains(event.getPlayer())) return;

        Location loc = event.getBlock().getLocation();
        if (event.getPlayer().getItemInHand().getType() == Material.DIAMOND_SPADE && region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()) && event.getBlock().getType() == Material.SNOW_BLOCK) {
            event.setInstaBreak(true);
            event.getPlayer().getInventory().addItem(new ItemStack(Material.SNOW_BALL, 4));
        }
    }

    @EventHandler
    public void snowBallHitSnow(ProjectileHitEvent event) {
        if (!started || !players.contains(event.getEntity().getShooter())) return;

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
