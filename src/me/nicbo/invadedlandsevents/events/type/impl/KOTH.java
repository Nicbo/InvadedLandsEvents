package me.nicbo.invadedlandsevents.events.type.impl;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.events.type.TimerEvent;
import me.nicbo.invadedlandsevents.messages.impl.ListMessage;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import me.nicbo.invadedlandsevents.scoreboard.line.Line;
import me.nicbo.invadedlandsevents.scoreboard.line.TrackLine;
import me.nicbo.invadedlandsevents.util.GeneralUtils;
import me.nicbo.invadedlandsevents.util.SpigotUtils;
import me.nicbo.invadedlandsevents.util.StringUtils;
import me.nicbo.invadedlandsevents.util.item.Enchant;
import me.nicbo.invadedlandsevents.util.item.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Function;

/**
 * All players get a kit
 * One player can be capturing the zone at a time
 * Once a player reaches the max points they win
 *
 * @author Nicbo
 */

public final class KOTH extends TimerEvent {
    private final ItemStack[] armour;
    private final ItemStack[] kit;

    private final ProtectedRegion region;

    private final List<Location> locations;

    private final BukkitRunnable regionChecker;
    private final BukkitRunnable incrementPoints;

    private final Set<Player> respawningPlayers;
    private final List<Player> playersInRegion;
    private final Map<Player, Integer> points;

    private Player capturing;
    private Player leader;

    private final int WIN_POINTS;

    private final List<String> description;

    public KOTH(InvadedLandsEvents plugin) {
        super(plugin, "King of the Hill", "koth");

        this.armour = new ItemStack[]{
                new ItemBuilder(Material.IRON_BOOTS).setEnchants(new Enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2)).build(),
                new ItemBuilder(Material.IRON_LEGGINGS).setEnchants(new Enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2)).build(),
                new ItemBuilder(Material.IRON_CHESTPLATE).setEnchants(new Enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2)).build(),
                new ItemBuilder(Material.IRON_HELMET).setEnchants(new Enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2)).build()
        };

        this.kit = new ItemStack[]{
                new ItemBuilder(Material.IRON_SWORD).setEnchants(new Enchant(Enchantment.DAMAGE_ALL, 1)).build(),
                new ItemStack(Material.BOW),
                new ItemStack(Material.GOLDEN_APPLE, 10),
                new ItemStack(Material.ARROW, 32)
        };

        this.region = getEventRegion("cap-region");

        this.locations = new ArrayList<>();

        for (int i = 1; i < 5; i++) {
            this.locations.add(getEventLocation("start-" + i));
        }

        this.points = new HashMap<>();
        this.playersInRegion = new ArrayList<>();
        this.respawningPlayers = new HashSet<>();

        this.WIN_POINTS = getEventInteger("win-points");

        this.regionChecker = new BukkitRunnable() {
            @Override
            public void run() {
                if (capturing == null) {
                    setRandomCapturing();
                }

                for (Player player : getPlayersView()) { // Loop through each player
                    if (SpigotUtils.isLocInRegion(player.getLocation(), region)) { // Check if player is in cap region
                        if (!playersInRegion.contains(player)) { // If they aren't already added to the playersInRegion list, add them
                            playersInRegion.add(player);
                        }
                    } else { // Player is not in cap region, remove them from list
                        playersInRegion.remove(player);
                    }
                }

                if (capturing != null && !playersInRegion.contains(capturing)) { // Player stepped out of cap region
                    lostCapturingPoint(capturing);
                    setRandomCapturing();
                }

            }
        };

        this.incrementPoints = new BukkitRunnable() {
            @Override
            public void run() {
                if (capturing != null) {
                    int newCapturingPoints = points.getOrDefault(capturing, 0) + 1;

                    points.put(capturing, newCapturingPoints);
                    if (newCapturingPoints % 5 == 0) {
                        broadcastEventMessage(Message.KOTH_CAPTURING_POINTS.get()
                                .replace("{player}", capturing.getName())
                                .replace("{points}", String.valueOf(newCapturingPoints)));
                    }

                    if (newCapturingPoints >= points.getOrDefault(leader, 0)) {
                        leader = capturing;
                    }

                    if (newCapturingPoints == WIN_POINTS) {
                        winEvent(capturing);
                    }
                }
            }
        };

        this.description = new ArrayList<>();

        for (String message : ListMessage.KOTH_DESCRIPTION.get()) {
            this.description.add(message.replace("{points}", String.valueOf(WIN_POINTS)));
        }
    }

    @Override
    public Player getTimerEndWinner() {
        return leader;
    }

    @Override
    protected void start() {
        this.leader = GeneralUtils.getRandom(getPlayersView());
        regionChecker.runTaskTimer(plugin, 0, 1);
        incrementPoints.runTaskTimer(plugin, 0, 20);

        for (Player player : getPlayersView()) {
            givePlayerKit(player);
            player.teleport(getRandomLocation());
        }

        super.start();
    }

    @Override
    protected void over() {
        super.over();
        this.regionChecker.cancel();
        this.incrementPoints.cancel();
    }

    @Override
    protected Function<Player, EventScoreboard> getScoreboardFactory() {
        return KOTHSB::new;
    }

    @Override
    protected List<String> getDescriptionMessage() {
        return description;
    }

    @Override
    public void leaveEvent(Player player) {
        super.leaveEvent(player);

        if (isRunning()) {
            points.remove(player);
            playersInRegion.remove(player);
        }
    }

    private void setRandomCapturing() {
        capturing = playersInRegion.isEmpty() ? null : GeneralUtils.getRandom(playersInRegion);

        if (capturing != null) {
            broadcastEventMessage(Message.KOTH_CAPTURING.get().replace("{player}", capturing.getName()));
        }
    }

    private void lostCapturingPoint(Player player) {
        broadcastEventMessage(Message.KOTH_LOST.get().replace("{player}", player.getName()));
    }

    private void givePlayerKit(Player player) {
        player.getInventory().setArmorContents(armour);
        player.getInventory().setContents(kit);
    }

    private Location getRandomLocation() {
        return GeneralUtils.getRandom(locations);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHitKOTH(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (ignoreEvent(player))
                return;

            if (player.getHealth() - event.getFinalDamage() <= 0) {
                SpigotUtils.clearInventory(player);
                if (player.equals(capturing)) {
                    lostCapturingPoint(player);
                }

                setRandomCapturing();
                respawningPlayers.add(player);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.spigot().respawn(), 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawnKOTH(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (respawningPlayers.remove(player)) {
            givePlayerKit(player);
            event.setRespawnLocation(getRandomLocation());
        }
    }

    private final class KOTHSB extends EventScoreboard {
        private final TrackLine playerCountTrack;
        private final TrackLine specCountTrack;
        private final TrackLine timeRemainingTrack;
        private final TrackLine pointsTrack;
        private final TrackLine leadTrack;
        private final TrackLine capturingTrack;

        private KOTHSB(Player player) {
            super(getConfigName(), player);
            this.playerCountTrack = new TrackLine("pctKOTH", "&ePlayers: ", "&5&6", "", 11);
            this.specCountTrack = new TrackLine("sctKOTH", "&eSpectators: ", "&d&6", "", 10);
            this.timeRemainingTrack = new TrackLine("trtKOTH", "&eTime Remain", "ing: &6", "", 9);
            this.pointsTrack = new TrackLine("ptKOTH", "&eYour Points: ", "&7&6", "", 8);
            Line blank1 = new Line("b1KOTH", "", "&1", "", 7);
            Line lead = new Line("lKOTH", "&eIn the Lead:", "&o", "", 6);
            this.leadTrack = new TrackLine("ltKOTH", "None", "&0&7", "", 5);
            Line blank2 = new Line("b2KOTH", "", "&a", "", 4);
            Line cap = new Line("cKOTH", "&3", "&eCapturing:", "", 3);
            this.capturingTrack = new TrackLine("ctKOTH", "&b", "&7", "", 2);
            this.initLines(playerCountTrack, specCountTrack, timeRemainingTrack, pointsTrack, blank1, lead, leadTrack, blank2, cap, capturingTrack);
        }

        @Override
        protected void refresh() {
            playerCountTrack.setSuffix(String.valueOf(getPlayersSize()));
            specCountTrack.setSuffix(String.valueOf(getSpectatorsSize()));
            timeRemainingTrack.setSuffix(StringUtils.formatSeconds(getTimeLeft()));
            pointsTrack.setSuffix(String.valueOf(points.getOrDefault(getPlayer(), 0)));

            String leaderName = leader.getName();
            ChatColor colour = leader == getPlayer() ? ChatColor.GREEN : ChatColor.RED;

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

            String capturingName = capturing == null ? "No one" : capturing.getName();

            if (capturing == null) {
                capturingTrack.setPrefix("");
                capturingTrack.setSuffix("&cNo one.");
            } else if (capturingName.length() > 14) {
                String p1 = capturingName.substring(0, 12);
                String p2 = capturingName.substring(12);
                capturingTrack.setPrefix(ChatColor.GOLD + p1);
                capturingTrack.setSuffix("&6" + p2 + " &7(" + points.getOrDefault(capturing, 0) + ")");
            } else {
                capturingTrack.setPrefix("&6" + capturingName);
                capturingTrack.setSuffix(" (" + points.getOrDefault(capturing, 0) + ")");
            }
        }
    }
}
