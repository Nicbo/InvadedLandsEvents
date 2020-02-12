package me.nicbo.InvadedLandsEvents.managers;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.events.*;
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
            "waterdrop", "woolshuffle"
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
//        events.put(eventNames[0], new Brackets(plugin));
//        events.put(eventNames[1], new KOTH(plugin));
//        events.put(eventNames[2], new LMS(plugin));
//        events.put(eventNames[3], new OITC(plugin));
//        events.put(eventNames[4], new RedRover(plugin));
//        events.put(eventNames[5], new RoD(plugin));
        events.put(eventNames[6], new Spleef(plugin));
//        events.put(eventNames[7], new TDM(plugin));
//        events.put(eventNames[8], new TNTTag(plugin));
//        events.put(eventNames[9], new Waterdrop(plugin));
//        events.put(eventNames[10], new WoolShuffle(plugin));
    }

    public boolean hostEvent(String name, String host) {
        if (events.containsKey(name) && events.get(name).isEnabled()) {
            currentEvent = events.get(name);
            startCountDown(host);
            return true;
        }
        return false;
    }

    private void startCountDown(String host) {
        eventRunning = true;
        currentEvent.init(plugin);
        String name = currentEvent.getName();

        new BukkitRunnable() {
            int time = 16; //for testing put back to 60

            @Override
            public void run() {
                if (time == 60 || time == 45 || time == 30 || time == 15 || time <= 5 && time >= 1) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&l"+ host + " is hosting a " + name + " event!"));
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&lStarting in " + time + " seconds " + "&a&l[Click to Join]"));
                } else if (time == 0) {
                    currentEvent.start();
                    this.cancel();
                }
                time--;
            }
        }.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    public EventStatus joinEvent(Player player) {
        if (currentEvent == null) {
            return EventStatus.NONE;
        } else if (currentEvent.isStarted()) {
            return EventStatus.STARTED;
        } else if (currentEvent.containsPlayer(player)) {
            return EventStatus.IN_EVENT;
        }
        currentEvent.joinEvent(player);
        return EventStatus.JOIN;
    }

    public EventStatus specEvent(Player player) {
        return EventStatus.NONE; // same as above
    }

    public EventStatus leaveEvent(Player player) {
        return EventStatus.NONE; // ||
    }

    public InvadedEvent getCurrentEvent() {
        return currentEvent;
    }
    
    public static String[] getEventNames() {
        return eventNames;   
    }

    public static boolean isEventRunning() {
        return eventRunning;
    }

    public static void setEventRunning(boolean running) {
        eventRunning = running;
    }
}
