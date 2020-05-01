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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Brackets extends InvadedEvent {
    private Location startLoc1;
    private Location startLoc2;

    private ItemStack[] armour;
    private ItemStack[] kit;

    private List<Player> fightingPlayers;

    private BukkitRunnable playerFreeze;

    private boolean frozen;

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
        initPlayerCheck();

        InvadedEvent instance = this;
        this.playerFreeze = new BukkitRunnable() {
            private int timer = 5;

            @Override
            public void run() {
                if (timer == 5) {
                    frozen = true;
                    EventUtils.broadcastEventMessage(instance, MATCH_START.replace("{player1}", fightingPlayers.get(0).getName())
                            .replace("{player2}", fightingPlayers.get(1).getName()));
                } else if (timer <= 1) {
                    EventUtils.broadcastEventMessage(instance, MATCH_STARTED);
                    frozen = false;
                    this.cancel();
                } else {
                    EventUtils.broadcastEventMessage(instance, MATCH_COUNTER.replace("{seconds}", String.valueOf(timer)));
                }
                timer--;
            }
        };
    }

    @Override
    public void start() {
        playerCheck.runTaskTimerAsynchronously(plugin, 0, 1);
        newRound();
    }

    @Override
    public void stop() {
        fightingPlayers.clear();
    }

    private void newRound() {
        fightingPlayers.clear();
        addTwoPlayers();
        applyKitAndTp();
        playerFreeze.runTaskTimerAsynchronously(plugin, 0, 20);
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
        EventUtils.broadcastEventMessage(this, MATCH_ENDED.replace("{loser}", loser.getName())
                .replace("{winner}", fightingPlayers.get(0).getName())
                .replace("{remaining}", String.valueOf(players.size())));
        newRound();
    }



    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (fightingPlayers.contains(player) && frozen) {
            event.setCancelled(true);
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
                roundOver(player);
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    player.spigot().respawn();
                    loseEvent(player);
                }, 1);
            }
        }
    }

    /*
    TODO:
        - heads up this event is completely broken I just had to commit cause im going to sleep
     */
}
