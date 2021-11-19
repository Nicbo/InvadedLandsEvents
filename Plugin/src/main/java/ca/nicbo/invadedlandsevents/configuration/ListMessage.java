package ca.nicbo.invadedlandsevents.configuration;

import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * All customizable list messages.
 *
 * @author Nicbo
 */
public enum ListMessage {
    INFO_MESSAGES("general.INFO_MESSAGES"),
    STATS_MESSAGES("general.STATS_MESSAGES"),
    USAGE_MESSAGES("general.USAGE_MESSAGES"),
    WIN_MESSAGES("general.WIN_MESSAGES"),

    TDM_WINNERS("tdm.WINNERS"),
    TDM_WINNERS_LIST("tdm.WINNERS_LIST"),

    BRACKETS1V1_DESCRIPTION("description.BRACKETS1V1"),
    BRACKETS2V2_DESCRIPTION("description.BRACKETS2V2"),
    BRACKETS3V3_DESCRIPTION("description.BRACKETS3V3"),
    SUMO1V1_DESCRIPTION("description.SUMO1V1"),
    SUMO2V2_DESCRIPTION("description.SUMO2V2"),
    SUMO3V3_DESCRIPTION("description.SUMO3V3"),
    KOTH_DESCRIPTION("description.KOTH"),
    LMS_DESCRIPTION("description.LMS"),
    OITC_DESCRIPTION("description.OITC"),
    REDROVER_DESCRIPTION("description.REDROVER"),
    ROD_DESCRIPTION("description.ROD"),
    SPLEEF_DESCRIPTION("description.SPLEEF"),
    TDM_DESCRIPTION("description.TDM"),
    TNTTAG_DESCRIPTION("description.TNTTAG"),
    WATERDROP_DESCRIPTION("description.WATERDROP"),
    WOOLSHUFFLE("description.WOOLSHUFFLE");

    private static FileConfiguration config;

    private final String path;

    ListMessage(String path) {
        this.path = path;
    }

    public List<String> get() {
        Validate.checkState(config != null, "config has not been set yet");
        List<String> messages = config.getStringList(path);
        Validate.checkNotNull(messages.isEmpty() ? null : messages, "could not find %s", path);
        return StringUtils.colour(messages);
    }

    public String getPath() {
        return path;
    }

    static void setConfig(FileConfiguration config) {
        Validate.checkArgumentNotNull(config, "config");
        ListMessage.config = config;
    }
}
