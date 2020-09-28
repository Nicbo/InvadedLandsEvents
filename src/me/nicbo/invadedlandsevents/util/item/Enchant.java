package me.nicbo.invadedlandsevents.util.item;

import org.bukkit.enchantments.Enchantment;

/**
 * Represents an enchantment
 *
 * @author Nicbo
 */

public class Enchant {
    private final Enchantment enchant;
    private final int level;

    /**
     * Creates instance of the enchant
     *
     * @param enchant the enchantment type
     * @param level   the level of the enchant
     */
    public Enchant(Enchantment enchant, int level) {
        this.enchant = enchant;
        this.level = level;
    }

    /**
     * Get the enchantment type
     *
     * @return the enchantment type
     */
    public Enchantment getEnchant() {
        return enchant;
    }

    /**
     * Get the enchantment level
     *
     * @return the enchantment level
     */
    public int getLevel() {
        return level;
    }
}