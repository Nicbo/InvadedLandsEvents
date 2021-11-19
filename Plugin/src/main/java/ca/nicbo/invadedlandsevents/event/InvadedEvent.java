package ca.nicbo.invadedlandsevents.event;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.configuration.ConfigSection;
import ca.nicbo.invadedlandsevents.api.data.PlayerEventData;
import ca.nicbo.invadedlandsevents.api.event.Event;
import ca.nicbo.invadedlandsevents.api.event.EventState;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostCountdownEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostEndEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostForceEndEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStartEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStopEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPreCountdownEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPreEndEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPreForceEndEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPreStartEvent;
import ca.nicbo.invadedlandsevents.api.event.event.EventPreStopEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPostJoinEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPostLeaveEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPostSpectateEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPostWinEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPreJoinEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPreLeaveEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPreSpectateEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPreWinEvent;
import ca.nicbo.invadedlandsevents.api.permission.EventPermission;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.configuration.InvadedConfigHandler;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;
import ca.nicbo.invadedlandsevents.configuration.Message;
import ca.nicbo.invadedlandsevents.event.event.player.EventPlayerDamageByEventPlayerEvent;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboardLine;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboardManager;
import ca.nicbo.invadedlandsevents.task.SyncedTask;
import ca.nicbo.invadedlandsevents.util.CollectionUtils;
import ca.nicbo.invadedlandsevents.util.ItemStackBuilder;
import ca.nicbo.invadedlandsevents.util.SpigotUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
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
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Partial implementation of {@link Event}.
 * <p>
 * The event is expected to manage its own state besides starting the countdown and force ending.
 *
 * @author Nicbo
 */
public abstract class InvadedEvent implements Event, Listener {
    private static final ItemStack LEAVE_EVENT_STAR = new ItemStackBuilder(Material.NETHER_STAR)
            .setName("&c&lLeave Event")
            .build();

    private final InvadedLandsEventsPlugin plugin;

    private final EventType eventType;

    private final String hostName;

    private final List<String> description;

    private final EventScoreboardManager eventScoreboardManager;

    private final ConfigSection eventConfig;

    private final Set<String> allowedCommands;
    private final Location spawn;
    private final Location spec;
    private final List<String> winCommands;

    private final int minPlayers;

    private final List<Player> players;
    private final List<Player> spectators;

    private final EventCountdownTask eventCountdownTask;

    private EventState state;

    protected InvadedEvent(InvadedLandsEventsPlugin plugin, EventType eventType, String hostName, List<String> description) {
        Validate.checkArgumentNotNull(plugin, "plugin");
        Validate.checkArgumentNotNull(eventType, "eventType");
        Validate.checkArgumentNotNull(hostName, "hostName");
        Validate.checkArgumentNotNull(description, "description");

        InvadedConfigHandler configHandler = plugin.getConfigurationManager().getConfigHandler();

        this.plugin = plugin;
        this.eventType = eventType;
        this.hostName = hostName;
        this.description = description;
        this.eventScoreboardManager = new EventScoreboardManager(this::createEventScoreboard, EventStartingScoreboard::new, EventEndingScoreboard::new);

        this.eventConfig = configHandler.getConfigSection(eventType.getConfigName());
        ConfigSection generalConfig = configHandler.getConfigSection("general");

        this.allowedCommands = new HashSet<>(generalConfig.getStringList("allowed-commands"));

        this.spawn = generalConfig.getLocation("spawn");
        this.spec = eventConfig.getLocation("spec");
        this.winCommands = generalConfig.getStringList("win-commands");

        this.players = new ArrayList<>();
        this.spectators = new ArrayList<>();

        this.minPlayers = eventConfig.getInteger("min-players");

        int hostCountdown = generalConfig.getInteger("host-countdown");
        this.eventCountdownTask = new EventCountdownTask(hostCountdown);

        this.state = EventState.WAITING;
    }

    protected abstract EventScoreboard createEventScoreboard(Player player);

    protected InvadedLandsEventsPlugin getPlugin() {
        return plugin;
    }

    public final ConfigSection getEventConfig() {
        return eventConfig;
    }

    @Override
    public int getCountdown() {
        return eventCountdownTask.getCountdown();
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public EventType getEventType() {
        return eventType;
    }

    @Override
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    @Override
    public List<Player> getSpectators() {
        return Collections.unmodifiableList(spectators);
    }

    @Override
    public final void join(Player player) {
        Validate.checkState(isState(EventState.COUNTDOWN), "event's state must be %s to join, current state: %s", EventState.COUNTDOWN, state);
        Validate.checkArgumentNotNull(player, "player");
        Validate.checkArgument(!isPlayerParticipating(player), "the player %s is already participating in the event", player.getName());

        Bukkit.getPluginManager().callEvent(new EventPlayerPreJoinEvent(this, player));

        for (String message : description) {
            player.sendMessage(message);
        }

        Bukkit.broadcastMessage(Message.JOINED_EVENT.get().replace("{player}", player.getName()));

        players.add(player);
        player.teleport(spec);
        SpigotUtils.clear(player);
        givePlayerStar(player);

        eventScoreboardManager.applyStartingScoreboard(player);

        Bukkit.getPluginManager().callEvent(new EventPlayerPostJoinEvent(this, player));
    }

    @Override
    public final void leave(Player player) {
        Validate.checkState(isState(EventState.COUNTDOWN) || isState(EventState.STARTED) || isState(EventState.ENDED),
                "event's state must be %s, %s or %s to leave, current state: %s", EventState.COUNTDOWN, EventState.STARTED, EventState.ENDED, state);
        Validate.checkArgumentNotNull(player, "player");
        Validate.checkArgument(isPlayerParticipating(player), "the player %s is not participating in the event", player.getName());

        Bukkit.getPluginManager().callEvent(new EventPlayerPreLeaveEvent(this, player));

        if (players.remove(player)) {
            broadcastMessage(Message.LEFT_EVENT.get().replace("{player}", player.getName()));
        } else {
            spectators.remove(player);
        }

        player.teleport(spawn);
        SpigotUtils.clear(player);
        eventScoreboardManager.removeScoreboard(player);

        if (isState(EventState.STARTED)) {
            checkPlayerCount();
        }

        Bukkit.getPluginManager().callEvent(new EventPlayerPostLeaveEvent(this, player));
    }

    @Override
    public final void spectate(Player player) {
        Validate.checkState(isState(EventState.COUNTDOWN) || isState(EventState.STARTED),
                "event's state must be %s or %s to spectate, current state: %s", EventState.COUNTDOWN, EventState.STARTED, state);
        Validate.checkArgumentNotNull(player, "player");
        Validate.checkArgument(!isPlayerParticipating(player), "the player %s is already participating in the event", player.getName());

        Bukkit.getPluginManager().callEvent(new EventPlayerPreSpectateEvent(this, player));

        spectators.add(player);
        player.teleport(spec);
        SpigotUtils.clear(player);
        givePlayerStar(player);

        if (isState(EventState.COUNTDOWN)) {
            eventScoreboardManager.applyStartingScoreboard(player);
        } else {
            eventScoreboardManager.applyEventScoreboard(player);
        }

        Bukkit.getPluginManager().callEvent(new EventPlayerPostSpectateEvent(this, player));
    }

    @Override
    public final void forceEnd(boolean silent) {
        Validate.checkState(isState(EventState.COUNTDOWN) || isState(EventState.STARTED),
                "event's state must be %s or %s to force end, current state: %s", EventState.COUNTDOWN, EventState.STARTED, state);

        Bukkit.getPluginManager().callEvent(new EventPreForceEndEvent(this));

        if (!silent) {
            broadcastMessage(Message.EVENT_FORCE_ENDED.get().replace("{event}", getDisplayName()));
        }

        if (isState(EventState.COUNTDOWN)) {
            eventCountdownTask.stop();
        }

        if (isState(EventState.STARTED)) {
            end(new EventEndingContext(null, Collections.emptyList(), true));
        }

        stop(Collections.emptyList()); // No winners

        Bukkit.getPluginManager().callEvent(new EventPostForceEndEvent(this));
    }

    @Override
    public void broadcastMessage(String message) {
        Validate.checkArgumentNotNull(message, "message");
        for (Player player : players) {
            player.sendMessage(message);
        }

        for (Player spectator : spectators) {
            spectator.sendMessage(message);
        }
    }

    @Override
    public EventState getState() {
        return state;
    }

    // Convenience methods
    public boolean isState(EventState state) {
        return this.state == state;
    }

    public boolean isPlayerPlaying(Player player) {
        Validate.checkArgumentNotNull(player, "player");
        return players.contains(player);
    }

    public boolean isPlayerSpectating(Player player) {
        Validate.checkArgumentNotNull(player, "player");
        return spectators.contains(player);
    }

    public boolean isPlayerParticipating(Player player) {
        return isPlayerPlaying(player) || isPlayerSpectating(player);
    }

    public int getPlayersSize() {
        return players.size();
    }

    public int getSpectatorsSize() {
        return spectators.size();
    }

    public String getDisplayName() {
        return eventType.getDisplayName();
    }

    public String getConfigName() {
        return eventType.getConfigName();
    }

    public Location getSpawn() {
        return spawn;
    }

    public Location getSpec() {
        return spec;
    }

    public InvadedEventValidationResult validate() {
        return new InvadedEventValidationResult(true);
    }

    public final void countdown() {
        Validate.checkState(isState(EventState.WAITING),
                "event's state must be %s to be changed to %s, current state: %s", EventState.WAITING, EventState.COUNTDOWN, state);
        Bukkit.getPluginManager().callEvent(new EventPreCountdownEvent(this));

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        this.state = EventState.COUNTDOWN;

        this.eventCountdownTask.start(plugin);

        // Start refreshing the scoreboards
        this.eventScoreboardManager.startRefreshing(plugin);

        Bukkit.getPluginManager().callEvent(new EventPostCountdownEvent(this));
    }

    private void start() {
        Validate.checkState(isState(EventState.COUNTDOWN),
                "event's state must be %s to be changed to %s, current state: %s", EventState.COUNTDOWN, EventState.STARTED, state);
        Bukkit.getPluginManager().callEvent(new EventPreStartEvent(this));
        this.state = EventState.STARTED;

        // Give scoreboard to participants
        for (Player player : players) {
            eventScoreboardManager.applyEventScoreboard(player);
        }

        for (Player spectator : spectators) {
            eventScoreboardManager.applyEventScoreboard(spectator);
        }

        Bukkit.getPluginManager().callEvent(new EventPostStartEvent(this));
    }

    protected final void end(EventEndingContext context) {
        Validate.checkState(isState(EventState.STARTED),
                "event's state must be %s to be changed to %s, current state: %s", EventState.STARTED, EventState.ENDED, state);
        Bukkit.getPluginManager().callEvent(new EventPreEndEvent(this));
        this.state = EventState.ENDED;

        if (!context.forced) {
            // Broadcast the winner
            for (String message : ListMessage.WIN_MESSAGES.get()) {
                Bukkit.broadcastMessage(message
                        .replace("{winner}", context.name)
                        .replace("{event}", getDisplayName()));
            }

            // Schedule the stop
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> stop(context.winners), 100);
        }

        eventScoreboardManager.stopRefreshing();

        for (Player player : players) {
            eventScoreboardManager.applyEndingScoreboard(player);
        }

        for (Player spectator : spectators) {
            eventScoreboardManager.applyEndingScoreboard(spectator);
        }

        Bukkit.getPluginManager().callEvent(new EventPostEndEvent(this));
    }

    private void stop(Collection<Player> winners) {
        Validate.checkState(isState(EventState.COUNTDOWN) || isState(EventState.ENDED),
                "event's state must be %s or %s to be changed to %s, current state: %s", EventState.COUNTDOWN, EventState.ENDED, EventState.STOPPED, state);
        Bukkit.getPluginManager().callEvent(new EventPreStopEvent(this));
        this.state = EventState.STOPPED;

        // Clear scoreboards
        eventScoreboardManager.removeAllScoreboards();

        // Remove players
        for (Iterator<Player> iterator = players.iterator(); iterator.hasNext(); iterator.remove()) {
            Player player = iterator.next();
            player.closeInventory();
            SpigotUtils.clear(player);
            player.teleport(spawn);
        }

        // Remove spectators
        for (Iterator<Player> iterator = spectators.iterator(); iterator.hasNext(); iterator.remove()) {
            Player spectator = iterator.next();
            spectator.closeInventory();
            SpigotUtils.clear(spectator);
            spectator.teleport(spawn);
        }

        for (Player winner : winners) {
            Bukkit.getPluginManager().callEvent(new EventPlayerPreWinEvent(this, winner));

            // Dispatch win commands
            for (String command : winCommands) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command
                        .replace("{winner}", winner.getName())
                        .replace("{winner_uuid}", winner.getUniqueId().toString())
                        .replace("{winner_name}", winner.getDisplayName())
                        .replace("{event}", getConfigName())
                        .replace("{event_name}", getDisplayName()));
            }

            // Update wins
            PlayerEventData data = plugin.getPlayerDataManager().getPlayerData(winner.getUniqueId()).getEventData(eventType);
            data.setWins(data.getWins() + 1);

            Bukkit.getPluginManager().callEvent(new EventPlayerPostWinEvent(this, winner));
        }

        HandlerList.unregisterAll(this);

        Bukkit.getPluginManager().callEvent(new EventPostStopEvent(this));
    }

    protected void lose(Player player) {
        players.remove(player);
        spectate(player);
        checkPlayerCount();
    }

    protected void lose(Iterable<Player> players) {
        for (Player player : players) {
            this.players.remove(player);
            spectate(player);
        }

        checkPlayerCount();
    }

    protected void checkPlayerCount() {
        int playerCount = getPlayersSize();
        if (playerCount == 1) {
            end(new EventEndingContext(players.get(0)));
        } else if (playerCount == 0) {
            end(new EventEndingContext());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        if (isPlayerParticipating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (isPlayerParticipating(player)) {
            leave(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isPlayerParticipating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @SuppressWarnings("deprecation") // EntityPickupItemEvent not available in 1.8
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (isPlayerParticipating(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (LEAVE_EVENT_STAR.equals(event.getItem()) && isPlayerParticipating(player)) {
            leave(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (isPlayerParticipating(player)) {
                ItemStack item = event.getCurrentItem();
                if (SpigotUtils.isEmpty(item)) {
                    return;
                }

                event.setCancelled(true);
                player.sendMessage(Message.CRAFT_IN_EVENT.get());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission(EventPermission.COMMAND_BYPASS) && isPlayerParticipating(player)) {
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
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
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlotType() == InventoryType.SlotType.ARMOR && event.getWhoClicked() instanceof Player && isPlayerParticipating((Player) event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEventPlayerDamageByEventPlayer(EventPlayerDamageByEventPlayerEvent event) {
        if (isEventPlayerTechnicallySpectating(event.getPlayer()) || isEventPlayerTechnicallySpectating(event.getDamager())) {
            event.setCancelled(true);
        }
    }

    private boolean isEventPlayerTechnicallySpectating(Player player) {
        return isPlayerSpectating(player) || (isPlayerParticipating(player) && !isState(EventState.STARTED));
    }

    // Block any damage that isn't involving 2 event players.
    // This way we can exclusively listen to EventPlayerDamageByEventPlayer for damage.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        EntityDamageByEntityEvent originalEvent = event instanceof EntityDamageByEntityEvent ? (EntityDamageByEntityEvent) event : null;
        Player player = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;
        Player damager = originalEvent != null ? SpigotUtils.getPlayerFromDamager(originalEvent.getDamager()) : null;

        boolean playerInEvent = player != null && isPlayerParticipating(player);
        boolean damagerInEvent = damager != null && isPlayerParticipating(damager);
        if (playerInEvent && damagerInEvent) { // Both players are in the event, call it
            EventPlayerDamageByEventPlayerEvent e = new EventPlayerDamageByEventPlayerEvent(this, player, damager, originalEvent);
            Bukkit.getPluginManager().callEvent(e);
            if (!e.isApplyingKnockback()) {
                // Apply the player's previous velocity 1 tick later to negate knockback
                final Vector velocity = player.getVelocity();
                plugin.getServer().getScheduler().runTask(plugin, () -> player.setVelocity(velocity));
            }
        } else if (playerInEvent || damagerInEvent) { // Only one player is in the event, cancel damage (XOR)
            event.setCancelled(true);
        }
    }

    protected static void givePlayerStar(Player player) {
        player.getInventory().setItem(8, LEAVE_EVENT_STAR);
    }

    private class EventCountdownTask extends SyncedTask {
        private static final long DELAY = 0;
        private static final long PERIOD = 20;

        private final String hostMessage;
        private final TextComponent joinMessage;
        private final int initialCountdown;

        private int countdown;

        public EventCountdownTask(int initialCountdown) {
            super(DELAY, PERIOD);
            this.hostMessage = Message.HOSTING_EVENT.get()
                    .replace("{player}", hostName)
                    .replace("{event}", getDisplayName());

            this.joinMessage = new TextComponent();

            // noinspection deprecation (compatibility)
            joinMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Message.CLICK_TO_JOIN.get().replace("{event}", getDisplayName())).create()));
            joinMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"));

            this.initialCountdown = initialCountdown;
            this.countdown = initialCountdown;
        }

        @Override
        protected void onStart() {
            // Give some feedback to console
            plugin.getServer().getConsoleSender().sendMessage(hostMessage);
        }

        @Override
        protected void run() {
            if (countdown == 0) {
                if (getPlayersSize() >= minPlayers) {
                    InvadedEvent.this.start();
                    this.stop();
                } else {
                    Bukkit.broadcastMessage(Message.NOT_ENOUGH_PEOPLE.get());
                    forceEnd(true); // force end calls this.stop()
                }

                return;
            }

            if (countdown % 15 == 0 || (countdown <= 5 && countdown >= 1) || countdown == initialCountdown) {
                joinMessage.setText(Message.STARTING_IN.get().replace("{seconds}", String.valueOf(countdown)));
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    player.sendMessage(hostMessage);
                    player.spigot().sendMessage(joinMessage);
                }
            }

            countdown--;
        }

        public int getCountdown() {
            return countdown;
        }
    }

    private class EventStartingScoreboard extends EventScoreboard {
        private final EventScoreboardLine playerCountLine;
        private final EventScoreboardLine countdownLine;

        public EventStartingScoreboard(Player player) {
            super(player, Message.TITLE_EVENT_STARTING.get(), "event_starting");
            this.playerCountLine = new EventScoreboardLine(4);
            EventScoreboardLine blankLine = new EventScoreboardLine(3);
            this.countdownLine = new EventScoreboardLine(2);
            this.setLines(playerCountLine, blankLine, countdownLine);
        }

        @Override
        protected void refresh() {
            playerCountLine.setText("&ePlayers: &6" + getPlayersSize());

            // Add one because broadcast happens before decrement, this will sync the scoreboard and the chat
            countdownLine.setText("&eStarting in &6" + (getCountdown() + 1) + "s");
        }
    }

    protected static class EventEndingContext {
        private final String name;
        private final Collection<Player> winners;
        private final boolean forced;

        // No winner
        public EventEndingContext() {
            this("No one", Collections.emptyList(), false);
        }

        // One winner
        public EventEndingContext(Player winner) {
            this(winner.getName(), Collections.singletonList(winner), false);
        }

        // Team of players
        public EventEndingContext(InvadedEventTeam team) {
            this(team.getName(), CollectionUtils.toList(team), false);
        }

        public EventEndingContext(String name, Collection<Player> winners) {
            this(name, winners, false);
        }

        public EventEndingContext(String name, Collection<Player> winners, boolean forced) {
            this.name = name;
            this.winners = winners;
            this.forced = forced;
        }
    }

    // does not need access to outer
    private static class EventEndingScoreboard extends EventScoreboard {
        public EventEndingScoreboard(Player player) {
            super(player, Message.TITLE_EVENT_ENDING.get(), "event_ending");
            EventScoreboardLine messageLine = new EventScoreboardLine(2, "&eEvent has ended");
            this.setLines(messageLine);
        }

        @Override
        protected void refresh() {
            // Not needed
        }
    }
}
