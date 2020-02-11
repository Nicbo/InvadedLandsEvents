package me.nicbo.InvadedLandsEvents;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.nicbo.InvadedLandsEvents.commands.EventCommand;
import me.nicbo.InvadedLandsEvents.commands.EventConfigCommand;
import me.nicbo.InvadedLandsEvents.listeners.GeneralEventListener;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class EventsMain extends JavaPlugin {
    private Logger log = getLogger();
    private EventManager eventManager;
    private WorldGuardPlugin worldGuardPlugin;

    @Override
    public void onEnable() { // MAKE EVENT WORLD IF DOESNT EXIST
        worldGuardPlugin = getWorldGuard();
        saveConfig();

        ConfigUtils.setEventWorld(getConfig().getString("event-world"));
        try {
            ConfigUtils.setSpawnLoc(getConfig().getConfigurationSection("spawn-loc"));
        } catch (ClassCastException cce) {
            log.info("Spawn location not configured yet!");
        }

        eventManager = new EventManager(this);
        registerCommands();
        getServer().getPluginManager().registerEvents(new GeneralEventListener(eventManager), this);
        log.info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        saveConfig();
        log.info("Plugin disabled!");
    }

    private void registerCommands() {
        getCommand("event").setExecutor(new EventCommand(eventManager));
        getCommand("eventconfig").setExecutor(new EventConfigCommand(this));
    }

    private WorldGuardPlugin getWorldGuard() { //im broken i think
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            log.severe("WorldGuard not found!");
            return null;
        }

        return (WorldGuardPlugin) plugin;
    }

    public WorldGuardPlugin getWorldGuardPlugin() {
        return worldGuardPlugin;
    }
}
