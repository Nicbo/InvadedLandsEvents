package me.nicbo.InvadedLandsEvents.manager.managers;

import me.nicbo.InvadedLandsEvents.EventMessage;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.events.*;
import me.nicbo.InvadedLandsEvents.events.sumo.*;
import me.nicbo.InvadedLandsEvents.manager.Manager;
import me.nicbo.InvadedLandsEvents.manager.ManagerHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

/**
 * Event manager handles hosting, spectating, joining etc.
 *
 * @author Nicbo
 * @author StarZorroww
 * @since 2020-03-12
 */

public final class EventManager extends Manager {
    private EventsMain plugin;
    private HashMap<String, InvadedEvent> events;
    private InvadedEvent currentEvent;

    private static String[] eventNames;

    static {
        eventNames = new String[]{
                "brackets", "koth", "lms",
                "oitc", "redrover", "rod",
                "spleef", "tdm", "tnttag",
                "waterdrop", "woolshuffle",
                "sumo"
        };
    }

    public EventManager(ManagerHandler handler) {
        super(handler);
        this.plugin = handler.getPlugin();
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
        String name = currentEvent.getEventName();

        new BukkitRunnable() {
            int time = 15; //for testing put back to 60

            @Override
            public void run() {
                if (currentEvent == null) {
                    this.cancel();
                    return;
                }

                if (time == 60 || time == 45 || time == 30 || time == 15 || time <= 5 && time >= 1) {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&l"+ host + " is hosting a " + name + " event!"));
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', "&c&lStarting in " + time + " seconds " + "&a&l[Click to Join]"));
                    // Add Click Event text
                } else if (time == 0) {
//                  if (!currentEvent.getSize() >= 6) { For testing disable this, will later allow customizing minimum event size.
                    currentEvent.setStarted(true);
                    currentEvent.start();
                    this.cancel();
//                  }
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
        return null;
    }

    public EventMessage specEvent(Player player) {
        if (currentEvent == null) {
            return EventMessage.NONE;
        } else if (currentEvent.containsPlayer(player)) {
            return EventMessage.IN_EVENT;
        }
        currentEvent.specEvent(player);
        return EventMessage.SPECTATING;
    }

    public EventMessage endEvent(Player player) {
        if (currentEvent == null) {
            return EventMessage.NONE;
        } else if (!currentEvent.isStarted()) {
            return EventMessage.EVENT_ENDING;
        }
        currentEvent.forceEndEvent();
        return EventMessage.ENDED;
    }

    public EventMessage eventInfo(Player player) {
        if (currentEvent == null) {
            return EventMessage.NONE;
        } else if (!currentEvent.isStarted()) {
            return EventMessage.EVENT_ENDING;
        }
        currentEvent.eventInfo(player);
        return null;
    }

    public InvadedEvent getCurrentEvent() {
        return currentEvent;
    }

    public boolean isEventRunning() {
        return currentEvent != null;
    }

    public void setCurrentEvent(InvadedEvent event) {
        this.currentEvent = event;
    }

    public static String[] getEventNames() {
        return eventNames;   
    }

    public static boolean eventExists(String name) {
        for (String event : eventNames) {
            if (event.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    /*
    TODO:
        - Make descriptions editable based on config (later version)
     */
}
