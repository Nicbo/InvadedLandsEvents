package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * OITC event:
 * All players have a wooden sword, a bow, and 1 arrow
 * Arrows one shot and when you get a kill you receive an arrow
 *
 * @author Nicbo
 * @author StarZorrow
 * @since 2020-03-13
 */

public final class OITC extends InvadedEvent {
    private List<Location> locations;
    private ItemStack[] kit;

    private HashMap<Player, Integer> points;
    private Set<Player> respawningPlayers;

    private final String killMessage;
    private final int WIN_POINTS;

    public OITC(EventsMain plugin) {
        super("One in the Chamber", "oitc", plugin);
        this.locations = new ArrayList<>();

        for (int i = 1; i < 9; i++) {
            this.locations.add(ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-" + i), eventWorld));
        }

        this.kit = new ItemStack[] {
                new ItemStack(Material.WOOD_SWORD, 1),
                new ItemStack(Material.BOW, 1),
                new ItemStack(Material.ARROW, 1)
        };

        this.points = new HashMap<>();
        this.respawningPlayers = new HashSet<>();

        this.killMessage = getEventMessage("KILL_MESSAGE");
        this.WIN_POINTS = eventConfig.getInt("int-win-points");
    }

    @Override
    public void init(EventsMain plugin) {
        initPlayerCheck();
    }

    @Override
    public void start() {
        playerCheck.runTaskTimerAsynchronously(plugin, 0, 1);
        for (Player player : players) {
            points.put(player, 0);
            preparePlayer(player);
            player.teleport(getRandomLocation());
        }
    }

    @Override
    public void stop() {
        started = false;
        playerCheck.cancel();
        removeParticipants();
        points.clear();
        respawningPlayers.clear();
    }

    private void preparePlayer(Player player) {
        player.getInventory().setContents(kit);
    }

    private Location getRandomLocation() {
        return GeneralUtils.getRandom(locations);
    }

    private String getKillMessage(Player killer, int killerPoints, Player player, int playerPoints) {
        return killMessage.replace("{killer}", killer.getName())
                .replace("{killer_points}", String.valueOf(killerPoints))
                .replace("{player}", player.getName())
                .replace("{player_points}", String.valueOf(playerPoints));
    }

    @EventHandler
    public void playerHurt(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (blockListener(player))
                return;

            if (event.getDamager() instanceof Arrow) {
                if (player.isBlocking()) {
                    event.setDamage(20);
                }
                else {
                    event.setDamage(20);
                }
            }

            if (event.getDamage() >= player.getHealth()) {
                player.getInventory().clear();
                respawningPlayers.add(player);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.spigot().respawn(), 1);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!respawningPlayers.contains(player))
            return;

        Player killer = player.getKiller();
        if (killer != null && player != killer) {
            points.put(killer, points.get(killer) + 1);
            EventUtils.broadcastEventMessage(getKillMessage(killer, points.get(killer), player, points.get(player)));
            killer.setHealth(20);
            killer.getInventory().addItem(kit[2]);
            if (points.get(killer) == WIN_POINTS)
                playerWon(killer);
        }

        preparePlayer(player);
        event.setRespawnLocation(getRandomLocation());
        respawningPlayers.remove(player);
    }

}
