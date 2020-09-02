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
    TDM_WINNERS("tdm.WINNERS");

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
