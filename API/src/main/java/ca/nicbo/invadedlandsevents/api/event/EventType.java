package ca.nicbo.invadedlandsevents.api.event;

import ca.nicbo.invadedlandsevents.api.permission.EventPermission;
import org.jetbrains.annotations.NotNull;

/**
 * The different {@link Event} types.
 *
 * @author Nicbo
 */
public enum EventType {
    BRACKETS_1V1("1v1 Brackets", "brackets1v1", EventPermission.HOST_BRACKETS_1V1),
    BRACKETS_2V2("2v2 Brackets", "brackets2v2", EventPermission.HOST_BRACKETS_2V2),
    BRACKETS_3V3("3v3 Brackets", "brackets3v3", EventPermission.HOST_BRACKETS_3V3),
    KING_OF_THE_HILL("King of the Hill", "koth", EventPermission.HOST_KOTH),
    LAST_MAN_STANDING("Last Man Standing", "lms", EventPermission.HOST_LMS),
    ONE_IN_THE_CHAMBER("One in the Chamber", "oitc", EventPermission.HOST_OITC),
    REDROVER("Redrover", "redrover", EventPermission.HOST_REDROVER),
    RACE_OF_DEATH("Race of Death", "rod", EventPermission.HOST_ROD),
    SPLEEF("Spleef", "spleef", EventPermission.HOST_SPLEEF),
    SUMO_1V1("1v1 Sumo", "sumo1v1", EventPermission.HOST_SUMO_1V1),
    SUMO_2V2("2v2 Sumo", "sumo2v2", EventPermission.HOST_SUMO_2V2),
    SUMO_3V3("3v3 Sumo", "sumo3v3", EventPermission.HOST_SUMO_3V3),
    TEAM_DEATHMATCH("Team Deathmatch", "tdm", EventPermission.HOST_TDM),
    TNT_TAG("TNT Tag", "tnttag", EventPermission.HOST_TNTTAG),
    WATERDROP("Waterdrop", "waterdrop", EventPermission.HOST_WATERDROP),
    WOOL_SHUFFLE("Wool Shuffle", "woolshuffle", EventPermission.HOST_WOOLSHUFFLE);

    private final String displayName;
    private final String configName;
    private final String permission;

    EventType(@NotNull String displayName, @NotNull String configName, @NotNull String permission) {
        this.displayName = displayName;
        this.configName = configName;
        this.permission = permission;
    }

    /**
     * Returns the display name of the event type.
     *
     * @return the display name
     */
    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the config name of the event type.
     *
     * @return the config name
     */
    @NotNull
    public String getConfigName() {
        return configName;
    }

    /**
     * Returns the permission node required to host this event type.
     *
     * @return the permission node
     */
    @NotNull
    public String getPermission() {
        return permission;
    }
}
