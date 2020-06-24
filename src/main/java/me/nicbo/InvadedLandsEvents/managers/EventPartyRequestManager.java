package me.nicbo.InvadedLandsEvents.managers;

import java.util.*;

import org.bukkit.entity.*;

/**
 * Handles party requests
 *
 * @author StarZorrow
 * @since 2020-03-12
 */

public final class EventPartyRequestManager {
    private Map<UUID, List<UUID>> partyRequestMap;

    public EventPartyRequestManager() {
        this.partyRequestMap = new HashMap<>();
    }

    public void addPartyRequest(Player requested, Player requester) {
        if (!this.hasPartyRequests(requested)) {
            this.partyRequestMap.put(requested.getUniqueId(), new ArrayList<>());
        }
        this.partyRequestMap.get(requested.getUniqueId()).add(requester.getUniqueId());
    }

    public boolean hasPartyRequestFromPlayer(Player requested, Player requester) {
        return this.partyRequestMap.get(requested.getUniqueId()).contains(requester.getUniqueId());
    }

    public boolean hasPartyRequests(Player player) {
        return this.partyRequestMap.containsKey(player.getUniqueId());
    }

    public void removePartyRequest(Player requested, Player requester) {
        this.partyRequestMap.get(requested.getUniqueId()).remove(requester.getUniqueId());
    }
}
