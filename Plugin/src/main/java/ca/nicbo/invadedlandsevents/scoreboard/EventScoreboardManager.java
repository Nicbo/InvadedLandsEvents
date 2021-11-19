package ca.nicbo.invadedlandsevents.scoreboard;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.task.SyncedTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

/**
 * Manages the creation and refreshing of the {@link EventScoreboard} instances.
 *
 * @author Nicbo
 */
public class EventScoreboardManager {
    private final Function<Player, EventScoreboard> eventScoreboardFunction;
    private final Function<Player, EventScoreboard> startingScoreboardFunction;
    private final Function<Player, EventScoreboard> endingScoreboardFunction;
    private final ScoreboardManager bukkitScoreboardManager;
    private final Map<Player, EventScoreboard> scoreboardMap;
    private final ScoreboardRefreshTask scoreboardRefreshTask;

    public EventScoreboardManager(Function<Player, EventScoreboard> eventScoreboardFunction,
                                  Function<Player, EventScoreboard> startingScoreboardFunction,
                                  Function<Player, EventScoreboard> endingScoreboardFunction) {
        Validate.checkArgumentNotNull(eventScoreboardFunction, "eventScoreboardFunction");
        Validate.checkArgumentNotNull(startingScoreboardFunction, "startingScoreboardFunction");
        Validate.checkArgumentNotNull(endingScoreboardFunction, "endingScoreboardFunction");
        this.eventScoreboardFunction = eventScoreboardFunction;
        this.startingScoreboardFunction = startingScoreboardFunction;
        this.endingScoreboardFunction = endingScoreboardFunction;
        this.bukkitScoreboardManager = Bukkit.getScoreboardManager();
        Validate.checkNotNull(bukkitScoreboardManager, "world has not loaded yet, can't get instance of scoreboard manager");
        this.scoreboardMap = new HashMap<>();
        this.scoreboardRefreshTask = new ScoreboardRefreshTask();
    }

    public void applyEventScoreboard(Player player) {
        Validate.checkArgumentNotNull(player, "player");
        applyScoreboard(player, eventScoreboardFunction);
    }

    public void applyStartingScoreboard(Player player) {
        Validate.checkArgumentNotNull(player, "player");
        applyScoreboard(player, startingScoreboardFunction);
    }

    public void applyEndingScoreboard(Player player) {
        Validate.checkArgumentNotNull(player, "player");
        applyScoreboard(player, endingScoreboardFunction);
    }

    private void applyScoreboard(Player player, Function<Player, EventScoreboard> scoreboardFunction) {
        EventScoreboard scoreboard = scoreboardFunction.apply(player);
        scoreboard.open();
        scoreboardMap.put(player, scoreboard);
    }

    public void removeScoreboard(Player player) {
        Validate.checkArgumentNotNull(player, "player");
        player.setScoreboard(bukkitScoreboardManager.getMainScoreboard());
        scoreboardMap.remove(player);
    }

    public void removeAllScoreboards() {
        Iterator<Map.Entry<Player, EventScoreboard>> iterator = scoreboardMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Player player = iterator.next().getKey();
            player.setScoreboard(bukkitScoreboardManager.getMainScoreboard());
            iterator.remove();
        }
    }

    public void startRefreshing(InvadedLandsEventsPlugin plugin) {
        Validate.checkArgumentNotNull(plugin, "plugin");
        this.scoreboardRefreshTask.start(plugin);
    }

    public void stopRefreshing() {
        this.scoreboardRefreshTask.stop();
    }

    private class ScoreboardRefreshTask extends SyncedTask {
        private static final long DELAY = 0;
        private static final long PERIOD = 1;

        public ScoreboardRefreshTask() {
            super(DELAY, PERIOD);
        }

        @Override
        protected void run() {
            for (EventScoreboard scoreboard : scoreboardMap.values()) {
                scoreboard.updateScoreboard();
            }
        }
    }
}
