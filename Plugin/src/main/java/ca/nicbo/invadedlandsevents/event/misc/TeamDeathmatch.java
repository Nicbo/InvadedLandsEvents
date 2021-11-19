package ca.nicbo.invadedlandsevents.event.misc;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostEndEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStartEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPreStartEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPreLeaveEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPreSpectateEvent;
import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;
import ca.nicbo.invadedlandsevents.configuration.Message;
import ca.nicbo.invadedlandsevents.event.InvadedEvent;
import ca.nicbo.invadedlandsevents.event.InvadedEventTeam;
import ca.nicbo.invadedlandsevents.event.event.player.EventPlayerDamageByEventPlayerEvent;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboardLine;
import ca.nicbo.invadedlandsevents.task.event.MatchCountdownTask;
import ca.nicbo.invadedlandsevents.util.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Team Deathmatch.
 *
 * @author Nicbo
 */
public class TeamDeathmatch extends InvadedEvent {
    private final Kit blueKit;
    private final Kit redKit;
    private final Location blueStartLocation;
    private final Location redStartLocation;

    private final Map<Player, Integer> kills;

    private final InvadedEventTeam spectatorTeam;
    private final InvadedEventTeam blueTeam;
    private final InvadedEventTeam redTeam;

    private final MatchCountdownTask matchCountdownTask;

    private InvadedEventTeam winner; // need access in end event

    public TeamDeathmatch(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.TEAM_DEATHMATCH, hostName, ListMessage.TDM_DESCRIPTION.get());
        this.blueKit = getEventConfig().getKit("blue-kit");
        this.redKit = getEventConfig().getKit("red-kit");
        this.blueStartLocation = getEventConfig().getLocation("blue-start");
        this.redStartLocation = getEventConfig().getLocation("red-start");
        this.kills = new HashMap<>();
        this.spectatorTeam = new InvadedEventTeam(ChatColor.GRAY + "Spectators");
        this.blueTeam = new InvadedEventTeam(ChatColor.BLUE + "Blue Team");
        this.redTeam = new InvadedEventTeam(ChatColor.RED + "Red Team");
        this.matchCountdownTask = new MatchCountdownTask.Builder(this::broadcastMessage)
                .setStarting(Message.TDM_MATCH_STARTING.get())
                .setCounter(Message.TDM_MATCH_COUNTER.get())
                .setStarted(Message.TDM_MATCH_STARTED.get())
                .build();
    }

    @Override
    protected EventScoreboard createEventScoreboard(Player player) {
        return new TeamDeathmatchScoreboard(player);
    }

    @Override
    protected void lose(Player player) {
        InvadedEventTeam team = getPlayersTeam(player);
        team.remove(player);
        spectatorTeam.add(player);
        super.lose(player);
    }

    @Override
    protected void checkPlayerCount() {
        final InvadedEventTeam winner;
        if (blueTeam.isEmpty()) {
            winner = redTeam;
        } else if (redTeam.isEmpty()) {
            winner = blueTeam;
        } else {
            return;
        }

        this.winner = winner;

        end(new EventEndingContext(winner.getName(), getWinnerTopKillers().keySet()));
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEventPlayerDamageByEventPlayer(EventPlayerDamageByEventPlayerEvent event) {
        super.onEventPlayerDamageByEventPlayer(event);

        Player player = event.getPlayer();
        Player damager = event.getDamager();
        if (event.isCancelled()) {
            return;
        }

        InvadedEventTeam playerTeam = getPlayersTeam(player);
        InvadedEventTeam damagerTeam = getPlayersTeam(damager);
        if (matchCountdownTask.isRunning() || playerTeam.equals(damagerTeam)) {
            event.setCancelled(true);
            return;
        }

        if (event.isKillingBlow()) {
            broadcastMessage(Message.TDM_ELIMINATED.get()
                    .replace("{player}", player.getName())
                    .replace("{remaining}", String.valueOf(getPlayersSize() - 1))
                    .replace("{killer}", damager.getName()));

            event.doFakeDeath();

            if (!player.equals(damager)) {
                CollectionUtils.incrementMap(kills, damager);
            }

            lose(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPlayerPreLeave(EventPlayerPreLeaveEvent event) {
        Player player = event.getPlayer();
        InvadedEventTeam team = getPlayersTeam(player);
        team.remove(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPlayerPreSpectate(EventPlayerPreSpectateEvent event) {
        spectatorTeam.add(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPreStart(EventPreStartEvent event) {
        List<Player> players = getPlayers();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            InvadedEventTeam team = i % 2 == 0 ? blueTeam : redTeam;
            team.add(player);
        }

        blueTeam.backup();
        redTeam.backup();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostStart(EventPostStartEvent event) {
        for (Player player : blueTeam) {
            player.teleport(blueStartLocation);
            blueKit.apply(player);
        }

        for (Player player : redTeam) {
            player.teleport(redStartLocation);
            redKit.apply(player);
        }

        matchCountdownTask.start(getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostEnd(EventPostEndEvent event) {
        if (matchCountdownTask.isRunning()) {
            matchCountdownTask.stop();
        }

        if (winner != null) {
            List<Map.Entry<Player, Integer>> winners = new ArrayList<>(getWinnerTopKillers().entrySet());
            List<String> winnersMessages = new ArrayList<>();
            List<String> winnersListMessages = new ArrayList<>();

            // Set all placeholders
            for (String message : ListMessage.TDM_WINNERS.get()) {
                winnersMessages.add(message.replace("{winners}", String.valueOf(winners.size())));
            }

            List<String> originalWinnersListMessages = ListMessage.TDM_WINNERS_LIST.get();
            for (int i = 0; i < winners.size(); i++) {
                String message = originalWinnersListMessages.get(i);
                for (int j = 0; j < winners.size(); j++) {
                    final int ranking = j + 1;
                    Map.Entry<Player, Integer> winner = winners.get(j);
                    message = message
                            .replace("{winner" + ranking + "}", winner.getKey().getName())
                            .replace("{kills" + ranking + "}", String.valueOf(winner.getValue()));
                }
                winnersListMessages.add(message);
            }

            // Broadcast
            for (String winnersMessage : winnersMessages) {
                if ("{winners_list}".equalsIgnoreCase(winnersMessage)) {
                    for (String winnersListMessage : winnersListMessages) {
                        Bukkit.broadcastMessage(winnersListMessage);
                    }
                    continue;
                }

                Bukkit.broadcastMessage(winnersMessage);
            }
        }
    }

    private InvadedEventTeam getPlayersTeam(Player player) {
        if (redTeam.contains(player)) {
            return redTeam;
        } else if (blueTeam.contains(player)) {
            return blueTeam;
        } else if (spectatorTeam.contains(player)) {
            return spectatorTeam;
        } else {
            // Since this is only used internally, callers should only pass in players who have a team
            throw new IllegalArgumentException("the player " + player.getName() + " does not have a team");
        }
    }

    private Map<Player, Integer> getWinnerTopKillers() {
        return winner.getPlayersBackup().stream()
                .collect(Collectors.toMap(Function.identity(), player -> kills.getOrDefault(player, 0)))
                .entrySet()
                .stream()
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));
    }

    // ---------- Getters for Plugin module users ----------

    public Map<Player, Integer> getKills() {
        return Collections.unmodifiableMap(kills);
    }

    public InvadedEventTeam getSpectatorTeam() {
        return InvadedEventTeam.unmodifiableOf(spectatorTeam);
    }

    public InvadedEventTeam getBlueTeam() {
        return InvadedEventTeam.unmodifiableOf(blueTeam);
    }

    public InvadedEventTeam getRedTeam() {
        return InvadedEventTeam.unmodifiableOf(redTeam);
    }

    public InvadedEventTeam getWinner() {
        return winner == null ? null : InvadedEventTeam.unmodifiableOf(winner);
    }

    // -----------------------------------------------------

    private class TeamDeathmatchScoreboard extends EventScoreboard {
        private final EventScoreboardLine redPlayerCountLine;
        private final EventScoreboardLine bluePlayerCountLine;
        private final EventScoreboardLine killsLine;
        private final EventScoreboardLine playerCountLine;
        private final EventScoreboardLine spectatorCountLine;

        public TeamDeathmatchScoreboard(Player player) {
            super(player, Message.TITLE_TDM.get(), getConfigName());
            InvadedEventTeam team = getPlayersTeam(player);
            EventScoreboardLine teamLine = new EventScoreboardLine(8, "&eTeam: " + team.getName());
            this.redPlayerCountLine = new EventScoreboardLine(7);
            this.bluePlayerCountLine = new EventScoreboardLine(6);
            this.killsLine = new EventScoreboardLine(5);
            EventScoreboardLine blankLine = new EventScoreboardLine(4);
            this.playerCountLine = new EventScoreboardLine(3);
            this.spectatorCountLine = new EventScoreboardLine(2);
            this.setLines(teamLine, redPlayerCountLine, bluePlayerCountLine, killsLine, blankLine, playerCountLine, spectatorCountLine);
        }

        @Override
        protected void refresh() {
            redPlayerCountLine.setText("&eRed: &c" + redTeam.size());
            bluePlayerCountLine.setText("&eBlue: &9" + blueTeam.size());
            killsLine.setText("&eKills: &6" + kills.getOrDefault(getPlayer(), 0));
            playerCountLine.setText("&ePlayers: &6" + getPlayersSize());
            spectatorCountLine.setText("&eSpectators: &6" + getSpectatorsSize());
        }
    }
}
