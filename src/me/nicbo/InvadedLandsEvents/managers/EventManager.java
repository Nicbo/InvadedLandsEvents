package me.nicbo.InvadedLandsEvents.managers;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.events.*;
import me.nicbo.InvadedLandsEvents.events.sumo.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public final class EventManager {
    private EventsMain plugin;
    private static String[] eventNames = new String[]{
            "brackets", "koth", "lms",
            "oitc", "redrover", "rod",
            "spleef", "tdm", "tnttag",
            "waterdrop", "woolshuffle",
            "sumo"
        };
    private HashMap<String, InvadedEvent> events;
    private InvadedEvent currentEvent;
    private static boolean eventRunning;

    public EventManager(EventsMain plugin) {
        this.plugin = plugin;
        this.events = new HashMap<>();
        addEventsToMap();
    }
    
    private void addEventsToMap() {
        events.put(eventNames[0], new Brackets(plugin));
        events.put(eventNames[1], new KOTH(plugin));
        events.put(eventNames[2], new LMS(plugin));
        events.put(eventNames[3], new OITC(plugin));
        events.put(eventNames[4], new RedRover(plugin));
        events.put(eventNames[5], new RoD(plugin));
        events.put(eventNames[6], new Spleef(plugin));
        events.put(eventNames[7], new TDM(plugin));
        events.put(eventNames[8], new TNTTag(plugin));
        events.put(eventNames[9], new Waterdrop(plugin));
        events.put(eventNames[10], new WoolShuffle(plugin));
        events.put(eventNames[11], new Sumo1v1(plugin));
        events.put(eventNames[11], new Sumo2v2(plugin));
        events.put(eventNames[11], new Sumo3v3(plugin));
    }

    public EventMessage hostEvent(String name, String host) {
        if (currentEvent != null) {
            return EventMessage.HOST_STARTED;
        } else if (!events.containsKey(name)) {
            return EventMessage.DOES_NOT_EXIST;
        } else if (!events.get(name).isEnabled()) {
            return EventMessage.NOT_ENABLED;
        }
        currentEvent = events.get(name);
        startCountDown(host);
        return null;
    }

    private void startCountDown(String host) {
        currentEvent.init(plugin);
        String name = currentEvent.getName();

        new BukkitRunnable() {
            int time = 16; //for testing put back to 60

            @Override
            public void run() {
                if (time == 60 || time == 45 || time == 30 || time == 15 || time <= 5 && time >= 1) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&l"+ host + " is hosting a " + name + " event!"));
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&lStarting in " + time + " seconds " + "&a&l[Click to Join]"));
                    // Add Click Event text
                } else if (time == 0) {
                    currentEvent.start();
                    this.cancel();
                }
                time--;
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    public EventMessage joinEvent(Player player) {
        if (currentEvent == null) {
            return EventMessage.NONE;
        } else if (currentEvent.isStarted()) {
            return EventMessage.STARTED;
        } else if (currentEvent.containsPlayer(player)) {
            return EventMessage.IN_EVENT;
        }
        currentEvent.joinEvent(player);
        return null;
    }

    public EventMessage leaveEvent(Player player) {
        if (currentEvent == null) {
            return EventMessage.NONE;
        } else if (!currentEvent.containsPlayer(player)) {
            return EventMessage.NOT_IN_EVENT;
        }
        currentEvent.leaveEvent(player);
        return EventMessage.NONE;
    }

    public EventMessage specEvent(Player player) {
        if (currentEvent == null) {
            return EventMessage.NONE;
        } else if (currentEvent.containsPlayer(player)) {
            return EventMessage.IN_EVENT;
        }

        currentEvent.leaveEvent(player);
        return null;
    }

    public InvadedEvent getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(InvadedEvent event) {
        this.currentEvent = event;
    }

    public static boolean isEventRunning() {
        return eventRunning;
    }

    public static void setEventRunning(boolean bool) {
        eventRunning = bool;
    }

    public static String[] getEventNames() {
        return eventNames;   
    }

    public enum EventMessage {
        NONE(ChatColor.RED + "There currently isn't any event active right now."),
        STARTED(ChatColor.RED + "You cannot join the event as it has already started!"),
        HOST_STARTED(ChatColor.RED + "You cannot host an event as one is already in progress."),
        IN_EVENT(ChatColor.RED + "You're already in the event."),
        DOES_NOT_EXIST(ChatColor.RED + "There is no event named " + ChatColor.YELLOW + "{event}" + ChatColor.RED + "."),
        NOT_ENABLED(ChatColor.RED + "That event is not enabled!"),
        NOT_IN_EVENT(ChatColor.RED + "You aren't in an event!"),
        NO_PERMISSION(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command.");

        final String description;

        EventMessage(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

    /*
    TODO:
        - Make descriptions editable based on config (later version)
     */
    }
}
