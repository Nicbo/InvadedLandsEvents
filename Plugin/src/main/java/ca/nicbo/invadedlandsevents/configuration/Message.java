package ca.nicbo.invadedlandsevents.configuration;

import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * All customizable messages.
 *
 * @author Nicbo
 */
public enum Message {
    // general
    EVENT_NOT_RUNNING("general.EVENT_NOT_RUNNING"),
    JOIN_ALREADY_STARTED("general.JOIN_ALREADY_STARTED"),
    HOST_ALREADY_STARTED("general.HOST_ALREADY_STARTED"),
    FORCEEND_EVENT("general.FORCEEND_EVENT"),
    SPECTATE_EVENT("general.SPECTATE_EVENT"),
    JOINED_EVENT("general.JOINED_EVENT"),
    LEFT_EVENT("general.LEFT_EVENT"),
    EVENT_FORCE_ENDED("general.EVENT_FORCE_ENDED"),
    EVENT_ENDING("general.EVENT_ENDING"),
    ALREADY_IN_EVENT("general.ALREADY_IN_EVENT"),
    DOES_NOT_EXIST("general.DOES_NOT_EXIST"),
    EVENT_DISABLED("general.EVENT_DISABLED"),
    NOT_IN_EVENT("general.NOT_IN_EVENT"),
    INVENTORY_NOT_EMPTY("general.INVENTORY_NOT_EMPTY"),
    PLAYER_DEAD("general.PLAYER_DEAD"),
    CRAFT_IN_EVENT("general.CRAFT_IN_EVENT"),
    NO_PERMISSION("general.NO_PERMISSION"),
    HOST_COOLDOWN("general.HOST_COOLDOWN"),
    NOT_ENOUGH_PEOPLE("general.NOT_ENOUGH_PEOPLE"),
    ERROR("general.ERROR"),
    BLOCKED_COMMAND("general.BLOCKED_COMMAND"),
    JOIN_COOLDOWN("general.JOIN_COOLDOWN"),
    HOSTING_EVENT("general.HOSTING_EVENT"),
    STARTING_IN("general.STARTING_IN"),
    CLICK_TO_JOIN("general.CLICK_TO_JOIN"),
    PLAYER_NOT_FOUND("general.PLAYER_NOT_FOUND"),

    // brackets
    BRACKETS_MATCH_STARTING("brackets.MATCH_STARTING"),
    BRACKETS_MATCH_COUNTER("brackets.MATCH_COUNTER"),
    BRACKETS_MATCH_STARTED("brackets.MATCH_STARTED"),
    BRACKETS_ELIMINATED("brackets.ELIMINATED"),
    BRACKETS_TEAM_ELIMINATED("brackets.TEAM_ELIMINATED"),
    BRACKETS_TEAM_OF_ONE("brackets.TEAM_OF_ONE"),
    BRACKETS_TEAM_OF_TWO("brackets.TEAM_OF_TWO"),
    BRACKETS_TEAM_OF_THREE("brackets.TEAM_OF_THREE"),

    // koth
    KOTH_START("koth.START"),
    KOTH_CAPTURING("koth.CAPTURING"),
    KOTH_CAPTURING_POINTS("koth.CAPTURING_POINTS"),
    KOTH_LOST("koth.LOST"),

    // lms
    LMS_MATCH_STARTING("lms.MATCH_STARTING"),
    LMS_MATCH_COUNTER("lms.MATCH_COUNTER"),
    LMS_MATCH_STARTED("lms.MATCH_STARTED"),
    LMS_ELIMINATED("lms.ELIMINATED"),

    // oitc
    OITC_ELIMINATED_BY("oitc.ELIMINATED_BY"),

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

    // sumo
    SUMO_MATCH_STARTING("sumo.MATCH_STARTING"),
    SUMO_MATCH_COUNTER("sumo.MATCH_COUNTER"),
    SUMO_MATCH_STARTED("sumo.MATCH_STARTED"),
    SUMO_ELIMINATED("sumo.ELIMINATED"),
    SUMO_TEAM_ELIMINATED("sumo.TEAM_ELIMINATED"),
    SUMO_TEAM_OF_ONE("sumo.TEAM_OF_ONE"),
    SUMO_TEAM_OF_TWO("sumo.TEAM_OF_TWO"),
    SUMO_TEAM_OF_THREE("sumo.TEAM_OF_THREE"),

    // tdm
    TDM_MATCH_STARTING("tdm.MATCH_STARTING"),
    TDM_MATCH_COUNTER("tdm.MATCH_COUNTER"),
    TDM_MATCH_STARTED("tdm.MATCH_STARTED"),
    TDM_ELIMINATED("tdm.ELIMINATED"),

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
    WOOLSHUFFLE_COLOR("woolshuffle.COLOR"),
    WOOLSHUFFLE_PVP_ENABLED("woolshuffle.PVP_ENABLED"),
    WOOLSHUFFLE_PVP_DISABLED("woolshuffle.PVP_DISABLED"),

    // scoreboard
    TITLE_EVENT_STARTING("scoreboard.EVENT_STARTING"),
    TITLE_EVENT_ENDING("scoreboard.EVENT_ENDING"),
    TITLE_SUMO1V1("scoreboard.SUMO1V1"),
    TITLE_SUMO2V2("scoreboard.SUMO2V2"),
    TITLE_SUMO3V3("scoreboard.SUMO3V3"),
    TITLE_BRACKETS1V1("scoreboard.BRACKETS1V1"),
    TITLE_BRACKETS2V2("scoreboard.BRACKETS2V2"),
    TITLE_BRACKETS3V3("scoreboard.BRACKETS3V3"),
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

    private static FileConfiguration config;

    private final String path;

    Message(String path) {
        this.path = path;
    }

    public String get() {
        Validate.checkState(config != null, "config has not been set yet");
        String message = config.getString(path);
        Validate.checkNotNull(message, "could not find %s", path);
        return StringUtils.colour(message);
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return get();
    }

    static void setConfig(FileConfiguration config) {
        Validate.checkArgumentNotNull(config, "config");
        Message.config = config;
    }
}
