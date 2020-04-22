package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.protection.managers.RegionManager;
import me.nicbo.InvadedLandsEvents.EventMessage;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Abstract event class, all events extend this class
 *
 * @author Nicbo
 * @author StarZorroww
 * @since 2020-03-12
 */

public abstract class InvadedEvent implements Listener {
    protected EventsMain plugin;
    protected Logger logger;
    protected RegionManager regionManager;

    protected Location spawnLoc;
    protected Location specLoc;
    protected World eventWorld;
    protected String winCommand;

    private String eventName;
    protected boolean started;
    private boolean enabled;

    protected ConfigurationSection eventConfig;
    protected List<Player> players;
    protected List<Player> spectators;

    protected BukkitRunnable playerCheck;

    protected ItemStack star;

    /**
     *
     * @param eventName Name that gets broadcasted
     * @param configName Path is events.eventName
     * @param plugin Instance of main class
     */

    public InvadedEvent(String eventName, String configName, EventsMain plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.eventName = eventName;

        FileConfiguration config = this.plugin.getConfig();
        this.eventConfig = config.getConfigurationSection("events." + configName);
        this.eventWorld = Bukkit.getWorld(config.getString("event-world"));
        this.spawnLoc = ConfigUtils.deserializeLoc(config.getConfigurationSection("spawn-location"));
        this.specLoc = ConfigUtils.deserializeLoc(this.eventConfig.getConfigurationSection("spec-location"), this.eventWorld);
        this.winCommand = config.getString("win-command");

        this.regionManager = plugin.getWorldGuardPlugin().getRegionManager(eventWorld);

        this.enabled = eventConfig.getBoolean("enabled");
        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();

        this.star = new ItemStack(Material.NETHER_STAR);
        ItemMeta im = star.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lLeave Event"));
        this.star.setItemMeta(im);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (!this.enabled)
            logger.info(eventName + " not enabled!");
    }

    public abstract void init(EventsMain plugin);
    public abstract void start();
    public abstract void stop();

    public String getEventName() {
        return eventName;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isStarted() {
        return started;
    }
    public boolean isEnabled() {
        return enabled;
    }

    public int getSize() {
        return players.size();
    }

    protected void initPlayerCheck() {
        this.playerCheck = new BukkitRunnable() {
            @Override
            public void run() {
                int playerCount = players.size();
                if (playerCount < 2) {
                    playerWon(playerCount == 1 ? players.get(0) : null);
                    this.cancel();
                }
            }
        };
    }

    public boolean containsPlayer(Player player) {
        return players.contains(player) || spectators.contains(player);
    }

    public void joinEvent(Player player) {
        players.add(player);
        player.teleport(specLoc);
        EventUtils.clear(player);
        player.getInventory().setItem(8, star);

        for (Player p : GeneralUtils.getPlayers()) {
            p.sendMessage(EventMessage.JOINED_EVENT.toString().replace("{player}", player.getName()));
        }
        //add to team and scoreboard
    }

    public void leaveEvent(Player player) {
        players.remove(player);
        spectators.remove(player);
        EventUtils.clear(player);
        player.teleport(spawnLoc);

        for (Player p : started ? players : GeneralUtils.getPlayers()) {
            p.sendMessage(EventMessage.LEFT_EVENT.toString().replace("{player}", player.getName()));
        }
        //remove from team and scoreboard
    }

    public void specEvent(Player player) {
        spectators.add(player);
        player.teleport(specLoc);
        EventUtils.clear(player);
        player.getInventory().setItem(8, star);
    }

    public void eventInfo(Player player) {
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Active Event: " + eventName);
        player.sendMessage(ChatColor.YELLOW + "Type: " + ChatColor.GOLD + eventName);
        player.sendMessage(ChatColor.YELLOW + "Players: " + ChatColor.GOLD + players.size());
        player.sendMessage(ChatColor.YELLOW + "Spectators: " + ChatColor.GOLD + spectators.size());
    }

    public void forceEndEvent() {
        players.forEach(player -> player.sendMessage(EventMessage.EVENT_FORCE_ENDED.toString().replace("{event}", eventName)));

        this.plugin.getServer().getScheduler().runTask(this.plugin, new Runnable() {
            @Override
            public void run() {
                clearPlayers();
            }
        });

        started = false;
        removePlayers();
        plugin.getManagerHandler().getEventManager().setCurrentEvent(null);
    }

    protected void loseEvent(Player player) {
        players.remove(player);
        EventUtils.clear(player);
        specEvent(player);
    }

    protected void playerWon(Player player) {
        for (int i = 0; i < 4; i++) {
            Bukkit.broadcastMessage(ChatColor.GOLD + (player == null ? "No one" : player.getName()) + ChatColor.YELLOW + " won the " + ChatColor.GOLD + eventName + ChatColor.YELLOW + " event!");
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                stop();
                plugin.getManagerHandler().getEventManager().setCurrentEvent(null);
            }
        }, 100);

        if (player != null) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), winCommand.replace("{winner}", player.getName()));
                }
            }, 100);
        }
    }

    protected void removePlayers() {
        for (Player player : players) {
            EventUtils.clear(player);
            player.teleport(spawnLoc);
        }

        for (Player spectator : spectators) {
            EventUtils.clear(spectator);
            spectator.teleport(spawnLoc);
        }

        players.clear();
        spectators.clear();
    }

    protected void clearPlayers() {
        for (Player player : players) {
            EventUtils.clear(player);
        }
    }

    protected boolean blockListener(Player player) {
        return !started || !players.contains(player);
    }

    /*
    TODO:
        - Scoreboards/Teams
     */
}
