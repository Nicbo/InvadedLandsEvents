package me.nicbo.InvadedLandsEvents.messages;

import me.nicbo.InvadedLandsEvents.EventsMain;
import org.bukkit.ChatColor;

/**
 * All event party messages
 *
 * @author Nicbo
 * @author StarZorrow
 * @since 2020-03-12
 */

public final class EventPartyMessage {
    public static String NOT_IN_PARTY;
    public static String ALREADY_IN_PARTY;
    public static String PARTY_DOES_NOT_EXIST;
    public static String PLAYER_ALREADY_IN_PARTY;
    public static String PLAYER_NOT_IN_PARTY;
    public static String INVITE_ALREADY_SENT;
    public static String INVITE_DOES_NOT_EXIST;
    public static String NO_INVITE;
    public static String NOT_LEADER;
    public static String PARTY_CREATE;
    public static String PARTY_DISBAND;
    public static String PARTY_INVITE;
    public static String PARTY_INVITE_PLAYER;
    public static String PARTY_UNINVITE;
    public static String PARTY_UNINVITE_PLAYER;
    public static String PARTY_JOIN;
    public static String PARTY_LEFT;
    public static String PARTY_KICK;
    public static String PARTY_KICK_MEMBER;
    public static String EMPTY_INVENTORY_PARTY;
    public static String OFFLINE_PARTY_MEMBER;
    public static String CANNOT_JOIN_EVENT_LIMIT;
    public static String CLICK_TO_JOIN_FORMATTED;
    public static String CLICK_TO_JOIN;
    public static String CLICK_COMMAND;
    public static String PLAYER_NOT_FOUND;
    public static String PLAYER_NEVER_JOINED;
    public static String CANNOT_ACTION_SELF;
    public static String CANNOT_JOIN_SELF;
    public static String NO_PERMISSION;

    public static void reload() {
        NOT_IN_PARTY = getStringFromConfig("NOT_IN_PARTY");
        ALREADY_IN_PARTY = getStringFromConfig("ALREADY_IN_PARTY");
        PARTY_DOES_NOT_EXIST = getStringFromConfig("PARTY_DOES_NOT_EXIST");
        PLAYER_ALREADY_IN_PARTY = getStringFromConfig("PLAYER_ALREADY_IN_PARTY");
        PLAYER_NOT_IN_PARTY = getStringFromConfig("PLAYER_NOT_IN_PARTY");
        INVITE_ALREADY_SENT = getStringFromConfig("INVITE_ALREADY_SENT");
        INVITE_DOES_NOT_EXIST = getStringFromConfig("INVITE_DOES_NOT_EXIST");
        NO_INVITE = getStringFromConfig("NO_INVITE");
        NOT_LEADER = getStringFromConfig("NOT_LEADER");
        PARTY_CREATE = getStringFromConfig("PARTY_CREATE");
        PARTY_DISBAND = getStringFromConfig("PARTY_DISBAND");
        PARTY_INVITE = getStringFromConfig("PARTY_INVITE");
        PARTY_INVITE_PLAYER = getStringFromConfig("PARTY_INVITE_PLAYER");
        PARTY_UNINVITE = getStringFromConfig("PARTY_UNINVITE");
        PARTY_UNINVITE_PLAYER = getStringFromConfig("PARTY_UNINVITE_PLAYER");
        PARTY_JOIN = getStringFromConfig("PARTY_JOIN");
        PARTY_LEFT = getStringFromConfig("PARTY_LEFT");
        PARTY_KICK = getStringFromConfig("PARTY_KICK");
        PARTY_KICK_MEMBER = getStringFromConfig("PARTY_KICK_MEMBER");
        EMPTY_INVENTORY_PARTY = getStringFromConfig("EMPTY_INVENTORY_PARTY");
        OFFLINE_PARTY_MEMBER = getStringFromConfig("OFFLINE_PARTY_MEMBER");
        CANNOT_JOIN_EVENT_LIMIT = getStringFromConfig("CANNOT_JOIN_EVENT_LIMIT");
        CLICK_TO_JOIN_FORMATTED = getStringFromConfig("CLICK_TO_JOIN_FORMATTED");
        CLICK_TO_JOIN = getStringFromConfig("CLICK_TO_JOIN");
        CLICK_COMMAND = getStringFromConfig("CLICK_COMMAND");
        PLAYER_NOT_FOUND = getStringFromConfig("PLAYER_NOT_FOUND");
        PLAYER_NEVER_JOINED = getStringFromConfig("PLAYER_NEVER_JOINED");
        CANNOT_ACTION_SELF = getStringFromConfig("CANNOT_ACTION_SELF");
        CANNOT_JOIN_SELF = getStringFromConfig("CANNOT_JOIN_SELF");
        NO_PERMISSION = getStringFromConfig("NO_PERMISSION");
    }

    private static String getStringFromConfig(String path) {
        return ChatColor.translateAlternateColorCodes('&', EventsMain.getMessages().getConfig().getString("party." + path));
    }
}