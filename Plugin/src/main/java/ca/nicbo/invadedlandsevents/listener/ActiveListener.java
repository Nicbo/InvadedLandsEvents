package ca.nicbo.invadedlandsevents.listener;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPreJoinEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPreLeaveEvent;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerPreSpectateEvent;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.util.SpigotUtils;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Objects;
import java.util.UUID;

/**
 * This listener is always active.
 *
 * @author Nicbo
 */
public class ActiveListener implements Listener {
    private static final UUID NICBO = UUID.fromString("05b3c28a-a532-41bd-8b5e-f3ca452d3876");
    private static final UUID STARZORROW = UUID.fromString("742103ed-2145-4ada-b8ea-785e036a8898");

    private final InvadedLandsEventsPlugin plugin;

    public ActiveListener(InvadedLandsEventsPlugin plugin) {
        Validate.checkArgumentNotNull(plugin, "plugin");
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.getUniqueId().equals(NICBO) || player.getUniqueId().equals(STARZORROW)) {
            SpigotUtils.sendMessage(player, "&eThis server is running &6InvadedLandsEvents v" + plugin.getDescription().getVersion() + "&e.");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPlayerPreJoin(EventPlayerPreJoinEvent event) {
        removeThrownEnderPearls(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPlayerPreSpectate(EventPlayerPreSpectateEvent event) {
        removeThrownEnderPearls(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPlayerPreLeave(EventPlayerPreLeaveEvent event) {
        removeThrownEnderPearls(event.getPlayer());
    }

    private static void removeThrownEnderPearls(Player player) {
        player.getWorld().getEntities().stream()
                .filter(EnderPearl.class::isInstance)
                .map(EnderPearl.class::cast)
                .filter(p -> Objects.equals(p.getShooter(), player))
                .forEach(EnderPearl::remove);
    }
}
