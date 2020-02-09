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
    private HashMap<String, InvadedEvent> events = new HashMap<>();
    private InvadedEvent currentEvent;

    public EventManager(EventsMain plugin) {
        this.plugin = plugin;
        events.put("brackets", new Brackets(plugin));
        events.put("koth", new KOTH(plugin));
        events.put("lms", new LMS(plugin));
        events.put("oitc", new OITC(plugin));
        events.put("redrover", new RedRover(plugin));
        events.put("rod", new RoD(plugin));
        events.put("spleef", new Spleef(plugin));
        events.put("tdm", new TDM(plugin));
        events.put("tnttag", new TNTTag(plugin));
        events.put("waterdrop", new Waterdrop(plugin));
        events.put("woolshuffle", new WoolShuffle(plugin));
    }

    public boolean hostEvent(String name, String host) {
        if (events.containsKey(name)) {
            currentEvent = events.get(name);
            startCountDown(host);
            return true;
        }
        return false;
    }

    private void startCountDown(String host) {
        String name = currentEvent.getName();

        new BukkitRunnable() {
            int time = 60;

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
        return EventStatus.NONE; // -> do logic and  when  called can getdescription and send msg based on it else join event <- english good
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
}
