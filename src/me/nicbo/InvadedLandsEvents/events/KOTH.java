package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.InvadedLandsEvents.event.EventLeaveEvent;
import me.nicbo.InvadedLandsEvents.scoreboard.EventScoreboard;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import me.nicbo.InvadedLandsEvents.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * KOTH Event:
 * All players get a kit
 * One player can be capturing the zone at a time
 * Once a player reaches the max points they win
 *
 * @author Nicbo
 * @since 2020-05-11
 */

public final class KOTH extends InvadedEvent {
    private final ProtectedRegion region;

    private final List<Location> locations;
    private final ItemStack[] armour;
    private final ItemStack[] kit;

    private BukkitRunnable regionChecker;
    private BukkitRunnable incrementPoints;

    private Set<Player> respawningPlayers;

    private Map<Player, KOTHSB> scoreboards;
    private Map<Player, Integer> points;
    private List<Player> playersInRegion;

    private Player capturing;
    private Player leader;

    private final int TIME_LIMIT;
    private final int WIN_POINTS;

    private final String CAPTURING;
    private final String CAPTURING_POINTS;
    private final String LOST;

    public KOTH() {
        super("King Of The Hill", "koth");

        this.region = getRegion(eventConfig.getString("cap-region"));

        this.locations = new ArrayList<>();

        for (int i = 1; i < 5; i++) {
            this.locations.add(ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-" + i), eventWorld));
        }

        this.armour = new ItemStack[] {
                ItemUtils.addEnchant(new ItemStack(Material.IRON_BOOTS, 1), Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                ItemUtils.addEnchant(new ItemStack(Material.IRON_LEGGINGS, 1), Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                ItemUtils.addEnchant(new ItemStack(Material.IRON_CHESTPLATE, 1), Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                ItemUtils.addEnchant(new ItemStack(Material.IRON_HELMET, 1), Enchantment.PROTECTION_ENVIRONMENTAL, 2)
        };

        this.kit = new ItemStack[] {
                ItemUtils.addEnchant(new ItemStack(Material.IRON_SWORD, 1), Enchantment.DAMAGE_ALL, 1),
                new ItemStack(Material.BOW, 1),
                new ItemStack(Material.GOLDEN_APPLE, 10),
                new ItemStack(Material.ARROW, 32)
        };

        this.TIME_LIMIT = eventConfig.getInt("int-seconds-time-limit");
        this.WIN_POINTS = eventConfig.getInt("int-win-points");

        this.CAPTURING = getEventMessage("CAPTURING");
        this.CAPTURING_POINTS = getEventMessage("CAPTURING_POINTS");
        this.LOST = getEventMessage("LOST");

        setSpectatorSB(new KOTHSB(null));
    }

    public Player getLeader() {
        return leader;
    }

    @Override
    public void init() {
        this.scoreboards = new HashMap<>();
        this.points = new HashMap<>();
        this.playersInRegion = new ArrayList<>();
        this.respawningPlayers = new HashSet<>();
        this.regionChecker = new BukkitRunnable() {
            @Override
            public void run() {
                if (capturing == null) {
                    setRandomCapturing();
                }

                for (Player player : players) { // Loop through each player
                    if (EventUtils.isLocInRegion(player.getLocation(), region)) { // Check if player is in cap region
                        if (!playersInRegion.contains(player)) // If they aren't already added to the playersInRegion list, add them
                            playersInRegion.add(player);
                    } else // Player is not in cap region, remove them from list
                        playersInRegion.remove(player);
                }

                if (capturing != null && !playersInRegion.contains(capturing) ) { // Player stepped out of cap region
                    lostCapturingPoint(capturing);
                    setRandomCapturing();
                }

            }
        };

        this.incrementPoints = new BukkitRunnable() {
            @Override
            public void run() {
                if (capturing != null) {
                    points.put(capturing, points.get(capturing) + 1);
                    if (points.get(capturing) % 5 == 0)
                        EventUtils.broadcastEventMessage(CAPTURING_POINTS.replace("{player}", capturing.getName())
                                .replace("{points}", String.valueOf(points.get(capturing))));

                    if (points.get(capturing) >= points.get(leader))
                        leader = capturing;

                    if (points.get(capturing) == WIN_POINTS)
                        playerWon(capturing);
                }
            }
        };
    }

    @Override
    public void start() {
        startTimer(TIME_LIMIT);
        this.leader = GeneralUtils.getRandom(players);
        regionChecker.runTaskTimerAsynchronously(plugin, 0, 1);
        incrementPoints.runTaskTimerAsynchronously(plugin, 0, 20);

        for (Player player : players) {
            KOTHSB kothSB = new KOTHSB(player);
            scoreboards.put(player, kothSB);
            player.setScoreboard(kothSB.getScoreboard());
            points.put(player, 0);
            giveKit(player);
            player.teleport(getRandomLocation());
        }

        startRefreshing(scoreboards);
    }

    @Override
    public void over() {
        eventTimer.cancel();
        regionChecker.cancel();
        incrementPoints.cancel();

        this.scoreboards.clear();
        this.points.clear();
        this.playersInRegion.clear();
        this.respawningPlayers.clear();

        this.capturing = null;
        this.leader = null;
    }

    private void lostCapturingPoint(Player player) {
        EventUtils.broadcastEventMessage(LOST.replace("{player}", player.getName()));
    }

    private void setRandomCapturing() {
        capturing = GeneralUtils.getRandom(playersInRegion);

        if (capturing != null)
            EventUtils.broadcastEventMessage(CAPTURING.replace("{player}", capturing.getName()));
    }

    private void giveKit(Player player) {
        player.getInventory().setArmorContents(armour);
        player.getInventory().setContents(kit);
    }

    private Location getRandomLocation() {
        return GeneralUtils.getRandom(locations);
    }

    @EventHandler
    public void playerHurt(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (blockListener(player))
                return;

            if (event.getFinalDamage() >= player.getHealth()) {
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                if (player.equals(capturing)) {
                    lostCapturingPoint(player);
                }
                setRandomCapturing();
                respawningPlayers.add(player);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.spigot().respawn(), 1);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (respawningPlayers.contains(player)) {
            giveKit(player);
            event.setRespawnLocation(getRandomLocation());
            respawningPlayers.remove(player);
        }
    }

    @EventHandler
    public void onEventLeave(EventLeaveEvent event) {
        if (blockListener(event.getPlayer())) {
            return;
        }

        Player player = event.getPlayer();
        playersInRegion.remove(player);
        scoreboards.remove(player);
    }

    public final class KOTHSB extends EventScoreboard {
        private TrackRow playerCount;
        private TrackRow specCount;
        private TrackRow timeRemaining;
        private TrackRow pointsTrack;
        private TrackRow leadTrack;
        private TrackRow capturingTrack;

        private Row blank1;
        private Row blank2;
        private Row lead;
        private Row header;
        private Row footer;
        private Row cap;

        public KOTHSB(Player player) {
            super(player, "koth");
            this.header = new Row("header", HEADERFOOTER, ChatColor.BOLD.toString(), HEADERFOOTER, 12);
            this.playerCount = new TrackRow("playerCount", ChatColor.YELLOW + "Players: ", ChatColor.DARK_PURPLE + "" + ChatColor.GOLD, String.valueOf(0), 11);
            this.specCount = new TrackRow("specCount", ChatColor.YELLOW + "Spectators: ", ChatColor.LIGHT_PURPLE + "" + ChatColor.GOLD, String.valueOf(0), 10);
            this.timeRemaining = new TrackRow("timeRemaining", ChatColor.YELLOW + "Time Remain", "ing: " + ChatColor.GOLD, String.valueOf(0), 9);
            this.pointsTrack = new TrackRow("pointsTrack", ChatColor.YELLOW + "Your Points: ", ChatColor.GRAY + "" + ChatColor.GOLD, String.valueOf(0), 8);
            this.blank1 = new Row("blank1", "", ChatColor.DARK_BLUE.toString(), "", 7);
            this.lead = new Row("lead", ChatColor.YELLOW + "In the Lead:", ChatColor.ITALIC.toString(), ChatColor.RED.toString(), 6);
            this.leadTrack = new TrackRow("leadTrack", "None", ChatColor.BLACK + "" + ChatColor.GRAY, ": " + ChatColor.GOLD + "0/0", 5);
            this.blank2 = new Row("blank2", "", ChatColor.GREEN.toString(), "", 4);
            this.cap = new Row("capturing", ChatColor.DARK_AQUA.toString(), ChatColor.YELLOW + "Capturing:", ChatColor.WHITE.toString(), 3);
            this.capturingTrack = new TrackRow("capturingTrack", ChatColor.AQUA.toString(), ChatColor.GRAY.toString(), ChatColor.RED + "No one" + ChatColor.GRAY + " (0)", 2);
            this.footer = new Row("footer", HEADERFOOTER, ChatColor.DARK_PURPLE.toString(), HEADERFOOTER, 1);
            super.init(ChatColor.GOLD + "KOTH", header, playerCount, specCount, timeRemaining, pointsTrack, blank1, lead, leadTrack, blank2, cap, capturingTrack, footer);
        }

        @Override
        public void refresh() {
            playerCount.setSuffix(String.valueOf(players.size()));
            specCount.setSuffix(String.valueOf(spectators.size()));
            timeRemaining.setSuffix(GeneralUtils.formatSeconds(timeLeft));
            pointsTrack.setSuffix(String.valueOf(player == null ? 0 : points.get(player)));

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

            String capturingName = capturing == null ? "No one" : capturing.getName();

            if (capturing == null) {
                capturingTrack.setPrefix("");
                capturingTrack.setSuffix(ChatColor.RED + "No one.");
            } else if (capturingName.length() > 14) {
                String p1 = capturingName.substring(0, 12);
                String p2 = capturingName.substring(12);
                capturingTrack.setPrefix(ChatColor.GOLD + p1);
                capturingTrack.setSuffix(ChatColor.GOLD + p2 + ChatColor.GRAY + " (" + points.get(capturing) + ")");
            } else {
                capturingTrack.setPrefix(ChatColor.GOLD + capturingName);
                capturingTrack.setSuffix(" (" + points.get(capturing) + ")");
            }
        }
    }
}
