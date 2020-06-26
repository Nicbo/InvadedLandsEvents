package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.InvadedLandsEvents.scoreboard.EventScoreboard;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * RoD event:
 * All players are tp'd to a start location
 * Once player enters win region they win event
 *
 * @author Nicbo
 * @since 2020-03-10
 */

public final class RoD extends InvadedEvent {
    private final RoDSB rodSB;

    private final ProtectedRegion winRegion;
    private final Location startLoc;

    private final ItemStack boots;

    private BukkitRunnable didPlayerFinish;

    private final int TIME_LIMIT;

    public RoD() {
        super("Race of Death", "rod");
        this.rodSB = new RoDSB();
        this.winRegion = getRegion(eventConfig.getString("win-region"));
        this.startLoc = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location"), eventWorld);
        this.boots = new ItemStack(Material.LEATHER_BOOTS, 1);
        this.TIME_LIMIT = eventConfig.getInt("int-seconds-time-limit");
        setSpectatorSB(rodSB);
    }

    @Override
    public void init() {
        this.didPlayerFinish = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : players) {
                    if (EventUtils.isLocInRegion(player.getLocation(), winRegion)) {
                        playerWon(player);
                        this.cancel();
                    }
                }
            }
        };
    }

    @Override
    public void start() {
        for (Player player : players) {
            player.setScoreboard(rodSB.getScoreboard());
            player.getInventory().setBoots(boots);
        }

        startRefreshing(rodSB);
        plugin.getServer().getScheduler().runTask(plugin, this::tpApplyInvisibility);
        didPlayerFinish.runTaskTimerAsynchronously(plugin, 0, 1);
        startTimer(TIME_LIMIT);
    }

    @Override
    public void over() {
        didPlayerFinish.cancel();
        eventTimer.cancel();
    }

    private void tpApplyInvisibility() {
        for (Player player : players) {
             player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 6000, 1, false, false));
             player.teleport(startLoc);
        }
    }

    private class RoDSB extends EventScoreboard {
        private TrackRow playerCount;
        private TrackRow specCount;
        private TrackRow timeRemaining;

        private Row header;
        private Row footer;

        public RoDSB() {
            super("lms");
            this.header = new Row("header", HEADERFOOTER, ChatColor.BOLD.toString(), HEADERFOOTER, 5);
            this.playerCount = new TrackRow("playerCount", ChatColor.YELLOW + "Players: ", ChatColor.DARK_PURPLE + "" + ChatColor.GOLD, String.valueOf(0), 4);
            this.specCount = new TrackRow("specCount", ChatColor.YELLOW + "Spectators: ", ChatColor.LIGHT_PURPLE + "" + ChatColor.GOLD, String.valueOf(0), 3);
            this.timeRemaining = new TrackRow("timeRemaining", ChatColor.YELLOW + "Time Remain", "ing: " + ChatColor.GOLD, String.valueOf(0), 2);
            this.footer = new Row("footer", HEADERFOOTER, ChatColor.DARK_PURPLE.toString(), HEADERFOOTER, 1);
            super.init(ChatColor.GOLD + "RoD", header, playerCount, specCount, timeRemaining, footer);
        }

        @Override
        public void refresh() {
            playerCount.setSuffix(String.valueOf(players.size()));
            specCount.setSuffix(String.valueOf(spectators.size()));
            timeRemaining.setSuffix(GeneralUtils.formatSeconds(timeLeft));
        }
    }
}
