package me.nicbo.InvadedLandsEvents;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.nicbo.InvadedLandsEvents.commands.EventCommand;
import me.nicbo.InvadedLandsEvents.commands.EventConfigCommand;
import me.nicbo.InvadedLandsEvents.listeners.GUIListener;
import me.nicbo.InvadedLandsEvents.messages.EventMessage;
import me.nicbo.InvadedLandsEvents.utils.ConfigFile;
import me.nicbo.InvadedLandsEvents.listeners.GeneralEventListener;
import me.nicbo.InvadedLandsEvents.handlers.ManagerHandler;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

/**
 * Main class
 *
 * @author Nicbo
 * @author StarZorrow
 * @since 2020-02-05
 */

public class EventsMain extends JavaPlugin {
    private static EventsMain instance;
    private static ConfigFile messages;
    private static ManagerHandler managerHandler;
    private static WorldGuardPlugin worldGuardPlugin;

    @Override
    public void onEnable() {
        instance = this;
        worldGuardPlugin = getWorldGuard();
        saveDefaultConfig();
        messages = new ConfigFile("messages.yml", this);
        EventMessage.reload();

        managerHandler = new ManagerHandler();
        managerHandler.getEventManager().reloadEvents();
        registerCommands();
        getServer().getPluginManager().registerEvents(new GeneralEventListener(), this);
        getServer().getPluginManager().registerEvents(new GUIListener(), this);

        getLogger().info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        saveDefaultConfig();
        messages.save();

        if (managerHandler.getEventManager().isEventRunning())
            managerHandler.getEventManager().getCurrentEvent().forceEndEvent();

        getLogger().info("Plugin disabled!");
    }

    private void registerCommands() {
        EventConfigCommand eventConfigCommand = new EventConfigCommand();
        EventCommand eventCommand = new EventCommand();
        getCommand("event").setExecutor(eventCommand);
        getCommand("event").setTabCompleter(eventCommand);
        getCommand("eventconfig").setExecutor(eventConfigCommand);
        getCommand("eventconfig").setTabCompleter(eventConfigCommand);
    }

    private WorldGuardPlugin getWorldGuard() {
        Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");

        if (!(plugin instanceof WorldGuardPlugin)) {
            getLogger().severe("WorldGuard not found!");
            return null;
        }

        return (WorldGuardPlugin) plugin;
    }

    public static WorldGuardPlugin getWorldGuardPlugin() {
        return worldGuardPlugin;
    }

    public static ManagerHandler getManagerHandler() {
        return managerHandler;
    }

    public static EventsMain getInstance() {
        return instance;
    }

    public static ConfigFile getMessages() {
        return messages;
    }

    /*
    TODO:
        - clean this up
     */
}
