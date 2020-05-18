package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.scoreboard.EventScoreboard;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import me.nicbo.InvadedLandsEvents.utils.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * LMS event:
 * All players are tp'd to 2 start locations with a kit
 * Last player alive wins
 *
 * @author Nicbo
 * @author StarZorrow
 * @since 2020-05-07
 */

public final class LMS extends InvadedEvent {
    private LMSSB lmsSB;

    private Location start1;
    private Location start2;

    private ItemStack[] armour;
    private ItemStack[] kit;

    private boolean matchCountdown;

    private final int TIME_LIMIT;

    private final String MATCH_STARTING;
    private final String MATCH_COUNTER;
    private final String MATCH_START;
    private final String ELIMINATED;

    public LMS() {
        super("Last Man Standing", "lms");

        this.lmsSB = new LMSSB();

        this.start1 = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-1"), eventWorld);
        this.start2 = ConfigUtils.deserializeLoc(eventConfig.getConfigurationSection("start-location-2"), eventWorld);

        this.armour = new ItemStack[] {
                ItemUtils.addEnchant(new ItemStack(Material.IRON_BOOTS, 1), Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                ItemUtils.addEnchant(new ItemStack(Material.IRON_LEGGINGS, 1), Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                ItemUtils.addEnchant(new ItemStack(Material.IRON_CHESTPLATE, 1), Enchantment.PROTECTION_ENVIRONMENTAL, 2),
                ItemUtils.addEnchant(new ItemStack(Material.IRON_HELMET, 1), Enchantment.PROTECTION_ENVIRONMENTAL, 2)
        };

        this.kit = new ItemStack[] {
                ItemUtils.addEnchant(new ItemStack(Material.IRON_SWORD, 1), Enchantment.DAMAGE_ALL, 1),
                new ItemStack(Material.BOW, 1),
                new ItemStack(Material.GOLDEN_APPLE, 10),
                new ItemStack(Material.ARROW, 32)
        };

        this.TIME_LIMIT = eventConfig.getInt("int-seconds-time-limit");

        this.MATCH_STARTING = getEventMessage("MATCH_STARTING");
        this.MATCH_COUNTER = getEventMessage("MATCH_COUNTER");
        this.MATCH_START = getEventMessage("MATCH_START");
        this.ELIMINATED = getEventMessage("ELIMINATED");

        setSpectatorSB(lmsSB);
    }

    @Override
    public void init() {

    }

    @Override
    public void start() {
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            player.setScoreboard(lmsSB.getScoreboard());

            player.teleport(i % 2 == 0 ? start1 : start2);
            player.getInventory().setArmorContents(armour);
            player.getInventory().setContents(kit);
        }

        startRefreshing(lmsSB);
        startTimer(TIME_LIMIT);
        startMatchCountdown();
    }

    @Override
    public void over() {
        eventTimer.cancel();
    }


    private void startMatchCountdown() {
        matchCountdown = true;
        EventUtils.broadcastEventMessage(MATCH_STARTING);
        new BukkitRunnable() {
            private int timer = 5;

            @Override
            public void run() {
                if (!matchCountdown) {
                    this.cancel();
                    return;
                }

                EventUtils.broadcastEventMessage(MATCH_COUNTER.replace("{seconds}", String.valueOf(timer--)));

                if (timer == 0) {
                    EventUtils.broadcastEventMessage(MATCH_START);
                    matchCountdown = false;
                    this.cancel();
                }
            }

        }.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    @EventHandler
    public void playerHurt(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (blockListener(player))
                return;

            if (matchCountdown)
                event.setCancelled(true);

            if (event.getDamage() >= player.getHealth()) {
                EventUtils.broadcastEventMessage(ELIMINATED.replace("{player}", player.getName())
                        .replace("{remaining}", String.valueOf(players.size() - 1)));
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    player.spigot().respawn();
                    loseEvent(player);
                }, 1);
            }
        }
    }

    private class LMSSB extends EventScoreboard {
        private TrackRow playerCount;
        private TrackRow specCount;
        private TrackRow timeRemaining;

        private Row header;
        private Row footer;

        public LMSSB() {
            super(null, "lms");
            this.header = new Row("header", HEADERFOOTER, ChatColor.BOLD.toString(), HEADERFOOTER, 5);
            this.playerCount = new TrackRow("playerCount", ChatColor.YELLOW + "Players: ", ChatColor.DARK_PURPLE + "" + ChatColor.GOLD, String.valueOf(0), 4);
            this.specCount = new TrackRow("specCount", ChatColor.YELLOW + "Spectators: ", ChatColor.LIGHT_PURPLE + "" + ChatColor.GOLD, String.valueOf(0), 3);
            this.timeRemaining = new TrackRow("timeRemaining", ChatColor.YELLOW + "Time Remain", "ing: " + ChatColor.GOLD, String.valueOf(0), 2);
            this.footer = new Row("footer", HEADERFOOTER, ChatColor.DARK_PURPLE.toString(), HEADERFOOTER, 1);
            super.init("LMS", header, playerCount, specCount, timeRemaining, footer);
        }

        @Override
        public void refresh() {
            playerCount.setSuffix(String.valueOf(players.size()));
            specCount.setSuffix(String.valueOf(spectators.size()));
            timeRemaining.setSuffix(GeneralUtils.formatSeconds(timeLeft));
        }
    }
}
