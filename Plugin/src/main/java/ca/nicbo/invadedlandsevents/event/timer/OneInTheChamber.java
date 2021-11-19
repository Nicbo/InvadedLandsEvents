package ca.nicbo.invadedlandsevents.event.timer;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.configuration.ConfigSection;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStartEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPreStartEvent;
import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.configuration.InvadedConfigHandler;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;
import ca.nicbo.invadedlandsevents.configuration.Message;
import ca.nicbo.invadedlandsevents.event.event.player.EventPlayerDamageByEventPlayerEvent;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboardLine;
import ca.nicbo.invadedlandsevents.util.CollectionUtils;
import ca.nicbo.invadedlandsevents.util.RandomUtils;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * One in the Chamber.
 *
 * @author Nicbo
 * @author StarZorrow
 */
public class OneInTheChamber extends TimerEvent {
    private final Kit kit;

    private final List<Location> locations;
    private final Map<Player, Integer> kills;
    private final int winPoints;
    private Player leader;

    public OneInTheChamber(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.ONE_IN_THE_CHAMBER, hostName, prepareDescription(plugin.getConfigurationManager().getConfigHandler()));
        this.kit = getEventConfig().getKit("kit");
        this.locations = IntStream.range(1, 9)
                .mapToObj(i -> getEventConfig().getLocation("start-" + i))
                .collect(CollectionUtils.toUnmodifiableList());
        this.kills = new HashMap<>();
        this.winPoints = getEventConfig().getInteger("win-points");
    }

    @Override
    protected EventScoreboard createEventScoreboard(Player player) {
        return new OneInTheChamberScoreboard(player);
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEventPlayerDamageByEventPlayer(EventPlayerDamageByEventPlayerEvent event) {
        super.onEventPlayerDamageByEventPlayer(event);

        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Player damager = event.getDamager();
        if (player.equals(damager)) {
            event.setCancelled(true);
            return;
        }

        // Damage will kill player
        if (event.getProjectile() instanceof Arrow || event.isKillingBlow()) {
            int newKillerKills = getKills(damager) + 1;
            kills.put(damager, newKillerKills);

            broadcastMessage(Message.OITC_ELIMINATED_BY.get()
                    .replace("{killer}", damager.getName())
                    .replace("{killer_points}", String.valueOf(newKillerKills))
                    .replace("{player}", player.getName())
                    .replace("{player_points}", String.valueOf(getKills(player))));

            damager.setHealth(20);
            damager.getInventory().addItem(new ItemStack(Material.ARROW));

            // Killer is now in the lead
            if (newKillerKills >= getKills(leader)) {
                leader = damager;

                // Killer wins
                if (newKillerKills >= winPoints) {
                    event.setDamage(0);
                    end(new EventEndingContext(damager));
                    return;
                }
            }

            event.doFakeDeath();
            preparePlayer(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPreStart(EventPreStartEvent event) {
        this.leader = RandomUtils.randomElement(getPlayers());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostStart(EventPostStartEvent event) {
        super.onEventPostStart(event);
        for (Player player : getPlayers()) {
            preparePlayer(player);
        }
    }

    // ---------- Getters for Plugin module users ----------

    @Override
    public Player getCurrentWinner() {
        return leader;
    }

    public Map<Player, Integer> getKills() {
        return Collections.unmodifiableMap(kills);
    }

    public int getKills(Player player) {
        return kills.getOrDefault(player, 0);
    }

    // -----------------------------------------------------

    private void preparePlayer(Player player) {
        kit.apply(player);
        player.teleport(RandomUtils.randomElement(locations));
    }

    private static List<String> prepareDescription(InvadedConfigHandler configHandler) {
        ConfigSection section = configHandler.getConfigSection(EventType.ONE_IN_THE_CHAMBER.getConfigName());
        int winPoints = section.getInteger("win-points");
        List<String> description = new ArrayList<>();

        for (String message : ListMessage.OITC_DESCRIPTION.get()) {
            description.add(message.replace("{points}", String.valueOf(winPoints)));
        }

        return description;
    }

    private class OneInTheChamberScoreboard extends EventScoreboard {
        private final EventScoreboardLine playerCountLine;
        private final EventScoreboardLine spectatorCountLine;
        private final EventScoreboardLine timeRemainingLine;
        private final EventScoreboardLine killsLine;
        private final EventScoreboardLine leadLine;

        public OneInTheChamberScoreboard(Player player) {
            super(player, Message.TITLE_OITC.get(), getConfigName());
            this.playerCountLine = new EventScoreboardLine(8);
            this.spectatorCountLine = new EventScoreboardLine(7);
            this.timeRemainingLine = new EventScoreboardLine(6);
            this.killsLine = new EventScoreboardLine(5);
            EventScoreboardLine blankLine = new EventScoreboardLine(4);
            EventScoreboardLine leadTitleLine = new EventScoreboardLine(3, "&eIn the Lead:");
            this.leadLine = new EventScoreboardLine(2);
            this.setLines(playerCountLine, spectatorCountLine, timeRemainingLine, killsLine, blankLine, leadTitleLine, leadLine);
        }

        @Override
        protected void refresh() {
            playerCountLine.setText("&ePlayers: &6" + getPlayersSize());
            spectatorCountLine.setText("&eSpectators: &6" + getSpectatorsSize());
            timeRemainingLine.setText("&eTime Remaining: &6" + StringUtils.formatSeconds(getTimeLeft()));
            killsLine.setText("&eYour Points: &6" + getKills(getPlayer()));

            String leaderName = leader.getName();
            ChatColor colour = getPlayer().equals(leader) ? ChatColor.GREEN : ChatColor.RED;
            leadLine.setText(colour + leaderName + "&7: &6" + getKills(leader) + "/" + winPoints);
        }
    }
}
