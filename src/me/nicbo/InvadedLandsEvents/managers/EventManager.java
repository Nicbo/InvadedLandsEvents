package me.nicbo.InvadedLandsEvents.managers;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.events.*;
import me.nicbo.InvadedLandsEvents.events.sumo.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class EventManager {
    private EventsMain plugin;
    private InvadedEvent currentEvent;

    public EventManager(EventsMain plugin) {
        this.plugin = plugin;
    }

    public boolean hostEvent(String name, String host) {
        switch (name.toLowerCase()) {
            case "brackets":
                currentEvent = new Brackets(plugin);
                break;
            case "koth":
                currentEvent= new KOTH(plugin);
                break;
            case "lms":
                currentEvent = new LMS(plugin);
                break;
            case "oitc":
                currentEvent = new OITC(plugin);
                break;
            case "rod":
                currentEvent = new RoD(plugin);
                break;
            case "rr":
            case "redrover":
                currentEvent = new RedRover(plugin);
                break;
            case "spleef":
                currentEvent = new Spleef(plugin);
                break;
            case "sumo1v1":
                currentEvent = new Sumo1v1();
                break;
            case "sumo2v2":
                currentEvent = new Sumo2v2();
                break;
            case "sumo3v3":
                currentEvent = new Sumo3v3();
                break;
            case "tdm":
                currentEvent = new TDM(plugin);
                break;
            case "tnttag":
                currentEvent = new TNTTag(plugin);
                break;
            case "wd":
            case "waterdrop":
                currentEvent = new Waterdrop(plugin);
                break;
            default:
                return false;
        }
        startCountDown(host);
        return true;
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
}
