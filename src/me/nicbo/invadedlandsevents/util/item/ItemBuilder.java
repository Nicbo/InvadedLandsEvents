package me.nicbo.invadedlandsevents.util.item;

import me.nicbo.invadedlandsevents.util.StringUtils;
import me.nicbo.invadedlandsevents.util.misc.Builder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Builder class for creating ItemStacks
 *
 * @author Nicbo
 */

public final class ItemBuilder implements Builder<ItemStack> {
    private final Material material;
    private String name;
    private Enchant[] enchants;
    private int amount;
    private List<String> lore;
    private short durability;
    private boolean hideAttributes;

    /**
     * Creates instance of ItemBuilder
     *
     * @param material the material
     */
    public ItemBuilder(Material material) {
        this.material = material;
        this.name = null;
        this.enchants = new Enchant[0];
        this.amount = 1;
        this.lore = null;
        this.durability = -1;
        this.hideAttributes = false;
    }

    /**
     * Sets the item's name
     *
     * @param name the name
     * @return the ItemBuilder instance
     */
    public ItemBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the item's enchants
     *
     * @param enchants the enchants
     * @return the ItemBuilder instance
     */
    public ItemBuilder setEnchants(Enchant... enchants) {
        this.enchants = enchants;
        return this;
    }

    /**
     * Sets the item's amount
     *
     * @param amount the amount
     * @return the ItemBuilder instance
     */
    public ItemBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Sets the item's lore
     *
     * @param lore the lore
     * @return the ItemBuilder instance
     */
    public ItemBuilder setLore(List<String> lore) {
        this.lore = lore;
        return this;
    }

    /**
     * Sets the item's durability
     *
     * @param durability the durability
     * @return the ItemBuilder instance
     */
    public ItemBuilder setDurability(short durability) {
        this.durability = durability;
        return this;
    }

    /**
     * Sets whether or not the item should show attributes
     *
     * @param hideAttributes true if the item should hide attributes
     * @return the ItemBuilder instance
     */
    public ItemBuilder setHideAttributes(boolean hideAttributes) {
        this.hideAttributes = hideAttributes;
        return this;
    }

    /**
     * Builds the item
     *
     * @return the ItemStack
     */
    @Override
    public ItemStack build() {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (name != null) {
            meta.setDisplayName(StringUtils.colour(name));
        }

        if (lore != null) {
            meta.setLore(StringUtils.colour(lore));
        }

        for (Enchant enchant : enchants) {
            meta.addEnchant(enchant.getEnchant(), enchant.getLevel(), true);
        }

        if (hideAttributes) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }

        item.setItemMeta(meta);

        if (durability != -1) {
            item.setDurability(durability);
        }
        return item;
    }
}
