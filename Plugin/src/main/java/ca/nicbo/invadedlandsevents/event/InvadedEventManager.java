package ca.nicbo.invadedlandsevents.event;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.data.PlayerData;
import ca.nicbo.invadedlandsevents.api.data.PlayerEventData;
import ca.nicbo.invadedlandsevents.api.event.Event;
import ca.nicbo.invadedlandsevents.api.event.EventManager;
import ca.nicbo.invadedlandsevents.api.event.EventState;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStopEvent;
import ca.nicbo.invadedlandsevents.api.permission.EventPermission;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.configuration.InvadedConfigHandler;
import ca.nicbo.invadedlandsevents.configuration.InvadedConfigurationManager;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;
import ca.nicbo.invadedlandsevents.configuration.Message;
import ca.nicbo.invadedlandsevents.data.InvadedPlayerDataManager;
import ca.nicbo.invadedlandsevents.event.duel.brackets.Brackets1v1;
import ca.nicbo.invadedlandsevents.event.duel.brackets.Brackets2v2;
import ca.nicbo.invadedlandsevents.event.duel.brackets.Brackets3v3;
import ca.nicbo.invadedlandsevents.event.duel.sumo.Sumo1v1;
import ca.nicbo.invadedlandsevents.event.duel.sumo.Sumo2v2;
import ca.nicbo.invadedlandsevents.event.duel.sumo.Sumo3v3;
import ca.nicbo.invadedlandsevents.event.misc.TeamDeathmatch;
import ca.nicbo.invadedlandsevents.event.round.Redrover;
import ca.nicbo.invadedlandsevents.event.round.TNTTag;
import ca.nicbo.invadedlandsevents.event.round.Waterdrop;
import ca.nicbo.invadedlandsevents.event.round.WoolShuffle;
import ca.nicbo.invadedlandsevents.event.timer.KingOfTheHill;
import ca.nicbo.invadedlandsevents.event.timer.LastManStanding;
import ca.nicbo.invadedlandsevents.event.timer.OneInTheChamber;
import ca.nicbo.invadedlandsevents.event.timer.RaceOfDeath;
import ca.nicbo.invadedlandsevents.event.timer.Spleef;
import ca.nicbo.invadedlandsevents.util.SpigotUtils;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * Implementation of {@link EventManager}.
 *
 * @author Nicbo
 * @author StarZorrow
 */
public class InvadedEventManager implements EventManager, Listener {
    private static final Map<EventType, BiFunction<InvadedLandsEventsPlugin, String, InvadedEvent>> EVENT_MAP;

    private final InvadedLandsEventsPlugin plugin;
    private final InvadedConfigHandler configHandler;
    private final InvadedPlayerDataManager playerDataManager;
    private final Map<UUID, Long> timestampMap;
    private InvadedEvent currentEvent;

    static {
        Map<EventType, BiFunction<InvadedLandsEventsPlugin, String, InvadedEvent>> eventMap = new EnumMap<>(EventType.class);
        eventMap.put(EventType.BRACKETS_1V1, Brackets1v1::new);
        eventMap.put(EventType.BRACKETS_2V2, Brackets2v2::new);
        eventMap.put(EventType.BRACKETS_3V3, Brackets3v3::new);
        eventMap.put(EventType.KING_OF_THE_HILL, KingOfTheHill::new);
        eventMap.put(EventType.LAST_MAN_STANDING, LastManStanding::new);
        eventMap.put(EventType.ONE_IN_THE_CHAMBER, OneInTheChamber::new);
        eventMap.put(EventType.REDROVER, Redrover::new);
        eventMap.put(EventType.RACE_OF_DEATH, RaceOfDeath::new);
        eventMap.put(EventType.SPLEEF, Spleef::new);
        eventMap.put(EventType.SUMO_1V1, Sumo1v1::new);
        eventMap.put(EventType.SUMO_2V2, Sumo2v2::new);
        eventMap.put(EventType.SUMO_3V3, Sumo3v3::new);
        eventMap.put(EventType.TEAM_DEATHMATCH, TeamDeathmatch::new);
        eventMap.put(EventType.TNT_TAG, TNTTag::new);
        eventMap.put(EventType.WATERDROP, Waterdrop::new);
        eventMap.put(EventType.WOOL_SHUFFLE, WoolShuffle::new);
        EVENT_MAP = Collections.unmodifiableMap(eventMap);
    }

    public InvadedEventManager(InvadedLandsEventsPlugin plugin, InvadedConfigurationManager configManager, InvadedPlayerDataManager playerDataManager) {
        Validate.checkArgumentNotNull(plugin, "plugin");
        Validate.checkArgumentNotNull(configManager, "configManager");
        Validate.checkArgumentNotNull(playerDataManager, "playerDataManager");
        this.plugin = plugin;
        this.configHandler = configManager.getConfigHandler();
        this.playerDataManager = playerDataManager;
        this.timestampMap = new HashMap<>();
    }

    @Override
    public boolean hostEvent(EventType eventType, CommandSender sender) {
        Validate.checkArgumentNotNull(eventType, "eventType");
        Validate.checkArgumentNotNull(sender, "sender");

        if (!sender.hasPermission(EventPermission.HOST) || !sender.hasPermission(eventType.getPermission())) {
            sender.sendMessage(Message.NO_PERMISSION.get());
        } else if (!EVENT_MAP.containsKey(eventType)) {
            sender.sendMessage(Message.DOES_NOT_EXIST.get().replace("{event}", eventType.getConfigName()));
        } else if (!isEventEnabled(eventType)) {
            sender.sendMessage(Message.EVENT_DISABLED.get());
        } else if (isEventActive()) {
            sender.sendMessage(currentEvent.isState(EventState.ENDED) ? Message.EVENT_ENDING.get() : Message.HOST_ALREADY_STARTED.get());
        } else {
            // Everything except for player's can bypass, we only track player data
            Player player = sender instanceof Player ? (Player) sender : null;
            PlayerEventData playerEventData = null;

            if (player != null) {
                UUID uuid = player.getUniqueId();
                playerEventData = playerDataManager.getPlayerData(uuid).getEventData(eventType);

                // They can't bypass, check if they can host
                if (!player.hasPermission(EventPermission.HOST_COOLDOWN_BYPASS)) {
                    long secondsLeft = playerDataManager.getSecondsUntilHost(uuid, eventType);
                    if (secondsLeft > 0) {
                        sender.sendMessage(Message.HOST_COOLDOWN.get().replace("{time}", StringUtils.formatSeconds(secondsLeft)));
                        return false;
                    }
                }
            }

            this.currentEvent = EVENT_MAP.get(eventType).apply(plugin, sender.getName());

            InvadedEventValidationResult validationResult = currentEvent.validate();
            if (validationResult.isValid()) {
                currentEvent.countdown();
                if (playerEventData != null) {
                    playerEventData.setHostTimestamp(System.currentTimeMillis());
                }
                return true;
            }

            sender.sendMessage(ChatColor.RED + validationResult.getMessage());
            this.currentEvent = null;
        }

        return false;
    }

    @Override
    public boolean joinCurrentEvent(Player player) {
        Validate.checkArgumentNotNull(player, "player");

        if (!player.hasPermission(EventPermission.JOIN)) {
            player.sendMessage(Message.NO_PERMISSION.get());
        } else if (!isEventActive()) {
            player.sendMessage(Message.EVENT_NOT_RUNNING.get());
        } else if (currentEvent.isPlayerParticipating(player)) {
            player.sendMessage(Message.ALREADY_IN_EVENT.get());
        } else if (currentEvent.isState(EventState.STARTED)) {
            player.sendMessage(Message.JOIN_ALREADY_STARTED.get());
        } else if (currentEvent.isState(EventState.ENDED)) {
            player.sendMessage(Message.EVENT_ENDING.get());
        } else if (isForcingEmptyInventory() && !SpigotUtils.isInventoryEmpty(player)) {
            player.sendMessage(Message.INVENTORY_NOT_EMPTY.get());
        } else if (player.isDead()) {
            player.sendMessage(Message.PLAYER_DEAD.get());
        } else if (isPlayerOnJoinCooldown(player)) {
            player.sendMessage(Message.JOIN_COOLDOWN.get());
        } else {
            timestampMap.put(player.getUniqueId(), System.currentTimeMillis());
            currentEvent.join(player);
            return true;
        }

        return false;
    }

    @Override
    public boolean leaveCurrentEvent(Player player) {
        Validate.checkArgumentNotNull(player, "player");

        if (!player.hasPermission(EventPermission.LEAVE)) {
            player.sendMessage(Message.NO_PERMISSION.get());
        } else if (!isEventActive()) {
            player.sendMessage(Message.EVENT_NOT_RUNNING.get());
        } else if (!currentEvent.isPlayerParticipating(player)) {
            player.sendMessage(Message.NOT_IN_EVENT.get());
        } else {
            currentEvent.leave(player);
            return true;
        }

        return false;
    }

    @Override
    public boolean spectateCurrentEvent(Player player) {
        Validate.checkArgumentNotNull(player, "player");

        if (!player.hasPermission(EventPermission.SPECTATE)) {
            player.sendMessage(Message.NO_PERMISSION.get());
        } else if (!isEventActive()) {
            player.sendMessage(Message.EVENT_NOT_RUNNING.get());
        } else if (currentEvent.isPlayerParticipating(player)) {
            player.sendMessage(Message.ALREADY_IN_EVENT.get());
        } else if (currentEvent.isState(EventState.ENDED)) {
            player.sendMessage(Message.EVENT_ENDING.get());
        } else if (isForcingEmptyInventory() && !SpigotUtils.isInventoryEmpty(player)) {
            player.sendMessage(Message.INVENTORY_NOT_EMPTY.get());
        } else if (player.isDead()) {
            player.sendMessage(Message.PLAYER_DEAD.get());
        } else {
            player.sendMessage(Message.SPECTATE_EVENT.get().replace("{event}", currentEvent.getDisplayName()));
            currentEvent.spectate(player);
            return true;
        }

        return false;
    }

    @Override
    public boolean forceEndCurrentEvent(CommandSender sender) {
        Validate.checkArgumentNotNull(sender, "sender");

        if (!sender.hasPermission(EventPermission.FORCE_END)) {
            sender.sendMessage(Message.NO_PERMISSION.get());
        } else if (!isEventActive()) {
            sender.sendMessage(Message.EVENT_NOT_RUNNING.get());
        } else if (currentEvent.isState(EventState.ENDED)) {
            sender.sendMessage(Message.EVENT_ENDING.get());
        } else {
            currentEvent.forceEnd(false);
            sender.sendMessage(Message.FORCEEND_EVENT.get());
            return true;
        }

        return false;
    }

    @Override
    public boolean sendCurrentEventInfo(CommandSender sender) {
        Validate.checkArgumentNotNull(sender, "sender");

        if (!sender.hasPermission(EventPermission.INFO)) {
            sender.sendMessage(Message.NO_PERMISSION.get());
        } else if (!isEventActive()) {
            sender.sendMessage(Message.EVENT_NOT_RUNNING.get());
        } else {
            for (String message : ListMessage.INFO_MESSAGES.get()) {
                sender.sendMessage(message
                        .replace("{event}", currentEvent.getEventType().getDisplayName())
                        .replace("{players}", String.valueOf(currentEvent.getPlayers().size()))
                        .replace("{spectators}", String.valueOf(currentEvent.getSpectators().size())));
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean sendEventStats(CommandSender sender, Player player) {
        Validate.checkArgumentNotNull(sender, "sender");
        Validate.checkArgumentNotNull(player, "target");

        if (!sender.hasPermission(EventPermission.STATS) || (!sender.hasPermission(EventPermission.STATS_OTHER) && !sender.equals(player))) {
            sender.sendMessage(Message.NO_PERMISSION.get());
            return false;
        }

        PlayerData playerData = playerDataManager.getPlayerData(player.getUniqueId());
        for (String message : ListMessage.STATS_MESSAGES.get()) {
            message = message.replace("{player}", player.getName());
            for (Map.Entry<EventType, PlayerEventData> entry : playerData.getEventDataMap().entrySet()) {
                final String placeholder = "{" + entry.getKey().getConfigName() + "_wins}";
                final int wins = entry.getValue().getWins();
                message = message.replace(placeholder, String.valueOf(wins));
            }
            sender.sendMessage(message);
        }

        return true;
    }

    @Override
    public Event getCurrentEvent() {
        return currentEvent;
    }

    @Override
    public boolean isEventEnabled(EventType eventType) {
        Validate.checkArgumentNotNull(eventType, "eventType");
        return configHandler.getConfigSection(eventType.getConfigName()).getBoolean("enabled");
    }

    private boolean isEventActive() {
        return currentEvent != null;
    }

    private boolean isForcingEmptyInventory() {
        return configHandler.getConfigSection("general").getBoolean("force-empty-inventory");
    }

    private boolean isPlayerOnJoinCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        Long timestamp = timestampMap.get(uuid);

        if (timestamp == null) {
            return false;
        }

        int joinCooldown = configHandler.getConfigSection("general").getInteger("join-cooldown");
        if ((System.currentTimeMillis() - timestamp) / 1000 >= joinCooldown) {
            timestampMap.remove(uuid);
            return false;
        }

        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventStop(EventPostStopEvent event) {
        this.timestampMap.clear(); // We do it here because event start is not always called
        this.currentEvent = null;
    }
}
