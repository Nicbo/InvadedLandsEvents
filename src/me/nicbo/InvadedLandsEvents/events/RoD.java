package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class RoD extends InvadedEvent {
    private BukkitRunnable didPlayerFinish;

    public RoD(EventsMain plugin) {
        super("Race of Death", "rod", plugin);
        init(plugin);
    }

    @Override
    public void init(EventsMain plugin) {
        this.didPlayerFinish = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : players) {
                    // Check if player entered finish region
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
