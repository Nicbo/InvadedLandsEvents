package me.nicbo.InvadedLandsEvents.party;

import org.bukkit.*;
import java.util.*;
import org.bukkit.entity.*;

/**
 * Party class
 *
 * @author StarZorrow
 * @since 2020-03-12
 */

public class EventParty {
    private List<UUID> memberUUIDs;
    private UUID partyLeader;
    private String leaderName;
    private EventPartyState partyState;
    private boolean open;

    public EventParty(UUID partyLeader, String leaderName) {
        this.memberUUIDs = new ArrayList<>();
        this.partyLeader = partyLeader;
        this.leaderName = leaderName;
        this.partyState = EventPartyState.NOT_IN_EVENT;
    }

    public void addMember(UUID uuid) {
        this.memberUUIDs.add(uuid);
    }

    public void removeMember(UUID uuid) {
        this.memberUUIDs.remove(uuid);
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public void setPartyState(EventPartyState state) {
        this.partyState = state;
    }

    public List<UUID> getMembers() {
        return this.memberUUIDs;
    }

    public UUID getLeader() {
        return this.partyLeader;
    }

    public String getLeaderName() {
        return this.leaderName;
    }

    public boolean isOpen() {
        return this.open;
    }

    public EventPartyState getPartyState() {
        return this.partyState;
    }

    public int getSize() {
        return this.getMembers().size() + 1;
    }

    public List<UUID> getAllMembersOnline() {
        List<UUID> membersOnline = new ArrayList<>();
        for (UUID memberUUID : this.memberUUIDs) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                membersOnline.add(member.getUniqueId());
            }
        }
        membersOnline.add(this.partyLeader);
        return membersOnline;
    }
}