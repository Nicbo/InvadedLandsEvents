package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * OITC event:
 * All players have a wooden sword, a bow, and 1 arrow
 * Arrows one shot and when you get a kill you receive an arrow
 *
 * @author Nicbo
 * @since 2020-03-13
 */

public class OITC extends InvadedEvent {
    private List<Location> locations;
    private List<ItemStack> kit;

    private HashMap<Player, Integer> points;

    public OITC(EventsMain plugin) {
        super("One in the Chamber", "oitc", plugin);
        this.locations = new ArrayList<>();

        for (int i = 1; i < 9; i++) {
            this.locations.add(ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-" + i), eventWorld));
        }

        this.kit = Arrays.asList(new ItemStack(Material.WOOD_SWORD, 1), new ItemStack(Material.BOW, 1), new ItemStack(Material.ARROW, 1));
        this.points = new HashMap<>();
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
            randomSpawn(player);
        }
    }

    @Override
    public void stop() {
        started = false;
        playerCheck.cancel();
        removePlayers();
        plugin.getManagerHandler().getEventManager().setCurrentEvent(null);
    }

    private void randomSpawn(Player player) {
        player.teleport(locations.get(GeneralUtils.randomMinMax(0, 7)));
        player.setHealth(20);
        player.getInventory().clear();
        kit.forEach(item -> player.getInventory().addItem(item));
    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (blockEvent(player))
            return;

        if(event.getEntity() != null) {
            event.getEntity().spigot().respawn();
        }
    }

    @EventHandler
    public void arrowHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player hurtPlayer = (Player) event.getEntity();
            if (blockEvent(hurtPlayer))
                return;

            if (event.getDamager() instanceof Arrow) {
                event.setDamage(20);
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (blockEvent(event.getEntity()))
            return;

        Player deadPlayer = event.getEntity();
        Player killer = deadPlayer.getKiller();

        if (killer != null && deadPlayer != killer) {
            points.put(killer, points.get(killer) + 1);
            Bukkit.broadcastMessage(ChatColor.YELLOW + deadPlayer.getName() + "[" + points.get(deadPlayer) + "]" + " has been killed by " + killer.getName() + "[" + points.get(killer) + "]");
            killer.getInventory().addItem(kit.get(2));

            if (points.get(killer) == 20) {
                playerWon(killer);
            }
        }

        deadPlayer.spigot().respawn();
        deadPlayer.setVelocity(new Vector(0, 0, 0));
        randomSpawn(deadPlayer);
    }

    /*
    TODO:
        - Make point cap configurable (20)
     */

}
