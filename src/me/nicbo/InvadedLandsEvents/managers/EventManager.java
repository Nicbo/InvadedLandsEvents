package me.nicbo.InvadedLandsEvents.managers;

import me.nicbo.InvadedLandsEvents.events.duels.Brackets;
import me.nicbo.InvadedLandsEvents.messages.EventMessage;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.events.*;
import me.nicbo.InvadedLandsEvents.events.duels.sumo.*;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;

/**
 * Event manager handles hosting, spectating, joining etc.
 *
 * @author Nicbo
 * @author StarZorrow
 * @since 2020-03-12
 */

public final class EventManager {
    private EventsMain plugin;

    private HashMap<String, InvadedEvent> events;
    private InvadedEvent currentEvent;

    private int countDown;

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

    public EventManager(EventsMain plugin) {
        this.plugin = plugin;
        this.events = new HashMap<>();
    }

    public void reloadEvents() {
        events.clear();
        events.put(eventNames[0], new Brackets());
        events.put(eventNames[1], new KOTH());
        events.put(eventNames[2], new LMS());
        events.put(eventNames[3], new OITC());
        events.put(eventNames[4], new RedRover());
        events.put(eventNames[5], new RoD());
        events.put(eventNames[6], new Spleef());
        events.put(eventNames[7], new TDM());
        events.put(eventNames[8], new TNTTag());
        events.put(eventNames[9], new Waterdrop());
        events.put(eventNames[10], new WoolShuffle());
        events.put(eventNames[11], new Sumo1v1());
        events.put(eventNames[11], new Sumo2v2());
        events.put(eventNames[11], new Sumo3v3());
    }

    public String hostEvent(String name, String host) {
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
        currentEvent.init();
        String name = currentEvent.getEventName();
        countDown = 15; //for testing put back to 60

        new BukkitRunnable() {
            @Override
            public void run() {
                if (currentEvent == null) {
                    this.cancel();
                    return;
                }

                if (countDown == 60 || countDown == 45 || countDown == 30 || countDown == 15 || countDown <= 5 && countDown >= 1) {
                    for (Player player : GeneralUtils.getPlayers()) {
                        TextComponent join = new TextComponent(ChatColor.translateAlternateColorCodes('&', "&c&lStarting in " + countDown + " seconds " + "&a&l[Click to Join]"));
                        join.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Click to join " + currentEvent.getEventName()).create()));
                        join.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"));
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&l" + host + " is hosting a " + name + " event!"));
                        player.spigot().sendMessage(join);
                    }
                } else if (countDown == 0) {
//                  if (!currentEvent.getSize() >= 6) { For testing disable this, will later allow customizing minimum event size.
                    currentEvent.setStarted(true);
                    currentEvent.start();
                    currentEvent.giveSpectatorsSB();
                    currentEvent.startRefreshing();
                    this.cancel();
//                  }
                }
                currentEvent.getCountdownSB().refresh();
                countDown--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    public int getCountDown() {
        return countDown;
    }

    public String joinEvent(Player player) {
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

    public String leaveEvent(Player player) {
        if (currentEvent == null) {
            return EventMessage.NONE;
        } else if (!currentEvent.containsPlayer(player)) {
            return EventMessage.NOT_IN_EVENT;
        }
        currentEvent.leaveEvent(player);
        return null;
    }

    public String specEvent(Player player) {
        if (currentEvent == null) {
            return EventMessage.NONE;
        } else if (currentEvent.containsPlayer(player)) {
            return EventMessage.IN_EVENT;
        }
        currentEvent.specEvent(player);
        return EventMessage.SPECTATING;
    }

    public String endEvent() {
        if (currentEvent == null) {
            return EventMessage.NONE;
        } else if (!currentEvent.isStarted()) {
            return EventMessage.EVENT_ENDING;
        }
        currentEvent.forceEndEvent();
        return EventMessage.ENDED;
    }

    public String eventInfo(Player player) {
        if (currentEvent == null) {
            return EventMessage.NONE;
        } else if (!currentEvent.isStarted()) {
            return EventMessage.EVENT_ENDING;
        }
        currentEvent.sendEventInfo(player);
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
