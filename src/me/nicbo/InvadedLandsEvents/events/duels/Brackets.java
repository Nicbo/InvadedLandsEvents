package me.nicbo.InvadedLandsEvents.events.duels;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Brackets event:
 * 2 players tp'd and get kit
 * Whoever wins duel moves on to next round
 *
 * @author Nicbo
 * @author StarZorrow
 * @since 2020-04-30
 */


public final class Brackets extends Duel {
    private ItemStack[] armour;
    private ItemStack[] kit;

    public Brackets() {
        super("1v1 Brackets", "brackets");

        this.armour = new ItemStack[] {
                new ItemStack(Material.IRON_BOOTS, 1),
                new ItemStack(Material.IRON_LEGGINGS, 1),
                new ItemStack(Material.IRON_CHESTPLATE, 1),
                new ItemStack(Material.IRON_HELMET, 1)
        };

        this.kit = new ItemStack[] {
                new ItemStack(Material.IRON_SWORD, 1),
                new ItemStack(Material.BOW, 1),
                new ItemStack(Material.GOLDEN_APPLE, 10),
                new ItemStack(Material.ARROW, 32)
        };
    }

    @Override
    public void init() {
        frozen = false;
        initLeaveCheck();
    }

    @Override
    public void start() {
        //giveAllScoreboard(bracketsSB.getScoreboard());
        // startRefreshing(bracketsSB);
        leaveCheck.runTaskTimerAsynchronously(plugin, 0, 1);
        newRound();
    }

    @Override
    public void over() {
        leaveCheck.cancel();
        fightingPlayers.clear();
    }

    @Override
    public void tpFightingPlayers() {
        Player player1 = fightingPlayers.get(0);
        Player player2 = fightingPlayers.get(1);

        player1.getInventory().setArmorContents(armour);
        player1.getInventory().setContents(kit);
        player2.getInventory().setArmorContents(armour);
        player2.getInventory().setContents(kit);

        player1.teleport(startLoc1);
        player2.teleport(startLoc2);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        fightingPlayers.remove(player);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (fightingPlayers.contains(player) && frozen) {
            Location to = event.getTo();
            Location from = event.getFrom();
            if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
                player.teleport(from.setDirection(to.getDirection()));
            }
        }
    }

    @EventHandler
    public void playerHurt(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (blockListener(player))
                return;

            if (frozen)
                event.setCancelled(true);

            if (event.getDamage() >= player.getHealth()) {
                fightingPlayers.remove(player);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    player.spigot().respawn();
                    loseEvent(player);
                    roundOver(player);
                }, 1);
            }
        }
    }
    /*
    TODO:
        - Scoreboard for matches (Player vs. Player)
     */
}
