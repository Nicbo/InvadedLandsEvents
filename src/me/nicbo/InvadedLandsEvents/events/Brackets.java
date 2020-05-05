package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Brackets event:
 * 2 players tp'd and get kit
 * Whoever wins duel moves on to next round
 *
 * @author Nicbo
 * @since 2020-04-30
 */

public final class Brackets extends InvadedEvent {
    private Location startLoc1;
    private Location startLoc2;

    private ItemStack[] armour;
    private ItemStack[] kit;

    private List<Player> fightingPlayers;

    private boolean fighting;
    private boolean frozen;

    private BukkitRunnable leaveCheck;
    private BukkitRunnable playerFreeze;

    private final String MATCH_START;
    private final String MATCH_COUNTER;
    private final String MATCH_STARTED;
    private final String MATCH_ENDED;

    public Brackets(EventsMain plugin) {
        super("1v1 Brackets", "brackets", plugin);

        this.startLoc1 = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-1"), eventWorld);
        this.startLoc2 = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-2"), eventWorld);

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

        this.fightingPlayers = new ArrayList<>();

        this.MATCH_START = getEventMessage("MATCH_START");
        this.MATCH_COUNTER = getEventMessage("MATCH_COUNTER");
        this.MATCH_STARTED = getEventMessage("MATCH_STARTED");
        this.MATCH_ENDED = getEventMessage("MATCH_ENDED");
    }

    @Override
    public void init(EventsMain plugin) {
        this.leaveCheck = new BukkitRunnable() {
            @Override
            public void run() {
                if (fightingPlayers.size() == 1 && fighting) {
                    roundOver(fightingPlayers.get(0));
                    if (frozen)
                        playerFreeze.cancel();
                }
            }
        };
    }

    @Override
    public void start() {
        leaveCheck.runTaskTimerAsynchronously(plugin, 0, 1);
        newRound();
    }

    public void over() {
        leaveCheck.cancel();
        fightingPlayers.clear();
    }

    @Override
    public void stop() {
        started = false;
        removeParticipants();
    }

    private void restartPlayerFreeze() {
        this.playerFreeze = new BukkitRunnable() {
            private int timer = 5;

            @Override
            public void run() {
                if (timer == 5) {
                    frozen = true;
                    EventUtils.broadcastEventMessage(MATCH_START.replace("{player1}", fightingPlayers.get(0).getName())
                            .replace("{player2}", fightingPlayers.get(1).getName()));
                }

                EventUtils.broadcastEventMessage(MATCH_COUNTER.replace("{seconds}", String.valueOf(timer--)));
                if (timer <= 0) {
                    EventUtils.broadcastEventMessage(MATCH_STARTED);
                    frozen = false;
                    this.cancel();
                }
            }
        };

        this.playerFreeze.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    private void newRound() {
        if (players.size() < 2) {
            playerWon(players.get(0));
        } else {
            fightingPlayers.clear();
            addTwoPlayers();
            applyKitAndTp();
            restartPlayerFreeze();
            fighting = true;
        }
    }

    private void addTwoPlayers() {
        fightingPlayers.add(GeneralUtils.getRandom(players));
        Player player2 = GeneralUtils.getRandom(players);

        while (fightingPlayers.contains(player2)) {
            player2 = GeneralUtils.getRandom(players);
        }

        fightingPlayers.add(player2);
    }

    private void applyKitAndTp() {
        Player player1 = fightingPlayers.get(0);
        Player player2 = fightingPlayers.get(1);

        player1.getInventory().setArmorContents(armour);
        player1.getInventory().setContents(kit);
        player2.getInventory().setArmorContents(armour);
        player2.getInventory().setContents(kit);

        player1.teleport(startLoc1);
        player2.teleport(startLoc2);
    }

    private void roundOver(Player loser) {
        fighting = false;
        Player winner = fightingPlayers.get(0);
        EventUtils.broadcastEventMessage(MATCH_ENDED.replace("{loser}", loser.getName())
                .replace("{winner}", winner.getName())
                .replace("{remaining}", String.valueOf(players.size())));
        EventUtils.clear(winner);
        winner.teleport(specLoc);
        newRound();
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
}
