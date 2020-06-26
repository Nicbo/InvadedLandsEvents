package me.nicbo.InvadedLandsEvents.commands;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

/**
 * Event Command class, handles commands for /event
 *
 * @author Nicbo
 * @author StarZorrow
 * @since 2020-02-07
 */

public final class EventCommand implements CommandExecutor, TabCompleter {
    private final EventManager eventManager;

    private final String usage;
    private final List<String> args0;
    private final List<String> events;

    public EventCommand() {
        this.eventManager = EventsMain.getManagerHandler().getEventManager();
        this.usage = ChatColor.GOLD + "Usage: " + ChatColor.YELLOW + "/event <join|leave|spectate|info|host> (event)";
        this.args0 = Arrays.asList(
                "join",
                "leave",
                "spectate",
                "info",
                "forceend",
                "host"
        );

        this.events = Arrays.asList(EventManager.getEventNames());
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
                            String joinMsg = eventManager.joinEvent(player);
                            if (joinMsg != null)
                                player.sendMessage(joinMsg);
                            break;
                        }
                        case "l":
                        case "leave": {
                            String leaveMsg = eventManager.leaveEvent(player);
                            if (leaveMsg != null)
                                player.sendMessage(leaveMsg);
                            break;
                        }
                        case "s":
                        case "spec":
                        case "spectate": {
                            String spectateMsg = eventManager.specEvent(player);
                            player.sendMessage(spectateMsg);
                            break;
                        }
                        case "i":
                        case "info": {
                            String infoMsg = eventManager.eventInfo(player);
                            if (infoMsg != null)
                                player.sendMessage(infoMsg);
                            break;
                        }
                        case "fe":
                        case "stop":
                        case "forceend": {
                            String forceEndMsg = eventManager.endEvent();
                            player.sendMessage(forceEndMsg);
                            break;
                        }
                        case "h":
                        case "host": {
                            if (args.length >= 2) {
                                String hostMsg = eventManager.hostEvent(args[1], player.getName());
                                if (hostMsg != null)
                                    player.sendMessage(hostMsg.replace("{event}", args[1]));
                                break;
                            }
                        }
                        default: {
                            player.sendMessage(usage);
                            break;
                        }
                    }
                }
                else {
                    player.sendMessage(usage);
                    return true;
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
                } else if (args.length == 2) {
                    if ("host".equalsIgnoreCase(args[0])) {
                        StringUtil.copyPartialMatches(args[1], events, completions);
                        Collections.sort(completions);
                    }
                }
                return completions;
            }
        }
        return null;
    }

    /*
    TODO:
        - Host command needs a GUI
        - Permissions
     */
}
