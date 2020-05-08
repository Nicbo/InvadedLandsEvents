package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.scoreboard.EventScoreboard;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import sun.java2d.loops.FillRect;

import java.util.*;

/**
 * OITC event:
 * All players have a wooden sword, a bow, and 1 arrow
 * Arrows one shot and when you get a kill you receive an arrow
 *
 * @author Nicbo
 * @author StarZorrow
 * @since 2020-03-13
 */

public final class OITC extends InvadedEvent {
    private List<Location> locations;
    private ItemStack[] kit;

    private HashMap<Player, Integer> points;
    private Player leader;

    private Set<Player> respawningPlayers;

    private final int WIN_POINTS;
    private final int TIME_LIMIT;

    private final String KILL_MESSAGE;

    public OITC() {
        super("One in the Chamber", "oitc");
        this.locations = new ArrayList<>();

        for (int i = 1; i < 9; i++) {
            this.locations.add(ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-" + i), eventWorld));
        }

        this.kit = new ItemStack[] {
                new ItemStack(Material.WOOD_SWORD, 1),
                new ItemStack(Material.BOW, 1),
                new ItemStack(Material.ARROW, 1)
        };

        this.points = new HashMap<>();
        this.respawningPlayers = new HashSet<>();

        this.WIN_POINTS = eventConfig.getInt("int-win-points");
        this.TIME_LIMIT = eventConfig.getInt("int-seconds-time-limit");

        this.KILL_MESSAGE = getEventMessage("KILL_MESSAGE");
    }

    @Override
    public void init() {
        initPlayerCheck();
    }

    @Override
    public void start() {
        players.forEach(player -> giveScoreboard(player, new OITCSB(player)));
        this.leader = GeneralUtils.getRandom(players);
        startTimer(TIME_LIMIT);
        playerCheck.runTaskTimerAsynchronously(plugin, 0, 1);
        for (Player player : players) {
            points.put(player, 0);
            preparePlayer(player);
            player.teleport(getRandomLocation());
        }
    }

    @Override
    public void over() {
        eventTimer.cancel();
        playerCheck.cancel();
        points.clear();
        respawningPlayers.clear();
    }

    private void preparePlayer(Player player) {
        player.getInventory().setContents(kit);
    }

    private Location getRandomLocation() {
        return GeneralUtils.getRandom(locations);
    }

    private String getKillMessage(Player killer, int killerPoints, Player player, int playerPoints) {
        return KILL_MESSAGE.replace("{killer}", killer.getName())
                .replace("{killer_points}", String.valueOf(killerPoints))
                .replace("{player}", player.getName())
                .replace("{player_points}", String.valueOf(playerPoints));
    }

    @EventHandler
    public void playerHurt(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (blockListener(player))
                return;

            if (event.getDamager() instanceof Arrow) {
                event.setDamage(20);
            }

            if (event.getFinalDamage() >= player.getHealth()) {
                player.getInventory().clear();
                respawningPlayers.add(player);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.spigot().respawn(), 1);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!respawningPlayers.contains(player))
            return;

        Player killer = player.getKiller();
        if (killer != null && player != killer) {
            points.put(killer, points.get(killer) + 1);
            EventUtils.broadcastEventMessage(getKillMessage(killer, points.get(killer), player, points.get(player)));
            killer.setHealth(20);
            killer.getInventory().addItem(kit[2]);
            if (points.get(player) >= points.get(leader))
                leader = killer;

            if (points.get(killer) == WIN_POINTS)
                playerWon(killer);
        }

        preparePlayer(player);
        event.setRespawnLocation(getRandomLocation());
        respawningPlayers.remove(player);
    }

    public class OITCSB extends EventScoreboard {
        private TrackRow playerCount;
        private TrackRow specCount;
        private TrackRow timeRemaining;
        private TrackRow pointsTrack;
        private TrackRow leadTrack;

        private Row blank;
        private Row lead;
        private Row header;
        private Row footer;

        public OITCSB(Player player) {
            super(player, "oitc");
            this.header = new Row("header", HEADERFOOTER, ChatColor.BOLD.toString(), HEADERFOOTER, 9);
            this.playerCount = new TrackRow("playerCount", ChatColor.YELLOW + "Players: ", ChatColor.DARK_PURPLE + "" + ChatColor.GOLD, String.valueOf(0), 8);
            this.specCount = new TrackRow("specCount", ChatColor.YELLOW + "Spectators: ", ChatColor.LIGHT_PURPLE + "" + ChatColor.GOLD, String.valueOf(0), 7);
            this.timeRemaining = new TrackRow("timeRemaining", ChatColor.YELLOW + "Time Remain", "ing: " + ChatColor.GOLD, String.valueOf(0), 6);
            this.pointsTrack = new TrackRow("pointsTrack", ChatColor.YELLOW + "Your Points: ", ChatColor.BLACK + "" + ChatColor.GOLD, String.valueOf(0), 5);
            this.blank = new Row("blank", "", ChatColor.AQUA.toString(), "", 4);
            this.lead = new Row("lead", "", ChatColor.YELLOW + "In the Lead:", "", 3);
            this.leadTrack = new TrackRow("leadTrack", player.getName(), ChatColor.GRAY.toString(), ": " + ChatColor.GOLD + "0/20", 2);
            this.footer = new Row("footer", HEADERFOOTER, ChatColor.DARK_GRAY.toString(), HEADERFOOTER, 1);
            super.init("OITC", header, playerCount, specCount, timeRemaining, pointsTrack, blank, lead, leadTrack, footer);
        }

        @Override
        public void refresh() {
            playerCount.setSuffix(String.valueOf(players.size()));
            specCount.setSuffix(String.valueOf(spectators.size()));
            timeRemaining.setSuffix(GeneralUtils.formatSeconds(timeLeft));
            pointsTrack.setSuffix(String.valueOf(points.get(player)));

            String leaderName = leader.getName();
            ChatColor colour = leader == player ? ChatColor.GREEN : ChatColor.RED;

            if (leaderName.length() > 14) {
                String p1 = leaderName.substring(0, 12);
                String p2 = leaderName.substring(12);
                leadTrack.setPrefix(colour + p1);
                leadTrack.setSuffix(colour + p2 + ChatColor.GRAY + ": " + ChatColor.GOLD + points.get(leader) + "/" + WIN_POINTS);
            } else {
                leadTrack.setPrefix(colour + leaderName);
                leadTrack.setSuffix(ChatColor.GRAY + ": " + ChatColor.GOLD + points.get(leader) + "/" + WIN_POINTS);
            }
        }
    }
}
