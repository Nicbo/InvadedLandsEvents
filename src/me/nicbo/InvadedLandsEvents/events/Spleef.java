package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockVector;

public class Spleef extends InvadedEvent {
    private int minY;
    private ProtectedCuboidRegion region;
    public Spleef(EventsMain plugin) {
        super("Spleef", plugin.getConfig());
        BlockVector pos1 = (BlockVector) config.get("events.spleef.snow-position-1");
        BlockVector pos2 = (BlockVector) config.get("events.spleef.snow-position-2");
        buildSnow(pos1, pos2);
    }

    @Override
    public void start() {
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
            Location start = ConfigUtils.locFromConfig(config.getConfigurationSection("events.spleef.start-location-" + (i % 2 == 0 ? 1 : 2)));
            players.get(i).teleport(start);
        }
    }

    @EventHandler
    public void snowClick(BlockDamageEvent event) { // check if its in spleef region
        if (event.getBlock().getType() == Material.SNOW) {
            event.setInstaBreak(true);
        }
    }
}
