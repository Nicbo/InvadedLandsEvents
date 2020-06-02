package me.nicbo.InvadedLandsEvents.events;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.InvadedLandsEvents.event.EventLeaveEvent;
import me.nicbo.InvadedLandsEvents.events.TDM.*;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.messages.EventMessage;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.scoreboard.EventScoreboard;
import me.nicbo.InvadedLandsEvents.utils.ConfigFile;
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
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Abstract event class, all events extend this class
 *
 * @author Nicbo
 * @author StarZorrow
 * @since 2020-02-05
 */

public abstract class InvadedEvent implements Listener {
    protected static EventsMain plugin;
    private static ConfigFile messages;
    protected static Logger logger;
    protected static ItemStack star;

    private CountdownSB countdownSB;
    private EventOverSB eventOverSB;
    private EventScoreboard spectatorSB;

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

    protected BukkitRunnable eventTimer;

    protected int timeLeft;

    static {
        plugin = EventsMain.getInstance();
        messages = EventsMain.getMessages();
        logger = plugin.getLogger();
        star = new ItemStack(Material.NETHER_STAR);
        ItemMeta im = star.getItemMeta();
        im.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lLeave Event"));
        star.setItemMeta(im);

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
        this.eventOverSB = new EventOverSB();

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
        Bukkit.getPluginManager().registerEvents(this, plugin);

        if (!this.enabled)
            logger.info(eventName + " is not enabled!");
    }

    protected ProtectedRegion getRegion(String name) {
        ProtectedRegion region = regionManager.getRegion(name);
        if (region == null) {
            logger.severe(eventName + " region '" + name + "' does not exist. Disabling...");
            enabled = false;
        }
        return region;
    }

    public CountdownSB getCountdownSB() {
        return countdownSB;
    }

    protected void startRefreshing(EventScoreboard scoreboard) {
        this.refresher = new BukkitRunnable() {
            @Override
            public void run() {
                scoreboard.refresh();
                scoreboard.updateScoreboard();
                spectatorSB.refresh();
                spectatorSB.updateScoreboard();
            }
        };

        this.refresher.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    protected void startRefreshing(Map<Player, ? extends EventScoreboard> scoreboards) {
        this.refresher = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : scoreboards.keySet()) {
                    EventScoreboard scoreboard = scoreboards.get(player);
                    scoreboard.refresh();
                    scoreboard.updateScoreboard();
                }
                spectatorSB.refresh();
                spectatorSB.updateScoreboard();
            }
        };

        this.refresher.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    protected void stopRefreshing() {
        this.refresher.cancel();
    }

    protected void setSpectatorSB(EventScoreboard spectatorSB) {
        this.spectatorSB = spectatorSB;
    }

    // Don't use this if you loop through players already in start()
    protected void giveAllScoreboard(Scoreboard sb) {
        for (Player player : players) {
            player.setScoreboard(sb);
        }
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
        return ChatColor.translateAlternateColorCodes('&', messages.getConfig().getString(configName + "." + message));
    }

    protected List<String> getEventMessages(String message) {
        List<String> msgs = messages.getConfig().getStringList(configName + "." + message);
        List<String> colouredMessages = new ArrayList<>();

        for (String msg : msgs) {
            colouredMessages.add(ChatColor.translateAlternateColorCodes('&', msg));
        }

        return colouredMessages;
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
    }

    public void leaveEvent(Player player) {
        EventUtils.broadcastEventMessage(EventMessage.LEFT_EVENT.replace("{player}", player.getName()));
        players.remove(player);
        spectators.remove(player);
        player.teleport(spawnLoc);
        EventUtils.clear(player);
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

        checkPlayerCount();

        Bukkit.getPluginManager().callEvent(new EventLeaveEvent(player));
    }

    public void specEvent(Player player) {
        spectators.add(player);
        player.teleport(specLoc);
        EventUtils.clear(player);
        player.getInventory().setItem(8, star);
        player.setScoreboard(started ? spectatorSB.getScoreboard() : countdownSB.getScoreboard());
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
        giveAllScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        stopRefreshing();
        stop();
        plugin.getManagerHandler().getEventManager().setCurrentEvent(null);
    }

    protected void loseEvent(Player player) {
        players.remove(player);
        specEvent(player);
        checkPlayerCount();
    }

    /**
     * Called when a player is removed or leaves event
     * Will call playerWon() if playerCount is under 2
     */
    private void checkPlayerCount() {
        int playerCount = players.size();
        if (playerCount < 2) {
            playerWon(playerCount == 1 ? players.get(0) : null);
        }
    }

    protected void playerWon(Player player) {
        endEvent();
        broadcastWinner(player == null ? "No one" : player.getName());

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            stopEvent();
            if (player != null)
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), winCommand.replace("{winner}", player.getName()));
        }, 100);
    }

    protected void tdmTeamWon(TDMTeam team) {
        endEvent();
        broadcastWinner(team.getName());
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            stopEvent();

            for (TDMPlayer player : team.getTopKillers())
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), winCommand.replace("{winner}", player.getPlayer().getName()));
        }, 100);
    }

    // Calls over() and gives eventoverSB
    private void endEvent() {
        over();
        stopRefreshing();
        giveAllScoreboard(eventOverSB.getScoreboard());
    }

    // Calls stop and remove all players scoreboards
    private void stopEvent() {
        stop();
        giveAllScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        plugin.getManagerHandler().getEventManager().setCurrentEvent(null);
    }

    private void broadcastWinner(String winner) {
        for (int i = 0; i < 4; i++) {
            Bukkit.broadcastMessage(ChatColor.GOLD + winner + ChatColor.YELLOW + " won the " + ChatColor.GOLD + eventName + ChatColor.YELLOW + " event!");
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
                    if (InvadedEvent.this instanceof KOTH)
                        playerWon(((KOTH) InvadedEvent.this).getLeader());
                    else
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

    public final class CountdownSB extends EventScoreboard {
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

    private static final class EventOverSB extends EventScoreboard {
        private Row header;
        private Row message;
        private Row footer;

        public EventOverSB() {
            super(null, "eventover");
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
