package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.protection.managers.RegionManager;
import javafx.print.PageLayout;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.messages.EventMessage;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.scoreboard.FlickerlessScoreboard;
import me.nicbo.InvadedLandsEvents.scoreboard.FlickerlessScoreboard.Track;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.EventUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 * Abstract event class, all events extend this class
 *
 * @author Nicbo
 * @author StarZorrow
 * @since 2020-03-12
 */

public abstract class InvadedEvent implements Listener {
    protected static EventsMain plugin;
    protected static Logger logger;

    protected EventScoreboard scoreboard;
    private EventScoreboard countDownScoreboard;

    protected RegionManager regionManager;

    protected Location spawnLoc;
    protected Location specLoc;
    protected World eventWorld;
    protected String winCommand;

    private String eventName;
    protected String configName;
    protected boolean started;
    protected boolean enabled;

    protected ConfigurationSection eventConfig;

    protected List<Player> players;
    protected List<Player> spectators;

    protected BukkitRunnable playerCheck;
    protected BukkitRunnable eventTimer;

    protected ItemStack star;

    private int timeLeft;

    static {
        plugin = EventsMain.getInstance();
        logger = plugin.getLogger();
    }

    /**
     *
     * @param eventName Name that gets broadcasted
     * @param configName Path is events.eventName
     */

    public InvadedEvent(String eventName, String configName) {
        this.eventName = eventName;
        this.configName = configName;

        FileConfiguration config = plugin.getConfig();
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

        this.countDownScoreboard = new CountdownSB(plugin.getManagerHandler().getEventManager());

        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (!this.enabled)
            logger.info(eventName + " not enabled!");
    }

    public void setScoreboard(EventScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public EventScoreboard getScoreboard() {
        return scoreboard;
    }

    public CountdownSB getCountDownScoreboard() {
        return (CountdownSB) countDownScoreboard;
    }


    /**
     * Gets called every time event is hosted (start of countdown)
     * Init variables and call methods that need to be run before event starts here
     */
    public abstract void init();
    public abstract void start();
    public abstract void over();
    public abstract void stop();

    public List<Player> getPlayers() {
        return players;
    }

    public List<Player> getSpectators() {
        return spectators;
    }

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

        if (!started)
            countDownScoreboard.giveScoreboard(player);
        //add to team and scoreboard
    }

    public void leaveEvent(Player player) {
        EventUtils.broadcastEventMessage(EventMessage.LEFT_EVENT.replace("{player}", player.getName()));
        players.remove(player);
        spectators.remove(player);
        player.teleport(spawnLoc);
        EventUtils.clear(player);

        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
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
        EventUtils.broadcastEventMessage(EventMessage.EVENT_FORCE_ENDED.replace("{event}", eventName));
        over();
        stop();
        plugin.getManagerHandler().getEventManager().setCurrentEvent(null);
    }

    protected void loseEvent(Player player) {
        players.remove(player);
        specEvent(player);
    }

    protected void playerWon(Player player) {
        over();
        for (int i = 0; i < 4; i++) {
            Bukkit.broadcastMessage(ChatColor.GOLD + (player == null ? "No one" : player.getName()) + ChatColor.YELLOW + " won the " + ChatColor.GOLD + eventName + ChatColor.YELLOW + " event!");
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            stop();
            plugin.getManagerHandler().getEventManager().setCurrentEvent(null);
        }, 100);

        if (player != null) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
                    () -> plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), winCommand.replace("{winner}", player.getName())), 100);
        }
    }

    protected void removeParticipants() {
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

    protected void startTimer(int timeInSeconds) {
        this.timeLeft = timeInSeconds;
        eventTimer = new BukkitRunnable() {
            @Override
            public void run() {
                EventUtils.broadcastEventMessage(ChatColor.YELLOW + "timer: " + GeneralUtils.formatSeconds(timeLeft--));
                if (timeLeft <= 0) {
                    playerWon(null);
                    this.cancel();
                }
            }
        };
        eventTimer.runTaskTimer(plugin, 0, 20);
    }

    protected boolean blockListener(Player player) {
        return !started || !players.contains(player);
    }

    public abstract class EventScoreboard {
        protected FlickerlessScoreboard scoreboard;
        private BukkitRunnable refresher;

        protected final String LINE;

        protected EventScoreboard() {
            this.LINE = ChatColor.GRAY.toString() + ChatColor.STRIKETHROUGH.toString() + "----------";
        }

        public abstract void refresh();

        public void startRefreshing() {
            this.refresher = new BukkitRunnable() {
                @Override
                public void run() {
                    refresh();
                }
            };
            this.refresher.runTaskTimerAsynchronously(plugin, 0, 20);
        }

        public void stopRefreshing() {
            this.refresher.cancel();
        }

        public void giveScoreboard(Player player) {
            player.setScoreboard(scoreboard.getScoreboard());
        }

        public void giveScoreboard(List<Player> players) {
            players.forEach(player -> player.setScoreboard(scoreboard.getScoreboard()));
        }

        public void removeScoreboard(Player player) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        public void removeScoreboard(List<Player> players) {
            players.forEach(player -> player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard()));
        }
    }

    public final class CountdownSB extends EventScoreboard {
        private EventManager eventManager;
        private Track headerTrack;
        private Track playerCountTrack;
        private Track countDownTrack;
        private Track footerTrack;

        public CountdownSB(EventManager eventManager) {
            this.eventManager = eventManager;
            this.headerTrack = new FlickerlessScoreboard.Track("headerCD", ChatColor.GOLD.toString(), 5, LINE, LINE);
            this.playerCountTrack = new FlickerlessScoreboard.Track("playerCountCD", ChatColor.RESET.toString(), 4, ChatColor.YELLOW + "Players: ", ChatColor.GOLD + String.valueOf(spectators.size()));
            this.countDownTrack = new FlickerlessScoreboard.Track("countDownCD", ChatColor.DARK_AQUA.toString(), 2, ChatColor.YELLOW + "Starting in ", ChatColor.GOLD + String.valueOf(eventManager.getCountDown()));
            this.footerTrack = new FlickerlessScoreboard.Track("footerCD", ChatColor.WHITE.toString(), 1, LINE, LINE);
            super.scoreboard = new FlickerlessScoreboard(ChatColor.GOLD.toString() + ChatColor.BOLD.toString() + "NAME", DisplaySlot.SIDEBAR, headerTrack, playerCountTrack, countDownTrack, footerTrack);
            super.scoreboard.addBlankLine(3, ChatColor.BOLD);
        }

        @Override
        public void refresh() {
            countDownTrack.setSuffix(ChatColor.GOLD + String.valueOf(eventManager.getCountDown()));
            playerCountTrack.setSuffix(ChatColor.GOLD + String.valueOf(players.size()));
            scoreboard.updateScoreboard();
        }
    }

    /*
    TODO:
        - Scoreboards/Teams
        - Need per player sbs (oitc, tdm, etc.)
     */
}
