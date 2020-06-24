package me.nicbo.InvadedLandsEvents.managers;

import java.util.*;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.messages.EventPartyMessage;
import me.nicbo.InvadedLandsEvents.party.EventParty;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.*;

/**
 * Handles parties
 *
 * @author StarZorrow
 * @since 2020-03-12
 */

public final class EventPartyManager {
    private EventsMain plugin;
    private EventPartyRequestManager eventPartyRequestManager;

    private Map<UUID, EventParty> leaderUUIDtoParty;
    private Map<UUID, UUID> playerUUIDtoLeaderUUID;

    public EventPartyManager(EventsMain plugin, EventPartyRequestManager eventPartyRequestManager) {
        this.plugin = plugin;
        this.eventPartyRequestManager = eventPartyRequestManager;
        this.leaderUUIDtoParty = new HashMap<>();
        this.playerUUIDtoLeaderUUID = new HashMap<>();
    }

    public EventParty getParty(UUID player) {
        if (this.leaderUUIDtoParty.containsKey(player)) return this.leaderUUIDtoParty.get(player);
        if (this.playerUUIDtoLeaderUUID.containsKey(player)) {
            UUID leader = this.playerUUIDtoLeaderUUID.get(player);
            return this.leaderUUIDtoParty.get(leader);
        }
        return null;
    }

    public Map<UUID, EventParty> getPartyMap() {
        return this.leaderUUIDtoParty;
    }

    public EventParty createParty(UUID leader, String leaderName) {
        EventParty party = new EventParty(leader, leaderName);
        this.leaderUUIDtoParty.put(leader, party);
        return party;
    }

    public void destroyParty(UUID leader) {
        EventParty party = this.leaderUUIDtoParty.get(leader);
        this.leaderUUIDtoParty.remove(leader);
        for (UUID member : party.getMembers()) { this.playerUUIDtoLeaderUUID.remove(member); }
    }

    public void leaveParty(UUID player) {
        UUID leader = this.playerUUIDtoLeaderUUID.get(player);
        this.playerUUIDtoLeaderUUID.remove(player);
        EventParty party = this.leaderUUIDtoParty.get(leader);
        party.removeMember(player);
    }

    public void joinParty(UUID leader, UUID player) {
        EventParty party = this.leaderUUIDtoParty.get(leader);
        party.addMember(player);
        this.playerUUIDtoLeaderUUID.put(player, leader);
    }

    public void notifyParty(EventParty party, String message) {
        Player leaderPlayer = Bukkit.getPlayer(party.getLeader());
        leaderPlayer.sendMessage(message);
        for (UUID uuid : party.getMembers()) {
            Player memberPlayer = Bukkit.getPlayer(uuid);
            if (memberPlayer == null) continue;
            memberPlayer.sendMessage(message);
        }
    }

    public String createParty(Player player) {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        if (getParty(uuid) != null) {
            return EventPartyMessage.ALREADY_IN_PARTY;
        }
        EventParty party = createParty(uuid, playerName);
        notifyParty(party, EventPartyMessage.PARTY_CREATE.replace("{leader}", playerName));
        return null;
    }

    public String disbandParty(Player player) {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        if (getParty(uuid) != null) {
            EventParty party = getParty(uuid);
            if (party.getLeader() != uuid) {
                return EventPartyMessage.NOT_LEADER;
            }
            notifyParty(party, EventPartyMessage.PARTY_DISBAND.replace("{leader}", playerName));
            destroyParty(uuid);
            return null;
        }
        return EventPartyMessage.NOT_IN_PARTY;
    }

    public String invitePlayer(Player player, String targetName) {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        if (getParty(uuid) == null) {
            return EventPartyMessage.NOT_IN_PARTY;
        }
        EventParty party = getParty(uuid);
        if (party.getLeader() != uuid) {
            return EventPartyMessage.NOT_LEADER;
        }
        Player target = Bukkit.getServer().getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            return EventPartyMessage.PLAYER_NOT_FOUND.replace("{player}", targetName);
        }
        UUID uuidTarget = target.getUniqueId();
        targetName = target.getName();
        if (party.getLeader() == uuidTarget) {
            return EventPartyMessage.CANNOT_ACTION_SELF.replace("{action}", "invite");
        }
        if (getParty(uuidTarget) != null) {
            return EventPartyMessage.PLAYER_ALREADY_IN_PARTY.replace("{player}", targetName);
        }
        if (eventPartyRequestManager.hasPartyRequests(target) && eventPartyRequestManager.hasPartyRequestFromPlayer(target, player)) {
            return EventPartyMessage.INVITE_ALREADY_SENT.replace("{player}", targetName);
        }
        notifyParty(party, EventPartyMessage.PARTY_INVITE_PLAYER.replace("{player}", targetName));
        eventPartyRequestManager.addPartyRequest(target, player);

        TextComponent invite = new TextComponent(EventPartyMessage.PARTY_INVITE.replace("{leader}", playerName));
        TextComponent part = new TextComponent(EventPartyMessage.CLICK_TO_JOIN_FORMATTED.replace("{leader}", playerName));
        part.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(EventPartyMessage.CLICK_TO_JOIN).create()));
        part.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, EventPartyMessage.CLICK_COMMAND.replace("{leader}", playerName)));
        invite.addExtra(part);
        target.spigot().sendMessage(invite);
        return null;
    }

    public String uninvitePlayer(Player player, String targetName) {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        if (getParty(uuid) == null) {
            return EventPartyMessage.NOT_IN_PARTY;
        }
        EventParty party = getParty(uuid);
        if (party.getLeader() != uuid) {
            return EventPartyMessage.NOT_LEADER;
        }
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            return EventPartyMessage.PLAYER_NOT_FOUND.replace("{player}", targetName);
        }
        UUID uuid1 = target.getUniqueId();
        targetName = target.getName();
        if (party.getLeader() == uuid1) {
            return EventPartyMessage.CANNOT_ACTION_SELF.replace("{action}", "uninvite");
        }
        if (eventPartyRequestManager.hasPartyRequests(target) && eventPartyRequestManager.hasPartyRequestFromPlayer(target, player)) {
            notifyParty(party, EventPartyMessage.PARTY_UNINVITE_PLAYER.replace("{player}", targetName));
            eventPartyRequestManager.removePartyRequest(target, player);

            target.sendMessage(EventPartyMessage.PARTY_UNINVITE.replace("{leader}", playerName));
            return null;
        }
        return EventPartyMessage.INVITE_DOES_NOT_EXIST.replace("{player}", targetName);
    }

    public String kickPlayer(Player player, String targetName) {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        if (getParty(uuid) == null) {
            return EventPartyMessage.NOT_IN_PARTY;
        }
        EventParty party = getParty(uuid);
        if (party.getLeader() != uuid) {
            return EventPartyMessage.NOT_LEADER;
        }
        OfflinePlayer target = plugin.getServer().getOfflinePlayer(targetName);
        if (target == null) {
            return EventPartyMessage.PLAYER_NEVER_JOINED.replace("{player}", targetName);
        }
        UUID uuid1 = target.getUniqueId();
        targetName = target.getName();
        if (party.getLeader() == uuid1) {
            return EventPartyMessage.CANNOT_ACTION_SELF.replace("{action}", "kick");
        }
        if (getParty(uuid1) == null) {
            return EventPartyMessage.PLAYER_NOT_IN_PARTY.replace("{player}", targetName);
        }
        if (getParty(uuid1).getLeader() == uuid) {
            notifyParty(party, EventPartyMessage.PARTY_KICK_MEMBER.replace("{member}", targetName));
            leaveParty(uuid1);

            if (target.isOnline())
                plugin.getServer().getPlayer(target.getUniqueId()).sendMessage(EventPartyMessage.PARTY_KICK.replace("{leader}", playerName));
            return null;
        }
        return EventPartyMessage.NOT_LEADER;
    }

    public String joinParty(Player player, String targetName) {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        if (plugin.getServer().getPlayer(targetName) == null) {
            return EventPartyMessage.PLAYER_NOT_FOUND.replace("{player}", targetName);
        }
        if (getParty(uuid) != null) {
            return EventPartyMessage.ALREADY_IN_PARTY;
        }
        Player target = plugin.getServer().getPlayer(targetName);
        UUID uuid1 = target.getUniqueId();
        targetName = target.getName();
        if (getParty(uuid1) == null) {
            return EventPartyMessage.PARTY_DOES_NOT_EXIST.replace("{player}", targetName);
        }
        EventParty party = getParty(uuid1);
        if (party.getLeader() == uuid) {
            return EventPartyMessage.CANNOT_JOIN_SELF;
        }
        if (eventPartyRequestManager.hasPartyRequests(player) && eventPartyRequestManager.hasPartyRequestFromPlayer(player, target)) {
            joinParty(uuid1, uuid);
            notifyParty(party, EventPartyMessage.PARTY_JOIN.replace("{member}", playerName));
            eventPartyRequestManager.removePartyRequest(player, target);

            return null;
        } else {
            return EventPartyMessage.NO_INVITE.replace("{leader}", targetName);
        }
    }

    public String leaveParty(Player player) {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        if (getParty(uuid) != null) {
            EventParty party = getParty(uuid);
            if (party.getLeader() == uuid) {
                notifyParty(party, EventPartyMessage.PARTY_DISBAND.replace("{leader}", playerName));
                destroyParty(uuid);
            } else {
                notifyParty(party, EventPartyMessage.PARTY_LEFT.replace("{member}", playerName));
                leaveParty(uuid);
            }
            return null;
        }
        return EventPartyMessage.NOT_IN_PARTY;
    }

    public String[] partyInfo(Player player) {
        UUID uuid = player.getUniqueId();
        String playerName = player.getName();

        EventParty party = getParty(uuid);
        Player leader = plugin.getServer().getPlayer(party.getLeader());
        StringJoiner members = new StringJoiner(", ");
        for (UUID memberUUID : party.getMembers()) {
            Player member = plugin.getServer().getPlayer(memberUUID);
            members.add(member.getName());
        }
        return new String[]{ ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------------------------------------",
                ChatColor.GOLD + "" + ChatColor.BOLD + "Party Information:",
                ChatColor.YELLOW + "Leader: " + ChatColor.GOLD + leader.getName(),
                ChatColor.YELLOW + "Members " + ChatColor.GRAY + "[" +
                        ChatColor.GOLD + "" + (party.getMembers().size()) + ChatColor.GRAY + "]" + ChatColor.YELLOW + ":",
                ChatColor.GOLD + ((party.getSize() >= 2) ? "" + members : "None"),
                ChatColor.GRAY  + "" + ChatColor.STRIKETHROUGH + "----------------------------------------------------" };
    }
}
