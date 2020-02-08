package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.Material;
import org.bukkit.util.BlockVector;
public class Spleef extends InvadedEvent {

    public Spleef(EventsMain plugin) {
        super("Spleef", plugin.getConfig());
        BlockVector pos1 = (BlockVector) config.get("events.spleef.snow-position-1");
        BlockVector pos2 = (BlockVector) config.get("events.spleef.snow-position-2");
        buildSnow(pos1, pos2);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    private void buildSnow(BlockVector pos1, BlockVector pos2) {
        int minX = (int) Math.min(pos1.getX(), pos2.getX());
        int minY = (int) Math.min(pos1.getY(), pos2.getY());
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
}
