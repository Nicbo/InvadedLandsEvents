package me.nicbo.InvadedLandsEvents.gui;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Host gui for /e host
 *
 * @author Nicbo
 * @since 2020-06-30
 */

public final class HostGUI extends GUI {
    private final static EventManager eventManager;

    static {
        eventManager = EventsMain.getManagerHandler().getEventManager();
    }

    public HostGUI(Player player) {
        super("Host an Event", 36, player);

        // Events
        this.setItem(10, Material.DIAMOND_SWORD, "&e&lBrackets", createLore("brackets","&7Beat your opponents in a head to head battle!"), () -> tryHost("brackets"));
        this.setItem(11, Material.GOLD_BLOCK, "&e&lKing of The Hill", createLore("koth","&7The longer you capture the hill", "&7the more points you will collect.", "&7You require " + eventManager.getEventConfig().getInt("koth.int-win-points") + " in-order to win the event."), () -> tryHost("koth"));
        this.setItem(12, Material.IRON_SWORD, "&e&lLast Man Standing", createLore("lms", "&7Be the last man standing! Kill all your opponents!"), () -> tryHost("lms"));
        this.setItem(13, Material.BOW, "&e&lOne in The Chamber", createLore("oitc","&7The first person to obtain " + eventManager.getEventConfig().getInt("oitc.int-win-points") + " points", "&7in this free-for-all match, will win."), () -> tryHost("oitc"));
        this.setItem(14, Material.RAILS, "&e&lRace of Death", createLore("rod", "&7The first player to make it to the end of the parkour wins!"), () -> tryHost("rod"));
        this.setItem(15, Material.REDSTONE_BLOCK, "&e&lRedrover", createLore("redrover", "&7Try to cross the middle without dying!"), () -> tryHost("redrover"));
        this.setItem(16, Material.DIAMOND_SPADE, "&e&lSpleef", createLore("spleef", "&7Try and break the blocks under other players!"), () -> tryHost("spleef"));
        this.setItem(20, Material.LEASH, "&e&lSumo", createLore("sumo", "&7Knock your opponent off the platform"), () -> {
            close();
            SumoHostGUI sumoHostGUI = new SumoHostGUI();
            sumoHostGUI.open();
        });
        this.setItem(21, Material.GOLD_SWORD, "&e&lTeam Deathmatch", createLore("tdm", "&cRed &7vs &9Blue&7", "", "&7Classic Team Deathmatch where two teams", "&7face against each other, and which ever", "&7team is last standing wins!", "&7The top 5 killers on the winning team obtain event keys"), () -> tryHost("tdm"));
        this.setItem(22, Material.TNT, "&e&lTNT Tag", createLore("tnttag", "&7Don't get stuck with the TNT!"), () -> tryHost("tnttag"));
        this.setItem(23, Material.WATER_BUCKET, "&e&lWaterdrop", createLore("waterdrop", "&7The goal of this game is to jump into the water!", "&7It will get progressively harder,", "&7and the hole will get smaller."), () -> tryHost("waterdrop"));
        this.setItem(24, Material.WOOL, 6, "&e&lWool Shuffle", createLore("woolshuffle", "&7Your hotbar will fill up with a colour of wool", "&7each round and you must run around the arena", "&7and stand on the colour of wool which", "&7fills your hotbar!"), () -> tryHost("woolshuffle"));

        // Rainbow background
        for (int i = 0, j = 6; i < 36; i++, j++) {
            if (j == 15) {
                j = 6;
            }
            if (this.isSlotEmpty(i)) {
                this.setBlankItem(i, j);
            }
        }
    }

    /**
     * Attempts to host event, if event is hosted the gui is closed
     * @param event Event name to host
     */

    private void tryHost(String event) {
        String hostMessage = eventManager.hostEvent(event, player);
        if (hostMessage == null) {
            close();
        } else {
            player.sendMessage(hostMessage);
        }
    }

    /**
     * Creates lore for item desc
     * @param event Event config name used for permission/cooldown check
     * @param lines Description of event
     * @return lore
     */

    private List<String> createLore(String event, String... lines) {
        List<String> lore = new ArrayList<>(Arrays.asList(lines));
        lore.add("");
        // perm check
        // cooldown check
        lore.add("&aLeft click to host");
        return GeneralUtils.translateAlternateColorCodes(lore);
    }

    private final class SumoHostGUI extends GUI {
        public SumoHostGUI() {
            super("Host Sumo", 9, HostGUI.this.player);
            this.setItem(0, Material.WOOL, "&e1 vs. 1", null, () -> tryHost("sumo1v1"));
            this.setItem(1, Material.WOOL, "&e2 vs. 2", null, () -> tryHost("sumo2v2"));
            this.setItem(2, Material.WOOL, "&e3 vs. 3", null, () -> tryHost("sumo3v3"));
        }
    }


    /*
    TODO:
        - cooldown in lore
        - perms in lore (Can't host or can host)
        - sumo sub gui
     */
}
