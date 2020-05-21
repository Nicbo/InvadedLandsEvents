package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
    private Location startLoc;

    private int round;
    private int timer;
    private BukkitRunnable roundTimer;

    private final String ROUND_START;
    private final String FAILED;
    private final String ELIMINATED;
    private final String COLOUR;

    public WoolShuffle() {
        super("Wool Shuffle", "woolshuffle");

        this.startLoc = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location"), eventWorld);

        this.ROUND_START = getEventMessage("ROUND_START");
        this.FAILED = getEventMessage("FAILED");
        this.ELIMINATED = getEventMessage("ELIMINATED");
        this.COLOUR = getEventMessage("COLOUR");
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
        for (Player player : players) {
            // give sb
            player.teleport(startLoc);
        }
    }

    @Override
    public void over() {

    }

    private void newRound() {
        // Check if all players are on correct wool
        Iterator<Player> iterator = players.iterator();

        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (!isPlayerOnWool(player)) {
                // elim player
            }

        }

        // Pick new colour
        // Random potion (speed, slow)
        // Pvp toggle (only first 10 rounds)
    }

    private boolean isPlayerOnWool(Player player) {
        Location loc = player.getLocation();

        // Block under player
        loc.setY(loc.getY() - 1);
        Block block = loc.getWorld().getBlockAt(loc);

        // In case player is in air
        loc.setY(loc.getY() - 1);
        Block blockUnder = loc.getWorld().getBlockAt(loc);

        //return block == woolID || blockUnder == woolID;
        return true;
    }
}
