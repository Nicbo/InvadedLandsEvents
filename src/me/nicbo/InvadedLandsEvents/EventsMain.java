package me.nicbo.InvadedLandsEvents;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.nicbo.InvadedLandsEvents.commands.EventCommand;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class EventsMain extends JavaPlugin {
    private final String prefix = "[InvadedLandsEvents] ";
    private Logger log = getLogger();

    private EventManager eventManager;
    private WorldGuardPlugin worldGuardPlugin;

    @Override
    public void onEnable() { // MAKE EVENT WORLD IF DOESNT EXIST
        reloadConfig();
        worldGuardPlugin = getWorldGuard();
        eventManager = new EventManager(this);

        ConfigUtils.setEventWorld(getConfig().getString("event-world"));
        registerCommands();
        log.info(prefix + "enabled!");
    }

    @Override
    public void onDisable() {
        saveDefaultConfig();
        log.info(prefix + "disabled!");
    }

    private void registerCommands() {
        getCommand("event").setExecutor(new EventCommand(eventManager));
    }

    private WorldGuardPlugin getWorldGuard() { //im broken i think
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            log.severe(prefix + "WorldGuard not found!");
            return null;
        }

        return (WorldGuardPlugin) plugin;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public WorldGuardPlugin getWorldGuardPlugin() {
        return worldGuardPlugin;
    }
}
