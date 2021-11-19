package ca.nicbo.invadedlandsevents.configuration;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.configuration.ConfigHandler;
import ca.nicbo.invadedlandsevents.api.configuration.ConfigSection;
import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.compatibility.CompatibleMaterial;
import ca.nicbo.invadedlandsevents.kit.InvadedKit;
import ca.nicbo.invadedlandsevents.util.ItemStackBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link ConfigHandler}.
 *
 * @author Nicbo
 */
public class InvadedConfigHandler extends InvadedConfigurationHandler implements ConfigHandler {
    private static final String CONFIG_FILE_NAME = "config.yml";

    private final Map<String, Kit> defaultKitMap;

    public InvadedConfigHandler(InvadedLandsEventsPlugin plugin) {
        super(CONFIG_FILE_NAME, plugin);
        this.defaultKitMap = createDefaultKitMap();
    }

    public void loadDefaultKits() {
        for (Map.Entry<String, Kit> entry : defaultKitMap.entrySet()) {
            String[] path = entry.getKey().split("\\.");
            Kit kit = entry.getValue();
            getConfigSection(path[0]).setKit(path[1], kit);
        }

        save();
    }

    @Override
    public ConfigSection getConfigSection(String name) {
        return new InvadedConfigSection(name, this);
    }

    @Override
    public Set<String> getConfigSectionNames() {
        Set<String> keys = getInternalConfiguration().getKeys(false);
        keys.remove("version");
        return keys;
    }

    public Map<String, Kit> getDefaultKitMap() {
        return defaultKitMap;
    }

    private static Map<String, Kit> createDefaultKitMap() {
        Map<String, Kit> defaultKitMap = new HashMap<>();

        Kit bracketsKit = new InvadedKit(Arrays.asList(
                new ItemStack(Material.IRON_SWORD),
                new ItemStack(Material.BOW),
                new ItemStack(Material.GOLDEN_APPLE, 10),
                new ItemStack(Material.ARROW, 32)
        ), Arrays.asList(
                new ItemStack(Material.IRON_BOOTS),
                new ItemStack(Material.IRON_LEGGINGS),
                new ItemStack(Material.IRON_CHESTPLATE),
                new ItemStack(Material.IRON_HELMET)
        ));

        defaultKitMap.put("brackets1v1.kit", bracketsKit);
        defaultKitMap.put("brackets2v2.kit", bracketsKit);
        defaultKitMap.put("brackets3v3.kit", bracketsKit);

        Kit kothLmsKit = new InvadedKit(Arrays.asList(
                new ItemStackBuilder(Material.IRON_SWORD).addEnchant(Enchantment.DAMAGE_ALL, 1).build(),
                new ItemStack(Material.BOW),
                new ItemStack(Material.GOLDEN_APPLE, 10),
                new ItemStack(Material.ARROW, 32)
        ), Arrays.asList(
                new ItemStackBuilder(Material.IRON_BOOTS).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build(),
                new ItemStackBuilder(Material.IRON_LEGGINGS).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build(),
                new ItemStackBuilder(Material.IRON_CHESTPLATE).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build(),
                new ItemStackBuilder(Material.IRON_HELMET).addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build()
        ));

        defaultKitMap.put("koth.kit", kothLmsKit);
        defaultKitMap.put("lms.kit", kothLmsKit);

        Kit oitcKit = new InvadedKit(Arrays.asList(
                CompatibleMaterial.WOODEN_SWORD.createItemStack(),
                new ItemStack(Material.BOW),
                new ItemStack(Material.ARROW)
        ));

        defaultKitMap.put("oitc.kit", oitcKit);

        Kit redroverKillerKit = new InvadedKit(Collections.singletonList(new ItemStack(Material.DIAMOND_SWORD)), Arrays.asList(
                new ItemStack(Material.DIAMOND_BOOTS),
                new ItemStack(Material.DIAMOND_LEGGINGS),
                new ItemStack(Material.DIAMOND_CHESTPLATE),
                new ItemStack(Material.DIAMOND_HELMET)
        ));

        defaultKitMap.put("redrover.kit", InvadedKit.EMPTY);
        defaultKitMap.put("redrover.killer-kit", redroverKillerKit);

        Kit rodKit = new InvadedKit(Collections.emptyList(), Collections.singletonList(new ItemStack(Material.LEATHER_BOOTS)));

        defaultKitMap.put("rod.kit", rodKit);

        Kit spleefKit = new InvadedKit(Collections.singletonList(new ItemStackBuilder(CompatibleMaterial.DIAMOND_SHOVEL)
                .addEnchant(Enchantment.DIG_SPEED, 5)
                .build()));

        defaultKitMap.put("spleef.kit", spleefKit);

        defaultKitMap.put("sumo1v1.kit", InvadedKit.EMPTY);
        defaultKitMap.put("sumo2v2.kit", InvadedKit.EMPTY);
        defaultKitMap.put("sumo3v3.kit", InvadedKit.EMPTY);

        Kit tdmBlueKit = createTDMKit(Color.BLUE);
        Kit tdmRedKit = createTDMKit(Color.RED);

        defaultKitMap.put("tdm.blue-kit", tdmBlueKit);
        defaultKitMap.put("tdm.red-kit", tdmRedKit);

        ItemStack tnt = new ItemStack(Material.TNT);
        Kit tntTagTaggedKit = new InvadedKit(
                Arrays.asList(tnt, tnt, tnt, tnt, tnt, tnt, tnt, tnt, tnt),
                Arrays.asList(null, null, null, tnt)
        );

        defaultKitMap.put("tnttag.kit", InvadedKit.EMPTY);
        defaultKitMap.put("tnttag.tagged-kit", tntTagTaggedKit);

        defaultKitMap.put("waterdrop.kit", InvadedKit.EMPTY);

        return Collections.unmodifiableMap(defaultKitMap);
    }

    private static Kit createTDMKit(Color color) {
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
        Validate.checkNotNull(meta);
        meta.setColor(color);
        meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
        meta.addEnchant(Enchantment.DURABILITY, 3, true);
        helmet.setItemMeta(meta);

        return new InvadedKit(Arrays.asList(
                new ItemStack(Material.IRON_SWORD),
                new ItemStack(Material.BOW),
                new ItemStack(Material.GOLDEN_APPLE, 10),
                new ItemStack(Material.ARROW, 32)
        ), Arrays.asList(
                new ItemStack(Material.IRON_BOOTS, 1),
                new ItemStack(Material.IRON_LEGGINGS, 1),
                new ItemStack(Material.IRON_CHESTPLATE, 1),
                helmet
        ));
    }
}
