package me.nicbo.invadedlandsevents.messages.impl;

import me.nicbo.invadedlandsevents.messages.IMessage;
import org.bukkit.ChatColor;

/**
 * Messages for InvadedLandsEvents
 *
 * @author Nicbo
 */

public enum Message implements IMessage<String> {
    // event
    EVENT_NOT_RUNNING("event.EVENT_NOT_RUNNING"),
    JOIN_ALREADY_STARTED("event.JOIN_ALREADY_STARTED"),
    HOST_ALREADY_STARTED("event.HOST_ALREADY_STARTED"),
    FORCEEND_EVENT("event.FORCEEND_EVENT"),
    SPECTATE_EVENT("event.SPECTATE_EVENT"),
    JOINED_EVENT_BROADCAST("event.JOINED_EVENT_BROADCAST"),
    LEFT_EVENT_BROADCAST("event.LEFT_EVENT_BROADCAST"),
    EVENT_FORCE_ENDED("event.EVENT_FORCE_ENDED"),
    EVENT_ENDING("event.EVENT_ENDING"),
    ALREADY_IN_EVENT("event.ALREADY_IN_EVENT"),
    DOES_NOT_EXIST("event.DOES_NOT_EXIST"),
    EVENT_DISABLED("event.EVENT_DISABLED"),
    NOT_IN_EVENT("event.NOT_IN_EVENT"),
    INVENTORY_NOT_EMPTY("event.INVENTORY_NOT_EMPTY"),
    PLAYER_DEAD("event.PLAYER_DEAD"),
    CRAFT_IN_EVENT("event.CRAFT_IN_EVENT"),
    NO_PERMISSION("event.NO_PERMISSION"),
    HOST_COOLDOWN("event.HOST_COOLDOWN"),
    NOT_ENOUGH_PEOPLE("event.NOT_ENOUGH_PEOPLE"),
    INVALID_EVENT("event.INVALID_EVENT"),
    ERROR("event.ERROR"),
    BLOCKED_COMMAND("event.BLOCKED_COMMAND"),
    JOIN_COOLDOWN("event.JOIN_COOLDOWN"),
    HOSTING_EVENT("event.HOSTING_EVENT"),
    STARTING_IN("event.STARTING_IN"),
    CLICK_TO_JOIN("event.CLICK_TO_JOIN"),

    // sumo
    SUMO_MATCH_STARTING("sumo.MATCH_STARTING"),
    SUMO_MATCH_COUNTER("sumo.MATCH_COUNTER"),
    SUMO_MATCH_STARTED("sumo.MATCH_STARTED"),
    SUMO_ELIMINATED("sumo.ELIMINATED"),
    SUMO_TEAM_ELIMINATED("sumo.TEAM_ELIMINATED"),
    SUMO_TEAM_OF_ONE("sumo.TEAM_OF_ONE"),
    SUMO_TEAM_OF_TWO("sumo.TEAM_OF_TWO"),
    SUMO_TEAM_OF_THREE("sumo.TEAM_OF_THREE"),

    // brackets
    BRACKETS_MATCH_STARTING("brackets.MATCH_STARTING"),
    BRACKETS_MATCH_COUNTER("brackets.MATCH_COUNTER"),
    BRACKETS_MATCH_STARTED("brackets.MATCH_STARTED"),
    BRACKETS_ELIMINATED("brackets.ELIMINATED"),

    // koth
    KOTH_CAPTURING("koth.CAPTURING"),
    KOTH_CAPTURING_POINTS("koth.CAPTURING_POINTS"),
    KOTH_LOST("koth.LOST"),

    // lms
    LMS_MATCH_STARTING("lms.MATCH_STARTING"),
    LMS_MATCH_COUNTER("lms.MATCH_COUNTER"),
    LMS_MATCH_STARTED("lms.MATCH_STARTED"),
    LMS_ELIMINATED("lms.ELIMINATED"),

    // oitc
    OITC_KILL_MESSAGE("oitc.KILL_MESSAGE"),

    // redrover
    REDROVER_ELIMINATED("redrover.ELIMINATED"),
    REDROVER_ELIMINATED_BY("redrover.ELIMINATED_BY"),
    REDROVER_ROUND_STARTING("redrover.ROUND_STARTING"),
    REDROVER_RUN_TO("redrover.RUN_TO"),
    REDROVER_SELECTED("redrover.SELECTED"),

    // rod
    ROD_START("rod.START"),

    // spleef
    SPLEEF_MATCH_COUNTER("spleef.MATCH_COUNTER"),
    SPLEEF_MATCH_STARTED("spleef.MATCH_STARTED"),
    SPLEEF_ELIMINATED("spleef.ELIMINATED"),

    // tdm
    TDM_MATCH_STARTING("tdm.MATCH_STARTING"),
    TDM_MATCH_COUNTER("tdm.MATCH_COUNTER"),
    TDM_MATCH_STARTED("tdm.MATCH_STARTED"),
    TDM_ELIMINATED("tdm.ELIMINATED"),
    TDM_TOP_5("tdm.TOP_5"),

    // tnttag
    TNTTAG_ROUND_STARTING("tnttag.ROUND_STARTING"),
    TNTTAG_TAG("tnttag.TAG"),
    TNTTAG_ELIMINATED("tnttag.ELIMINATED"),

    // waterdrop
    WATERDROP_ROUND_STARTING("waterdrop.ROUND_STARTING"),
    WATERDROP_SUCCESS_JUMP("waterdrop.SUCCESS_JUMP"),
    WATERDROP_FAIL_JUMP("waterdrop.FAIL_JUMP"),
    WATERDROP_ELIMINATED("waterdrop.ELIMINATED"),

    // woolshuffle
    WOOLSHUFFLE_ROUND_STARTING("woolshuffle.ROUND_STARTING"),
    WOOLSHUFFLE_FAILED("woolshuffle.FAILED"),
    WOOLSHUFFLE_ELIMINATED("woolshuffle.ELIMINATED"),
    WOOLSHUFFLE_COLOUR("woolshuffle.COLOUR"),
    WOOLSHUFFLE_PVP_ENABLED("woolshuffle.PVP_ENABLED"),
    WOOLSHUFFLE_PVP_DISABLED("woolshuffle.PVP_DISABLED"),

    // scoreboard
    TITLE_EVENT_STARTING("scoreboard.EVENT_STARTING"),
    TITLE_EVENT_ENDED("scoreboard.EVENT_ENDED"),
    TITLE_SUMO1V1("scoreboard.SUMO1V1"),
    TITLE_SUMO2V2("scoreboard.SUMO2V2"),
    TITLE_SUMO3V3("scoreboard.SUMO3V3"),
    TITLE_BRACKETS("scoreboard.BRACKETS"),
    TITLE_KOTH("scoreboard.KOTH"),
    TITLE_LMS("scoreboard.LMS"),
    TITLE_OITC("scoreboard.OITC"),
    TITLE_REDROVER("scoreboard.REDROVER"),
    TITLE_ROD("scoreboard.ROD"),
    TITLE_SPLEEF("scoreboard.SPLEEF"),
    TITLE_TDM("scoreboard.TDM"),
    TITLE_TNTTAG("scoreboard.TNTTAG"),
    TITLE_WATERDROP("scoreboard.WATERDROP"),
    TITLE_WOOLSHUFFLE("scoreboard.WOOLSHUFFLE");

    private final String path;
    private String message;

    Message(String path) {
        this.path = path;
        this.message = ChatColor.RED + "NOT_LOADED";
    }

    @Override
    public String get() {
        return message;
    }

    @Override
    public void set(String message) {
        this.message = message;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return get();
    }
}
