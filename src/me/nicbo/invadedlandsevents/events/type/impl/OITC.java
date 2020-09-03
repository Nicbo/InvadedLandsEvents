package me.nicbo.invadedlandsevents.events.type.impl;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.events.type.TimerEvent;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import me.nicbo.invadedlandsevents.scoreboard.line.Line;
import me.nicbo.invadedlandsevents.scoreboard.line.TrackLine;
import me.nicbo.invadedlandsevents.util.SpigotUtils;
import me.nicbo.invadedlandsevents.util.GeneralUtils;
import me.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Function;


/**
 * All players have a wooden sword, a bow, and 1 arrow
 * Arrows one shot and when you get a kill you receive an arrow
 *
 * @author Nicbo
 * @author StarZorrow
 */

public final class OITC extends TimerEvent {
    private final ItemStack[] kit;

    private final List<Location> locations;
    private final Map<Player, Integer> points;
    private Player leader;

    private final Set<Player> respawningPlayers;

    private final int WIN_POINTS;

    public OITC(InvadedLandsEvents plugin) {
        super(plugin, "One in the Chamber", "oitc");

        this.kit = new ItemStack[]{
                new ItemStack(Material.WOOD_SWORD),
                new ItemStack(Material.BOW),
                new ItemStack(Material.ARROW)
        };

        this.locations = new ArrayList<>();

        for (int i = 1; i < 9; i++) {
            this.locations.add(getEventLocation("start-" + i));
        }

        this.points = new HashMap<>();
        this.respawningPlayers = new HashSet<>();

        this.WIN_POINTS = getEventInteger("win-points");
    }

    @Override
    public void leaveEvent(Player player) {
        super.leaveEvent(player);

        if (isRunning()) {
            points.remove(player);
        }
    }

    @Override
    protected void start() {
        List<Player> players = getPlayersView();
        this.leader = GeneralUtils.getRandom(players);

        for (Player player : players) {
            player.getInventory().setContents(kit);
            player.teleport(GeneralUtils.getRandom(locations));
        }

        super.start();
    }

    @Override
    protected Function<Player, EventScoreboard> getScoreboardFactory() {
        return OITCSB::new;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHitOITC(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (ignoreEvent(player)) {
                return;
            }

            if (isEnding()) {
                event.setCancelled(true);
                return;
            }

            Entity damager = event.getDamager();
            Player shooter = null;


            // Player hit with arrow shot from another player
            if (damager instanceof Arrow && ((Arrow) damager).getShooter() instanceof Player) {
                shooter = (Player) ((Arrow) damager).getShooter();

                if (player.equals(shooter)) {
                    event.setCancelled(true);
                    return;
                } else {
                    event.setDamage(20);
                }
            }

            if (player.getHealth() - event.getFinalDamage() <= 0) { // Damage will kill player
                SpigotUtils.clearInventory(player);
                respawningPlayers.add(player);

                Player killer = null;

                // Killer shot arrow
                if (shooter != null) {
                    killer = shooter;
                } else if (damager instanceof Player) {
                    killer = (Player) damager;
                }

                // Killer is not the same player
                if (killer != null && !player.equals(killer)) {
                    int newKillerPoints = points.getOrDefault(killer, 0) + 1;
                    points.put(killer, newKillerPoints);


                    broadcastEventMessage(Message.OITC_KILL_MESSAGE.get()
                            .replace("{killer}", killer.getName())
                            .replace("{killer_points}", String.valueOf(newKillerPoints))
                            .replace("{player}", player.getName())
                            .replace("{player_points}", String.valueOf(points.getOrDefault(player, 0))));

                    killer.setHealth(20);
                    killer.getInventory().addItem(kit[2]);

                    // Killer is now in the lead
                    if (newKillerPoints >= points.getOrDefault(leader, 0)) {
                        leader = killer;
                    }

                    // Killer wins
                    if (newKillerPoints >= WIN_POINTS) {
                        event.setCancelled(true);
                        winEvent(killer);
                    }
                }

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.spigot().respawn(), 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawnOITC(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (respawningPlayers.remove(player)) {
            player.getInventory().setContents(kit);
            event.setRespawnLocation(GeneralUtils.getRandom(locations));
        }
    }

    private final class OITCSB extends EventScoreboard {
        private final TrackLine playerCountTrack;
        private final TrackLine specCountTrack;
        private final TrackLine timeRemainingTrack;
        private final TrackLine pointsTrack;
        private final TrackLine leadTrack;

        private OITCSB(Player player) {
            super(getConfigName(), player);
            this.playerCountTrack = new TrackLine("pctOITC", "&ePlayers: ", "&a&6", "", 8);
            this.specCountTrack = new TrackLine("sctOITC", "&eSpectators: ", "&d&6", "", 7);
            this.timeRemainingTrack = new TrackLine("trtOITC", "&eTime Remain", "ing: &6", "", 6);
            this.pointsTrack = new TrackLine("ptOITC", "&eYour Points: ", "&0&6", "", 5);
            Line blank = new Line("bOITC", "", "&b", "", 4);
            Line lead = new Line("lOITC", "", "&eIn the Lead:", "", 3);
            this.leadTrack = new TrackLine("ltOITC", "None", "&7", ": " + "", 2);
            this.initLines(playerCountTrack, specCountTrack, timeRemainingTrack, pointsTrack, blank, lead, leadTrack);
        }

        @Override
        protected void refresh() {
            playerCountTrack.setSuffix(String.valueOf(getPlayersSize()));
            specCountTrack.setSuffix(String.valueOf(getSpectatorsSize()));
            timeRemainingTrack.setSuffix(StringUtils.formatSeconds(getTimeLeft()));
            pointsTrack.setSuffix(String.valueOf(points.getOrDefault(getPlayer(), 0)));

            String leaderName = leader.getName();
            ChatColor colour = leader.equals(getPlayer()) ? ChatColor.GREEN : ChatColor.RED;

            int leaderPoints = points.getOrDefault(leader, 0);
            if (leaderName.length() > 14) {
                String p1 = leaderName.substring(0, 12);
                String p2 = leaderName.substring(12);
                leadTrack.setPrefix(colour + p1);
                leadTrack.setSuffix(colour + p2 + "&7: &6" + leaderPoints + "/" + WIN_POINTS);
            } else {
                leadTrack.setPrefix(colour + leaderName);
                leadTrack.setSuffix("&7: &6" + leaderPoints + "/" + WIN_POINTS);
            }
        }
    }

    /*
    TODO:
        - When this was tested on a 1 gb server it was really laggy (check if it was just hardware or event)
     */
}
