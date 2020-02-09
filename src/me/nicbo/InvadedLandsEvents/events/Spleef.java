package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

public class Spleef extends InvadedEvent {
    private int minY;
    private ProtectedRegion region;

    public Spleef(EventsMain plugin) {
        super("Spleef", plugin);
    }

    @Override
    protected void init() {
        BlockVector pos1 = (BlockVector) eventConfig.get("snow-position-1");
        BlockVector pos2 = (BlockVector) eventConfig.get("snow-position-2");
        buildSnow(pos1, pos2);
    }

    @Override
    public void start() {
        started = true;
        tpPlayers();
        players.forEach(player -> player.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SPADE, 1)));
    }

    @Override
    public void stop() {

    }

    private void buildSnow(BlockVector pos1, BlockVector pos2) {
        int minX = (int) Math.min(pos1.getX(), pos2.getX());
        minY = (int) Math.min(pos1.getY(), pos2.getY());
        int minZ = (int) Math.min(pos1.getZ(), pos2.getZ());
        int maxX = (int) Math.max(pos1.getX(), pos2.getX());
        int maxY = (int) Math.max(pos1.getY(), pos2.getY());
        int maxZ = (int) Math.max(pos1.getZ(), pos2.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    ConfigUtils.getEventWorld().getBlockAt(x, y, z).setType(Material.SNOW);
                }
            }
        }
    }

    private void tpPlayers() {
        for (int i = 0; i < players.size(); i++) {
            Location start = ConfigUtils.locFromConfig(eventConfig.getConfigurationSection("events.spleef.start-location-" + (i % 2 == 0 ? 1 : 2)));
            players.get(i).teleport(start);
        }
    }

    @EventHandler
    public void snowClick(BlockDamageEvent event) {
        if (!started) return;

        Location loc = event.getBlock().getLocation();
        if (region.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()) && event.getBlock().getType() == Material.SNOW) {
            event.setInstaBreak(true);
            event.getPlayer().getInventory().addItem(new ItemStack(Material.SNOW_BALL, 4));
        }
    }

    @EventHandler
    public void snowBallHitSnow(ProjectileHitEvent event) {
        if (!started) return;

        Block block = event.getEntity().getLocation().getBlock();
        if (block.getType().equals(Material.SNOW)) {
            block.setType(Material.AIR);
        }
    }
}
