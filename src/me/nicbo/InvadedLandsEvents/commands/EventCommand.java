package me.nicbo.InvadedLandsEvents.commands;

import me.nicbo.InvadedLandsEvents.messages.EventPartyMessage;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.managers.EventPartyManager;
import me.nicbo.InvadedLandsEvents.managers.EventPartyRequestManager;
import me.nicbo.InvadedLandsEvents.party.EventParty;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
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
 * @since 2020-03-12
 */

public final class EventCommand implements CommandExecutor, TabCompleter {
    private EventManager eventManager;
    private EventPartyManager eventPartyManager;
    private EventPartyRequestManager eventPartyRequestManager;
    private final String usage;
    private EventsMain plugin;

    private List<String> args0;
    private List<String> args1;
    private List<String> events;

    public EventCommand(EventsMain plugin) {
        this.plugin = plugin;
        this.eventManager = plugin.getManagerHandler().getEventManager();
        this.eventPartyManager = plugin.getManagerHandler().getEventPartyManager();
        this.eventPartyRequestManager = plugin.getManagerHandler().getEventPartyRequestManager();
        this.usage = ChatColor.GOLD + "Usage: " + ChatColor.YELLOW;
        this.args0 = new ArrayList<>(Arrays.asList(
                "party",
                "join",
                "leave",
                "spectate",
                "info",
                "forceend",
                "host"
        ));

        this.args1 = new ArrayList<>(Arrays.asList(
                "create",
                "disband",
                "invite",
                "uninvite",
                "kick",
                "join",
                "leave"
        ));

        this.events = new ArrayList<>(Arrays.asList(EventManager.getEventNames()));
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (cmd.getName().toLowerCase().equalsIgnoreCase("event")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                UUID uuid = player.getUniqueId();
                String playerName = player.getName();
                if (args.length >= 1) {
                    switch (args[0].toLowerCase()) {
                        case "p":
                        case "party": {
                            if (args.length >= 2) {
                                switch (args[1].toLowerCase()) {
                                    case "c":
                                    case "create": {
                                        String createPartyMsg = eventPartyManager.createParty(player);
                                        if (createPartyMsg != null)
                                            player.sendMessage(createPartyMsg);
                                        break;
                                    }
                                    case "d":
                                    case "disband": {
                                        String disbandPartyMsg = eventPartyManager.disbandParty(player);
                                        if (disbandPartyMsg != null)
                                            player.sendMessage(disbandPartyMsg);
                                        break;
                                    }
                                    case "i":
                                    case "invite": {
                                        if (args.length >= 3) {
                                            String invitePartyMsg = eventPartyManager.invitePlayer(player, args[2]);
                                            if (invitePartyMsg != null)
                                                player.sendMessage(invitePartyMsg);
                                            break;
                                        }
                                        player.sendMessage(usage + "/event <join|leave|spectate|info|host> (event)");
                                        break;
                                    }
                                    case "ui":
                                    case "uninvite": {
                                        if (args.length >= 3) {
                                            String uninvitePartyMsg = eventPartyManager.uninvitePlayer(player, args[2]);
                                            if (uninvitePartyMsg != null)
                                                player.sendMessage(uninvitePartyMsg);
                                            break;
                                        }
                                        player.sendMessage(usage + "/event <join|leave|spectate|info|host> (event)");
                                        break;
                                    }
                                    case "k":
                                    case "kick": {
                                        if (args.length >= 3) {
                                            String kickPartyMsg = eventPartyManager.kickPlayer(player, args[2]);
                                            if (kickPartyMsg != null)
                                                player.sendMessage(kickPartyMsg);
                                            break;
                                        }
                                        player.sendMessage(usage + "/event <join|leave|spectate|info|host> (event)");
                                        break;
                                    }
                                    case "j":
                                    case "join": {
                                        if (args.length >= 3) {
                                            String joinPartyMsg = eventPartyManager.joinParty(player, args[2]);
                                            if (joinPartyMsg != null)
                                                player.sendMessage(joinPartyMsg);
                                            break;
                                        }
                                        player.sendMessage(usage + "/event <join|leave|spectate|info|host> (event)");
                                        break;
                                    }
                                    case "l":
                                    case "leave": {
                                        String leavePartyMsg = eventPartyManager.leaveParty(player);
                                        if (leavePartyMsg != null)
                                            player.sendMessage(leavePartyMsg);
                                        break;
                                    }
                                }
                                return true;
                            } else if (eventPartyManager.getParty(uuid) != null) {
                                String[] partyInfoMsg = eventPartyManager.partyInfo(player);
                                player.sendMessage(partyInfoMsg);
                                break;
                            }
                            player.sendMessage(EventPartyMessage.NOT_IN_PARTY);
                            break;
                        }
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
                            // If only 1 arg then go to default case.
                        }
                        default: {
                            player.sendMessage(usage + "/event <join|leave|spectate|info|host> (event)");
                        }
                    }
                }
                else {
                    player.sendMessage(usage + "/event <join|leave|spectate|info|host> (event)");
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
                    switch (args[0].toLowerCase()) {
                        case "host": {
                            StringUtil.copyPartialMatches(args[1], events, completions);
                            Collections.sort(completions);
                            break;
                        }
                        case "party": {
                            StringUtil.copyPartialMatches(args[1], args1, completions);
                            Collections.sort(completions);
                            break;
                        }
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
        - Needs optimizing
        - econfig reload and save don't work as intended
     */
}
