package me.nicbo.invadedlandsevents.events.type;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.event.EventStopEvent;
import me.nicbo.invadedlandsevents.events.util.team.EventTeam;
import me.nicbo.invadedlandsevents.events.util.team.SumoTeam;
import me.nicbo.invadedlandsevents.events.util.team.TDMTeam;
import me.nicbo.invadedlandsevents.messages.impl.ListMessage;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.permission.EventPermission;
import me.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import me.nicbo.invadedlandsevents.scoreboard.line.Line;
import me.nicbo.invadedlandsevents.scoreboard.line.TrackLine;
import me.nicbo.invadedlandsevents.util.misc.CompositeUnmodifiableList;
import me.nicbo.invadedlandsevents.util.ConfigUtils;
import me.nicbo.invadedlandsevents.util.SpigotUtils;
import me.nicbo.invadedlandsevents.util.StringUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;

import java.util.*;
import java.util.function.Function;

/**
 * Base event class
 *
 * @author Nicbo
 * @author StarZorrow
 */

public abstract class InvadedEvent implements Listener {
    private final ItemStack star;

    protected final InvadedLandsEvents plugin;

    private final EventScoreboardManager eventScoreboardManager;

    private final Set<String> allowedCommands;

    private final World eventWorld;
    private final RegionManager regionManager;

    private final ConfigurationSection eventConfig;
    private final Location spawnLoc;
    private final Location specLoc;
    private final String winCommand;

    private final String eventName;
    private final String configName;

    private boolean valid;
    private boolean running;
    private boolean ending;

    private final List<Player> players;
    private final List<Player> spectators;

    // For access of internal players
    // We do not want to be able to change players/spectators directly in subclasses/outside this class
    private final List<Player> playersView;
    private final List<Player> spectatorsView;

    private BukkitRunnable countDownRunnable;
    private int countDown;

    /**
     * Creates instance of InvadedEvent
     *
     * @param plugin     the instance of main class
     * @param eventName  the name that gets broadcasted
     * @param configName the name that is used in config (events.configName)
     */
    protected InvadedEvent(InvadedLandsEvents plugin, String eventName, String configName) {
        this.star = new ItemStack(Material.NETHER_STAR);
        ItemMeta im = this.star.getItemMeta();
        im.setDisplayName(StringUtils.colour("&c&lLeave Event"));
        this.star.setItemMeta(im);

        this.plugin = plugin;

        this.eventName = eventName;
        this.configName = configName;
        this.valid = true;
        this.running = false;

        this.eventScoreboardManager = new EventScoreboardManager();

        FileConfiguration config = plugin.getConfig();

        ConfigurationSection generalConfig = config.getConfigurationSection("events.general");

        this.allowedCommands = new HashSet<>(generalConfig.getStringList("allowed-commands.value"));
        this.eventConfig = config.getConfigurationSection("events." + configName);
        this.eventWorld = Bukkit.getWorld(generalConfig.getString("event-world.value"));

        this.spawnLoc = ConfigUtils.deserializeFullLocation(generalConfig.getConfigurationSection("spawn"));
        this.specLoc = getEventLocation("spec");
        this.winCommand = generalConfig.getString("win-command.value");

        RegionManager regionManagerVal = null;
        try {
            regionManagerVal = plugin.getWorldGuardPlugin().getRegionManager(eventWorld);
        } catch (NullPointerException npe) {
            plugin.getLogger().severe("Event world does not exist. Could not create region manager.");
            this.valid = false;
        }
        this.regionManager = regionManagerVal;

        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();

        this.playersView = Collections.unmodifiableList(players);
        this.spectatorsView = Collections.unmodifiableList(spectators);

        this.countDown = generalConfig.getInt("host-seconds.value");

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    protected void givePlayerStar(Player player) {
        player.getInventory().setItem(8, star);
    }

    /**
     * Starts the count down runnable
     */
    public void startCountDown(String host) {
        this.eventScoreboardManager.startRefreshing();

        final int MIN_PLAYERS = getEventInteger("min-players");

        this.countDownRunnable = new BukkitRunnable() {
            private final int INITIAL_COUNTDOWN = countDown;
            private int LAST_BROADCAST = countDown;

            @Override
            public void run() {
                if (countDown == INITIAL_COUNTDOWN || LAST_BROADCAST - 15 == countDown || (countDown <= 5 && countDown >= 1)) {
                    TextComponent join = new TextComponent(Message.STARTING_IN.get().replace("{seconds}", String.valueOf(countDown)));
                    join.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Message.CLICK_TO_JOIN.get().replace("{event}", eventName)).create()));
                    join.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"));
                    String hosting = Message.HOSTING_EVENT.get()
                            .replace("{host}", host)
                            .replace("{event}", eventName);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.sendMessage(hosting);
                        player.spigot().sendMessage(join);
                    }

                    LAST_BROADCAST = countDown;
                } else if (countDown <= 0) {
                    if (getPlayersSize() >= MIN_PLAYERS) {
                        start();
                    } else {
                        Bukkit.broadcastMessage(Message.NOT_ENOUGH_PEOPLE.get());
                        forceEndEvent();
                    }
                    this.cancel();
                }
                countDown--;
            }
        };

        this.countDownRunnable.runTaskTimer(plugin, 0, 20);
    }

    /**
     * Attempts to get region through the name in config using event worlds region manager
     * If it was unsuccessful in getting the event region this will set valid to false
     *
     * @param path the path of the region
     * @return the region (null if it could not be found)
     */
    protected final ProtectedRegion getEventRegion(String path) {
        String regionName = getEventString(path);

        if (regionManager == null) {
            plugin.getLogger().severe("Unable to get " + regionName + " region. Region manager was not created.");
            valid = false;
            return null;
        }

        ProtectedRegion region = regionManager.getRegion(regionName);
        if (region == null) {
            plugin.getLogger().severe(eventName + " region '" + regionName + "' does not exist.");
            valid = false;
        }
        return region;
    }

    /**
     * Gets a string from eventConfig
     *
     * @param path the path
     * @return the string
     */
    protected final String getEventString(String path) {
        return eventConfig.getString(path + ".value");
    }

    /**
     * Gets an integer from eventConfig
     *
     * @param path the path
     * @return the integer
     */
    protected final int getEventInteger(String path) {
        return eventConfig.getInt(path + ".value");
    }

    /**
     * Gets a location from eventConfig
     *
     * @param path the path
     * @return the location
     */
    protected final Location getEventLocation(String path) {
        Location loc = ConfigUtils.deserializeEventLocation(eventConfig.getConfigurationSection(path));
        loc.setWorld(eventWorld);
        return loc;
    }

    /**
     * Gets a block vector from eventConfig
     *
     * @param path the path
     * @return the block vector
     */
    protected final BlockVector getEventBlockVector(String path) {
        return ConfigUtils.deserializeBlockVector(eventConfig.getConfigurationSection(path));
    }

    /**
     * Called when the event starts
     * EventManager will call this method after countdown is over
     */
    protected void start() {
        running = true;

        // Give scoreboard to players
        eventScoreboardManager.giveEventScoreboard(players);

        // Give scoreboard to all people who spectated the event before it started
        eventScoreboardManager.giveEventScoreboard(spectators);
    }

    /**
     * Called when the event ends
     */
    protected void over() {
        running = false;
        ending = true;
        eventScoreboardManager.stopRefreshing();
        eventScoreboardManager.giveEventOverSB(players);
        eventScoreboardManager.giveEventOverSB(spectators);

        // Prepare spawn
        spawnLoc.getChunk().load();
    }

    /**
     * Called when event fully ends
     * 5 seconds after over is called
     */
    private void stop() {
        eventScoreboardManager.removeAllScoreboards();
        removeAllParticipants();
        HandlerList.unregisterAll(this);

        // Countdown does not start if event is invalid
        if (!running && valid) {
            this.countDownRunnable.cancel();
        }

        plugin.getServer().getPluginManager().callEvent(new EventStopEvent(this));
    }

    /**
     * Returns a function to create scoreboard for the event
     * Every event has an inner scoreboard class
     *
     * @return the scoreboard creator function
     */
    protected abstract Function<Player, EventScoreboard> getScoreboardFactory();

    /**
     * Gets the events spectate location
     *
     * @return the spectate location
     */
    protected Location getSpecLoc() {
        return specLoc;
    }

    protected World getEventWorld() {
        return eventWorld;
    }

    /**
     * Gets the events display name
     *
     * @return the event name
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Gets the events config name
     *
     * @return the config name
     */
    public String getConfigName() {
        return configName;
    }

    /**
     * If event config values are set properly
     *
     * @return true if valid
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Set if the config values are proper
     *
     * @param valid true if the event is valid
     */
    protected void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * Indicates if the event is running (started)
     *
     * @return true if the event is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Indicates if the event is ending (last 5 seconds)
     *
     * @return true if the event is ending
     */
    public boolean isEnding() {
        return ending;
    }

    /**
     * Get count of all participants in the event
     *
     * @return the size of all the participants
     */
    public int getSize() {
        return getPlayersSize() + getSpectatorsSize();
    }

    /**
     * Get count of all spectators in the event
     *
     * @return the size of spectators
     */
    public int getSpectatorsSize() {
        return spectators.size();
    }

    /**
     * Get count of all players in the event
     *
     * @return the size of players
     */
    public int getPlayersSize() {
        return players.size();
    }

    /**
     * Returns an unmodifiable view of internal players
     *
     * @return the players in the event
     */
    public List<Player> getPlayersView() {
        return playersView;
    }

    /**
     * Returns an unmodifiable view of internal spectators
     *
     * @return the spectators in the event
     */
    public List<Player> getSpectatorsView() {
        return spectatorsView;
    }

    /**
     * Get the current count down time
     *
     * @return the count down time
     */
    public int getCountDown() {
        return countDown;
    }

    /**
     * Shuffles the internal players
     */
    protected void shufflePlayers() {
        Collections.shuffle(players);
    }

    /**
     * Returns an unmodifiable view of internal participants
     *
     * @return the players and spectators in the event
     */
    public List<Player> getParticipants() {
        return new CompositeUnmodifiableList<>(players, spectators);
    }

    /**
     * If the player is participating in the event
     *
     * @param player the player to check
     * @return true if the player is in players or spectators
     */
    public boolean isPlayerParticipating(Player player) {
        return players.contains(player) || spectators.contains(player);
    }

    /**
     * Get the instance of the events scoreboard manager
     *
     * @return the event scoreboard manager
     */
    public EventScoreboardManager getEventScoreboardManager() {
        return eventScoreboardManager;
    }

    /**
     * Adds player to the event
     *
     * This method will:
     * - Broadcast to the server that they joined
     * - Add them to the players
     * - Clear them
     * - Give them the event leave star
     * - If the event has not started it will give them the countdown scoreboard
     *
     * @param player the player that is joining the event
     */
    public void joinEvent(Player player) {
        Bukkit.broadcastMessage(Message.JOINED_EVENT_BROADCAST.get().replace("{player}", player.getName()));

        players.add(player);
        player.teleport(specLoc);
        SpigotUtils.clear(player);
        givePlayerStar(player);

        if (!running) {
            eventScoreboardManager.giveCountdownSB(player);
        }
    }

    /**
     * Removes player from the event
     *
     * This method will:
     * - Broadcast to the participants that they left (if they were a player)
     * - Remove them from players/spectators
     * - Teleport them to the spawn location
     * - Clear them
     * - Remove their scoreboard
     * - Call InvadedEvent#checkPlayerCount()
     *
     * @param player the player that is leaving the event
     */
    public void leaveEvent(Player player) {
        // Remove player after because the left message should be sent to the player who left
        if (players.contains(player)) {
            broadcastEventMessage(Message.LEFT_EVENT_BROADCAST.get().replace("{player}", player.getName()));
            players.remove(player);
        } else {
            spectators.remove(player);
        }

        player.teleport(spawnLoc);
        SpigotUtils.clear(player);
        eventScoreboardManager.removeScoreboard(player);

        if (running) {
            checkPlayerCount();
        }
    }

    /**
     * Adds player to spectators
     *
     * This method will:
     * - Add them to the spectators
     * - Clear them
     * - Give them the event leave star
     * - If the event has started it will give them the event scoreboard
     * - If the event has not started it will give them the countdown scoreboard
     *
     * @param player the player that is joining the spectators
     */
    public void specEvent(Player player) {
        spectators.add(player);
        player.teleport(specLoc);
        SpigotUtils.clear(player);
        givePlayerStar(player);

        if (running) {
            eventScoreboardManager.giveEventScoreboard(player);
        } else {
            eventScoreboardManager.giveCountdownSB(player);
        }
    }

    /**
     * Sends the player the events info
     *
     * @param player the player to send to
     */
    public void sendEventInfo(Player player) {
        for (String message : ListMessage.INFO_MESSAGES.get()) {
            player.sendMessage(message
                    .replace("{event}", eventName)
                    .replace("{players}", String.valueOf(players.size()))
                    .replace("{spectators}", String.valueOf(spectators.size())));
        }
    }

    /**
     * Sends a message to all participants
     *
     * @param message the message to send
     */
    public void broadcastEventMessage(String message) {
        for (Player player : players) {
            player.sendMessage(message);
        }

        for (Player spectator : spectators) {
            spectator.sendMessage(message);
        }
    }

    /**
     * Force ends the event
     */
    public void forceEndEvent() {
        broadcastEventMessage(Message.EVENT_FORCE_ENDED.get().replace("{event}", eventName));
        if (running) {
            over();
        }
        stop();
    }

    /**
     * Will remove the player and add them to the spectators
     *
     * @param player the player that lost
     */
    protected void loseEvent(Player player) {
        players.remove(player);
        specEvent(player);

        if (running) {
            checkPlayerCount();
        }
    }

    /**
     * Will remove the players and add them to the spectators
     *
     * @param players the players that lost
     */
    protected void loseEvent(Iterable<Player> players) {
        for (Player player : players) {
            this.players.remove(player);
            specEvent(player);
        }

        if (running) {
            checkPlayerCount();
        }
    }

    /**
     * Called when a player is removed or leaves event
     * Will call winEvent if playerCount is under 2
     *
     * @return true if the event was won
     */
    protected boolean checkPlayerCount() {
        int playerCount = players.size();
        if (playerCount < 2) {
            winEvent(playerCount == 1 ? players.get(0) : null);
            return true;
        }
        return false;
    }

    protected final void winEvent(Player player) {
        over();
        broadcastWinner(player == null ? "No one" : player.getName());

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            stop();
            if (player != null) {
                dispatchWinCommand(player);
            }
        }, 100);
    }

    protected final void winEvent(EventTeam team) {
        broadcastWinner(team == null ? "No one" : team.getName());

        Iterable<Player> winners = null;

        if (team != null) {
            if (team instanceof SumoTeam) {
                winners = ((SumoTeam) team).getInitialPlayers();
            } else if (team instanceof TDMTeam) {
                TDMTeam tdmTeam = (TDMTeam) team;
                Map<Player, Integer> kills = tdmTeam.getSortedKills();
                List<Player> players = new ArrayList<>(kills.keySet()).subList(0, Math.min(kills.size(), 5));

                Bukkit.broadcastMessage(Message.TDM_TOP_5.get().replace("{amount}", String.valueOf(players.size())));

                List<String> messages = ListMessage.TDM_WINNERS.get();

                for (int i = 0; i < players.size(); i++) {
                    Player player = players.get(i);
                    Bukkit.broadcastMessage(messages.get(i)
                            .replace("{winner}", player.getName())
                            .replace("{kills}", String.valueOf(kills.get(player))));
                }

                winners = players;
            } else {
                winners = team.getPlayers();
            }
        }

        Iterable<Player> finalWinners = winners;

        over();
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            stop();

            if (finalWinners != null) {
                for (Player winner : finalWinners) {
                    dispatchWinCommand(winner);
                }
            }
        }, 100);
    }

    /**
     * Runs the events win command
     *
     * @param player the player to run the command on
     */
    private void dispatchWinCommand(Player player) {
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), winCommand
                .replace("{winner}", player.getName())
                .replace("{winner_uuid}", player.getUniqueId().toString())
                .replace("{winner_name}", player.getDisplayName())
                .replace("{event}", configName)
                .replace("{event_name}", eventName));
    }

    /**
     * Broadcasts the winner to the server
     *
     * @param winner the winner name
     */
    private void broadcastWinner(String winner) {
        for (String message : ListMessage.WIN_MESSAGES.get()) {
            Bukkit.broadcastMessage(message
                    .replace("{winner}", winner)
                    .replace("{event}", eventName));
        }
    }

    /**
     * Removes all participants from the event
     * Empties players and spectators list
     */
    private void removeAllParticipants() {
        for (Iterator<Player> iterator = players.iterator(); iterator.hasNext(); iterator.remove()) {
            Player player = iterator.next();
            SpigotUtils.clear(player);
            player.teleport(spawnLoc);
        }

        for (Iterator<Player> iterator = spectators.iterator(); iterator.hasNext(); iterator.remove()) {
            Player spectator = iterator.next();
            SpigotUtils.clear(spectator);
            spectator.teleport(spawnLoc);
        }
    }

    /**
     * Check if event should be ignored
     *
     * @param player the player that the event is fired on
     * @return true if the event should be ignored
     */
    protected boolean ignoreEvent(Player player) {
        return !running || !players.contains(player);
    }

    // Events

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (isPlayerParticipating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (isPlayerParticipating(player)) {
            leaveEvent(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (isPlayerParticipating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (isPlayerParticipating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteractNetherStar(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (star.equals(event.getItem()) && isPlayerParticipating(player)) {
            leaveEvent(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemStack(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (isPlayerParticipating(player)) {
                ItemStack item = event.getCurrentItem();
                if (item == null || item.getType() == Material.AIR) {
                    return;
                }

                event.setCancelled(true);
                player.sendMessage(Message.CRAFT_IN_EVENT.get());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (isPlayerParticipating(player)) {
            event.setDeathMessage("");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(EventPermission.BYPASS_COMMAND) && isPlayerParticipating(player)) {
            PluginCommand command = plugin.getServer().getPluginCommand(event.getMessage().split("\\s+")[0].replace("/", ""));
            if (command == null || allowedCommands.contains(command.getName())) {
                return;
            }

            for (String alias : command.getAliases()) {
                if (allowedCommands.contains(alias)) {
                    return;
                }
            }

            player.sendMessage(Message.BLOCKED_COMMAND.get());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (isEnding() || isPlayerSpectating(event.getDamager()) || isPlayerSpectating(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    private boolean isPlayerSpectating(Entity entity) {
        if (entity instanceof Player) {
            return spectators.contains(entity) || (!running && players.contains(entity));
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEnderPearl(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL && isPlayerParticipating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLoseHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player && isPlayerParticipating((Player) event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isPlayerParticipating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isPlayerParticipating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onArmourClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player && isPlayerParticipating((Player) event.getWhoClicked()) && event.getSlotType() == InventoryType.SlotType.ARMOR) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && isPlayerParticipating((Player) event.getEntity())) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL ||
                    event.getCause() == EntityDamageEvent.DamageCause.DROWNING ||
                    event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                    event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK ||
                    event.getCause() == EntityDamageEvent.DamageCause.FIRE) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Used when event has not started yet and is counting down
     */
    private final class CountdownSB extends EventScoreboard {
        private final TrackLine playerCountTrack;
        private final TrackLine countDownTrack;

        private CountdownSB(Player player) {
            super("event_starting", player);
            this.playerCountTrack = new TrackLine("pctCountdown", "&ePlayers: ", "&7&6", "", 4);
            Line blank = new Line("bCountdown", "", "&l&9", "", 3);
            this.countDownTrack = new TrackLine("cdtCountdown", "&eStarting in ", "&6", "", 2);
            this.initLines(playerCountTrack, blank, countDownTrack);
        }

        @Override
        protected void refresh() {
            playerCountTrack.setSuffix(String.valueOf(getPlayersSize()));
            countDownTrack.setSuffix(countDown + "s");
        }
    }

    /**
     * Used when the event ended
     */
    private static final class EventEndedSB extends EventScoreboard {
        private EventEndedSB(Player player) {
            super("event_ended", player);
            Line message = new Line("mEventEnded", "&eEvent has ", "ended", "", 2);
            this.initLines(message);
        }

        @Override
        protected void refresh() {
        }
    }

    private final class EventScoreboardManager {
        private final Function<Player, EventScoreboard> scoreboardFactory;
        private final Map<Player, EventScoreboard> scoreboards;
        private final BukkitRunnable refresher;

        /**
         * Creates instance of the event scoreboard manager
         */
        private EventScoreboardManager() {
            this.scoreboardFactory = getScoreboardFactory();
            this.scoreboards = new HashMap<>();
            this.refresher = new BukkitRunnable() {
                @Override
                public void run() {
                    for (EventScoreboard scoreboard : scoreboards.values()) {
                        scoreboard.updateScoreboard();
                    }
                }
            };
        }

        /**
         * Give the player a fresh scoreboard
         *
         * @param player the player to give the scoreboard to
         */
        public void giveNewScoreboard(Player player) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        /**
         * Gives the players a fresh scoreboard
         *
         * @param players the players to give the scoreboard to
         */
        public void giveNewScoreboard(Iterable<Player> players) {
            for (Player player : players) {
                giveNewScoreboard(player);
            }
        }

        /**
         * Gives the player the current events scoreboard
         *
         * @param player the player to give the scoreboard to
         */
        public void giveEventScoreboard(Player player) {
            scoreboards.put(player, scoreboardFactory.apply(player));
        }

        /**
         * Gives the players the current events scoreboard
         *
         * @param players the players to give the scoreboard to
         */
        public void giveEventScoreboard(Iterable<Player> players) {
            for (Player player : players) {
                giveEventScoreboard(player);
            }
        }

        /**
         * Gives the player a countdown scoreboard
         *
         * @param player the player to give the scoreboard to
         */
        public void giveCountdownSB(Player player) {
            scoreboards.put(player, new CountdownSB(player));
        }

        /**
         * Gives the player an event over scoreboard
         *
         * @param player the player to give the scoreboard to
         */
        public void giveEventOverSB(Player player) {
            scoreboards.put(player, new EventEndedSB(player));
        }

        /**
         * Gives the players an event over scoreboard
         *
         * @param players the players to give the scoreboard to
         */
        public void giveEventOverSB(Iterable<Player> players) {
            for (Player player : players) {
                giveEventOverSB(player);
            }
        }

        /**
         * Removes a players scoreboard and gives them a fresh one
         *
         * @param player the player to remove the scoreboard from
         */
        public void removeScoreboard(Player player) {
            giveNewScoreboard(player);
            scoreboards.remove(player);
        }

        /**
         * Removes all players scoreboards and gives them all a fresh one
         */
        public void removeAllScoreboards() {
            for (Iterator<Player> iterator = scoreboards.keySet().iterator(); iterator.hasNext(); iterator.remove()) {
                giveNewScoreboard(iterator.next());
            }
        }

        /**
         * Start refreshing the scoreboards
         */
        public void startRefreshing() {
            refresher.runTaskTimer(plugin, 0, 5);
        }

        /**
         * Stop refreshing the scoreboards
         */
        public void stopRefreshing() {
            refresher.cancel();
        }
    }

    /*
    TODO:
        - this class needs some refactoring it's an absolute disaster
     */
}
