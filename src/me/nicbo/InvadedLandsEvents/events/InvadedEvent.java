package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.protection.managers.RegionManager;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.messages.EventMessage;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.scoreboard.EventScoreboard;
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

    private CountdownSB countdownSB;
    private EventOverSB eventOverSB;
    private EventScoreboard spectatorSB;
    protected HashMap<Player, EventScoreboard> scoreboards;
    private BukkitRunnable refresher;


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

    protected int timeLeft;

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

        this.countdownSB = new CountdownSB(plugin.getManagerHandler().getEventManager(), null);
        this.eventOverSB = new EventOverSB(null);
        this.scoreboards = new HashMap<>();

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

        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (!this.enabled)
            logger.info(eventName + " not enabled!");
    }

    public CountdownSB getCountdownSB() {
        return countdownSB;
    }

    public void startRefreshing() {
        this.refresher = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : scoreboards.keySet()) {
                    EventScoreboard scoreboard = scoreboards.get(player);
                    scoreboard.refresh();
                    scoreboard.updateScoreboard();
                }
            }
        };

        this.refresher.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    protected void stopRefreshing() {
        this.refresher.cancel();
    }

    protected void giveScoreboard(Player player, EventScoreboard scoreboard) {
        scoreboards.put(player, scoreboard);
        player.setScoreboard(scoreboard.getScoreboard());
    }

    protected void removeScoreboard(Player player) {
        scoreboards.remove(player);
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    protected void removeAllScoreboards() {
        for (Player player : scoreboards.keySet()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
        scoreboards.clear();
    }

    private void giveAllEventOverSB() {
        for (Player player : getParticipants()) {
            player.setScoreboard(eventOverSB.getScoreboard());
        }
    }

    public void setSpectatorSB(EventScoreboard spectatorSB) {
        this.spectatorSB = spectatorSB;
    }

    /**
     * Gets called every time event is hosted (start of countdown)
     * Init variables and call methods that need to be run before event starts here
     */
    public abstract void init();
    public abstract void start();
    public abstract void over();

    public void stop() {
        removeParticipants();
        started = false;
    }

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

        if (!started) {
            player.setScoreboard(countdownSB.getScoreboard());
        }

        //add to team and scoreboard
    }

    public void leaveEvent(Player player) {
        EventUtils.broadcastEventMessage(EventMessage.LEFT_EVENT.replace("{player}", player.getName()));
        players.remove(player);
        spectators.remove(player);
        player.teleport(spawnLoc);
        EventUtils.clear(player);
        removeScoreboard(player);
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
        removeAllScoreboards();
        stopRefreshing();
        stop();
        plugin.getManagerHandler().getEventManager().setCurrentEvent(null);
    }

    protected void loseEvent(Player player) {
        players.remove(player);
        specEvent(player);
    }

    protected void playerWon(Player player) {
        over();
        stopRefreshing();
        giveAllEventOverSB();
        for (int i = 0; i < 4; i++) {
            Bukkit.broadcastMessage(ChatColor.GOLD + (player == null ? "No one" : player.getName()) + ChatColor.YELLOW + " won the " + ChatColor.GOLD + eventName + ChatColor.YELLOW + " event!");
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            removeAllScoreboards();
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

    protected void startTimer(int timeInSeconds) {
        this.timeLeft = timeInSeconds;
        this.eventTimer = new BukkitRunnable() {
            @Override
            public void run() {
                timeLeft--;
                if (timeLeft <= 0) {
                    playerWon(null);
                    this.cancel();
                }
            }
        };
        this.eventTimer.runTaskTimer(plugin, 0, 20);
    }

    protected boolean blockListener(Player player) {
        return !started || !players.contains(player);
    }

    public class CountdownSB extends EventScoreboard {
        private EventManager eventManager;

        private TrackRow playerCount;
        private TrackRow countDown;

        private Row header;
        private Row footer;
        private Row blank;

        public CountdownSB(EventManager eventManager, Player player) {
            super(player, "countdown");
            this.eventManager = eventManager;
            this.header = new Row("header", HEADERFOOTER, ChatColor.RED.toString(), HEADERFOOTER, 5);
            this.playerCount = new TrackRow("playerCount", ChatColor.YELLOW + "Players: ", ChatColor.GRAY + "" + ChatColor.GOLD, String.valueOf(0), 4);
            this.blank = new Row("blank", "", ChatColor.BOLD + "" + ChatColor.BLUE, "", 3);
            this.countDown = new TrackRow("countDown", ChatColor.YELLOW + "Starting in ", ChatColor.ITALIC + "" + ChatColor.GOLD, 60 + "s", 2);
            this.footer = new Row("footer", HEADERFOOTER, ChatColor.DARK_BLUE.toString(), HEADERFOOTER, 1);
            super.init("Countdown", header, playerCount, blank, countDown, footer);
        }

        @Override
        public void refresh() {
            playerCount.setSuffix(String.valueOf(players.size()));
            countDown.setSuffix(eventManager.getCountDown() + "s");
            updateScoreboard();
        }
    }

    // event is over scoreboard

    public class EventOverSB extends EventScoreboard {
        private Row header;
        private Row message;
        private Row footer;

        public EventOverSB(Player player) {
            super(player, "eventover");
            this.header = new Row("header", HEADERFOOTER, ChatColor.BOLD.toString(), HEADERFOOTER, 3);
            this.message = new Row("message", ChatColor.YELLOW + "Event has ", "ended", "", 2);
            this.footer = new Row("footer", HEADERFOOTER, ChatColor.DARK_GRAY.toString(), HEADERFOOTER, 1);
            super.init("EventOverSB", header, message, footer);
        }

        @Override
        public void refresh() {
            throw new UnsupportedOperationException("Can't refresh EventOverSB");
        }
    }
}
