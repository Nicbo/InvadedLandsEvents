package me.nicbo.InvadedLandsEvents.manager.managers;

import java.util.*;

import me.nicbo.InvadedLandsEvents.manager.Manager;
import me.nicbo.InvadedLandsEvents.manager.ManagerHandler;
import org.bukkit.entity.*;

/**
 * Handles party requests
 *
 * @author StarZorroww
 * @since 2020-03-12
 */

public class EventPartyRequestManager extends Manager
{
    private Map<UUID, List<UUID>> partyRequestMap;

    public EventPartyRequestManager(ManagerHandler handler) {
        super(handler);
        this.partyRequestMap = new HashMap<>();
    }

    public void addPartyRequest(Player requested, Player requester) {
        if (!this.hasPartyRequests(requested)) {
            this.partyRequestMap.put(requested.getUniqueId(), new ArrayList<UUID>());
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
