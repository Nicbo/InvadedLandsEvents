package ca.nicbo.invadedlandsevents.gui.host;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.configuration.ConfigSection;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.gui.Button;
import ca.nicbo.invadedlandsevents.api.permission.EventPermission;
import ca.nicbo.invadedlandsevents.api.util.Callback;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.compatibility.CompatibleMaterial;
import ca.nicbo.invadedlandsevents.configuration.InvadedConfigurationManager;
import ca.nicbo.invadedlandsevents.gui.InvadedGui;
import ca.nicbo.invadedlandsevents.util.ItemStackBuilder;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The {@link InvadedGui} that is used for hosting events.
 *
 * @author Nicbo
 */
public abstract class HostGui extends InvadedGui {
    private final InvadedLandsEventsPlugin plugin;
    private final InvadedConfigurationManager configManager;

    protected HostGui(Player player, String title, int size, InvadedLandsEventsPlugin plugin) {
        super(player, title, size);
        this.plugin = plugin;
        this.configManager = plugin.getConfigurationManager();
    }

    @Override
    public void open() {
        this.update(); // Update the lore before opening
        super.open();
    }

    @Override
    public void update() {
        for (Map.Entry<Integer, Button> entry : getButtonMap().entrySet()) {
            if (!(entry.getValue() instanceof HostButton)) {
                continue;
            }
            HostButton button = (HostButton) entry.getValue();
            updateHostButtonItemStack(button);
        }

        super.update();
    }

    protected void updateHostButtonItemStack(HostButton button) {
        HostButtonType type = button.getType();
        String message = "&aLeft click to host.";

        Player player = getPlayer();
        if (type == HostButtonType.SUMO) {
            if (!player.hasPermission(EventType.SUMO_1V1.getPermission()) &&
                    !player.hasPermission(EventType.SUMO_2V2.getPermission()) &&
                    !player.hasPermission(EventType.SUMO_3V3.getPermission())) {
                message = "&c&lYou don't have permission to host this event.";
            }
        } else if (type == HostButtonType.BRACKETS) {
            if (!player.hasPermission(EventType.BRACKETS_1V1.getPermission()) &&
                    !player.hasPermission(EventType.BRACKETS_2V2.getPermission()) &&
                    !player.hasPermission(EventType.BRACKETS_3V3.getPermission())) {
                message = "&c&lYou don't have permission to host this event.";
            }
        } else {
            EventType eventType = type.getEventType();
            Validate.checkNotNull(eventType, "button's type must have an event type: %s", type);

            long secondsLeft = plugin.getPlayerDataManager().getSecondsUntilHost(player.getUniqueId(), eventType);
            if (secondsLeft > 0 && !player.hasPermission(EventPermission.HOST_COOLDOWN_BYPASS)) {
                message = "&cYou must wait &e" + StringUtils.formatSeconds(secondsLeft) + "&c to host this event.";
            } else if (!player.hasPermission(eventType.getPermission())) {
                message = "&c&lYou don't have permission to host this event.";
            } else if (!plugin.getEventManager().isEventEnabled(eventType)) {
                message = "&cEvent is currently disabled.";
            }
        }

        ItemStack itemStack = button.getItemStack();
        ItemMeta meta = itemStack.getItemMeta();
        Validate.checkNotNull(meta);
        List<String> lore = meta.getLore();
        Validate.checkNotNull(lore);
        lore.set(lore.size() - 1, StringUtils.colour(message));
        meta.setLore(lore);
        itemStack.setItemMeta(meta);

        button.setItemStack(itemStack);
    }

    protected HostButton createHostButton(HostButtonType type) {
        final ItemStack item = createHostButtonItemStack(type);

        final Callback callback;

        // Remember to delay one tick before close(), see https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/inventory/InventoryClickEvent.html
        if (type.getEventType() == null) {
            callback = () -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                close();

                final HostGui gui;
                if (type == HostButtonType.SUMO) {
                    gui = SumoHostGui.create(getPlayer(), plugin);
                } else if (type == HostButtonType.BRACKETS) {
                    gui = BracketsHostGui.create(getPlayer(), plugin);
                } else {
                    throw new IllegalArgumentException("unexpected host button type: " + type);
                }

                gui.open();
            });
        } else {
            callback = () -> {
                Player player = getPlayer();
                if (plugin.getEventManager().hostEvent(type.getEventType(), player)) {
                    plugin.getServer().getScheduler().runTask(plugin, this::close);
                }
            };
        }

        return new HostButton(item, callback, type);
    }

    protected ItemStack createHostButtonItemStack(HostButtonType type) {
        final ItemStack item;
        switch (type) {
            case BRACKETS:
                item = new ItemStackBuilder(Material.IRON_SWORD)
                        .setName("&e&lBrackets")
                        .addLore("&7Beat your opponents in a head to head battle!")
                        .hideAttributes()
                        .build();
                break;
            case BRACKETS_1V1:
            case SUMO_1V1:
                item = new ItemStackBuilder(CompatibleMaterial.WHITE_WOOL)
                        .setName("&e1 vs. 1")
                        .hideAttributes()
                        .build();
                break;
            case BRACKETS_2V2:
            case SUMO_2V2:
                item = new ItemStackBuilder(CompatibleMaterial.WHITE_WOOL)
                        .setName("&e2 vs. 2")
                        .hideAttributes()
                        .build();
                break;
            case BRACKETS_3V3:
            case SUMO_3V3:
                item = new ItemStackBuilder(CompatibleMaterial.WHITE_WOOL)
                        .setName("&e3 vs. 3")
                        .hideAttributes()
                        .build();
                break;
            case KING_OF_THE_HILL:
                ConfigSection kothSection = configManager.getConfigHandler().getConfigSection(EventType.KING_OF_THE_HILL.getConfigName());
                int kothWinPoints = kothSection.getInteger("win-points");
                item = new ItemStackBuilder(Material.GOLD_BLOCK)
                        .setName("&e&l" + EventType.KING_OF_THE_HILL.getDisplayName())
                        .addLore("&7The longer you capture the hill")
                        .addLore("&7the more points you will collect.")
                        .addLore("&7You require " + kothWinPoints + " in order to win the event.")
                        .hideAttributes()
                        .build();
                break;
            case LAST_MAN_STANDING:
                item = new ItemStackBuilder(Material.IRON_SWORD)
                        .setName("&e&l" + EventType.LAST_MAN_STANDING.getDisplayName())
                        .addLore("&7Be the last man standing! Kill all your opponents!")
                        .hideAttributes()
                        .build();
                break;
            case ONE_IN_THE_CHAMBER:
                ConfigSection oitcSection = configManager.getConfigHandler().getConfigSection(EventType.ONE_IN_THE_CHAMBER.getConfigName());
                int oitcWinPoints = oitcSection.getInteger("win-points");
                item = new ItemStackBuilder(Material.BOW)
                        .setName("&e&l" + EventType.ONE_IN_THE_CHAMBER.getDisplayName())
                        .addLore("&7The first person to obtain " + oitcWinPoints + " points", "&7in this free-for-all match, will win.")
                        .hideAttributes()
                        .build();
                break;
            case RACE_OF_DEATH:
                item = new ItemStackBuilder(CompatibleMaterial.RAIL)
                        .setName("&e&l" + EventType.RACE_OF_DEATH.getDisplayName())
                        .addLore("&7The first player to make it to the end of the parkour wins!")
                        .hideAttributes()
                        .build();
                break;
            case REDROVER:
                item = new ItemStackBuilder(Material.REDSTONE_BLOCK)
                        .setName("&e&l" + EventType.REDROVER.getDisplayName())
                        .addLore("&7Try to cross the middle without dying!")
                        .hideAttributes()
                        .build();
                break;
            case SPLEEF:
                item = new ItemStackBuilder(CompatibleMaterial.DIAMOND_SHOVEL)
                        .setName("&e&l" + EventType.SPLEEF.getDisplayName())
                        .addLore("&7Try and break the blocks under other players!")
                        .hideAttributes()
                        .build();
                break;
            case SUMO:
                item = new ItemStackBuilder(CompatibleMaterial.LEAD)
                        .setName("&e&lSumo")
                        .addLore("&7Knock your opponent off the platform.")
                        .hideAttributes()
                        .build();
                break;
            case TEAM_DEATHMATCH:
                item = new ItemStackBuilder(CompatibleMaterial.GOLDEN_SWORD)
                        .setName("&e&lTeam Deathmatch")
                        .addLore("&cRed &7vs &9Blue&7!")
                        .addLore("")
                        .addLore("&7Classic Team Deathmatch where two teams")
                        .addLore("&7face against each other, and which ever")
                        .addLore("&7team is last standing wins!")
                        .addLore("&7The top 5 killers on the winning team obtain event keys.")
                        .hideAttributes()
                        .build();
                break;
            case TNT_TAG:
                item = new ItemStackBuilder(Material.TNT)
                        .setName("&e&lTNT Tag")
                        .addLore("&7Don't get stuck with the TNT!")
                        .hideAttributes()
                        .build();
                break;
            case WATERDROP:
                item = new ItemStackBuilder(Material.WATER_BUCKET)
                        .setName("&e&lWaterdrop")
                        .addLore("&7The goal of this game is to jump into the water!")
                        .addLore("&7It will get progressively harder,")
                        .addLore("&7and the hole will get smaller.")
                        .hideAttributes()
                        .build();
                break;
            case WOOL_SHUFFLE:
                item = new ItemStackBuilder(CompatibleMaterial.WHITE_WOOL) // This will be changed in update method
                        .setName("&e&lWool Shuffle")
                        .addLore("&7Your hotbar will fill up with a colour of wool")
                        .addLore("&7each round and you must run around the arena")
                        .addLore("&7and stand on the colour of wool which")
                        .addLore("&7fills your hotbar!")
                        .hideAttributes()
                        .build();
                break;
            default:
                throw new IllegalArgumentException("unexpected host button type: " + type);
        }

        // Add 2 spaces at the end of the lore
        ItemMeta meta = item.getItemMeta();
        Validate.checkNotNull(meta);
        List<String> lore = meta.getLore() == null ? new ArrayList<>() : meta.getLore();
        lore.add("");
        lore.add("");
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }
}
