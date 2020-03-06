package me.nicbo.InvadedLandsEvents.manager.managers;

import java.util.*;

import me.nicbo.InvadedLandsEvents.manager.Manager;
import me.nicbo.InvadedLandsEvents.manager.ManagerHandler;
import me.nicbo.InvadedLandsEvents.party.EventParty;
import org.bukkit.entity.*;

public class EventPartyManager extends Manager {
    private Map<UUID, EventParty> leaderUUIDtoParty;
    private Map<UUID, UUID> playerUUIDtoLeaderUUID;

    public EventPartyManager(ManagerHandler handler) {
        super(handler);
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
        Player leaderPlayer = this.handler.getPlugin().getServer().getPlayer(party.getLeader());
        leaderPlayer.sendMessage(message);
        for (UUID uuid : party.getMembers()) {
            Player memberPlayer = this.handler.getPlugin().getServer().getPlayer(uuid);
            if (memberPlayer == null) continue;
            memberPlayer.sendMessage(message);
        }
    }
}
