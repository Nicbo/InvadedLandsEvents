package me.nicbo.InvadedLandsEvents.commands;

import me.nicbo.InvadedLandsEvents.messages.EventMessage;
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
 * @author StarZorroww
 * @author Nicbo
 * @since 2020-03-12
 */

public class EventCommand implements CommandExecutor, TabCompleter {
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
                                        if (eventPartyManager.getParty(uuid) != null) {
                                            player.sendMessage(EventPartyMessage.ALREADY_IN_PARTY.toString());
                                            break;
                                        }
                                        EventParty party = eventPartyManager.createParty(uuid, playerName);
                                        eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_CREATE.toString().replace("{leader}", playerName));
                                        break;
                                    }
                                    case "d":
                                    case "disband": {
                                        if (eventPartyManager.getParty(uuid) != null) {
                                            EventParty party = eventPartyManager.getParty(uuid);
                                            if (party.getLeader() != uuid) {
                                                player.sendMessage(EventPartyMessage.NOT_LEADER.toString());
                                                break;
                                            }
                                            eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_DISBAND.toString().replace("{leader}", playerName));
                                            eventPartyManager.destroyParty(uuid);
                                            break;
                                        }
                                        player.sendMessage(EventPartyMessage.NOT_IN_PARTY.toString());
                                        break;
                                    }
                                    case "i":
                                    case "invite": {
                                        if (args.length >= 3) {
                                            if (eventPartyManager.getParty(uuid) == null) {
                                                player.sendMessage(EventPartyMessage.NOT_IN_PARTY.toString());
                                                break;
                                            }
                                            EventParty party = eventPartyManager.getParty(uuid);
                                            if (party.getLeader() != uuid) {
                                                player.sendMessage(EventPartyMessage.NOT_LEADER.toString());
                                                break;
                                            }
                                            Player target = this.plugin.getServer().getPlayer(args[2]);
                                            if (target == null || !target.isOnline()) {
                                                player.sendMessage(EventPartyMessage.PLAYER_NOT_FOUND.toString().replace("{player}", args[2]));
                                                break;
                                            }
                                            String targetname = target.getName();
                                            UUID uuid1 = target.getUniqueId();
                                            if (party.getLeader() == uuid1) {
                                                player.sendMessage(EventPartyMessage.CANNOT_ACTION_SELF.toString().replace("{action}", "invite"));
                                                break;
                                            }
                                            if (eventPartyManager.getParty(uuid1) != null) {
                                                player.sendMessage(EventPartyMessage.PLAYER_ALREADY_IN_PARTY.toString().replace("{player}", targetname));
                                                break;
                                            }
                                            if (eventPartyRequestManager.hasPartyRequests(target) && eventPartyRequestManager.hasPartyRequestFromPlayer(target, player)) {
                                                player.sendMessage(EventPartyMessage.INVITE_ALREADY_SENT.toString().replace("{player}", targetname));
                                                break;
                                            }
                                            eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_INVITE_PLAYER.toString().replace("{player}", targetname));
                                            eventPartyRequestManager.addPartyRequest(target, player);

                                            TextComponent invite = new TextComponent(EventPartyMessage.PARTY_INVITE.toString().replace("{leader}", playerName));
                                            TextComponent part = new TextComponent(EventPartyMessage.CLICK_TO_JOIN_FORMATTED.toString().replace("{leader}", playerName));
                                            part.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(EventPartyMessage.CLICK_TO_JOIN.toString()).create()));
                                            part.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, EventPartyMessage.CLICK_COMMAND.toString().replace("{leader}", playerName)));
                                            invite.addExtra(part);
                                            target.spigot().sendMessage(invite);
                                            break;
                                        }
                                        player.sendMessage(usage + "/event <join|leave|spectate|info|host> (event)");
                                        break;
                                    }
                                    case "ui":
                                    case "uninvite": {
                                        if (args.length >= 3) {
                                            if (eventPartyManager.getParty(uuid) == null) {
                                                player.sendMessage(EventPartyMessage.NOT_IN_PARTY.toString());
                                                break;
                                            }
                                            EventParty party = eventPartyManager.getParty(uuid);
                                            if (party.getLeader() != uuid) {
                                                player.sendMessage(EventPartyMessage.NOT_LEADER.toString());
                                                break;
                                            }
                                            Player target = this.plugin.getServer().getPlayer(args[2]);
                                            if (target == null || !target.isOnline()) {
                                                player.sendMessage(EventPartyMessage.PLAYER_NOT_FOUND.toString().replace("{player}", args[2]));
                                                break;
                                            }
                                            UUID uuid1 = target.getUniqueId();
                                            String targetName = target.getName();
                                            if (party.getLeader() == uuid1) {
                                                player.sendMessage(EventPartyMessage.CANNOT_ACTION_SELF.toString().replace("{action}", "deinvite"));
                                                break;
                                            }
                                            if (eventPartyRequestManager.hasPartyRequests(target) && eventPartyRequestManager.hasPartyRequestFromPlayer(target, player)) {
                                                eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_UNINVITE_PLAYER.toString().replace("{player}", targetName));
                                                eventPartyRequestManager.removePartyRequest(target, player);

                                                target.sendMessage(EventPartyMessage.PARTY_UNINVITE.toString().replace("{leader}", playerName));
                                                break;
                                            }
                                            player.sendMessage(EventPartyMessage.INVITE_DOES_NOT_EXIST.toString().replace("{player}", targetName));
                                            break;
                                        }
                                        player.sendMessage(usage + "/event <join|leave|spectate|info|host> (event)");
                                        break;
                                    }
                                    case "k":
                                    case "kick": {
                                        if (args.length >= 3) {
                                            if (eventPartyManager.getParty(uuid) == null) {
                                                player.sendMessage(EventPartyMessage.NOT_IN_PARTY.toString());
                                                break;
                                            }
                                            EventParty party = eventPartyManager.getParty(uuid);
                                            if (party.getLeader() != uuid) {
                                                player.sendMessage(EventPartyMessage.NOT_LEADER.toString());
                                                break;
                                            }
                                            OfflinePlayer target = this.plugin.getServer().getOfflinePlayer(args[2]);
                                            if (target == null) {
                                                player.sendMessage(EventPartyMessage.PLAYER_NEVER_JOINED.toString().replace("{player}", args[2]));
                                                break;
                                            }
                                            UUID uuid1 = target.getUniqueId();
                                            String targetname = target.getName();
                                            if (party.getLeader() == uuid1) {
                                                player.sendMessage(EventPartyMessage.CANNOT_ACTION_SELF.toString().replace("{action}", "kick"));
                                                break;
                                            }
                                            if (eventPartyManager.getParty(uuid1) == null) {
                                                player.sendMessage(EventPartyMessage.PLAYER_NOT_IN_PARTY.toString().replace("{player}", targetname));
                                                break;
                                            }
                                            if (eventPartyManager.getParty(uuid1).getLeader() == uuid) {
                                                eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_KICK_MEMBER.toString().replace("{member}", targetname));
                                                eventPartyManager.leaveParty(uuid1);

                                                if (target.isOnline()) this.plugin.getServer().getPlayer(target.getUniqueId()).sendMessage(EventPartyMessage.PARTY_KICK.toString().replace("{leader}", playerName));
                                                break;
                                            }
                                            player.sendMessage(EventPartyMessage.NOT_LEADER.toString());
                                            break;
                                        }
                                        player.sendMessage(usage + "/event <join|leave|spectate|info|host> (event)");
                                        break;
                                    }
                                    case "j":
                                    case "join": {
                                        if (args.length >= 3) {
                                            if (this.plugin.getServer().getPlayer(args[2]) == null) {
                                                player.sendMessage(EventPartyMessage.PLAYER_NOT_FOUND.toString().replace("{player}", args[2]));
                                                break;
                                            }
                                            if (eventPartyManager.getParty(uuid) != null) {
                                                player.sendMessage(EventPartyMessage.ALREADY_IN_PARTY.toString());
                                                break;
                                            }
                                            Player target = this.plugin.getServer().getPlayer(args[2]);
                                            UUID uuid1 = target.getUniqueId();
                                            String targetName = target.getName();
                                            if (eventPartyManager.getParty(uuid1) == null) {
                                                player.sendMessage(EventPartyMessage.PARTY_DOES_NOT_EXIST.toString().replace("{player}", targetName));
                                                break;
                                            }
                                            EventParty party = eventPartyManager.getParty(uuid1);
                                            if (party.getLeader() == uuid) {
                                                player.sendMessage(EventPartyMessage.CANNOT_ACTION_SELF.toString().replace("{action} yourself", "join your own party"));
                                                break;
                                            }
                                            if (eventPartyRequestManager.hasPartyRequests(player) && eventPartyRequestManager.hasPartyRequestFromPlayer(player, target)) {
                                                eventPartyManager.joinParty(uuid1, uuid);
                                                eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_JOIN.toString().replace("{member}", playerName));
                                                eventPartyRequestManager.removePartyRequest(player, target);
                                                break;
                                            } else {
                                                player.sendMessage(EventPartyMessage.NO_INVITE.toString().replace("{leader}", targetName));
                                                break;
                                            }
                                        }
                                        player.sendMessage(usage + "/event <join|leave|spectate|info|host> (event)");
                                        break;
                                    }
                                    case "l":
                                    case "leave": {
                                        if (eventPartyManager.getParty(uuid) != null) {
                                            EventParty party = eventPartyManager.getParty(uuid);
                                            if (party.getLeader() == uuid) {
                                                eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_DISBAND.toString().replace("{leader}", playerName));
                                                eventPartyManager.destroyParty(uuid);
                                            } else {
                                                eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_LEFT.toString().replace("{member}", playerName));
                                                eventPartyManager.leaveParty(uuid);
                                            }
                                            break;
                                        }
                                        player.sendMessage(EventPartyMessage.NOT_IN_PARTY.toString());
                                        break;
                                    }
                                }
                                return true;
                            } else if (eventPartyManager.getParty(uuid) != null) {
                                EventParty party = eventPartyManager.getParty(uuid);
                                Player leader = this.plugin.getServer().getPlayer(party.getLeader());
                                StringJoiner members = new StringJoiner(", ");
                                for (UUID memberUUID : party.getMembers()) {
                                    Player member = this.plugin.getServer().getPlayer(memberUUID);
                                    members.add(member.getName());
                                }
                                String[] information = { ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------------------------------------",
                                        ChatColor.GOLD + "" + ChatColor.BOLD + "Party Information:",
                                        ChatColor.YELLOW + "Leader: " + ChatColor.GOLD + leader.getName(),
                                        ChatColor.YELLOW + "Members " + ChatColor.GRAY + "[" +
                                        ChatColor.GOLD + "" + (party.getMembers().size()) + ChatColor.GRAY + "]" + ChatColor.YELLOW + ":",
                                        ChatColor.GOLD + ((party.getSize() >= 2) ? "" + members : "None"),
                                        ChatColor.GRAY  + "" + ChatColor.STRIKETHROUGH + "----------------------------------------------------" };
                                player.sendMessage(information);
                                break;
                            }
                            player.sendMessage(EventPartyMessage.NOT_IN_PARTY.toString());
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
     */
}
