package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class RoD extends InvadedEvent {
    private BukkitRunnable didPlayerFinish;
    private ProtectedRegion winRegion;
    private Location startLoc;

    public RoD(EventsMain plugin) {
        super("Race of Death", "rod", plugin);

        String regionName = eventConfig.getString("win-region");
        try {
            this.winRegion = regionManager.getRegion(regionName);
        } catch (NullPointerException npe) {
            logger.severe("RoD region '" + regionName + "' does not exist");
        }

        this.startLoc = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location"));
    }

    @Override
    public void init(EventsMain plugin) {
        initPlayerCheck();
        this.didPlayerFinish = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : players) {
                    if (winRegion.contains(plugin.getWorldGuardPlugin().wrapPlayer(player).getPosition())) {
                        playerWon(player);
                    }
                }
            }
        };
    }

    @Override
    public void start() {
        didPlayerFinish.runTaskTimerAsynchronously(plugin, 0, 1);
        playerCheck.runTaskTimerAsynchronously(plugin, 0, 1);
        tpApplyInvisibility();
    }

    @Override
    public void stop() {
        started = false;
        didPlayerFinish.cancel();
        playerCheck.cancel();
        removePlayers();
        plugin.getManagerHandler().getEventManager().setCurrentEvent(null);
    }

    private void tpApplyInvisibility() {
        for (Player player : players) {
             player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 6000, 1, false, false));
             player.teleport(startLoc);
        }
    }
}
