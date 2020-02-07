package me.nicbo.InvadedLandsEvents.managers;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.events.*;
import me.nicbo.InvadedLandsEvents.events.sumo.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

public final class EventManager {
    private static InvadedEvent currentEvent;

    private EventManager() {}

    public static boolean hostEvent(String name, String host) {
        switch (name.toLowerCase()) {
            case "brackets":
                currentEvent = new Brackets();
                break;
            case "koth":
                currentEvent= new KOTH();
                break;
            case "lms":
                currentEvent = new LMS();
                break;
            case "oitc":
                currentEvent = new OITC();
                break;
            case "rod":
                currentEvent = new RoD();
                break;
            case "rr":
            case "redrover":
                currentEvent = new RedRover();
                break;
            case "spleef":
                currentEvent = new Spleef();
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
                currentEvent = new TDM();
                break;
            case "tnttag":
                currentEvent = new TNTTag();
                break;
            case "wd":
            case "waterdrop":
                currentEvent = new Waterdrop();
                break;
            default:
                return false;
        }
        startCountDown(host);
        return true;
    }

    private static void startCountDown(String host) {
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
        }.runTaskTimerAsynchronously(EventsMain.getInstance(), 0, 20);
    }

    private static boolean isInventoryEmpty(Inventory inv) {
        return true; // <- is inv empty?
    }

    public static EventStatus joinEvent(Player player) {
        return EventStatus.NONE; // -> do logic and  when  called can getdescription and send msg based on it else join event <- english good
    }

    public static EventStatus specEvent(Player player) {
        return EventStatus.NONE; // same as above
    }

    public static EventStatus leaveEvent(Player player) {
        return EventStatus.NONE; // ||
    }
    /*
    TODO:
        - STATIC BAD MAKE ME NOT STATIC PLS AND THANKS
     */
}
