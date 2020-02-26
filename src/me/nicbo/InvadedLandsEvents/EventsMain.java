package me.nicbo.InvadedLandsEvents;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.nicbo.InvadedLandsEvents.commands.EventCommand;
import me.nicbo.InvadedLandsEvents.commands.EventConfigCommand;
import me.nicbo.InvadedLandsEvents.listeners.GeneralEventListener;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class EventsMain extends JavaPlugin {
    private Logger log = getLogger();
    private EventManager eventManager;
    private WorldGuardPlugin worldGuardPlugin;

    @Override
    public void onEnable() {
        worldGuardPlugin = getWorldGuard();
        saveDefaultConfig();

        ConfigUtils.setEventWorld(getConfig().getString("event-world"));
        ConfigUtils.setWinCommand(getConfig().getString("win-command"));
        ConfigUtils.setSpawnLoc((Location) getConfig().get("spawn-location"));

        eventManager = new EventManager(this);
        registerCommands();
        getServer().getPluginManager().registerEvents(new GeneralEventListener(eventManager), this);
        log.info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        saveDefaultConfig();
        log.info("Plugin disabled!");
    }

    private void registerCommands() {
        EventConfigCommand eventConfigCommand = new EventConfigCommand(this);
        getCommand("event").setExecutor(new EventCommand(eventManager));
        getCommand("eventconfig").setExecutor(eventConfigCommand);
        getCommand("eventconfig").setTabCompleter(eventConfigCommand);
    }

    private WorldGuardPlugin getWorldGuard() { //im broken i think
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        if (!(plugin instanceof WorldGuardPlugin)) {
            log.severe("WorldGuard not found!");
            return null;
        }

        return (WorldGuardPlugin) plugin;
    }

    public WorldGuardPlugin getWorldGuardPlugin() {
        return worldGuardPlugin;
    }
}
