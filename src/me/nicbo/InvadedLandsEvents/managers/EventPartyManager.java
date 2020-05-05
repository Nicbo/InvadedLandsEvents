package me.nicbo.InvadedLandsEvents.managers;

import java.util.*;

import me.nicbo.InvadedLandsEvents.handlers.ManagerHandler;
import me.nicbo.InvadedLandsEvents.party.EventParty;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;

/**
 * Handles parties
 *
 * @author StarZorrow
 * @since 2020-03-12
 */

public final class EventPartyManager {
    private Map<UUID, EventParty> leaderUUIDtoParty;
    private Map<UUID, UUID> playerUUIDtoLeaderUUID;

    public EventPartyManager() {
        this.leaderUUIDtoParty = new HashMap<>();
        this.playerUUIDtoLeaderUUID = new HashMap<>();
    }

    public EventParty getParty(UUID player) {
        if (this.leaderUUIDtoParty.containsKey(player)) return this.leaderUUIDtoParty.get(player);
        if (this.playerUUIDtoLeaderUUID.containsKey(player)) {
            UUID leader = this.playerUUIDtoLeaderUUID.get(player);
            return this.leaderUUIDtoParty.get(leader);
        }
        return null;
    }

    public Map<UUID, EventParty> getPartyMap() {
        return this.leaderUUIDtoParty;
    }

    public EventParty createParty(UUID leader, String leaderName) {
        EventParty party = new EventParty(leader, leaderName);
        this.leaderUUIDtoParty.put(leader, party);
        return party;
    }

    public void destroyParty(UUID leader) {
        EventParty party = this.leaderUUIDtoParty.get(leader);
        this.leaderUUIDtoParty.remove(leader);
        for (UUID member : party.getMembers()) { this.playerUUIDtoLeaderUUID.remove(member); }
    }

    public void leaveParty(UUID player) {
        UUID leader = this.playerUUIDtoLeaderUUID.get(player);
        this.playerUUIDtoLeaderUUID.remove(player);
        EventParty party = this.leaderUUIDtoParty.get(leader);
        party.removeMember(player);
    }

    public void joinParty(UUID leader, UUID player) {
        EventParty party = this.leaderUUIDtoParty.get(leader);
        party.addMember(player);
        this.playerUUIDtoLeaderUUID.put(player, leader);
    }

    public void notifyParty(EventParty party, String message) {
        Player leaderPlayer = Bukkit.getPlayer(party.getLeader());
        leaderPlayer.sendMessage(message);
        for (UUID uuid : party.getMembers()) {
            Player memberPlayer = Bukkit.getPlayer(uuid);
            if (memberPlayer == null) continue;
            memberPlayer.sendMessage(message);
        }
    }
}
