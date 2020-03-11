package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.InvadedLandsEvents.EventsMain;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class RoD extends InvadedEvent {
    private BukkitRunnable didPlayerFinish;
    private ProtectedRegion winRegion;

    public RoD(EventsMain plugin) {
        super("Race of Death", "rod", plugin);

        String regionName = eventConfig.getString("win-region");
        try {
            this.winRegion = regionManager.getRegion(regionName);
        } catch (NullPointerException npe) {
            logger.severe("RoD region '" + regionName + "' does not exist");
        }
    }

    @Override
    public void init(EventsMain plugin) {
        this.didPlayerFinish = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : players) {
                    Vector vector = player.getLocation().toVector();
                    // Idk trying to do this w/out worldedit api
                    if (winRegion.contains()) {

                    }

                }
            }
        };
    }

    @Override
    public void start() {
        tpApplyInvisibility();
    }

    @Override
    public void stop() {

    }

    private void tpApplyInvisibility() {
        for (Player player : players) {
             player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 6000, 1, false, false));
             player.teleport((Location) eventConfig.get("start-location"));
        }
    }
}
