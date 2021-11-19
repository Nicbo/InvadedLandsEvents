package ca.nicbo.invadedlandsevents.api.kit;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A kit consisting of items, armour and possibly an offhand item.
 *
 * @author Nicbo
 */
public interface Kit {
    /**
     * Applies the kit to the provided player. Note that the offhand slot will not be applied on versions <1.9.
     *
     * @param player the player that is receiving the kit
     * @throws NullPointerException if the player is null
     */
    void apply(@NotNull Player player);

    /**
     * Returns an unmodifiable list of the items. This list is guaranteed to have a size of 36 and empty items will be
     * null instead of air.
     *
     * @return an unmodifiable list of the items
     */
    @NotNull
    List<ItemStack> getItems();

    /**
     * Returns an unmodifiable list of the armour. This list is guaranteed to have a size of 4 and empty items will be
     * null instead of air.
     *
     * @return an unmodifiable list of the armour
     */
    @NotNull
    List<ItemStack> getArmour();

    /**
     * Returns the offhand item stack or null if it does not exist
     *
     * @return the offhand item stack
     */
    @Nullable
    ItemStack getOffhand();
}
