package ca.nicbo.invadedlandsevents.kit;

import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.compatibility.NMSVersion;
import ca.nicbo.invadedlandsevents.util.SpigotUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link Kit}.
 *
 * @author Nicbo
 */
public class InvadedKit implements Kit {
    public static final InvadedKit EMPTY = new InvadedKit(Collections.emptyList());

    private static final ItemStack[] EMPTY_ITEM_STACK_ARRAY = new ItemStack[0];

    private static final int ITEMS_SIZE = 36;
    private static final int ARMOUR_SIZE = 4;
    private static final int OFFHAND_INDEX = 40;

    private final List<ItemStack> items;
    private final List<ItemStack> armour;
    private final ItemStack offhand;

    public InvadedKit(List<ItemStack> items) {
        this(items, Collections.emptyList());
    }

    public InvadedKit(List<ItemStack> items, List<ItemStack> armour) {
        this(items, armour, null);
    }

    public InvadedKit(List<ItemStack> items, List<ItemStack> armour, ItemStack offhand) {
        Validate.checkArgumentNotNull(items, "items");
        Validate.checkArgumentNotNull(armour, "armour");
        this.items = prepare(items, ITEMS_SIZE);
        this.armour = prepare(armour, ARMOUR_SIZE);
        this.offhand = nullIfAir(offhand);
    }

    @Override
    public void apply(Player player) {
        Validate.checkArgumentNotNull(player, "player");

        PlayerInventory inventory = player.getInventory();
        if (NMSVersion.getCurrentVersion().isPreCombatUpdate()) {
            inventory.setContents(items.toArray(EMPTY_ITEM_STACK_ARRAY));
            inventory.setArmorContents(armour.toArray(EMPTY_ITEM_STACK_ARRAY));
        } else {
            List<ItemStack> contents = new ArrayList<>();
            contents.addAll(items);
            contents.addAll(armour);
            contents.add(offhand);
            inventory.setContents(contents.toArray(EMPTY_ITEM_STACK_ARRAY));
        }

        player.updateInventory();
    }

    @Override
    public List<ItemStack> getItems() {
        return items;
    }

    @Override
    public List<ItemStack> getArmour() {
        return armour;
    }

    @Override
    public ItemStack getOffhand() {
        return offhand;
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, armour, offhand);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        InvadedKit other = (InvadedKit) obj;
        return items.equals(other.items) && armour.equals(other.armour) && Objects.equals(offhand, other.offhand);
    }

    @Override
    public String toString() {
        return "(items: " + items + ", armour: " + armour + "}";
    }

    public static InvadedKit from(PlayerInventory inventory) {
        Validate.checkArgumentNotNull(inventory, "inventory");
        return new InvadedKit(getItems(inventory), getArmour(inventory), getOffhand(inventory));
    }

    private static List<ItemStack> getItems(PlayerInventory inventory) {
        return Arrays.asList(Arrays.copyOf(inventory.getContents(), ITEMS_SIZE));
    }

    private static List<ItemStack> getArmour(PlayerInventory inventory) {
        return Arrays.asList(inventory.getArmorContents());
    }

    private static ItemStack getOffhand(PlayerInventory inventory) {
        ItemStack[] content = inventory.getContents();
        return content.length < OFFHAND_INDEX + 1 ? null : content[OFFHAND_INDEX];
    }

    private static ItemStack nullIfAir(ItemStack item) {
        return SpigotUtils.isEmpty(item) ? null : item;
    }

    private static List<ItemStack> prepare(List<ItemStack> original, int expectedSize) {
        List<ItemStack> list = new ArrayList<>(original); // create copy, original may be immutable

        // replace all air items with null
        list.replaceAll(InvadedKit::nullIfAir);

        // make sure list size is equal to expectedSize
        final int currentSize = list.size();
        if (currentSize > expectedSize) { // trim
            list.subList(expectedSize, currentSize).clear();
        } else if (currentSize < expectedSize) { // fill
            list.addAll(Collections.nCopies(expectedSize - list.size(), null));
        }

        return Collections.unmodifiableList(list);
    }
}
