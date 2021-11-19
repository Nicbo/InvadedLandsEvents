package ca.nicbo.invadedlandsevents.configuration;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.configuration.ConfigurationManager;
import ca.nicbo.invadedlandsevents.api.configuration.WandLocationHolder;
import ca.nicbo.invadedlandsevents.api.permission.EventPermission;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.compatibility.CompatibleMaterial;
import ca.nicbo.invadedlandsevents.util.ItemStackBuilder;
import ca.nicbo.invadedlandsevents.util.SpigotUtils;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of {@link ConfigurationManager}.
 *
 * @author Nicbo
 */
public class InvadedConfigurationManager implements ConfigurationManager, Listener {
    public static final ItemStack WAND = new ItemStackBuilder(CompatibleMaterial.GOLDEN_AXE)
            .setName("&6InvadedLandsEvents Region Wand")
            .addLore("&7The &6region wand &7for &6InvadedLandsEvents")
            .addLore("&7Obtained with &6/econfig wand")
            .addLore("&6Left click &7a block to set &6location one")
            .addLore("&6Right click &7a block to set &6location two")
            .addLore("&7You must have &6" + EventPermission.CONFIG + "&7 to use this wand")
            .hideAttributes()
            .build();

    private static final String PREFIX = "&e[&6ILE&e] ";

    private final InvadedConfigHandler configHandler;
    private final InvadedMessagesHandler messagesHandler;
    private final Map<Player, WandLocationHolder> wandLocationHolderMap;

    public InvadedConfigurationManager(InvadedLandsEventsPlugin plugin) {
        Validate.checkArgumentNotNull(plugin, "plugin");
        this.configHandler = new InvadedConfigHandler(plugin);
        this.messagesHandler = new InvadedMessagesHandler(plugin);
        this.wandLocationHolderMap = new HashMap<>();
    }

    @Override
    public InvadedConfigHandler getConfigHandler() {
        return configHandler;
    }

    @Override
    public InvadedMessagesHandler getMessagesHandler() {
        return messagesHandler;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (event.getItem() != null && block != null && event.getItem().isSimilar(WAND) && player.hasPermission(EventPermission.CONFIG)) {
            Action action = event.getAction();
            Location location = block.getLocation();

            final String locationNumber;
            if (action == Action.LEFT_CLICK_BLOCK) {
                locationNumber = "First";
                wandLocationHolderMap.computeIfAbsent(player, p -> new InvadedWandLocationHolder()).setLocationOne(location);
            } else if (action == Action.RIGHT_CLICK_BLOCK) {
                locationNumber = "Second";
                wandLocationHolderMap.computeIfAbsent(player, p -> new InvadedWandLocationHolder()).setLocationTwo(location);
            } else {
                return;
            }

            SpigotUtils.sendMessage(player, PREFIX + "&e" + locationNumber + " location set to &6" + StringUtils.locationToString(location, false) + "&e.");
            event.setCancelled(true);
        }
    }

    // Using this instead of setUnbreakable because of compatibility issues
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        if (event.getItem().isSimilar(WAND) && player.hasPermission(EventPermission.CONFIG)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        wandLocationHolderMap.remove(event.getPlayer());
    }

    @Override
    public WandLocationHolder getWandLocationHolder(Player player) {
        Validate.checkArgumentNotNull(player, "player");
        return wandLocationHolderMap.getOrDefault(player, new InvadedWandLocationHolder());
    }

    @Override
    public Map<Player, WandLocationHolder> getWandLocationHolderMap() {
        return Collections.unmodifiableMap(wandLocationHolderMap);
    }
}
