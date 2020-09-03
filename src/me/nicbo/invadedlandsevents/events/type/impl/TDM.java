package me.nicbo.invadedlandsevents.events.type.impl;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.events.type.InvadedEvent;
import me.nicbo.invadedlandsevents.events.util.MatchCountdown;
import me.nicbo.invadedlandsevents.events.util.team.TDMTeam;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import me.nicbo.invadedlandsevents.scoreboard.line.Line;
import me.nicbo.invadedlandsevents.scoreboard.line.TrackLine;
import me.nicbo.invadedlandsevents.util.SpigotUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;
import java.util.function.Function;

/**
 * There are 2 teams with kits
 * The 5 players with top kills from the winning team get rewards
 *
 * @author Nicbo
 */

public final class TDM extends InvadedEvent {
    private final TDMTeam red;
    private final TDMTeam blue;
    private final TDMTeam spec;

    private final MatchCountdown matchCountdown;

    public TDM(InvadedLandsEvents plugin) {
        super(plugin, "Team Deathmatch", "tdm");

        this.red = new TDMTeam(ChatColor.RED + "Red Team", Color.RED, getEventLocation("red-start"));
        this.blue = new TDMTeam(ChatColor.BLUE + "Blue Team", Color.BLUE, getEventLocation("blue-start"));
        this.spec = new TDMTeam(ChatColor.GRAY + "Spectators", Color.GRAY, null);

        this.matchCountdown = new MatchCountdown(this::broadcastEventMessage, Message.TDM_MATCH_COUNTER, Message.TDM_MATCH_STARTED);
    }

    @Override
    protected void start() {
        List<Player> players = getPlayersView();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (i % 2 == 0) {
                red.addPlayer(player);
            } else {
                blue.addPlayer(player);
            }
        }

        red.preparePlayers();
        blue.preparePlayers();

        // Scoreboards need to be applied after
        super.start();

        broadcastEventMessage(Message.TDM_MATCH_STARTING.get());
        matchCountdown.start(plugin);
    }

    @Override
    protected void over() {
        super.over();
        if (matchCountdown.isCounting()) {
            matchCountdown.cancel();
        }
    }

    @Override
    public void specEvent(Player player) {
        spec.addPlayer(player);
        super.specEvent(player);
    }


    @Override
    protected Function<Player, EventScoreboard> getScoreboardFactory() {
        return TDMSB::new;
    }

    @Override
    protected void checkPlayerCount() {
        if (red.isEmpty()) {
            winEvent(blue);
        } else if (blue.isEmpty()) {
            winEvent(red);
        }
    }

    @Override
    public void leaveEvent(Player player) {
        if (isRunning()) {
            removeFromTeam(player);
        }

        super.leaveEvent(player);
    }


    @Override
    protected void loseEvent(Player player) {
        removeFromTeam(player);
        super.loseEvent(player);
    }

    private static Player getPlayerFromDamager(Entity entity) {
        Player player = null;
        if (entity instanceof Player) {
            player = (Player) entity;
        } else if (entity instanceof Arrow && ((Arrow) entity).getShooter() instanceof Player) {
            player = (Player) ((Arrow) entity).getShooter();
        }
        return player;
    }

    private boolean doPlayersShareTeam(Player player1, Player player2) {
        return getPlayersTeam(player1).equals(getPlayersTeam(player2));
    }

    private TDMTeam getPlayersTeam(Player player) {
        if (red.contains(player)) {
            return red;
        } else if (blue.contains(player)) {
            return blue;
        }
        return spec;
    }

    /**
     * Removes player from their team
     *
     * @param player the player to be removed
     */
    private void removeFromTeam(Player player) {
        TDMTeam team = getPlayersTeam(player);
        team.removePlayer(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHitTDM(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (ignoreEvent(player)) {
                return;
            }

            Player damager = getPlayerFromDamager(event.getDamager());

            if (matchCountdown.isCounting() || (damager != null && doPlayersShareTeam(damager, player))) {
                event.setCancelled(true);
                return;
            }

            if (player.getHealth() - event.getFinalDamage() <= 0) {
                SpigotUtils.clearInventory(player);

                broadcastEventMessage(Message.TDM_ELIMINATED.get()
                        .replace("{player}", player.getName())
                        .replace("{remaining}", String.valueOf(getPlayersSize() - 1))
                        .replace("{killer}", damager == null ? event.getDamager().getName() : damager.getName()));

                if (damager != null) {
                    getPlayersTeam(damager).addKill(damager);
                }

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    player.spigot().respawn();
                    loseEvent(player);
                }, 1);
            }
        }
    }

    private final class TDMSB extends EventScoreboard {
        private final TDMTeam team;

        private final TrackLine redCountTrack;
        private final TrackLine blueCountTrack;
        private final TrackLine killTrack;
        private final TrackLine playerCountTrack;
        private final TrackLine specCountTrack;

        private TDMSB(Player player) {
            super(getConfigName(), player);
            this.team = getPlayersTeam(player);
            Line teamLine = new Line("ttTDM", "&eTeam: ", "&b", team.getName(), 8);
            this.redCountTrack = new TrackLine("rctTDM", "&eRed: ", "&c", "", 7);
            this.blueCountTrack = new TrackLine("bctTDM", "&eBlue: ", "&9", "", 6);
            this.killTrack = new TrackLine("ktTDM", "&eKills: ", "&6", "", 5);
            Line blank = new Line("bTDM", "", "&2", "", 4);
            this.playerCountTrack = new TrackLine("pctTDM", "&ePlayers: ", "&d&6", "", 3);
            this.specCountTrack = new TrackLine("sctTDM", "&eSpectators: ", "&3&6", "", 2);
            this.initLines(teamLine, redCountTrack, blueCountTrack, killTrack, blank, playerCountTrack, specCountTrack);
        }

        @Override
        protected void refresh() {
            redCountTrack.setSuffix(String.valueOf(red.getSize()));
            blueCountTrack.setSuffix(String.valueOf(blue.getSize()));
            killTrack.setSuffix(String.valueOf(team.getKills(getPlayer())));
            playerCountTrack.setSuffix(String.valueOf(getPlayersSize()));
            specCountTrack.setSuffix(String.valueOf(getSpectatorsSize()));
        }
    }

    /*
    TODO:
        - Keep kills on death
     */
}
