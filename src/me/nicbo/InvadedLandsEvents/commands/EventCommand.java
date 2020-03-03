package me.nicbo.InvadedLandsEvents.commands;

import me.nicbo.InvadedLandsEvents.EventMessage;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.manager.managers.EventManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class EventCommand implements CommandExecutor, TabCompleter {
    private EventManager eventManager;
    private final String usage;
    private EventsMain plugin;
    private List<String> args0;
    private List<String> events;

    public EventCommand(EventsMain plugin) {
        this.plugin = plugin;
        this.eventManager = plugin.getManagerHandler().getEventManager();
        this.usage = ChatColor.GOLD + "Usage: " + ChatColor.YELLOW;
        this.args0 = new ArrayList<>();
        this.args0.add("join");
        this.args0.add("leave");
        this.args0.add("spectate");
        this.args0.add("info");
        this.args0.add("forceend");
        this.args0.add("host");

        this.events = new ArrayList<>();
        this.events.addAll(Arrays.asList(EventManager.getEventNames()));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (cmd.getName().toLowerCase().equalsIgnoreCase("event")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length >= 1) {
                    switch (args[0].toLowerCase()) {
                        case "j":
                        case "join": {
                            EventMessage joinMsg = eventManager.joinEvent(player);
                            if (joinMsg != null)
                                player.sendMessage(joinMsg.getDescription());
                            break;
                        }
                        case "l":
                        case "leave": {
                            EventMessage leaveMsg = eventManager.leaveEvent(player);
                            player.sendMessage(leaveMsg.getDescription());
                            break;
                        }
                        case "s":
                        case "spec":
                        case "spectate": {
                            EventMessage spectateMsg = eventManager.specEvent(player);
                            player.sendMessage(spectateMsg.getDescription());
                            break;
                        }
                        case "i":
                        case "info": {
                            EventMessage infoMsg = eventManager.eventInfo(player);
                            if (infoMsg != null)
                                player.sendMessage(infoMsg.getDescription());
                            break;
                        }
                        case "fe":
                        case "stop":
                        case "forceend": {
                            EventMessage forceEndMsg = eventManager.endEvent(player);
                            player.sendMessage(forceEndMsg.getDescription());
                            break;
                        }
                        case "h":
                        case "host": {
                            if (args.length >= 2) {
                                EventMessage hostMsg = eventManager.hostEvent(args[1], player.getName());
                                if (hostMsg != null)
                                    player.sendMessage(hostMsg.getDescription().replace("{event}", args[1]));
                                break;
                            }
                            // If only 1 arg then go to default case.
                        }
                        default: {
                            player.sendMessage(usage + "/event <join|leave|spectate|info|host> (event)");
                        }
                    }
                }
                else {
                    player.sendMessage(usage + "/event <join|leave|spectate|info|host> (event)");
                }
            }
            else {
                sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        if (cmd.getName().equalsIgnoreCase("event") || cmd.getName().equalsIgnoreCase("e")) {
            if (sender instanceof Player) {
                List<String> completions = new ArrayList<>();
                if (args.length == 1) {
                    StringUtil.copyPartialMatches(args[0], args0, completions);
                    Collections.sort(completions);
                } else if (args.length == 2 && args[0].equalsIgnoreCase("host")) {
                    StringUtil.copyPartialMatches(args[1], events, completions);
                    Collections.sort(completions);
                }
                return completions;
            }
        }
        return null;
    }

    /*
    TODO:
        - Sub commands partially complete
        - Right now this is just for testing I will work on actual command when events are done
     */
}
