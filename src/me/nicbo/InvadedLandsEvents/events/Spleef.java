package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.scoreboard.EventScoreboard;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Spleef event:
 * All players are teleported to 2 start locations with shovels
 * Thrown snowballs break whatever block is hit
 *
 * @author Nicbo
 * @author StarZorrow
 * @since 2020-03-12
 */

public final class Spleef extends InvadedEvent {
    private boolean matchCountdown;
    private BukkitRunnable heightCheck;

    private int minY;
    private Location start1;
    private Location start2;
    private BlockVector pos1;
    private BlockVector pos2;

    private ItemStack shovel;

    private final int TIME_LIMIT;

    private final String MATCH_COUNTER;
    private final String MATCH_START;
    private final String ELIMINATED;

    public Spleef() {
        super("Spleef", "spleef");

        this.shovel = new ItemStack(Material.DIAMOND_SPADE);
        ItemMeta itemMeta = shovel.getItemMeta();
        itemMeta.addEnchant(Enchantment.DIG_SPEED, 5, true);
        this.shovel.setItemMeta(itemMeta);

        this.start1 = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-1"), eventWorld);
        this.start2 = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-2"), eventWorld);

        this.pos1 = ConfigUtils.deserializeBlockVector(eventConfig.getConfigurationSection("snow-position-1"));
        this.pos2 = ConfigUtils.deserializeBlockVector(eventConfig.getConfigurationSection("snow-position-2"));

        this.TIME_LIMIT = eventConfig.getInt("int-seconds-time-limit");

        this.MATCH_COUNTER = getEventMessage("MATCH_COUNTER");
        this.MATCH_START = getEventMessage("MATCH_START");
        this.ELIMINATED = getEventMessage("ELIMINATED");
    }

    @Override
    public void init() {
        buildSnow(pos1, pos2);
        this.heightCheck = new BukkitRunnable() {
            private List<Player> toLose = new ArrayList<>();

            @Override
            public void run() {
                for (Player player : players) {
                    if (player.getLocation().getY() < minY - 1) {
                        toLose.add(player);
                    }
                }
                for (Player player : toLose) {
                    loseEvent(player);
                    EventUtils.broadcastEventMessage(ELIMINATED.replace("{player}", player.getName())
                            .replace("{remaining}", String.valueOf(players.size())));
                }
                toLose.clear();
            }
        };

        initPlayerCheck();
    }

    @Override
    public void start() {
        startTimer(TIME_LIMIT);

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            giveScoreboard(player, new SpleefSB(player));
            player.getInventory().clear();
            player.teleport(i % 2 == 0 ? start1 : start2);
        }

        heightCheck.runTaskTimerAsynchronously(plugin, 0, 1);
        playerCheck.runTaskTimerAsynchronously(plugin, 0, 1);
        startMatchCountdown();
        players.forEach(player -> player.getInventory().setItem(0, shovel));
    }

    @Override
    public void over() {
        eventTimer.cancel();
        heightCheck.cancel();
        playerCheck.cancel();
    }

    public void buildSnow(BlockVector pos1, BlockVector pos2) {
        int minX = (int) Math.min(pos1.getX(), pos2.getX());
        this.minY = (int) Math.min(pos1.getY(), pos2.getY());
        int minZ = (int) Math.min(pos1.getZ(), pos2.getZ());
        int maxX = (int) Math.max(pos1.getX(), pos2.getX());
        int maxY = (int) Math.max(pos1.getY(), pos2.getY());
        int maxZ = (int) Math.max(pos1.getZ(), pos2.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    eventWorld.getBlockAt(x, y, z).setType(Material.SNOW_BLOCK);
                }
            }
        }
    }

    private void startMatchCountdown() {
        matchCountdown = true;
        new BukkitRunnable() {
            private int timer = 5;

            @Override
            public void run() {
                if (!matchCountdown) {
                    this.cancel();
                    return;
                }

                EventUtils.broadcastEventMessage(MATCH_COUNTER.replace("{seconds}", String.valueOf(timer)));
                if (timer == 1) {
                    EventUtils.broadcastEventMessage(MATCH_START);
                    matchCountdown = false;
                    this.cancel();
                }

                timer--;
            }

        }.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    @EventHandler
    public void snowBreak(BlockBreakEvent event) {
        if (blockListener(event.getPlayer()))
            return;

        if (matchCountdown) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();
        if (event.getBlock().getType() == Material.SNOW_BLOCK) {
            block.setType(Material.AIR);
            event.getPlayer().getInventory().addItem(new ItemStack(Material.SNOW_BALL, 4));
        }
    }

    @EventHandler
    public void snowballHitSnow(ProjectileHitEvent event) { // Needs changing
        ProjectileSource shooter = event.getEntity().getShooter();
        if (!(shooter instanceof Player) || blockListener((Player) shooter))
            return;

        Entity entity = event.getEntity();

        if (entity instanceof Snowball) {
            Location loc = entity.getLocation();
            Vector vec = entity.getVelocity();
            Location loc2 = new Location(loc.getWorld(), loc.getX() + vec.getX(), loc.getY() + vec.getY(), loc.getZ() + vec.getZ());
            if (loc2.getBlock().getType() == Material.SNOW_BLOCK) {
                loc2.getBlock().setType(Material.AIR);
            }
        }
    }


    private class SpleefSB extends EventScoreboard {
        private TrackRow playerCount;
        private TrackRow specCount;
        private TrackRow timeRemaining;

        private Row header;
        private Row footer;

        public SpleefSB(Player player) {
            super(player, "spleef");
            this.header = new Row("header", HEADERFOOTER, ChatColor.BOLD.toString(), HEADERFOOTER, 5);
            this.playerCount = new TrackRow("playerCount", ChatColor.YELLOW + "Players: ", ChatColor.DARK_PURPLE + "" + ChatColor.GOLD, String.valueOf(0), 4);
            this.specCount = new TrackRow("specCount", ChatColor.YELLOW + "Spectators: ", ChatColor.LIGHT_PURPLE + "" + ChatColor.GOLD, String.valueOf(0), 3);
            this.timeRemaining = new TrackRow("timeRemaining", ChatColor.YELLOW + "Time Remain", "ing: " + ChatColor.GOLD, String.valueOf(0), 2);
            this.footer = new Row("footer", HEADERFOOTER, ChatColor.DARK_GRAY.toString(), HEADERFOOTER, 1);
            super.init("Spleef", header, playerCount, specCount, timeRemaining, footer);
        }

        @Override
        public void refresh() {
            playerCount.setSuffix(String.valueOf(players.size()));
            specCount.setSuffix(String.valueOf(spectators.size()));
            timeRemaining.setSuffix(GeneralUtils.formatSeconds(timeLeft));
        }
    }

    /*
    TODO:
        - Make ProjectileHitEvent event more reliable
     */
}
