package me.nicbo.invadedlandsevents.gui.host;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.gui.button.Button;
import me.nicbo.invadedlandsevents.util.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Host GUI for /e host
 *
 * @author Nicbo
 */

public final class MainHostGUI extends HostGUI {
    private static final short[] WOOL_IDS = { 1, 4, 5, 6, 9, 10, 11, 14 };

    private int index;

    public MainHostGUI(InvadedLandsEvents plugin, Player player) {
        super(plugin, player, "Host an Event", 36);

        // Events
        this.setButton(10, new Button(new ItemBuilder(Material.DIAMOND_SWORD)
                .setName("&e&lBrackets")
                .setLore(createLore("&7Beat your opponents in a head to head battle!"))
                .setHideAttributes(true)
                .build(), () -> tryHost("brackets"), "brackets"));

        this.setButton(11, new Button(new ItemBuilder(Material.GOLD_BLOCK)
                .setName("&e&lKing of The Hill")
                .setLore(createLore("&7The longer you capture the hill", "&7the more points you will collect.", "&7You require " + plugin.getConfig().getInt("events.koth.int-win-points.value") + " in-order to win the event."))
                .setHideAttributes(true)
                .build(), () -> tryHost("koth"), "koth"));

        this.setButton(12, new Button(new ItemBuilder(Material.IRON_SWORD)
                .setName("&e&lLast Man Standing")
                .setLore(createLore("&7Be the last man standing! Kill all your opponents!"))
                .setHideAttributes(true)
                .build(), () -> tryHost("lms"), "lms"));

        this.setButton(13, new Button(new ItemBuilder(Material.BOW)
                .setName("&e&lOne in The Chamber")
                .setLore(createLore("&7The first person to obtain " + plugin.getConfig().getInt("events.oitc.int-win-points.value") + " points", "&7in this free-for-all match, will win."))
                .setHideAttributes(true)
                .build(), () -> tryHost("oitc"), "oitc"));

        this.setButton(14, new Button(new ItemBuilder(Material.RAILS)
                .setName("&e&lRace of Death")
                .setLore(createLore("&7The first player to make it to the end of the parkour wins!"))
                .setHideAttributes(true)
                .build(), () -> tryHost("rod"), "rod"));

        this.setButton(15, new Button(new ItemBuilder(Material.REDSTONE_BLOCK)
                .setName("&e&lRedrover")
                .setLore(createLore("&7Try to cross the middle without dying!"))
                .setHideAttributes(true)
                .build(), () -> tryHost("redrover"), "redrover"));

        this.setButton(16, new Button(new ItemBuilder(Material.DIAMOND_SPADE)
                .setName("&e&lSpleef")
                .setLore(createLore("&7Try and break the blocks under other players!"))
                .setHideAttributes(true)
                .build(), () -> tryHost("spleef"), "spleef"));

        this.setButton(20, new Button(new ItemBuilder(Material.LEASH)
                .setName("&e&lSumo")
                .setLore(createLore("&7Knock your opponent off the platform"))
                .setHideAttributes(true)
                .build(), () -> {
            close();
            SumoHostGUI sumoHostGUI = new SumoHostGUI(plugin, player);
            sumoHostGUI.open();
        }, "sumo"));

        this.setButton(21, new Button(new ItemBuilder(Material.GOLD_SWORD)
                .setName("&e&lTeam Deathmatch")
                .setLore(createLore("&cRed &7vs &9Blue&7", "", "&7Classic Team Deathmatch where two teams", "&7face against each other, and which ever", "&7team is last standing wins!", "&7The top 5 killers on the winning team obtain event keys"))
                .setHideAttributes(true)
                .build(), () -> tryHost("tdm"), "tdm"));

        this.setButton(22, new Button(new ItemBuilder(Material.TNT)
                .setName("&e&lTNT Tag")
                .setLore(createLore("&7Don't get stuck with the TNT!"))
                .setHideAttributes(true)
                .build(), () -> tryHost("tnttag"), "tnttag"));

        this.setButton(23, new Button(new ItemBuilder(Material.WATER_BUCKET)
                .setName("&e&lWaterdrop")
                .setLore(createLore("&7The goal of this game is to jump into the water!", "&7It will get progressively harder,", "&7and the hole will get smaller."))
                .setHideAttributes(true)
                .build(), () -> tryHost("waterdrop"), "waterdrop"));

        this.setButton(24, new Button(new ItemBuilder(Material.WOOL)
                .setName("&e&lWool Shuffle")
                .setLore(createLore("&7Your hotbar will fill up with a colour of wool", "&7each round and you must run around the arena", "&7and stand on the colour of wool which", "&7fills your hotbar!"))
                .setDurability(WOOL_IDS[index])
                .setHideAttributes(true)
                .build(), () -> tryHost("woolshuffle"), "woolshuffle"));

        // Rainbow background
        for (int i = 0, j = 1; i < 36; i++, j++) {
            if (j == 10) {
                j = 1;
            }
            if (this.isSlotEmpty(i)) {
                this.addBlankSlot(i, (byte) j);
            }
        }

        this.update();
    }

    @Override
    public void update() {
        if (++index == WOOL_IDS.length) {
            index = 0;
        }

        this.getButton(24).getItem().setDurability(WOOL_IDS[index]);
        super.update();
    }
}
