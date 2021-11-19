package ca.nicbo.invadedlandsevents.event.duel;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventState;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostEndEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStartEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPreStartEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPreLeaveEvent;
import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.configuration.Message;
import ca.nicbo.invadedlandsevents.event.InvadedEvent;
import ca.nicbo.invadedlandsevents.event.InvadedEventTeam;
import ca.nicbo.invadedlandsevents.event.event.player.EventPlayerDamageByEventPlayerEvent;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboardLine;
import ca.nicbo.invadedlandsevents.task.event.MatchCountdownTask;
import ca.nicbo.invadedlandsevents.util.CollectionUtils;
import ca.nicbo.invadedlandsevents.util.RandomUtils;
import ca.nicbo.invadedlandsevents.util.SpigotUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Partial implementation of {@link InvadedEvent}.
 * <p>
 * This type of event has rounds that end based on which team comes out on top. The last team standing wins.
 *
 * @author Nicbo
 */
public abstract class DuelEvent extends InvadedEvent {
    private static final int EVENT_DELAY_TICKS = 100;

    private final int teamSize;

    private final String duelConfigNameUpper;

    private final Kit kit;
    private final Location startOne;
    private final Location startTwo;

    private final List<InvadedEventTeam> teams;
    private final Set<Player> fightingPlayers;

    private MatchCountdownTask matchCountdownTask;

    private InvadedEventTeam teamOne;
    private InvadedEventTeam teamTwo;

    protected DuelEvent(InvadedLandsEventsPlugin plugin, EventType eventType, String hostName, List<String> description, int teamSize) {
        super(plugin, eventType, hostName, description);
        Validate.checkArgument(teamSize >= 1 && teamSize <= 3, "teamSize must be between 1 and 3 (inclusive), given size: %s", teamSize);
        this.teamSize = teamSize;

        // brackets3v3 -> BRACKETS
        this.duelConfigNameUpper = eventType.getConfigName().substring(0, getConfigName().length() - 3).toUpperCase();

        this.kit = getEventConfig().getKit("kit");
        this.startOne = getEventConfig().getLocation("start-1");
        this.startTwo = getEventConfig().getLocation("start-2");

        this.teams = new ArrayList<>();
        this.fightingPlayers = new HashSet<>();
    }

    private String getTeamName(Set<Player> players) {
        String name;
        switch (players.size()) {
            case 1: name = Message.valueOf(duelConfigNameUpper + "_TEAM_OF_ONE").get(); break;
            case 2: name = Message.valueOf(duelConfigNameUpper + "_TEAM_OF_TWO").get(); break;
            case 3: name = Message.valueOf(duelConfigNameUpper + "_TEAM_OF_THREE").get(); break;
            default: throw new IllegalArgumentException("team size must be between 1 and 3 (inclusive), given size: " + players.size());
        }

        int playerNumber = 1;
        for (Player player : players) {
            name = name.replace("{player" + playerNumber++ + "}", player.getName());
        }

        return name;
    }

    @Override
    protected EventScoreboard createEventScoreboard(Player player) {
        return new DuelScoreboard(player);
    }

    @Override
    protected void checkPlayerCount() {
        int teamCount = teams.size();
        if (teamCount == 1) {
            InvadedEventTeam team = teams.get(0);
            end(new EventEndingContext(team));
        } else if (teamCount == 0) {
            end(new EventEndingContext());
        }
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEventPlayerDamageByEventPlayer(EventPlayerDamageByEventPlayerEvent event) {
        super.onEventPlayerDamageByEventPlayer(event);

        if (event.isCancelled()) {
            return;
        }

        if (matchCountdownTask.isRunning() || // Countdown
                !isFighting(event.getPlayer()) || // Player isn't fighting
                !isFighting(event.getDamager()) || // Damager isn't fighting
                getPlayersTeam(event.getPlayer()).equals(getPlayersTeam(event.getDamager()))) { // Players share team
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPlayerPreLeave(EventPlayerPreLeaveEvent event) {
        Player player = event.getPlayer();

        if (isState(EventState.STARTED)) {
            if (isFighting(player)) {
                eliminatePlayer(player, true);
            } else {
                InvadedEventTeam team = getPlayersTeam(player);
                team.remove(player);

                if (team.isEmpty()) {
                    teams.remove(team);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPreStart(EventPreStartEvent event) {
        List<Player> players = CollectionUtils.shuffledCopy(getPlayers());

        for (int i = 0; i < players.size(); i += teamSize) {
            Set<Player> teamPlayers = new LinkedHashSet<>(); // maintain order
            for (int j = 0; j < teamSize; j++) {
                if (i + j >= players.size()) {
                    break;
                }
                teamPlayers.add(players.get(i + j));
            }

            teams.add(new InvadedEventTeam(getTeamName(teamPlayers), teamPlayers));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostStart(EventPostStartEvent event) {
        startRound();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostEnd(EventPostEndEvent event) {
        if (matchCountdownTask != null && matchCountdownTask.isRunning()) {
            matchCountdownTask.stop();
        }
    }

    private void startRound() {
        this.teamOne = RandomUtils.randomElement(teams);
        this.teamTwo = RandomUtils.randomElementNotEqual(teams, teamOne);

        for (Player player : teamOne) {
            player.teleport(startOne);
            fightingPlayers.add(player);
            kit.apply(player);
        }

        for (Player player : teamTwo) {
            player.teleport(startTwo);
            fightingPlayers.add(player);
            kit.apply(player);
        }

        this.matchCountdownTask = new MatchCountdownTask.Builder(this::broadcastMessage)
                .setStarting(Message.valueOf(duelConfigNameUpper + "_MATCH_STARTING").get()
                        .replace("{team1}", teamOne.getName())
                        .replace("{team2}", teamTwo.getName()))
                .setCounter(Message.valueOf(duelConfigNameUpper + "_MATCH_COUNTER").get())
                .setStarted(Message.valueOf(duelConfigNameUpper + "_MATCH_STARTED").get())
                .build();
        this.matchCountdownTask.start(getPlugin());
    }

    private void endRound(InvadedEventTeam winner, InvadedEventTeam loser) {
        this.teamOne = null;
        this.teamTwo = null;

        fightingPlayers.clear();

        for (Player player : winner) {
            player.teleport(getSpec());
            SpigotUtils.clear(player);
            givePlayerStar(player);
        }

        teams.remove(loser);

        broadcastMessage(Message.valueOf(duelConfigNameUpper + "_TEAM_ELIMINATED").get()
                .replace("{winner}", winner.getName())
                .replace("{loser}", loser.getName())
                .replace("{remaining}", String.valueOf(getPlayersSize() - loser.size())));

        lose(loser);

        getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
            if (isState(EventState.STARTED)) { // In case the event ended
                startRound();
            }
        }, EVENT_DELAY_TICKS);
    }

    protected void eliminatePlayer(Player player, boolean leaving) {
        Validate.checkArgument(fightingPlayers.remove(player), "%s is not fighting", player.getName());

        if (!leaving) {
            player.teleport(getSpec());
            givePlayerStar(player);
        }

        InvadedEventTeam team = getPlayersTeam(player);
        if (!isTeamAlive(team)) {
            broadcastMessage(Message.valueOf(duelConfigNameUpper + "_ELIMINATED").get().replace("{player}", player.getName()));
            InvadedEventTeam opponent = getCurrentOpponent(team);
            endRound(opponent, team);
        }
    }

    private boolean isTeamAlive(InvadedEventTeam team) {
        return team.stream().anyMatch(this::isFighting);
    }

    private InvadedEventTeam getCurrentOpponent(InvadedEventTeam team) {
        return CollectionUtils.getOther(Arrays.asList(teamOne, teamTwo), team);
    }

    private InvadedEventTeam getPlayersTeam(Player player) {
        return teams.stream()
                .filter(team -> team.contains(player))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(player.getName() + " does not have a team"));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (isState(EventState.STARTED) && matchCountdownTask != null && matchCountdownTask.isRunning() && fightingPlayers.contains(player)) {
            Location to = event.getTo();
            Location from = event.getFrom();
            // noinspection ConstantConditions (getTo() is not nullable on PlayerMoveEvent (see https://hub.spigotmc.org/jira/browse/SPIGOT-5668))
            if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
                player.teleport(from.setDirection(to.getDirection()));
            }
        }
    }

    // ---------- Getters for Plugin module users ----------
    public int getTeamSize() {
        return teamSize;
    }

    public InvadedEventTeam getTeamOne() {
        return InvadedEventTeam.unmodifiableOf(teamOne);
    }

    public InvadedEventTeam getTeamTwo() {
        return InvadedEventTeam.unmodifiableOf(teamTwo);
    }

    public List<InvadedEventTeam> getTeams() {
        return teams.stream()
                .map(InvadedEventTeam::unmodifiableOf)
                .collect(CollectionUtils.toUnmodifiableList());
    }

    public Set<Player> getFightingPlayers() {
        return Collections.unmodifiableSet(fightingPlayers);
    }

    public boolean isFighting() {
        return !fightingPlayers.isEmpty();
    }

    public boolean isFighting(Player player) {
        return fightingPlayers.contains(player);
    }
    // -----------------------------------------------------

    private class DuelScoreboard extends EventScoreboard {
        private final EventScoreboardLine pickingPlayersLine;
        private final EventScoreboardLine blankLine;
        private final EventScoreboardLine playerCountLine;
        private final EventScoreboardLine spectatorCountLine;

        private boolean showingFighters;

        public DuelScoreboard(Player player) {
            super(player, Message.valueOf("TITLE_" + getConfigName().toUpperCase()).get(), getConfigName());
            this.pickingPlayersLine = new EventScoreboardLine(5, "&6Picking new players...");
            this.blankLine = new EventScoreboardLine(4);
            this.playerCountLine = new EventScoreboardLine(3);
            this.spectatorCountLine = new EventScoreboardLine(2);
            this.setLines(pickingPlayersLine, blankLine, playerCountLine, spectatorCountLine);
        }

        @Override
        protected void refresh() {
            if (!showingFighters && isFighting()) {
                showingFighters = true;

                List<EventScoreboardLine> lines = new ArrayList<>();

                for (Player player : teamOne) {
                    lines.add(new EventScoreboardLine(getNextLineNumber(lines), "&6" + player.getName()));
                }

                lines.add(new EventScoreboardLine(getNextLineNumber(lines), "&evs."));

                for (Player player : teamTwo) {
                    lines.add(new EventScoreboardLine(getNextLineNumber(lines), "&6" + player.getName()));
                }

                lines.add(blankLine);
                lines.add(playerCountLine);
                lines.add(spectatorCountLine);

                setLines(lines.toArray(new EventScoreboardLine[0]));
            } else if (showingFighters && !isFighting()) {
                showingFighters = false;
                setLines(pickingPlayersLine, blankLine, playerCountLine, spectatorCountLine);
            }

            playerCountLine.setText("&ePlayers: &6" + getPlayersSize());
            spectatorCountLine.setText("&eSpectators: &6" + getSpectatorsSize());
        }

        // it works don't question it
        private int getNextLineNumber(List<EventScoreboardLine> lines) {
            // header, footer, blank, players, spectators, vs + team players
            final int totalLines = 6 + teamOne.size() + teamTwo.size();
            final int offset = 1; // header is added in background
            return totalLines - (lines.size() + offset);
        }
    }
}
