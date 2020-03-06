package me.nicbo.InvadedLandsEvents;

import org.bukkit.ChatColor;

public enum EventPartyMessage {
    NOT_IN_PARTY(ChatColor.RED + "You're not in a party."),
    ALREADY_IN_PARTY(ChatColor.RED + "You're already in a party."),
    PARTY_DOES_NOT_EXIST(ChatColor.YELLOW + "{player}" + ChatColor.RED + " doesn't have a party."),
    PLAYER_ALREADY_IN_PARTY(ChatColor.YELLOW + "{player}" + ChatColor.RED + " is already in a party."),
    PLAYER_NOT_IN_PARTY(ChatColor.YELLOW + "{player}" + ChatColor.RED + " is not in a party."),
    INVITE_ALREADY_SENT(ChatColor.RED + "You have already sent an invite to " + ChatColor.YELLOW + "{player}" + ChatColor.RED + ". /event party deinvite {player} to revoke the invite."),
    INVITE_DOES_NOT_EXIST(ChatColor.YELLOW + "{player}" + ChatColor.RED + " doesn't have an invite available."),
    NO_INVITE(ChatColor.RED + "You don't have an invite from " + ChatColor.YELLOW + "{leader}" + ChatColor.RED + "."),
    NOT_LEADER(ChatColor.RED + "You're not the party leader."),
    PARTY_CREATE(ChatColor.GOLD + "{leader}" + ChatColor.YELLOW + " has created and joined the party."),
    PARTY_DISBAND(ChatColor.GOLD + "{leader}" + ChatColor.YELLOW + " has disbanded the party."),
    PARTY_INVITE(ChatColor.GOLD + "{leader}" + ChatColor.YELLOW + " has invited you to the party."),
    PARTY_INVITE_PLAYER(ChatColor.GOLD + "{player}" + ChatColor.YELLOW + " has been invited to the party."),
    PARTY_DEINVITE(ChatColor.GOLD + "{leader}" + ChatColor.YELLOW + " has revoked your invite to their party."),
    PARTY_DEINVITE_PLAYER(ChatColor.GOLD + "{player}" + ChatColor.YELLOW + " has been deinvited from the party."),
    PARTY_JOIN(ChatColor.GOLD + "{member}" + ChatColor.YELLOW + " has joined the party."),
    PARTY_LEFT(ChatColor.GOLD + "{member}" + ChatColor.YELLOW + " has left the party."),
    PARTY_KICK(ChatColor.GOLD + "{leader}" + ChatColor.YELLOW + " has kicked you from the party."),
    PARTY_KICK_MEMBER(ChatColor.GOLD + "{member}" + ChatColor.YELLOW + " has been kicked from the party."),
    EMPTY_INVENTORY_PARTY(ChatColor.YELLOW + "{member}" + ChatColor.RED + " must clear their inventory before joining the event."),
    OFFLINE_PARTY_MEMBER(ChatColor.YELLOW + "{members}" + ChatColor.RED + " are offline and are required to be online before joining the event."),
    CANNOT_JOIN_EVENT_LIMIT(ChatColor.RED + "Your party has too many members and cannot join the event. " + ChatColor.GRAY + "(" + ChatColor.YELLOW + "{amount}" + ChatColor.GRAY + "/" + ChatColor.YELLOW + "{limit}" + ChatColor.GRAY + ")"),
    CLICK_TO_JOIN_FORMATTED(ChatColor.YELLOW + "Type /event party join " + ChatColor.GOLD + "{leader}" + ChatColor.YELLOW + " to join or " + ChatColor.GOLD + "[" + ChatColor.YELLOW + "Click to Join" + ChatColor.GOLD + "]"),
    CLICK_TO_JOIN(ChatColor.GREEN + "Click to join the party"),
    CLICK_COMMAND("/event party join {leader}"),
    PLAYER_NOT_FOUND(ChatColor.YELLOW + "{player}" + ChatColor.RED + " is not online."),
    PLAYER_NEVER_JOINED(ChatColor.YELLOW + "{player}" + ChatColor.RED + " has never joined the server before."),
    CANNOT_ACTION_SELF(ChatColor.RED + "You cannot {action} yourself."),
    NO_PERMISSION(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command.");

    final String message;

    EventPartyMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return message;
    }
}