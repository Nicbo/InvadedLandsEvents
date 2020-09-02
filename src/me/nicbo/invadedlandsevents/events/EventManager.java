package me.nicbo.invadedlandsevents.events;

import com.google.common.collect.ImmutableMap;
import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.data.PlayerData;
import me.nicbo.invadedlandsevents.data.PlayerDataManager;
import me.nicbo.invadedlandsevents.event.EventStopEvent;
import me.nicbo.invadedlandsevents.events.type.InvadedEvent;
import me.nicbo.invadedlandsevents.events.type.impl.*;
import me.nicbo.invadedlandsevents.events.type.impl.sumo.Sumo1v1;
import me.nicbo.invadedlandsevents.events.type.impl.sumo.Sumo2v2;
import me.nicbo.invadedlandsevents.events.type.impl.sumo.Sumo3v3;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.permission.EventPermission;
import me.nicbo.invadedlandsevents.util.SpigotUtils;
import me.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Event manager handles all events and the countdown
 *
 * @author Nicbo
 * @author StarZorrow
 */

public final class EventManager implements Listener {
    private static final Map<String, Function<InvadedLandsEvents, InvadedEvent>> EVENTS;

    private final InvadedLandsEvents plugin;
    private InvadedEvent currentEvent;
    private final PlayerDataManager playerDataManager;

    static {
        EVENTS = ImmutableMap.<String, Function<InvadedLandsEvents, InvadedEvent>>
                builder()
                .put("brackets", Brackets::new)
                .put("koth", KOTH::new)
                .put("lms", LMS::new)
                .put("oitc", OITC::new)
                .put("redrover", RedRover::new)
                .put("rod", RoD::new)
                .put("spleef", Spleef::new)
                .put("tdm", TDM::new)
                .put("tnttag", TNTTag::new)
                .put("waterdrop", Waterdrop::new)
                .put("woolshuffle", WoolShuffle::new)
                .put("sumo1v1", Sumo1v1::new)
                .put("sumo2v2", Sumo2v2::new)
                .put("sumo3v3", Sumo3v3::new)
                .build();
    }

    public EventManager(InvadedLandsEvents plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Attempts to host event
     *
     * @param player the player that is trying to host the event
     * @param event  the event name
     * @return the message if the event could not be hosted
     */
    public String hostEvent(Player player, String event) {
        if (!player.hasPermission(EventPermission.HOST_EVENT) || !player.hasPermission(EventPermission.HOST_PREFIX + event)) {
            return Message.NO_PERMISSION.get();
        } else if (!EVENTS.containsKey(event)) {
            return Message.DOES_NOT_EXIST.get().replace("{event}", event);
        } else if (!isEventEnabled(event)) {
            return Message.EVENT_DISABLED.get();
        } else if (isEventRunning()) {
            return Message.HOST_ALREADY_STARTED.get();
        }

        // Cooldown check
        PlayerData playerData = playerDataManager.getData(player.getUniqueId());

        long secondsLeft = playerData.getSecondsUntilHost(event);
        boolean canBypass = player.hasPermission(EventPermission.BYPASS_COOLDOWN);
        if (secondsLeft > 0) {
            if (canBypass) {
                playerData.clearTimestamps();
            } else {
                return Message.HOST_COOLDOWN.get().replace("{time}", StringUtils.formatSeconds(secondsLeft));
            }
        }

        this.currentEvent = EVENTS.get(event).apply(plugin);
        if (currentEvent.isValid()) {
            currentEvent.startCountDown(player.getName());
            if (!canBypass) {
                playerData.addTimestamp(event);
            }
        } else {
            currentEvent.forceEndEvent();
            return Message.INVALID_EVENT.get();
        }
        return null;
    }

    /**
     * Adds player to current events players
     *
     * @param player the player that is trying to join
     * @return the message if the player could not join the event
     */
    public String joinEvent(Player player) {
        if (!player.hasPermission(EventPermission.JOIN_EVENT)) {
            return Message.NO_PERMISSION.get();
        } else if (!isEventRunning()) {
            return Message.EVENT_NOT_RUNNING.get();
        } else if (currentEvent.isPlayerParticipating(player)) {
            return Message.ALREADY_IN_EVENT.get();
        } else if (currentEvent.isRunning()) {
            return Message.JOIN_ALREADY_STARTED.get();
        } else if (currentEvent.isEnding()) {
            return Message.EVENT_ENDING.get();
        } else if (!SpigotUtils.isInventoryEmpty(player)) {
            return Message.INVENTORY_NOT_EMPTY.get();
        } else if (player.isDead()) {
            return Message.PLAYER_DEAD.get();
        }
        currentEvent.joinEvent(player);
        return null;
    }

    /**
     * Removes player from the current event
     *
     * @param player the player trying to leave
     * @return the message if the player could not leave the event
     */
    public String leaveEvent(Player player) {
        if (!player.hasPermission(EventPermission.LEAVE_EVENT)) {
            return Message.NO_PERMISSION.get();
        } else if (!isEventRunning()) {
            return Message.EVENT_NOT_RUNNING.get();
        } else if (!currentEvent.isPlayerParticipating(player)) {
            return Message.NOT_IN_EVENT.get();
        }
        currentEvent.leaveEvent(player);
        return null;
    }

    /**
     * Adds player to current events spectators
     *
     * @param player the player trying to spectate
     * @return the message if the player could not spectate the event
     */
    public String specEvent(Player player) {
        if (!player.hasPermission(EventPermission.SPECTATE_EVEMT)) {
            return Message.NO_PERMISSION.get();
        } else if (!isEventRunning()) {
            return Message.EVENT_NOT_RUNNING.get();
        } else if (currentEvent.isPlayerParticipating(player)) {
            return Message.ALREADY_IN_EVENT.get();
        } else if (currentEvent.isEnding()) {
            return Message.EVENT_ENDING.get();
        } else if (!SpigotUtils.isInventoryEmpty(player)) {
            return Message.INVENTORY_NOT_EMPTY.get();
        } else if (player.isDead()) {
            return Message.PLAYER_DEAD.get();
        }
        currentEvent.specEvent(player);
        return Message.SPECTATE_EVENT.get().replace("{event}", currentEvent.getEventName());
    }

    /**
     * Force end the current event
     *
     * @param sender the sender that is trying to force end the event
     * @return the message if the event could not be force ended
     */
    public String endEvent(CommandSender sender) {
        if (!sender.hasPermission(EventPermission.FORCEEND_EVENT)) {
            return Message.NO_PERMISSION.get();
        } else if (!isEventRunning()) {
            return Message.EVENT_NOT_RUNNING.get();
        } else if (currentEvent.isEnding()) {
            return Message.EVENT_ENDING.get();
        }
        currentEvent.forceEndEvent();
        return Message.FORCEEND_EVENT.get();
    }

    /**
     * Send event info to the player
     *
     * @param player the player to send the info to
     * @return the message if the info could not be sent
     */
    public String eventInfo(Player player) {
        if (!player.hasPermission(EventPermission.INFO_EVENT)) {
            return Message.NO_PERMISSION.get();
        } else if (!isEventRunning()) {
            return Message.EVENT_NOT_RUNNING.get();
        }
        currentEvent.sendEventInfo(player);
        return null;
    }

    /**
     * Check if an event is running
     *
     * @return true if an event is running
     */
    public boolean isEventRunning() {
        return currentEvent != null;
    }

    /**
     * Get the current event
     *
     * @return the current event
     */
    public InvadedEvent getCurrentEvent() {
        return currentEvent;
    }

    /**
     * Check if event is enabled
     *
     * @param event the event config name
     * @return true if the event is enabled
     */
    public boolean isEventEnabled(String event) {
        return plugin.getConfig().getBoolean("events." + event + ".enabled.value");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventStop(EventStopEvent event) {
        if (event.getEvent().equals(currentEvent)) {
            this.currentEvent = null;
        }
    }

    /**
     * Check if the event is an event
     *
     * @param event the event config name
     * @return true if the event is an existing event
     */
    public static boolean isEvent(String event) {
        return EVENTS.containsKey(event);
    }

    /**
     * Gets all the event config names
     *
     * @return the event config names
     */
    public static Set<String> getEventNames() {
        return EVENTS.keySet();
    }
}
