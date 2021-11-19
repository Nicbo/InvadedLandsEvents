package ca.nicbo.invadedlandsevents.compatibility;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Compatible materials for the changes from legacy to 1.13.
 *
 * @author Nicbo
 */
public enum CompatibleMaterial {
    WOODEN_SWORD("WOODEN_SWORD", "WOOD_SWORD"),
    GOLDEN_SWORD("GOLDEN_SWORD", "GOLD_SWORD"),
    GOLDEN_AXE("GOLDEN_AXE", "GOLD_AXE"),
    DIAMOND_SHOVEL("DIAMOND_SHOVEL", "DIAMOND_SPADE"),
    RAIL("RAIL", "RAILS"),
    LEAD("LEAD", "LEASH"),
    SNOWBALL("SNOWBALL", "SNOW_BALL"),
    WHITE_WOOL("WHITE_WOOL", "WOOL", Colour.WHITE),
    ORANGE_WOOL("ORANGE_WOOL", "WOOL", Colour.ORANGE),
    MAGENTA_WOOL("MAGENTA_WOOL", "WOOL", Colour.MAGENTA),
    LIGHT_BLUE_WOOL("LIGHT_BLUE_WOOL", "WOOL", Colour.BLUE),
    YELLOW_WOOL("YELLOW_WOOL", "WOOL", Colour.YELLOW),
    LIME_WOOL("LIME_WOOL", "WOOL", Colour.LIME),
    PINK_WOOL("PINK_WOOL", "WOOL", Colour.PINK),
    GREY_WOOL("GRAY_WOOL", "WOOL", Colour.GREY),
    LIGHT_GREY_WOOL("LIGHT_GRAY_WOOL", "WOOL", Colour.LIGHT_GREY),
    CYAN_WOOL("CYAN_WOOL", "WOOL", Colour.CYAN),
    PURPLE_WOOL("PURPLE_WOOL", "WOOL", Colour.PURPLE),
    BLUE_WOOL("BLUE_WOOL", "WOOL", Colour.BLUE),
    BROWN_WOOL("BROWN_WOOL", "WOOL", Colour.BROWN),
    GREEN_WOOL("GREEN_WOOL", "WOOL", Colour.GREEN),
    RED_WOOL("RED_WOOL", "WOOL", Colour.RED),
    BLACK_WOOL("BLACK_WOOL", "WOOL", Colour.BLACK),
    WHITE_GLASS_PANE("WHITE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.WHITE),
    ORANGE_GLASS_PANE("ORANGE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.ORANGE),
    MAGENTA_GLASS_PANE("MAGENTA_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.MAGENTA),
    LIGHT_BLUE_GLASS_PANE("LIGHT_BLUE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.LIGHT_BLUE),
    YELLOW_GLASS_PANE("YELLOW_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.YELLOW),
    LIME_GLASS_PANE("LIME_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.LIME),
    PINK_GLASS_PANE("PINK_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.PINK),
    GREY_GLASS_PANE("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.GREY),
    LIGHT_GREY_GLASS_PANE("LIGHT_GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.LIGHT_GREY),
    CYAN_GLASS_PANE("CYAN_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.CYAN),
    PURPLE_GLASS_PANE("PURPLE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.PURPLE),
    BLUE_GLASS_PANE("BLUE_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.BLUE),
    BROWN_GLASS_PANE("BROWN_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.BROWN),
    GREEN_GLASS_PANE("GREEN_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.GREEN),
    RED_GLASS_PANE("RED_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.RED),
    BLACK_GLASS_PANE("BLACK_STAINED_GLASS_PANE", "STAINED_GLASS_PANE", Colour.BLACK);

    private final String name;
    private final String legacyName;
    private final Colour colour;

    CompatibleMaterial(String name, String legacyName) {
        this(name, legacyName, null);
    }

    CompatibleMaterial(String name, String legacyName, Colour colour) {
        this.name = name;
        this.legacyName = legacyName;
        this.colour = colour;
    }

    public String getName() {
        return name;
    }

    public String getLegacyName() {
        return legacyName;
    }

    public Colour getColour() {
        return colour;
    }

    public ItemStack createItemStack() {
        return createItemStack(1);
    }

    public ItemStack createItemStack(int amount) {
        Material material = createMaterial();
        ItemStack item = new ItemStack(material);

        if (NMSVersion.getCurrentVersion().isLegacy() && colour != null) {
            // noinspection deprecation (compatibility)
            item.setDurability(colour.getData());
        }

        item.setAmount(amount);
        return item;
    }

    public Material createMaterial() {
        return Material.valueOf(NMSVersion.getCurrentVersion().isLegacy() ? legacyName : name);
    }
}
