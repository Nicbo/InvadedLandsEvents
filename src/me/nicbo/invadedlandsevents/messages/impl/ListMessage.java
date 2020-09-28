package me.nicbo.invadedlandsevents.messages.impl;

import me.nicbo.invadedlandsevents.messages.IMessage;
import org.bukkit.ChatColor;

import java.util.Collections;
import java.util.List;

/**
 * Messages for InvadedLandsEvents that are lists
 *
 * @author Nicbo
 */

public enum ListMessage implements IMessage<List<String>> {
    INFO_MESSAGES("event.INFO_MESSAGES"),
    USAGE_MESSAGES("event.USAGE_MESSAGES"),
    WIN_MESSAGES("event.WIN_MESSAGES"),
    TDM_WINNERS("tdm.WINNERS"),

    SUMO1V1_DESCRIPTION("description.SUMO1V1"),
    SUMO2V2_DESCRIPTION("description.SUMO2V2"),
    SUMO3V3_DESCRIPTION("description.SUMO3V3"),
    BRACKETS_DESCRIPTION("description.BRACKETS"),
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

    private final String path;
    private List<String> messages;

    ListMessage(String path) {
        this.path = path;
        this.messages = Collections.singletonList(ChatColor.RED + "NOT_LOADED");
    }

    @Override
    public List<String> get() {
        return messages;
    }

    @Override
    public void set(List<String> message) {
        this.messages = message;
    }

    @Override
    public String getPath() {
        return path;
    }
}
