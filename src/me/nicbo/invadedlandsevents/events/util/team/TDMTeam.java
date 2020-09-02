package me.nicbo.invadedlandsevents.events.util.team;

import me.nicbo.invadedlandsevents.events.util.team.EventTeam;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.*;

/**
 * TDM Team
 *
 * @author Nicbo
 */

public final class TDMTeam extends EventTeam {
    private final ItemStack[] kit;
    private final ItemStack[] armour;

    private final Location spawnLoc;
    private final Map<Player, Integer> kills;

    public TDMTeam(String name, Color colour, Location spawnLoc) {
        super(name);

        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
        meta.setColor(colour);
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
        meta.addEnchant(Enchantment.DURABILITY, 3, true);
        helmet.setItemMeta(meta);

        kit = new ItemStack[]{
                new ItemStack(Material.IRON_SWORD),
                new ItemStack(Material.BOW),
                new ItemStack(Material.GOLDEN_APPLE, 10),
                new ItemStack(Material.ARROW, 32)
        };

        armour = new ItemStack[]{
                new ItemStack(Material.IRON_BOOTS, 1),
                new ItemStack(Material.IRON_LEGGINGS, 1),
                new ItemStack(Material.IRON_CHESTPLATE, 1),
                helmet
        };

        this.spawnLoc = spawnLoc;
        this.kills = new HashMap<>();
    }

    public void preparePlayers() {
        for (Player player : this.getPlayers()) {
            player.teleport(spawnLoc);
            player.getInventory().setArmorContents(armour);
            player.getInventory().setContents(kit);
        }
    }

    @Override
    public void addPlayer(Player player) {
        super.addPlayer(player);
        kills.put(player, 0);
    }

    public int getKills(Player player) {
        // Default for safety, should never happen
        return kills.getOrDefault(player, 0);
    }

    public void addKill(Player player) {
        kills.put(player, getKills(player) + 1);
    }

    @Override
    public void clear() {
        super.clear();
        kills.clear();
    }

    public Map<Player, Integer> getSortedKills() {
        List<Map.Entry<Player, Integer>> list = new LinkedList<>(kills.entrySet());

        // Sort the list based on values
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        Map<Player, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Player, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}