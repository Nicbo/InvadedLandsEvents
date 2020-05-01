package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.protection.managers.RegionManager;
import me.nicbo.InvadedLandsEvents.messages.EventMessage;
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
    protected String configName;
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
        this.configName = configName;

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

    /**
     * Gets called every time event is hosted (start of countdown)
     * Init variables and call methods that need to be run before event starts here
     * @param plugin Plugin instance
     */
    public abstract void init(EventsMain plugin);
    public abstract void start();
    public abstract void stop();

    public List<Player> getParticipants() {
        List<Player> participants = new ArrayList<>(players);
        participants.addAll(spectators);
        return participants;
    }

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

    protected String getEventMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', EventsMain.getMessages().getConfig().getString(configName + "." + message));
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
        for (Player p : GeneralUtils.getPlayers()) {
            p.sendMessage(EventMessage.JOINED_EVENT.replace("{player}", player.getName()));
        }

        players.add(player);
        player.teleport(specLoc);
        EventUtils.clear(player);
        player.getInventory().setItem(8, star);
        //add to team and scoreboard
    }

    public void leaveEvent(Player player) {
        EventUtils.broadcastEventMessage(this, EventMessage.LEFT_EVENT.replace("{player}", player.getName()));
        players.remove(player);
        spectators.remove(player);
        player.teleport(spawnLoc);
        EventUtils.clear(player);
        //remove from team and scoreboard
    }

    public void specEvent(Player player) {
        spectators.add(player);
        player.teleport(specLoc);
        EventUtils.clear(player);
        player.getInventory().setItem(8, star);
    }

    public void sendEventInfo(Player player) {
        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Active Event: " + eventName);
        player.sendMessage(ChatColor.YELLOW + "Type: " + ChatColor.GOLD + eventName);
        player.sendMessage(ChatColor.YELLOW + "Players: " + ChatColor.GOLD + players.size());
        player.sendMessage(ChatColor.YELLOW + "Spectators: " + ChatColor.GOLD + spectators.size());
    }

    public void forceEndEvent() {
        EventUtils.broadcastEventMessage(this, EventMessage.EVENT_FORCE_ENDED.replace("{event}", eventName));

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
        for (Player player : getParticipants()) {
            EventUtils.clear(player);
            player.teleport(spawnLoc);
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
