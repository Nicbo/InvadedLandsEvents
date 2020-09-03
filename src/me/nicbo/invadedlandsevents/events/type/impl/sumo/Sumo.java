package me.nicbo.invadedlandsevents.events.type.impl.sumo;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.events.type.DuelEvent;
import me.nicbo.invadedlandsevents.events.util.team.SumoTeam;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import me.nicbo.invadedlandsevents.scoreboard.line.Line;
import me.nicbo.invadedlandsevents.scoreboard.line.TrackLine;
import me.nicbo.invadedlandsevents.util.GeneralUtils;
import me.nicbo.invadedlandsevents.util.misc.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.function.Function;

/**
 * Two sumo teams are teleported each round
 * First team to have all their players hit the min y value get eliminated
 * Last team standing wins
 *
 * @author Nicbo
 */

public abstract class Sumo extends DuelEvent {
    private final int teamSize;

    private final BukkitRunnable minYCheck;
    private final int MIN_Y;

    private final List<SumoTeam> teams;

    private Pair<SumoTeam, SumoTeam> fightingTeams;

    Sumo(InvadedLandsEvents plugin, String eventName, String configName, int teamSize) {
        super(plugin, eventName, configName, "SUMO");

        this.teamSize = teamSize;

        this.MIN_Y = getEventInteger("min-y");

        this.minYCheck = new BukkitRunnable() {
            @Override
            public void run() {
                if (isFighting()) {
                    for (Iterator<Player> iterator = fightingPlayers.iterator(); iterator.hasNext(); ) {
                        Player player = iterator.next();
                        if (player.getLocation().getY() <= MIN_Y) {
                            broadcastEventMessage(Message.SUMO_ELIMINATED.get().replace("{player}", player.getName()));
                            player.teleport(getSpecLoc());
                            iterator.remove();
                        }
                    }

                    checkRoundPlayerCount();
                }
            }
        };

        this.teams = new ArrayList<>();
    }

    private void checkRoundPlayerCount() {
        if (isFighting()) {
            if (isTeamNotFighting(fightingTeams.getLeft())) {
                teamWonRound(fightingTeams.getRight(), fightingTeams.getLeft());
            } else if (isTeamNotFighting(fightingTeams.getRight())) {
                teamWonRound(fightingTeams.getLeft(), fightingTeams.getRight());
            }
        }
    }

    private boolean isTeamNotFighting(SumoTeam team) {
        for (Player player : team.getPlayers()) {
            if (fightingPlayers.contains(player)) {
                return false;
            }
        }
        return true;
    }

    private void teamWonRound(SumoTeam winner, SumoTeam loser) {
        broadcastEventMessage(Message.SUMO_TEAM_ELIMINATED.get()
                .replace("{winner}", winner.getName())
                .replace("{loser}", loser.getName())
                .replace("{remaining}", String.valueOf(getPlayersSize() - loser.getSize())));

        endRound();

        for (Player player : winner.getPlayers()) {
            player.teleport(getSpecLoc());
        }

        loseEvent(loser.getPlayers());

        teams.remove(loser);
        checkPlayerCount();
    }

    private SumoTeam getPlayersTeam(Player player) {
        for (SumoTeam team : teams) {
            if (team.contains(player)) {
                return team;
            }
        }
        return null;
    }

    @Override
    protected Collection<Player> prepareRound() {
        // Just in case, if the list size is one than the while loop while crash the server
        checkPlayerCount();
        if (!isRunning()) {
            return null;
        }

        SumoTeam team1 = GeneralUtils.getRandom(teams);
        SumoTeam team2;
        do {
            team2 = GeneralUtils.getRandom(teams);
        } while (team1.equals(team2));

        this.fightingTeams = new Pair<>(team1, team2);

        broadcastEventMessage(Message.SUMO_MATCH_STARTING.get()
                .replace("{team1}", team1.getName())
                .replace("{team2}", team2.getName()));

        List<Player> players = new ArrayList<>();

        for (Player player : team1.getPlayers()) {
            player.teleport(startLoc1);
            players.add(player);
        }

        for (Player player : team2.getPlayers()) {
            player.teleport(startLoc2);
            players.add(player);
        }

        return players;
    }

    @Override
    public void leaveEvent(Player player) {
        super.leaveEvent(player);

        removeFromTeam(player);
        if (isRunning()) {
            checkRoundPlayerCount();
        }
    }

    private void removeFromTeam(Player player) {
        SumoTeam team = getPlayersTeam(player);

        if (team != null) {
            team.removePlayer(player);

            if (team.isEmpty()) {
                teams.remove(team);
            }
        }
    }

    @Override
    protected void start() {
        minYCheck.runTaskTimer(plugin, 0, 1);

        shufflePlayers();

        List<Player> players = getPlayersView();

        for (int i = 0; i < players.size(); i += teamSize) {
            Set<Player> teamPlayers = new HashSet<>();
            for (int j = 0; j < teamSize; j++) {
                if (i + j >= players.size()) {
                    break;
                }
                teamPlayers.add(players.get(i + j));
            }
            this.teams.add(new SumoTeam(teamPlayers));
        }

        super.start();
    }

    @Override
    protected void over() {
        super.over();
        minYCheck.cancel();
    }

    @Override
    protected void checkPlayerCount() {
        int teamsCount = teams.size();
        if (teamsCount < 2) {
            winEvent(teamsCount == 1 ? teams.get(0) : null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHitSumo(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && !ignoreEvent((Player) event.getEntity())) {
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getEntity();
                Player damager = (Player) event.getDamager();

                SumoTeam playerTeam = getPlayersTeam(player);
                if (playerTeam != null && playerTeam.equals(getPlayersTeam(damager))) {
                    event.setCancelled(true);
                    return;
                }
            }

            event.setDamage(0);
        }
    }

    @Override
    protected Function<Player, EventScoreboard> getScoreboardFactory() {
        return SumoSB::new;
    }

    private final class SumoSB extends EventScoreboard {
        private final TrackLine playerCountTrack;
        private final TrackLine specCountTrack;

        private final Line pickingPlayers;
        private final Line blank;

        private boolean showingFighters;

        private SumoSB(Player player) {
            super(getConfigName(), player);

            this.pickingPlayers = new Line("ppSumo", "&6Picking new ", "players...", "", 5);
            this.blank = new Line("bSumo", "", "&b&b", "", 4);
            this.playerCountTrack = new TrackLine("pctSumo", "&ePlayers: ", "&a&6", "", 3);
            this.specCountTrack = new TrackLine("sctSumo", "&eSpectators: ", "&d&6", "", 2);

            this.initLines(pickingPlayers, blank, playerCountTrack, specCountTrack);
        }

        @Override
        protected void refresh() {
            if (!showingFighters && isFighting()) {
                clearLines();

                List<Line> lines = new ArrayList<>();

                lines.add(specCountTrack);
                lines.add(playerCountTrack);
                lines.add(blank);


                int i = 0;
                for (Player player : fightingTeams.getLeft().getInitialPlayers()) {
                    lines.add(new Line("t1p" + i++ + "Sumo", "", "&" + i + "&6", player.getName(), lines.size() + 2));
                }

                lines.add(new Line("vsSumo", "", "&evs.", "", lines.size() + 2));

                i = 0;
                for (Player player : fightingTeams.getRight().getInitialPlayers()) {
                    lines.add(new Line("t2p" + i++ + "Sumo", "", "&" + i + "&b&6", player.getName(), lines.size() + 2));
                }

                this.showingFighters = true;

                initLines(lines.toArray(new Line[0]));
            } else if (showingFighters && !isFighting()) {
                clearLines();
                this.showingFighters = false;
                initLines(pickingPlayers, blank, playerCountTrack, specCountTrack);
            }

            this.playerCountTrack.setSuffix(String.valueOf(getPlayersSize()));
            this.specCountTrack.setSuffix(String.valueOf(getSpectatorsSize()));
        }
    }
}
