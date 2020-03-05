package me.nicbo.InvadedLandsEvents.commands;

import me.nicbo.InvadedLandsEvents.EventMessage;
import me.nicbo.InvadedLandsEvents.EventPartyMessage;
import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.manager.managers.EventManager;
import me.nicbo.InvadedLandsEvents.manager.managers.EventPartyManager;
import me.nicbo.InvadedLandsEvents.manager.managers.EventPartyRequestManager;
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
        this.args0 = new ArrayList<>();
        this.args1 = new ArrayList<>();
        this.args0.add("party");
        this.args1.add("create");
        this.args1.add("disband");
        this.args1.add("invite");
        this.args1.add("deinvite");
        this.args1.add("kick");
        this.args1.add("join");
        this.args1.add("leave");
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
                UUID uuid = player.getUniqueId();
                String playername = player.getName();
                if (args.length >= 1) {
                    switch (args[0].toLowerCase()) {
                        case "p":
                        case "party": {
                            if (args.length >= 2) {
                                switch (args[1].toLowerCase()) {
                                    case "c":
                                    case "create": {
                                        if (eventPartyManager.getParty(uuid) != null) {
                                            player.sendMessage(EventPartyMessage.ALREADY_IN_PARTY.getDescription());
                                            break;
                                        }
                                        EventParty party = eventPartyManager.createParty(uuid, playername);
                                        eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_CREATE.getDescription().replace("{leader}", playername));
                                        break;
                                    }
                                    case "d":
                                    case "disband": {
                                        if (eventPartyManager.getParty(uuid) != null) {
                                            EventParty party = eventPartyManager.getParty(uuid);
                                            if (party.getLeader() == uuid) {
                                                eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_DISBAND.getDescription().replace("{leader}", playername));
                                                eventPartyManager.destroyParty(uuid);
                                                break;
                                            } else {
                                                player.sendMessage(EventPartyMessage.NOT_LEADER.getDescription());
                                                break;
                                            }
                                        }
                                        player.sendMessage(EventPartyMessage.NOT_IN_PARTY.getDescription());
                                        break;
                                    }
                                    case "i":
                                    case "invite": {
                                        if (args.length >= 3) {
                                            if (eventPartyManager.getParty(uuid) != null) {
                                                EventParty party = eventPartyManager.getParty(uuid);
                                                if (party.getLeader() == uuid) {
                                                    Player target = this.plugin.getServer().getPlayer(args[2]);
                                                    if (target != null || target.isOnline()) {
                                                        String targetname = target.getName();
                                                        UUID uuid1 = target.getUniqueId();
                                                        if (party.getLeader() != uuid1) {
                                                            if (eventPartyManager.getParty(uuid1) == null) {
                                                                if (eventPartyRequestManager.hasPartyRequests(target) && eventPartyRequestManager.hasPartyRequestFromPlayer(target, player)) {
                                                                    player.sendMessage(EventPartyMessage.INVITE_ALREADY_SENT.getDescription().replace("{player}", targetname));
                                                                    break;
                                                                }
                                                                eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_INVITE_PLAYER.getDescription().replace("{player}", targetname));
                                                                eventPartyRequestManager.addPartyRequest(target, player);

                                                                TextComponent invite = new TextComponent(EventPartyMessage.PARTY_INVITE.getDescription().replace("{leader}", playername));
                                                                TextComponent part = new TextComponent(EventPartyMessage.CLICK_TO_JOIN_FORMATTED.getDescription().replace("{leader}", playername));
                                                                part.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(EventPartyMessage.CLICK_TO_JOIN.getDescription()).create()));
                                                                part.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, EventPartyMessage.CLICK_COMMAND.getDescription().replace("{leader}", playername)));
                                                                invite.addExtra(part);
                                                                target.spigot().sendMessage(invite);
                                                                break;
                                                            } else {
                                                                player.sendMessage(EventPartyMessage.PLAYER_ALREADY_IN_PARTY.getDescription().replace("{player}", playername));
                                                                break;
                                                            }
                                                        } else {
                                                            player.sendMessage(EventPartyMessage.CANNOT_ACTION_SELF.getDescription().replace("{action}", "invite"));
                                                            break;
                                                        }
                                                    } else {
                                                        player.sendMessage(EventPartyMessage.PLAYER_NOT_FOUND.getDescription().replace("{player}", args[2]));
                                                        break;
                                                    }
                                                } else {
                                                    player.sendMessage(EventPartyMessage.NOT_LEADER.getDescription());
                                                    break;
                                                }
                                            } else {
                                                player.sendMessage(EventPartyMessage.NOT_IN_PARTY.getDescription());
                                                break;
                                            }
                                        }
                                        player.sendMessage(usage + "/event <join|leave|spectate|info|host> (event)");
                                        break;
                                    }
                                    case "di":
                                    case "deinvite": {
                                        if (args.length >= 3) {
                                            if (eventPartyManager.getParty(uuid) != null) {
                                                EventParty party = eventPartyManager.getParty(uuid);
                                                if (party.getLeader() == uuid) {
                                                    Player target = this.plugin.getServer().getPlayer(args[2]);
                                                    if (target != null || target.isOnline()) {
                                                        UUID uuid1 = target.getUniqueId();
                                                        String targetname = target.getName();
                                                        if (party.getLeader() != uuid1) {
                                                            if (eventPartyRequestManager.hasPartyRequests(target) && eventPartyRequestManager.hasPartyRequestFromPlayer(target, player)) {
                                                                eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_DEINVITE_PLAYER.getDescription().replace("{player}", targetname));
                                                                eventPartyRequestManager.removePartyRequest(target, player);

                                                                target.sendMessage(EventPartyMessage.PARTY_DEINVITE.getDescription().replace("{leader}", playername));
                                                                break;
                                                            }
                                                            player.sendMessage(EventPartyMessage.INVITE_DOES_NOT_EXIST.getDescription().replace("{player}", targetname));
                                                            break;
                                                        } else {
                                                            player.sendMessage(EventPartyMessage.CANNOT_ACTION_SELF.getDescription().replace("{action}", "deinvite"));
                                                            break;
                                                        }
                                                    } else {
                                                        player.sendMessage(EventPartyMessage.PLAYER_NOT_FOUND.getDescription().replace("{player}", args[2]));
                                                        break;
                                                    }
                                                } else {
                                                    player.sendMessage(EventPartyMessage.NOT_LEADER.getDescription());
                                                    break;
                                                }
                                            } else {
                                                player.sendMessage(EventPartyMessage.NOT_IN_PARTY.getDescription());
                                                break;
                                            }
                                        }
                                        player.sendMessage(usage + "/event <join|leave|spectate|info|host> (event)");
                                        break;
                                    }
                                    case "k":
                                    case "kick": {
                                        if (args.length >= 3) {
                                            if (eventPartyManager.getParty(uuid) != null) {
                                                EventParty party = eventPartyManager.getParty(uuid);
                                                if (party.getLeader() == uuid) {
                                                    OfflinePlayer target = this.plugin.getServer().getOfflinePlayer(args[2]);
                                                    if (target != null) {
                                                        UUID uuid1 = target.getUniqueId();
                                                        String targetname = target.getName();
                                                        if (party.getLeader() != uuid1) {
                                                            if (eventPartyManager.getParty(uuid1) != null) {
                                                                if (eventPartyManager.getParty(uuid1).getLeader() == uuid) {
                                                                    eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_KICK_MEMBER.getDescription().replace("{member}", targetname));
                                                                    eventPartyManager.leaveParty(uuid1);

                                                                    if (target.isOnline()) this.plugin.getServer().getPlayer(target.getUniqueId()).sendMessage(EventPartyMessage.PARTY_KICK.getDescription().replace("{leader}", playername));
                                                                    break;
                                                                }
                                                                player.sendMessage(EventPartyMessage.NOT_LEADER.getDescription());
                                                                break;
                                                            } else {
                                                                player.sendMessage(EventPartyMessage.PLAYER_NOT_IN_PARTY.getDescription().replace("{player}", targetname));
                                                                break;
                                                            }
                                                        } else {
                                                            player.sendMessage(EventPartyMessage.CANNOT_ACTION_SELF.getDescription().replace("{action}", "kick"));
                                                            break;
                                                        }
                                                    } else {
                                                        player.sendMessage(EventPartyMessage.PLAYER_NEVER_JOINED.getDescription().replace("{player}", args[2]));
                                                        break;
                                                    }
                                                } else {
                                                    player.sendMessage(EventPartyMessage.NOT_LEADER.getDescription());
                                                    break;
                                                }
                                            } else {
                                                player.sendMessage(EventPartyMessage.NOT_IN_PARTY.getDescription());
                                                break;
                                            }
                                        }
                                        player.sendMessage(usage + "/event <join|leave|spectate|info|host> (event)");
                                        break;
                                    }
                                    case "j":
                                    case "join": {
                                        if (args.length >= 3) {
                                            if (this.plugin.getServer().getPlayer(args[2]) != null) {
                                                Player target = this.plugin.getServer().getPlayer(args[2]);
                                                UUID uuid1 = target.getUniqueId();
                                                String targetname = target.getName();
                                                if (eventPartyManager.getParty(uuid1) != null) {
                                                    EventParty party = eventPartyManager.getParty(uuid1);
                                                    if (party.getLeader() == uuid) {
                                                        if (eventPartyRequestManager.hasPartyRequests(player) && eventPartyRequestManager.hasPartyRequestFromPlayer(player, target)) {
                                                            eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_JOIN.getDescription().replace("{member}", playername));
                                                            eventPartyManager.joinParty(uuid1, uuid);
                                                            break;
                                                        } else {
                                                            player.sendMessage(EventPartyMessage.NO_INVITE.getDescription().replace("{leader}", targetname));
                                                            break;
                                                        }
                                                    } else {
                                                        player.sendMessage(EventPartyMessage.CANNOT_ACTION_SELF.getDescription().replace("{action} yourself", "join your own party"));
                                                        break;
                                                    }
                                                } else {
                                                    player.sendMessage(EventPartyMessage.PARTY_DOES_NOT_EXIST.getDescription().replace("{player}", targetname));
                                                    break;
                                                }
                                            } else {
                                                player.sendMessage(EventPartyMessage.PLAYER_NOT_FOUND.getDescription().replace("{player}", args[2]));
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
                                                eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_DISBAND.getDescription().replace("{leader}", playername));
                                                eventPartyManager.destroyParty(uuid);
                                                break;
                                            } else {
                                                eventPartyManager.notifyParty(party, EventPartyMessage.PARTY_LEFT.getDescription().replace("{member}", playername));
                                                eventPartyManager.leaveParty(uuid);
                                                break;
                                            }
                                        }
                                        player.sendMessage(EventPartyMessage.NOT_IN_PARTY.getDescription());
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
                                String[] information = { ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------------------------------------", ChatColor.GOLD + "" + ChatColor.BOLD + "Party Information:", ChatColor.YELLOW + "Leader: " + ChatColor.GOLD + leader.getName(), ChatColor.YELLOW + "Members " + ChatColor.GRAY + "[" + ChatColor.GOLD + "" + (party.getMembers().size()) + ChatColor.GRAY + "]" + ChatColor.YELLOW + ":", ChatColor.GOLD + ((party.getSize() >= 2) ? ", " + members : "None"), ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------------------------------------" };
                                player.sendMessage(information);
                                break;
                            }
                            player.sendMessage(EventPartyMessage.NOT_IN_PARTY.getDescription());
                            break;
                        }
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
        - Sub commands complete basically
        - Host command needs a GUI
        - Right now this is just for testing I will work on actual command when events are done
     */
}
