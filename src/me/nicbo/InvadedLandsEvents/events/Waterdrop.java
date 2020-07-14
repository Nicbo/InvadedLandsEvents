package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.InvadedLandsEvents.scoreboard.EventScoreboard;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Waterdrop event:
 * All players are tp'd to a start location every round
 * Once player enters safe region they pass the round
 * Last player to fail wins
 *
 * @author Nicbo
 * @since 2020-02-29
 */

public final class Waterdrop extends InvadedEvent {
    private final WaterdropSB waterdropSB;

    private ProtectedRegion region;
    private Location startLoc;
    private BlockVector pos1;
    private BlockVector pos2;

    private Material[][] mainCover;
    private Material[][] closedCover;

    private final List<Material[][]> easyCovers;
    private final List<Material[][]> mediumCovers;
    private final List<Material[][]> hardCovers;

    private final Material water;
    private final Material redstone;

    private int round;
    private int timer;

    private List<Player> jumped;
    private List<Player> eliminated;

    private BukkitRunnable fallCheck;
    private BukkitRunnable waterdropTimer;

    private final String ROUND_START;
    private final String SUCCESS_JUMP;
    private final String FAIL_JUMP;
    private final String ELIMINATED;

    public Waterdrop() {
        super("Waterdrop", "waterdrop");

        this.waterdropSB = new WaterdropSB();

        this.region = getRegion(eventConfig.getString("safe-region"));

        this.startLoc = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location"), eventWorld);
        this.pos1 = ConfigUtils.deserializeBlockVector(eventConfig.getConfigurationSection("water-position-1"));
        this.pos2 = ConfigUtils.deserializeBlockVector(eventConfig.getConfigurationSection("water-position-2"));

        validateBlockVectors();

        this.water = Material.WATER;
        this.redstone = Material.REDSTONE_BLOCK;

        this.jumped = new ArrayList<>();
        this.eliminated = new ArrayList<>();

        this.easyCovers = new ArrayList<>();
        this.mediumCovers = new ArrayList<>();
        this.hardCovers = new ArrayList<>();

        loadCovers();

        this.ROUND_START = getEventMessage("ROUND_START");
        this.SUCCESS_JUMP = getEventMessage("SUCCESS_JUMP");
        this.FAIL_JUMP = getEventMessage("FAIL_JUMP");
        this.ELIMINATED = getEventMessage("ELIMINATED");

        setSpectatorSB(waterdropSB);
    }

    @Override
    public void init() {
        this.round = 1;
        this.timer = 20;
        this.waterdropTimer = new BukkitRunnable() {
            private final int[] times = new int[] { 20, 20, 19, 19, 18, 17, 17, 16, 15, 14, 14, 13, 12, 12, 10, 10, 10, 9, 8, 7, 7, 6 };

            @Override
            public void run() {
                timer--;
                if (timer <= 0) {
                    round++;
                    if (round >= times.length)
                        timer = 6;
                    else
                        timer = times[round - 1];
                    newRound();
                }
            }
        };

        this.fallCheck = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : players) {
                    if (startLoc.getBlockY() - player.getLocation().getBlockY() > 2 && player.isOnGround() && !jumped.contains(player)) {
                        if (EventUtils.isLocInRegion(player.getLocation(), region)) { // Player is in safe zone
                            EventUtils.broadcastEventMessage(SUCCESS_JUMP.replace("{player}", player.getName()));
                            eliminated.remove(player);
                        } else { // Player missed
                            EventUtils.broadcastEventMessage(FAIL_JUMP.replace("{player}", player.getName()));
                        }
                        jumped.add(player);
                    }
                }
            }
        };
    }

    @Override
    public void start() {
        players.forEach(player -> player.setScoreboard(waterdropSB.getScoreboard()));
        startRefreshing(waterdropSB);
        fallCheck.runTaskTimerAsynchronously(plugin,0, 1);
        waterdropTimer.runTaskTimer(plugin, 0, 20);
        newRound();
    }

    @Override
    public void over() {
        waterdropTimer.cancel();
        fallCheck.cancel();
    }

    @Override
    public void stop() {
        super.stop();
        eliminated.clear();
        jumped.clear();
    }

    private void newRound() {
        eliminatePlayers();
//        if (doNextRound()) {
//            eliminated = new ArrayList<>(players);
//            EventUtils.broadcastEventMessage(ROUND_START.replace("{round}", String.valueOf(round)));
//            jumped.clear();
//            setMainCover();
//            buildMainCover();
//            tpPlayers();
//        }
    }

    private void tpPlayers() {
        players.forEach(player -> player.teleport(startLoc));
    }

    private void eliminatePlayers() {
        for (Player player : eliminated) {
            EventUtils.broadcastEventMessage(ELIMINATED.replace("{player}", player.getName())
                    .replace("{remaining}", String.valueOf(players.size())));
        }
        loseEvent(eliminated);
    }

    private void setMainCover() {
        List<Material[][]> coverList;

        if (round <= 8) {
            coverList = easyCovers;
        } else if (round <= 14) {
            coverList = mediumCovers;
        } else if (round <= 20) {
            coverList = hardCovers;
        } else {
            mainCover = getHoleCover().clone();
            return;
        }

        mainCover = getRandomCover(coverList).clone();
    }

    /**
     * Gives random cover to use as main
     * @param coverList List to pick random cover from
     * @return Cover that is not equal to the last one used
     */

    private Material[][] getRandomCover(List<Material[][]> coverList) {
        Material[][] cover = GeneralUtils.getRandom(coverList);

        while (Arrays.deepEquals(mainCover, cover)) {
            cover = GeneralUtils.getRandom(coverList);
        }

        return cover;
    }

    private Material[][] getHoleCover() {
        Material[][] cover = closedCover.clone();

        for (int i = 0; i < (GeneralUtils.randomBoolean() ? 2 : 3); i++){
            cover[GeneralUtils.randomMinMax(0, 4)][GeneralUtils.randomMinMax(0, 4)] = water;
        }

        return cover;
    }

    private void buildMainCover() {
        int y = (int) pos1.getY();
        int minX = (int) Math.min(pos1.getX(), pos2.getX());
        int minZ = (int) Math.min(pos1.getZ(), pos2.getZ());
        int maxX = (int) Math.max(pos1.getX(), pos2.getX());
        int maxZ = (int) Math.max(pos1.getZ(), pos2.getZ());

        new BukkitRunnable() {
            public void run() {
                for (int x = minX; x <= maxX; x++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        eventWorld.getBlockAt(x, y, z).setType(mainCover[x - minX][z - minZ]);
                    }
                }
            }
        }.runTask(plugin);
    }

    private void validateBlockVectors() {
        enabled = false;
        if (pos1.getY() != pos2.getY()) {
            logger.severe("Waterdrop water position y values are not the same. Disabling.");
            enabled = false;
        } else if (Math.abs(pos1.getX() - pos2.getX()) != 4 || Math.abs(pos1.getZ() - pos2.getZ()) != 4) {
            logger.severe("Waterdrop water positions do not form a 5x5 square. Disabling.");
        } else {
            enabled = true;
        }
    }

    private void loadCovers() {
        this.closedCover = new Material[][] {
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone }
        };

        this.easyCovers.add(new Material[][]{ // Open
                { water, water, water, water, water },
                { water, water, water, water, water },
                { water, water, water, water, water },
                { water, water, water, water, water },
                { water, water, water, water, water },
        });

        this.easyCovers.add(new Material[][]{ // 3 lines
                { water, water, water, redstone, redstone },
                { water, water, water, redstone, redstone },
                { water, water, water, redstone, redstone },
                { water, water, water, redstone, redstone },
                { water, water, water, redstone, redstone },
        });

        this.easyCovers.add(new Material[][]{ // 2 lines
                { water, water, redstone, redstone, redstone },
                { water, water, redstone, redstone, redstone },
                { water, water, redstone, redstone, redstone },
                { water, water, redstone, redstone, redstone },
                { water, water, redstone, redstone, redstone },
        });

        this.easyCovers.add(new Material[][]{ // 3 lines reversed
                { redstone, redstone, water, water, water },
                { redstone, redstone, water, water, water },
                { redstone, redstone, water, water, water },
                { redstone, redstone, water, water, water },
                { redstone, redstone, water, water, water },
        });

        this.easyCovers.add(new Material[][]{ // 2 lines reversed
                { redstone, redstone, redstone, water, water },
                { redstone, redstone, redstone, water, water },
                { redstone, redstone, redstone, water, water },
                { redstone, redstone, redstone, water, water },
                { redstone, redstone, redstone, water, water },
        });

        this.easyCovers.add(new Material[][] { // N
                { redstone, water, water, water, redstone },
                { redstone, redstone, water, water, redstone },
                { redstone, water, redstone, water, redstone },
                { redstone, water, water, redstone, redstone },
                { redstone, water, water, water, redstone }
        });

        this.mediumCovers.add(new Material[][] { // H
                { water, redstone, redstone, redstone, water },
                { water, redstone, redstone, redstone, water },
                { water, water, water, water, water },
                { water, redstone, redstone, redstone, water },
                { water, redstone, redstone, redstone, water }
        });

        this.mediumCovers.add(new Material[][] { // -
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone },
                { water, water, water, water, water },
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone }
        });

        this.mediumCovers.add(new Material[][] { // |
                { redstone, redstone, water, redstone, redstone },
                { redstone, redstone, water, redstone, redstone },
                { redstone, redstone, water, redstone, redstone },
                { redstone, redstone, water, redstone, redstone },
                { redstone, redstone, water, redstone, redstone }
        });

        this.mediumCovers.add(new Material[][] { // +
                { redstone, redstone, water, redstone, redstone },
                { redstone, redstone, water, redstone, redstone },
                { water, water, water, water, water },
                { redstone, redstone, water, redstone, redstone },
                { redstone, redstone, water, redstone, redstone }
        });

        this.mediumCovers.add(new Material[][] { // Target
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, water, water, water, redstone },
                { redstone, water, redstone, water, redstone },
                { redstone, water, water, water, redstone },
                { redstone, redstone, redstone, redstone, redstone }
        });

        this.mediumCovers.add(new Material[][] { // Square
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, water, water, redstone, redstone },
                { redstone, water, water, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone }
        });

        this.mediumCovers.add(new Material[][] { // S
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, water, water, water, water },
                { redstone, redstone, redstone, redstone, redstone },
                { water, water, water, water, redstone },
                { redstone, redstone, redstone, redstone, redstone }
        });

        this.hardCovers.add(new Material[][] { // 4 corners
                { water, redstone, redstone, redstone, water },
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone },
                { water, redstone, redstone, redstone, water }
        });

        this.hardCovers.add(new Material[][] { // 2 bottom
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, redstone, redstone, redstone, redstone },
                { water, water, redstone, redstone, redstone }
        });

        this.hardCovers.add(new Material[][] { // X
                { water, redstone, redstone, redstone, water },
                { redstone, water, redstone, water, redstone },
                { redstone, redstone, water, redstone, redstone },
                { redstone, water, redstone, water, redstone },
                { water, redstone, redstone, redstone, water }
        });

        this.hardCovers.add(new Material[][] { // 4 holes
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, water, redstone, water, redstone },
                { redstone, redstone, redstone, redstone, redstone },
                { redstone, water, redstone, water, redstone },
                { redstone, redstone, redstone, redstone, redstone }
        });

        this.hardCovers.add(new Material[][] { // Checkered
                { water, redstone, water, redstone, water },
                { redstone, water, redstone, water, redstone },
                { water, redstone, water, redstone, water },
                { redstone, water, redstone, water, redstone },
                { water, redstone, water, redstone, water }
        });
    }

   /*
    easyCovers - up to round 8
    mediumCovers - up to round 14
    hardCovers - up to round 20
   */

    private final class WaterdropSB extends EventScoreboard {
        private final TrackRow roundTrack;
        private final TrackRow timerTrack;
        private final TrackRow playerCount;
        private final TrackRow specCount;

        public WaterdropSB() {
            super("waterdrop");
            Row header = new Row("header", HEADERFOOTER, ChatColor.BOLD.toString(), HEADERFOOTER, 7);
            this.roundTrack = new TrackRow("round", ChatColor.YELLOW + "Round: ", ChatColor.GOLD.toString(), String.valueOf(0), 6);
            this.timerTrack = new TrackRow("timer", ChatColor.YELLOW + "Time Remain", "ing: " + ChatColor.GOLD, String.valueOf(20), 5);
            Row blank = new Row("blank", "", ChatColor.AQUA.toString(), "", 4);
            this.playerCount = new TrackRow("playerCount", ChatColor.YELLOW + "Players: ", ChatColor.DARK_PURPLE + "" + ChatColor.GOLD, String.valueOf(0), 3);
            this.specCount = new TrackRow("specCount", ChatColor.YELLOW + "Spectators: ", ChatColor.LIGHT_PURPLE + "" + ChatColor.GOLD, String.valueOf(0), 2);
            Row footer = new Row("footer", HEADERFOOTER, ChatColor.DARK_GRAY.toString(), HEADERFOOTER, 1);
            super.init(ChatColor.GOLD + "Waterdrop", header, roundTrack, timerTrack, blank, playerCount, specCount, footer);
        }

        @Override
        public void refresh() {
            specCount.setSuffix(String.valueOf(spectators.size()));
            roundTrack.setSuffix(String.valueOf(round));
            timerTrack.setSuffix(String.valueOf(timer));
            playerCount.setSuffix(String.valueOf(players.size()));
        }
    }
}
