package ca.nicbo.invadedlandsevents.scoreboard;

import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

/**
 * Scoreboard for events.
 *
 * @author Nicbo
 */
public abstract class EventScoreboard {
    private static final String LINE = "&7&m--------------------";
    private static final String HEART = "❤";

    private final Player player;
    private final String title;
    private final String name;

    private final Scoreboard scoreboard;

    private Objective objective;
    private EventScoreboardLine[] lines;

    protected EventScoreboard(Player player, String title, String name) {
        Validate.checkArgumentNotNull(player, "player");
        Validate.checkArgumentNotNull(title, "title");
        Validate.checkArgumentNotNull(name, "name");

        this.player = player;
        this.title = title;
        this.name = name;

        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Validate.checkNotNull(scoreboardManager, "world has not loaded yet, can't get instance of scoreboard manager");

        this.scoreboard = scoreboardManager.getNewScoreboard();
        registerObjective();
        this.lines = new EventScoreboardLine[0];

        Objective health = scoreboard.registerNewObjective("ile_health", "health");
        health.setDisplayName(ChatColor.RED + HEART);
        health.setDisplaySlot(DisplaySlot.BELOW_NAME);
    }

    public void open() {
        // Update so there is no delay
        updateScoreboard();
        player.setScoreboard(scoreboard);
    }

    private void registerObjective() {
        this.objective = scoreboard.registerNewObjective(name, "dummy");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.objective.setDisplayName(StringUtils.colour(title));
    }

    private void resetScoreboard() {
        scoreboard.getTeams().forEach(Team::unregister);
        objective.unregister();
        registerObjective();
    }

    public final void setLines(EventScoreboardLine... lines) {
        Validate.checkArgumentNotNull(lines, "lines");

        // Clear existing lines
        if (this.lines.length > 0) {
            resetScoreboard();
        }

        this.lines = lines;

        // Register lines
        for (EventScoreboardLine line : lines) {
            registerLine(line);
        }

        // Register header and footer
        registerLine(new EventScoreboardLine(lines.length + 2, LINE));
        registerLine(new EventScoreboardLine(1, LINE));
    }

    private void registerLine(EventScoreboardLine line) {
        Team team = scoreboard.registerNewTeam(line.getTeam());
        team.addEntry(line.getBase());
        team.setPrefix(line.getPrefix());
        team.setSuffix(line.getSuffix());
        objective.getScore(line.getBase()).setScore(line.getLineNumber());
    }

    protected abstract void refresh();

    public final void updateScoreboard() {
        refresh();
        for (EventScoreboardLine line : lines) {
            Team team = scoreboard.getTeam(line.getTeam());
            Validate.checkState(team != null, "%s is not on scoreboard", line.getTeam());
            team.setPrefix(line.getPrefix());
            team.setSuffix(line.getSuffix());
        }
    }

    public Player getPlayer() {
        return player;
    }
}
