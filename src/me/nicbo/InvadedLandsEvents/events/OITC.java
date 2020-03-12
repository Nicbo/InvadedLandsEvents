package me.nicbo.InvadedLandsEvents.events;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.xml.stream.Location;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OITC extends InvadedEvent {
    private List<Location> locations;
    private List<ItemStack> kit;

    public OITC(EventsMain plugin) {
        super("One in the Chamber", "oitc", plugin);
        this.locations = new ArrayList<>();
        this.kit = Arrays.asList(new ItemStack(Material.WOOD_SWORD, 1), new ItemStack(Material.BOW, 1), new ItemStack(Material.ARROW, 1));
    }

    @Override
    public void init(EventsMain plugin) {

    }

    @Override
    public void start() {
        playerCheck.runTaskTimerAsynchronously(plugin, 0, 1);
        for (Player player : players) {
            randomSpawn(player);
        }
    }

    @Override
    public void stop() {
        started = false;
        playerCheck.cancel();
        removePlayers();
        plugin.getManagerHandler().getEventManager().setCurrentEvent(null);
    }

    private void randomSpawn(Player player) {
        player.teleport(locations.get(GeneralUtils.randomMinMax(0, 7)));
        player.setHealth(20);
        player.getInventory().clear();
        kit.forEach(item -> player.getInventory().addItem(item));
    }


}
