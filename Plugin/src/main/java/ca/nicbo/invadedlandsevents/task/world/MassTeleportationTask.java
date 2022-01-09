package ca.nicbo.invadedlandsevents.task.world;

import ca.nicbo.invadedlandsevents.task.SyncedTask;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Mass teleports players, {@value PERIOD} tick(s) at a time.
 *
 * @author Nicbo
 */
public class MassTeleportationTask extends SyncedTask {
    private static final long DELAY = 1;
    private static final long PERIOD = 1;

    private final Queue<Player> players;
    private final Location location;

    public MassTeleportationTask(Collection<Player> players, Location location) {
        super(DELAY, PERIOD);
        this.players = new ArrayDeque<>(players);
        this.location = location;
    }

    @Override
    protected void onStart() {
        // Make sure the chunk is loaded
        location.getChunk().load(true);
    }

    @Override
    protected void run() {
        if (players.isEmpty()) {
            stop();
            return;
        }

        players.remove().teleport(location);
    }
}
