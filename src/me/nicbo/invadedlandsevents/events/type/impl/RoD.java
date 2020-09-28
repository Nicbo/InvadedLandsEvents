package me.nicbo.invadedlandsevents.events.type.impl;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.events.type.TimerEvent;
import me.nicbo.invadedlandsevents.messages.impl.ListMessage;
import me.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import me.nicbo.invadedlandsevents.scoreboard.line.TrackLine;
import me.nicbo.invadedlandsevents.util.SpigotUtils;
import me.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.function.Function;

/**
 * All players are teleported to the start location
 * The first player to finish the parkour and reach
 * the win region wins the event
 *
 * @author Nicbo
 */

public final class RoD extends TimerEvent {
    private final ProtectedRegion winRegion;
    private final Location startLoc;
    private final BukkitRunnable didPlayerFinish;

    public RoD(InvadedLandsEvents plugin) {
        super(plugin, "Race of Death", "rod");
        this.winRegion = getEventRegion("win-region");
        this.startLoc = getEventLocation("start");

        this.didPlayerFinish = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getPlayersView()) {
                    if (SpigotUtils.isLocInRegion(player.getLocation(), winRegion)) {
                        winEvent(player);
                        this.cancel();
                    }
                }
            }
        };
    }

    @Override
    protected void start() {
        super.start();

        ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
        PotionEffect invis = new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false);

        for (Player player : getPlayersView()) {
            player.getInventory().setBoots(boots);
            player.addPotionEffect(invis);
            player.teleport(startLoc);
        }

        didPlayerFinish.runTaskTimer(plugin, 0, 1);
    }

    @Override
    protected void over() {
        super.over();
        this.didPlayerFinish.cancel();
    }

    @Override
    protected Function<Player, EventScoreboard> getScoreboardFactory() {
        return RoDSB::new;
    }

    @Override
    protected List<String> getDescriptionMessage() {
        return ListMessage.ROD_DESCRIPTION.get();
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && isPlayerParticipating((Player) event.getEntity())) {
            event.setCancelled(true);
        }
    }

    private final class RoDSB extends EventScoreboard {
        private final TrackLine playerCountTrack;
        private final TrackLine specCountTrack;
        private final TrackLine timeRemainingTrack;

        private RoDSB(Player player) {
            super(getConfigName(), player);
            this.playerCountTrack = new TrackLine("pctRoD", "&ePlayers: ", "&e&6", "", 4);
            this.specCountTrack = new TrackLine("sctRoD", "&eSpectators: ", "&0&6", "", 3);
            this.timeRemainingTrack = new TrackLine("trtRoD", "&eTime Remain", "ing: &6", "", 2);
            this.initLines(playerCountTrack, specCountTrack, timeRemainingTrack);
        }

        @Override
        protected void refresh() {
            playerCountTrack.setSuffix(String.valueOf(getPlayersSize()));
            specCountTrack.setSuffix(String.valueOf(getSpectatorsSize()));
            timeRemainingTrack.setSuffix(StringUtils.formatSeconds(getTimeLeft()));
        }
    }
}