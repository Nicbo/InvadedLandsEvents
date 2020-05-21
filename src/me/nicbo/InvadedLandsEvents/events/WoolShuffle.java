package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.scoreboard.EventScoreboard;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;

/**
 * WoolShuffle event:
 * Players must stand on the chosen wool
 * Last player standing wins
 *
 * @author Nicbo
 * @since 2020-05-19
 */

public final class WoolShuffle extends InvadedEvent {
    private WoolShuffleSB woolShuffleSB;

    private Location startLoc;

    private int index;

    private Wool[] wools;
    private final String[] woolNames;

    private boolean pvpEnabled;

    private int round;
    private int timer;
    private BukkitRunnable roundTimer;

    private final String ROUND_START;
    private final String FAILED;
    private final String ELIMINATED;
    private final String COLOUR;
    private final String PVP_ENABLED;
    private final String PVP_DISABLED;

    public WoolShuffle() {
        super("Wool Shuffle", "woolshuffle");

        this.woolShuffleSB = new WoolShuffleSB();

        this.startLoc = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location"), eventWorld);

        this.wools = new Wool[] {
                new Wool(DyeColor.ORANGE),
                new Wool(DyeColor.YELLOW),
                new Wool(DyeColor.GREEN),
                new Wool(DyeColor.PINK),
                new Wool(DyeColor.CYAN),
                new Wool(DyeColor.PURPLE),
                new Wool(DyeColor.BLUE),
        };

        this.woolNames = new String[] {
                ChatColor.GOLD + "Orange",
                ChatColor.YELLOW + "Yellow",
                ChatColor.GREEN + "Green",
                ChatColor.LIGHT_PURPLE + "Pink",
                ChatColor.DARK_AQUA + "Cyan",
                ChatColor.DARK_PURPLE + "Purple",
                ChatColor.BLUE + "Blue"
        };

        this.ROUND_START = getEventMessage("ROUND_START");
        this.FAILED = getEventMessage("FAILED");
        this.ELIMINATED = getEventMessage("ELIMINATED");
        this.COLOUR = getEventMessage("COLOUR");
        this.PVP_ENABLED = getEventMessage("PVP_ENABLED");
        this.PVP_DISABLED = getEventMessage("PVP_DISABLED");

        setSpectatorSB(woolShuffleSB);
    }

    @Override
    public void init() {
        this.round = 1;
        this.timer = 15;

        this.roundTimer = new BukkitRunnable() {
            private final int[] times = new int[]{15, 15, 14, 14, 13, 13, 12, 12, 11, 11, 10, 10, 9, 9, 8, 7, 7, 6, 6, 5, 5, 4, 4, 3, 2};

            @Override
            public void run() {
                timer--;
                if (timer <= 0) {
                    round++;
                    if (round >= times.length)
                        timer = 2;
                    else
                        timer = times[round - 1];
                    newRound();
                }
            }
        };
    }

    @Override
    public void start() {
        roundTimer.runTaskTimerAsynchronously(plugin, 0, 20);
        for (Player player : players) {
            player.setScoreboard(woolShuffleSB.getScoreboard());
            player.teleport(startLoc);
        }
        startRefreshing(woolShuffleSB);
        newRound();
    }

    @Override
    public void over() {
        roundTimer.cancel();
    }

    private void newRound() {
        // Check if all players are on correct wool
        EventUtils.broadcastEventMessage(ROUND_START.replace("{round}", String.valueOf(round)));
        if (round != 1) {
            Iterator<Player> iterator = players.iterator();

            while (iterator.hasNext()) {
                Player player = iterator.next();
                if (!isPlayerOnWool(player)) {
                    loseEvent(player);
                    EventUtils.broadcastEventMessage(FAILED.replace("{player}", player.getName()));
                    EventUtils.broadcastEventMessage(ELIMINATED.replace("{player}", player.getName()).replace("{remaining}", String.valueOf(players.size())));
                }

            }
        }

        // Pick new wool
        int newIndex = GeneralUtils.randomMinMax(0, 6);
        while (newIndex == index) {
            newIndex = GeneralUtils.randomMinMax(0, 6);
        }

        index = newIndex;
        EventUtils.broadcastEventMessage(COLOUR.replace("{colour}", woolNames[index]));

        // Give wool to all players
        // Random potion (speed, slow)
        for (Player player : players) {
            fillInvWool(player);
            // player.addPotionEffect();
        }

        // Pvp toggle (only first 5 rounds)
        if (round < 6) {
            pvpEnabled = GeneralUtils.randomBoolean();
        } else {
            pvpEnabled = true;
        }

        EventUtils.broadcastEventMessage(pvpEnabled ? PVP_ENABLED : PVP_DISABLED);
    }

    private void fillInvWool(Player player) {
        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, wools[index].toItemStack(1));
        }
    }

    private boolean isPlayerOnWool(Player player) {
        Location loc = player.getLocation();
        loc.setY(loc.getY() - 1);
        Block block = loc.getWorld().getBlockAt(loc);
        loc.setY(loc.getY() - 1);
        Block blockUnder = loc.getWorld().getBlockAt(loc);
        return isEqual(block, wools[index])|| isEqual(blockUnder, wools[index]);
    }

    private boolean isEqual(Block block, Wool wool) {
        if (block.getType() == Material.WOOL) {
            return wool.equals(block.getState().getData());
        }
        return false;
    }

    @EventHandler
    public void playerHurt(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (blockListener(player))
                return;

            if (!pvpEnabled) {
                event.setCancelled(true);
            }
        }
    }

    private class WoolShuffleSB extends EventScoreboard {
        private TrackRow roundTrack;
        private TrackRow countDown;
        private TrackRow colourTrack;
        private TrackRow pvpTrack;
        private TrackRow playerCount;
        private TrackRow specCount;

        private Row blank;
        private Row header;
        private Row footer;

        public WoolShuffleSB() {
            super(null, "woolshuffle");
            this.header = new Row("header", HEADERFOOTER, ChatColor.BOLD.toString(), HEADERFOOTER, 7);
            this.roundTrack = new TrackRow("round", ChatColor.YELLOW + "Round: ", ChatColor.GOLD.toString(), String.valueOf(0), 6);
            this.countDown = new TrackRow("countDown", ChatColor.YELLOW + "Time Remain", "ing: " + ChatColor.GOLD, String.valueOf(20), 5);
            this.colourTrack = new TrackRow("colourTrack", ChatColor.YELLOW + "Run to: ", ChatColor.AQUA + "" + ChatColor.RESET, "None", 6);
            this.pvpTrack = new TrackRow("pvpTrack", ChatColor.YELLOW + "PvP: ", ChatColor.RED + "" + ChatColor.RESET, "None", 5);
            this.blank = new Row("blank", "", ChatColor.AQUA.toString(), "", 4);
            this.playerCount = new TrackRow("playerCount", ChatColor.YELLOW + "Players: ", ChatColor.DARK_PURPLE + "" + ChatColor.GOLD, String.valueOf(0), 3);
            this.specCount = new TrackRow("specCount", ChatColor.YELLOW + "Spectators: ", ChatColor.LIGHT_PURPLE + "" + ChatColor.GOLD, String.valueOf(0), 2);
            this.footer = new Row("footer", HEADERFOOTER, ChatColor.DARK_GRAY.toString(), HEADERFOOTER, 1);
            super.init(ChatColor.GOLD + "Wool Shuffle", header, roundTrack, countDown, colourTrack, pvpTrack, blank, playerCount, specCount, footer);
        }

        @Override
        public void refresh() {
            specCount.setSuffix(String.valueOf(spectators.size()));
            roundTrack.setSuffix(String.valueOf(round));
            countDown.setSuffix(String.valueOf(timer));
            playerCount.setSuffix(String.valueOf(players.size()));
            colourTrack.setSuffix(woolNames[index]);
            pvpTrack.setSuffix(ChatColor.BOLD + (pvpEnabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"));
        }
    }

    /*
    TODO:
        - barely tested but it should work
        - Potion effects
     */
}
