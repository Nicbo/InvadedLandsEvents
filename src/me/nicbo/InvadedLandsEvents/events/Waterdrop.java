package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.InvadedLandsEvents.EventsMain;
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
import java.util.List;

/**
 * Waterdrop event:
 * All players are tp'd to a start location every round
 * Once player enters safe region they pass the round
 * Last player to fail wins
 *
 * @author Nicbo
 * @since 2020-05-10
 */

public final class Waterdrop extends InvadedEvent {
    private ProtectedRegion region;
    private Location startLoc;
    private BlockVector pos1;
    private BlockVector pos2;

    private Material[][] mainCover;
    private Material[][] closedCover;

    private List<Material[][]> easyCovers;
    private List<Material[][]> mediumCovers;
    private List<Material[][]> hardCovers;

    private Material water;
    private Material redstone;

    private int round;
    private int timer;

    private List<Player> jumped;
    private List<Player> eliminated;

    private boolean eventOver;
    private BukkitRunnable fallCheck;
    private BukkitRunnable waterdropTimer;

    private final String ROUND_START;
    private final String SUCCESS_JUMP;
    private final String FAIL_JUMP;
    private final String ELIMINATED;

    public Waterdrop(EventsMain plugin) {
        super("Waterdrop", "waterdrop", plugin);

        String regionName = eventConfig.getString("safe-region");
        try {
            this.region = regionManager.getRegion(regionName);
        } catch (NullPointerException npe) {
            logger.severe("Waterdrop region '" + regionName + "' does not exist");
        }

        this.startLoc = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location"), eventWorld);
        this.pos1 = ConfigUtils.deserializeBlockVector(eventConfig.getConfigurationSection("water-position-1"));
        this.pos2 = ConfigUtils.deserializeBlockVector(eventConfig.getConfigurationSection("water-position-2"));

        validateBlockVectors();

        this.water = Material.WATER;
        this.redstone = Material.REDSTONE_BLOCK;

        this.easyCovers = new ArrayList<>();
        this.mediumCovers = new ArrayList<>();
        this.hardCovers = new ArrayList<>();

        loadCovers();

        this.jumped = new ArrayList<>();
        this.eliminated = new ArrayList<>();

        this.ROUND_START = getEventMessage("ROUND_START");
        this.SUCCESS_JUMP = getEventMessage("SUCCESS_JUMP");
        this.FAIL_JUMP = getEventMessage("FAIL_JUMP");
        this.ELIMINATED = getEventMessage("ELIMINATED");
    }

    @Override
    public void init(EventsMain plugin) {
        this.round = 1;
        this.timer = 20;
        this.eventOver = false;
        this.waterdropTimer = new BukkitRunnable() {
            private int[] times = new int[] { 20, 20, 19, 19, 18, 17, 17, 16, 15, 14, 14, 13, 12, 12, 10, 10, 10, 9, 8, 7, 7, 6 };

            @Override
            public void run() {
                // When scoreboards are added use timer on sb instead of broadcasting
                EventUtils.broadcastEventMessage(ChatColor.YELLOW + "timer: " + timer--);
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
                    if (!eventOver && startLoc.getBlockY() - player.getLocation().getBlockY() > 2 && player.isOnGround() && !jumped.contains(player)) {
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
        fallCheck.runTaskTimerAsynchronously(plugin,0, 1);
        waterdropTimer.runTaskTimer(plugin, 0, 20);
        newRound();
    }

    @Override
    public void stop() {
        started = false;
        waterdropTimer.cancel();
        fallCheck.cancel();
        jumped.clear();
        eliminated.clear();
        removeParticipants();
    }

    private void newRound() {
        eliminatePlayers();
        if (!eventOver) {
            eliminated = new ArrayList<>(players);
            EventUtils.broadcastEventMessage(ROUND_START.replace("{round}", String.valueOf(round)));
            jumped.clear();
            setMainCover();
            buildMainCover();
            tpPlayers();
        }
    }

    private void tpPlayers() {
        players.forEach(player -> player.teleport(startLoc));
    }

    private void eliminatePlayers() {
        int playersSize = players.size();

        for (Player player : eliminated) {
            EventUtils.broadcastEventMessage(ELIMINATED.replace("{player}", player.getName())
                    .replace("{players_left}", String.valueOf(players.size())));
            loseEvent(player);
        }

        if (eliminated.size() == playersSize || players.size() == 0) {
            playerWon(null);
        } else if (players.size() == 1) {
            playerWon(players.get(0));
        } else {
            return;
        }

        eventOver = true;
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
        Material[][] cover = GeneralUtils.getRandom(coverList).clone();

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
}
