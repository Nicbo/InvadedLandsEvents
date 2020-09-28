package me.nicbo.invadedlandsevents;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.nicbo.invadedlandsevents.commands.EventCommand;
import me.nicbo.invadedlandsevents.commands.EventConfigCommand;
import me.nicbo.invadedlandsevents.data.PlayerDataManager;
import me.nicbo.invadedlandsevents.events.EventManager;
import me.nicbo.invadedlandsevents.gui.GUI;
import me.nicbo.invadedlandsevents.gui.host.HostGUI;
import me.nicbo.invadedlandsevents.listeners.GeneralListener;
import me.nicbo.invadedlandsevents.messages.MessageManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class that holds all instances of managers
 *
 * @author Nicbo
 * @author StarZorrow
 */

public final class InvadedLandsEvents extends JavaPlugin {
    private MessageManager messageManager;
    private EventManager eventManager;
    private PlayerDataManager playerDataManager;

    private WorldGuardPlugin worldGuardPlugin;

    @Override
    public void onEnable() {
        // Check for WorldGuard
        this.getLogger().info("Looking for WorldGuard...");
        Plugin worldguard = this.getServer().getPluginManager().getPlugin("WorldGuard");
        if (worldguard instanceof WorldGuardPlugin) {
            this.getLogger().info("Found WorldGuard!");
        } else {
            throw new RuntimeException("WorldGuard not found! InvadedLandsEvents can't run without it.");
        }
        this.worldGuardPlugin = (WorldGuardPlugin) worldguard;

        // Create config files
        this.saveDefaultConfig();

        // Create instance of message manager
        this.messageManager = new MessageManager(this);
        this.getLogger().info("Created instance of MessageManager.");
        this.messageManager.load();
        this.getLogger().info("Loaded messages.");

        // Create instance of cooldown manager
        this.playerDataManager = new PlayerDataManager(this);
        this.getLogger().info("Created instance of PlayerDataManager.");

        // Create instance of event manager
        this.eventManager = new EventManager(this);
        this.getLogger().info("Created instance of EventManager.");

        // Register commands and listeners
        this.registerCommands();
        this.getServer().getPluginManager().registerEvents(new GeneralListener(this), this);
        this.getLogger().info("Registered commands and listeners.");

        // Schedule task to update all open host guis
        this.getServer().getScheduler().runTaskTimer(this, () -> {
            for (GUI gui : GUI.getOpenGUIs().values()) {
                if (gui instanceof HostGUI) {
                    ((HostGUI) gui).update();
                }
            }
        }, 0, 10);
        this.getLogger().info("Scheduled task to update all open host guis.");

        this.getLogger().info("Done enabling!");
    }

    @Override
    public void onDisable() {
        // Force end if event is running
        if (eventManager.isEventActive()) {
            eventManager.getCurrentEvent().forceEndEvent(true);
            this.getLogger().info("Force ended active event.");
        }

        // Save all cooldowns to config
        this.playerDataManager.saveAll();
        this.getLogger().info("Saved all player data.");

        // Close all open GUIs in case of reload
        for (GUI gui : GUI.getOpenGUIs().values()) {
            gui.close();
        }
        this.getLogger().info("Closed all open GUIs.");

        this.getLogger().info("Done disabling!");
    }

    /**
     * Creates instances of commands and sets their executors and tab completers
     */
    private void registerCommands() {
        EventConfigCommand eventConfigCommand = new EventConfigCommand(this);
        EventCommand eventCommand = new EventCommand(this);
        getCommand("event").setExecutor(eventCommand);
        getCommand("event").setTabCompleter(eventCommand);

        getCommand("eventconfig").setExecutor(eventConfigCommand);
        getCommand("eventconfig").setTabCompleter(eventConfigCommand);
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public WorldGuardPlugin getWorldGuardPlugin() {
        return worldGuardPlugin;
    }

    /*
    TODO:
        - When invalid values are in event config it should set valid to false
        - This whole project should be cleaned up
     */
}
