package ca.nicbo.invadedlandsevents.util;

import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.compatibility.CompatibleMaterial;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder class for creating an {@link ItemStack}.
 *
 * @author Nicbo
 */
public class ItemStackBuilder {
    private final ItemStack item;

    private final List<String> lore;
    private final List<Enchant> enchants;

    private String name;
    private int amount;
    private boolean hideAttributes;

    public ItemStackBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemStackBuilder(CompatibleMaterial material) {
        this(material.createItemStack());
    }

    private ItemStackBuilder(ItemStack item) {
        this.item = item;
        this.lore = new ArrayList<>();
        this.enchants = new ArrayList<>();
        this.amount = item.getAmount();
    }

    public ItemStackBuilder addLore(String... lore) {
        return addLore(Arrays.asList(lore));
    }

    public ItemStackBuilder addLore(List<String> lore) {
        this.lore.addAll(StringUtils.colour(lore));
        return this;
    }

    public ItemStackBuilder addEnchant(Enchantment enchantment, int level) {
        this.enchants.add(new Enchant(enchantment, level));
        return this;
    }

    public ItemStackBuilder setName(String name) {
        this.name = StringUtils.colour(name);
        return this;
    }

    public ItemStackBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemStackBuilder hideAttributes() {
        this.hideAttributes = true;
        return this;
    }

    public ItemStack build() {
        ItemMeta meta = item.getItemMeta();
        Validate.checkNotNull(meta, "item does not have meta");

        if (name != null) {
            meta.setDisplayName(name);
        }

        item.setAmount(amount);

        if (!lore.isEmpty()) {
            meta.setLore(lore);
        }

        for (Enchant enchant : enchants) {
            meta.addEnchant(enchant.enchantment, enchant.level, true);
        }

        if (hideAttributes) {
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }

        item.setItemMeta(meta);
        return item;
    }

    private static class Enchant {
        private final Enchantment enchantment;
        private final int level;

        private Enchant(Enchantment enchantment, int level) {
            this.enchantment = enchantment;
            this.level = level;
        }
    }
}
