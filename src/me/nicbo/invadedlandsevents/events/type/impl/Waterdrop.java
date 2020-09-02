package me.nicbo.invadedlandsevents.events.type.impl;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.events.type.RoundEvent;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import me.nicbo.invadedlandsevents.scoreboard.line.Line;
import me.nicbo.invadedlandsevents.scoreboard.line.TrackLine;
import me.nicbo.invadedlandsevents.util.SpigotUtils;
import me.nicbo.invadedlandsevents.util.GeneralUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * All players are teleported to a start location every round
 * Once player enters safe region they pass the round
 * Last player to fail wins
 *
 * @author Nicbo
 */

public final class Waterdrop extends RoundEvent {
    private final Material water;

    private final Material[][] closedCover;

    private final List<Material[][]> easyCovers;
    private final List<Material[][]> mediumCovers;
    private final List<Material[][]> hardCovers;

    private Material[][] mainCover;

    private final ProtectedRegion region;
    private final Location startLoc;
    private final BlockVector pos1;
    private final BlockVector pos2;

    private final Set<Player> jumped;
    private final Set<Player> eliminated;

    private final BukkitRunnable fallCheck;

    public Waterdrop(InvadedLandsEvents plugin) {
        super(plugin, "waterdrop", "Waterdrop", new int[]{20, 20, 19, 19, 18, 17, 17, 16, 15, 14, 14, 13, 12, 12, 10, 10, 10, 9, 8, 7, 7, 6});

        water = Material.WATER;
        final Material redstone = Material.REDSTONE_BLOCK;

        closedCover = new Material[][]{
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, redstone, redstone, redstone, redstone}
        };

        easyCovers = Arrays.asList(new Material[][]{ // Open
                {water, water, water, water, water},
                {water, water, water, water, water},
                {water, water, water, water, water},
                {water, water, water, water, water},
                {water, water, water, water, water},
        }, new Material[][]{ // 3 lines
                {water, water, water, redstone, redstone},
                {water, water, water, redstone, redstone},
                {water, water, water, redstone, redstone},
                {water, water, water, redstone, redstone},
                {water, water, water, redstone, redstone},
        }, new Material[][]{ // 2 lines
                {water, water, redstone, redstone, redstone},
                {water, water, redstone, redstone, redstone},
                {water, water, redstone, redstone, redstone},
                {water, water, redstone, redstone, redstone},
                {water, water, redstone, redstone, redstone},
        }, new Material[][]{ // 3 lines reversed
                {redstone, redstone, water, water, water},
                {redstone, redstone, water, water, water},
                {redstone, redstone, water, water, water},
                {redstone, redstone, water, water, water},
                {redstone, redstone, water, water, water},
        }, new Material[][]{ // 2 lines reversed
                {redstone, redstone, redstone, water, water},
                {redstone, redstone, redstone, water, water},
                {redstone, redstone, redstone, water, water},
                {redstone, redstone, redstone, water, water},
                {redstone, redstone, redstone, water, water},
        }, new Material[][]{ // N
                {redstone, water, water, water, redstone},
                {redstone, redstone, water, water, redstone},
                {redstone, water, redstone, water, redstone},
                {redstone, water, water, redstone, redstone},
                {redstone, water, water, water, redstone}
        });

        mediumCovers = Arrays.asList(new Material[][]{ // H
                {water, redstone, redstone, redstone, water},
                {water, redstone, redstone, redstone, water},
                {water, water, water, water, water},
                {water, redstone, redstone, redstone, water},
                {water, redstone, redstone, redstone, water}
        }, new Material[][]{ // -
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, redstone, redstone, redstone, redstone},
                {water, water, water, water, water},
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, redstone, redstone, redstone, redstone}
        }, new Material[][]{ // |
                {redstone, redstone, water, redstone, redstone},
                {redstone, redstone, water, redstone, redstone},
                {redstone, redstone, water, redstone, redstone},
                {redstone, redstone, water, redstone, redstone},
                {redstone, redstone, water, redstone, redstone}
        }, new Material[][]{ // +
                {redstone, redstone, water, redstone, redstone},
                {redstone, redstone, water, redstone, redstone},
                {water, water, water, water, water},
                {redstone, redstone, water, redstone, redstone},
                {redstone, redstone, water, redstone, redstone}
        }, new Material[][]{ // Target
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, water, water, water, redstone},
                {redstone, water, redstone, water, redstone},
                {redstone, water, water, water, redstone},
                {redstone, redstone, redstone, redstone, redstone}
        }, new Material[][]{ // Square
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, water, water, redstone, redstone},
                {redstone, water, water, redstone, redstone},
                {redstone, redstone, redstone, redstone, redstone}
        }, new Material[][]{ // S
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, water, water, water, water},
                {redstone, redstone, redstone, redstone, redstone},
                {water, water, water, water, redstone},
                {redstone, redstone, redstone, redstone, redstone}
        });

        hardCovers = Arrays.asList(new Material[][]{ // 4 corners
                {water, redstone, redstone, redstone, water},
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, redstone, redstone, redstone, redstone},
                {water, redstone, redstone, redstone, water}
        }, new Material[][]{ // 2 bottom
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, redstone, redstone, redstone, redstone},
                {water, water, redstone, redstone, redstone}
        }, new Material[][]{ // X
                {water, redstone, redstone, redstone, water},
                {redstone, water, redstone, water, redstone},
                {redstone, redstone, water, redstone, redstone},
                {redstone, water, redstone, water, redstone},
                {water, redstone, redstone, redstone, water}
        }, new Material[][]{ // 4 holes
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, water, redstone, water, redstone},
                {redstone, redstone, redstone, redstone, redstone},
                {redstone, water, redstone, water, redstone},
                {redstone, redstone, redstone, redstone, redstone}
        }, new Material[][]{ // Checkered
                {water, redstone, water, redstone, water},
                {redstone, water, redstone, water, redstone},
                {water, redstone, water, redstone, water},
                {redstone, water, redstone, water, redstone},
                {water, redstone, water, redstone, water}
        });

        this.region = getEventRegion("safe-region");

        this.startLoc = getEventLocation("start");
        this.pos1 = getEventBlockVector("water-1");
        this.pos2 = getEventBlockVector("water-2");

        this.eliminated = new HashSet<>();

        this.fallCheck = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getPlayersView()) {
                    // TODO: Change isOnGround() check as it is calculated client side (can be spoofed)
                    if (startLoc.getBlockY() - player.getLocation().getBlockY() > 2 && ((LivingEntity) player).isOnGround() && !jumped.contains(player)) {
                        if (SpigotUtils.isLocInRegion(player.getLocation(), region)) { // Player is in safe zone
                            broadcastEventMessage(Message.WATERDROP_SUCCESS_JUMP.get().replace("{player}", player.getName()));
                            eliminated.remove(player);
                        } else { // Player missed
                            broadcastEventMessage(Message.WATERDROP_FAIL_JUMP.get().replace("{player}", player.getName()));
                        }
                        jumped.add(player);
                    }
                }
            }
        };

        if (pos1.getY() != pos2.getY() || Math.abs(pos1.getX() - pos2.getX()) != 4 || Math.abs(pos1.getZ() - pos2.getZ()) != 4) {
            plugin.getLogger().severe("Waterdrop water positions do not form a 5x1x5 square.");
            setValid(false);
        }

        this.jumped = new HashSet<>();
    }

    @Override
    protected void start() {
        super.start();
        fallCheck.runTaskTimer(plugin, 0, 1);
    }

    @Override
    protected void over() {
        super.over();
        this.fallCheck.cancel();
    }

    @Override
    protected boolean checkPlayerCount() {
        // Let rounds eliminate, not player count
        return false;
    }

    @Override
    protected Function<Player, EventScoreboard> getScoreboardFactory() {
        return WaterdropSB::new;
    }

    @Override
    protected void newRound() {
        broadcastEventMessage(Message.WATERDROP_ROUND_STARTING.get().replace("{round}", String.valueOf(getRound())));
        eliminated.addAll(getPlayersView());
        setMainCover();
        buildMainCover();
        tpPlayers();
    }

    @Override
    protected void eliminatePlayers() {
        int count = 0;
        for (Player player : eliminated) {
            broadcastEventMessage(Message.WATERDROP_ELIMINATED.get()
                    .replace("{player}", player.getName())
                    .replace("{remaining}", String.valueOf(getPlayersSize() - ++count)));
        }
        loseEvent(eliminated);

        jumped.clear();
        eliminated.clear();
    }

    private void tpPlayers() {
        for (Player player : getPlayersView()) {
            player.teleport(startLoc);
        }
    }


    private void setMainCover() {
        List<Material[][]> coverList;

        int round = getRound();

        if (round <= 8) {
            coverList = easyCovers;
        } else if (round <= 14) {
            coverList = mediumCovers;
        } else if (round <= 20) {
            coverList = hardCovers;
        } else {
            mainCover = getHoleCover();
            return;
        }

        mainCover = getRandomCover(coverList);
    }

    /**
     * Returns cover that has 2-3 random holes
     *
     * @return the cover with holes in it
     */
    private Material[][] getHoleCover() {
        Material[][] cover = deepCopyCover(closedCover);

        for (int i = 0; i < (GeneralUtils.randomBoolean() ? 2 : 3); i++) {
            cover[GeneralUtils.randomMinMax(0, 4)][GeneralUtils.randomMinMax(0, 4)] = water;
        }

        return cover;
    }

    /**
     * Returns a deep copy of the passed in 2d array
     *
     * @param cover the cover to be copied
     * @return deep copy of cover
     */
    private static Material[][] deepCopyCover(Material[][] cover) {
        Material[][] copy = new Material[cover.length][cover.length]; // All covers are same length and width
        for (int i = 0; i < cover.length; i++) {
            Material[] member = new Material[cover.length];
            System.arraycopy(cover[i], 0, member, 0, cover.length);
            copy[i] = member;
        }
        return copy;
    }

    /**
     * Gives random cover to use as main that is not equal to the last one used
     * easyCovers - up to round 8
     * mediumCovers - up to round 14
     * hardCovers - up to round 20
     *
     * @param coverList the list to pick the random cover from
     * @return cover the random cover
     */

    private Material[][] getRandomCover(List<Material[][]> coverList) {
        Material[][] cover;

        do {
            cover = GeneralUtils.getRandom(coverList);
        } while (Arrays.deepEquals(mainCover, cover));

        return cover;
    }

    private void buildMainCover() {
        int y = (int) pos1.getY();
        int minX = (int) Math.min(pos1.getX(), pos2.getX());
        int minZ = (int) Math.min(pos1.getZ(), pos2.getZ());
        int maxX = (int) Math.max(pos1.getX(), pos2.getX());
        int maxZ = (int) Math.max(pos1.getZ(), pos2.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                eventWorld.getBlockAt(x, y, z).setType(mainCover[x - minX][z - minZ]);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHitWD(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && !ignoreEvent((Player) event.getEntity())) {
            event.setCancelled(true);
        }
    }

    private final class WaterdropSB extends EventScoreboard {
        private final TrackLine roundTrack;
        private final TrackLine timerTrack;
        private final TrackLine playerCountTrack;
        private final TrackLine specCountTrack;

        private WaterdropSB(Player player) {
            super(getConfigName(), player);
            this.roundTrack = new TrackLine("rtWaterdrop", "&eRound: ", "&6", "", 6);
            this.timerTrack = new TrackLine("ttWaterdrop", "&eTime Remain", "ing: &6", "", 5);
            Line blank = new Line("bWaterdrop", "", "&b", "", 4);
            this.playerCountTrack = new TrackLine("pctWaterdrop", "&ePlayers: ", "&4&6", "", 3);
            this.specCountTrack = new TrackLine("sctWaterdrop", "&eSpectators: ", "&d&6", "", 2);
            this.initLines(roundTrack, timerTrack, blank, playerCountTrack, specCountTrack);
        }

        @Override
        protected void refresh() {
            specCountTrack.setSuffix(String.valueOf(getSpectatorsSize()));
            roundTrack.setSuffix(String.valueOf(getRound()));
            timerTrack.setSuffix(String.valueOf(getTimer()));
            playerCountTrack.setSuffix(String.valueOf(getPlayersSize()));
        }
    }
}
