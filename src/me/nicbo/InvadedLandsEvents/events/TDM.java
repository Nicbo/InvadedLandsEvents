package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.events.utils.EventTeam;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * TDM Event:
 * 2 teams with kits
 * 5 players with the top kills from the winning team get rewards
 *
 * @author Nicbo
 * @since 2020-05-18
 */

public final class TDM extends InvadedEvent {
    private TDMTeam red;
    private TDMTeam blue;

    private BukkitRunnable matchCountdownRunnable;

    private Map<Player, Integer> kills;
    private boolean matchCountdown;

    protected List<String> winnersMessages;
    private final String MATCH_COUNTER;
    private final String MATCH_START;
    private final String ELIMINATED;


    public TDM() {
        super("Team Deathmatch", "tdm");
        this.red = new TDMTeam(ChatColor.RED + "Red Team", Color.RED, ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("red-start-location"), eventWorld));
        this.blue = new TDMTeam(ChatColor.BLUE + "Blue Team", Color.BLUE, ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("blue-start-location"), eventWorld));

        this.kills = new HashMap<>();

        this.winnersMessages = getEventMessages("WINNERS");
        this.MATCH_COUNTER = getEventMessage("MATCH_COUNTER");
        this.MATCH_START = getEventMessage("MATCH_START");
        this.ELIMINATED = getEventMessage("ELIMINATED");
    }

    @Override
    public void init() {
        this.matchCountdownRunnable = new BukkitRunnable() {
            private int timer = 5;

            @Override
            public void run() {
                EventUtils.broadcastEventMessage(MATCH_COUNTER.replace("{seconds}", String.valueOf(timer)));
                if (timer == 1) {
                    EventUtils.broadcastEventMessage(MATCH_START);
                    matchCountdown = false;
                    this.cancel();
                }

                timer--;
            }

        };
    }

    @Override
    public void start() {
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
        startMatchCountdown();
    }

    @Override
    public void over() {
        if (matchCountdown)
            matchCountdownRunnable.cancel();
        red.clear();
        blue.clear();
        kills.clear();
    }

    private void startMatchCountdown() {
        matchCountdown = true;
        matchCountdownRunnable.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    private Player playerFromDamager(Entity entity) {
        Player player;
        if (entity instanceof Player) {
            player = (Player) entity;
        } else if (entity instanceof Arrow && ((Arrow) entity).getShooter() instanceof Player) {
            player = (Player) ((Arrow) entity).getShooter();
        } else {
            return null;
        }
        return player;
    }

    private boolean playersShareTeam(Player player1, Player player2) {
        return (red.contains(player1) && red.contains(player2)) || (blue.contains(player1) && red.contains(player2));
    }

    @EventHandler
    public void playerHurt(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (blockListener(player))
                return;

            Player damager = playerFromDamager(event.getDamager());

            if (matchCountdown || (damager != null && playersShareTeam(damager, player)))
                event.setCancelled(true);

            if (event.getDamage() >= player.getHealth()) {
                String elimMessage = ELIMINATED
                        .replace("{player}", player.getName())
                        .replace("{remaining}", String.valueOf(players.size() - 1));

                if (damager != null) {
                    elimMessage = elimMessage.replace("{killer}", damager.getName());
                }

                EventUtils.broadcastEventMessage(elimMessage);

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    player.spigot().respawn();
                    loseEvent(player);
                }, 1);
            }
        }
    }


    public class TDMTeam extends EventTeam {
        private final String name;

        private final ItemStack[] armour;
        private final ItemStack[] kit;

        private final Location spawnLoc;

        public TDMTeam(String name, Color colour, Location spawnLoc) {
            this.name = name;
            this.spawnLoc = spawnLoc;

            ItemStack helmet = new ItemStack(Material.LEATHER_HELMET, 1);
            LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
            meta.setColor(colour);
            meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
            meta.addEnchant(Enchantment.DURABILITY, 3, true);
            helmet.setItemMeta(meta);

            this.armour = new ItemStack[] {
                    new ItemStack(Material.IRON_BOOTS, 1),
                    new ItemStack(Material.IRON_LEGGINGS, 1),
                    new ItemStack(Material.IRON_CHESTPLATE, 1),
                    helmet,
            };

            this.kit = new ItemStack[] {
                    new ItemStack(Material.IRON_SWORD, 1),
                    new ItemStack(Material.BOW, 1),
                    new ItemStack(Material.GOLDEN_APPLE, 10),
                    new ItemStack(Material.ARROW, 32)
            };
        }

        public void preparePlayers() {
            for (Player player : players) {
                player.teleport(spawnLoc);
                player.getInventory().setArmorContents(armour);
                player.getInventory().setContents(kit);
            }
        }

        public List<TDMPlayer> getTopKillers() {
            Set<TDMPlayer> sorted = new TreeSet<>();

            for (Player player : kills.keySet()) {
                sorted.add(new TDMPlayer(player, kills.get(player)));
            }

            return new ArrayList<>(sorted).subList(0, 4);
        }

        public String getName() {
            return name;
        }
    }


    protected final class TDMPlayer implements Comparable<TDMPlayer> {
        private final Player key;
        private final int value;

        public TDMPlayer(Player key, int value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public int compareTo(TDMPlayer o) {
            return Integer.compare(value, o.value);
        }

        public Player getPlayer() {
            return key;
        }
    }

    /*
    TODO:
        - Rest of event
     */
}
