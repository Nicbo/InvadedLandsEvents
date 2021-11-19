package ca.nicbo.invadedlandsevents;

import ca.nicbo.invadedlandsevents.api.InvadedLandsEvents;
import ca.nicbo.invadedlandsevents.api.InvadedLandsEventsProvider;
import ca.nicbo.invadedlandsevents.api.data.PlayerData;
import ca.nicbo.invadedlandsevents.api.event.Event;
import ca.nicbo.invadedlandsevents.api.gui.Button;
import ca.nicbo.invadedlandsevents.api.gui.Gui;
import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.api.region.CuboidRegion;
import ca.nicbo.invadedlandsevents.api.util.Callback;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.command.EventCommand;
import ca.nicbo.invadedlandsevents.command.EventConfigCommand;
import ca.nicbo.invadedlandsevents.compatibility.NMSVersion;
import ca.nicbo.invadedlandsevents.configuration.InvadedConfigurationManager;
import ca.nicbo.invadedlandsevents.data.InvadedPlayerDataManager;
import ca.nicbo.invadedlandsevents.event.InvadedEventManager;
import ca.nicbo.invadedlandsevents.gui.InvadedButton;
import ca.nicbo.invadedlandsevents.gui.InvadedGui;
import ca.nicbo.invadedlandsevents.gui.InvadedGuiManager;
import ca.nicbo.invadedlandsevents.kit.InvadedKit;
import ca.nicbo.invadedlandsevents.listener.ActiveListener;
import ca.nicbo.invadedlandsevents.region.InvadedCuboidRegion;
import org.bukkit.Location;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base plugin class and implementation of {@link InvadedLandsEvents}.
 *
 * @author Nicbo
 */
public class InvadedLandsEventsPlugin extends JavaPlugin implements InvadedLandsEvents {
    private final String UNSUPPORTED_CONFIG_OPERATION = "use the ConfigurationManager instead";

    private InvadedConfigurationManager configurationManager;
    private InvadedPlayerDataManager playerDataManager;
    private InvadedEventManager eventManager;
    private InvadedGuiManager guiManager;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        Logger logger = getLogger();

        logger.info("Detected " + NMSVersion.getCurrentVersion() + " as the NMS version.");

        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new ActiveListener(this), this);
        logger.info("Registered ActiveListener.");

        this.configurationManager = new InvadedConfigurationManager(this);
        logger.info("Created instance of InvadedConfigurationManager.");
        pluginManager.registerEvents(configurationManager, this);
        logger.info("Registered InvadedConfigurationManager.");
        if (configurationManager.getConfigHandler().isInitial()) {
            logger.info("Initializing config.yml.");
            configurationManager.getConfigHandler().loadDefaultKits();
            logger.info("Loaded default kits into config.yml.");
        }
        configurationManager.getMessagesHandler().load();
        logger.info("Loaded messages.");

        this.playerDataManager = new InvadedPlayerDataManager(this, configurationManager);
        logger.info("Created instance of PlayerDataManager.");
        pluginManager.registerEvents(playerDataManager, this);
        logger.info("Registered PlayerDataManager.");

        if (playerDataManager.createPlayerDataFolder()) {
            logger.info("Created player data folder.");
        }

        playerDataManager.loadOnlinePlayers();
        logger.info("Loaded " + getServer().getOnlinePlayers().size() + " online player(s) player data.");
        playerDataManager.scheduleSaveTask();
        logger.info("Scheduled player data save task.");

        this.eventManager = new InvadedEventManager(this, configurationManager, playerDataManager);
        logger.info("Created instance of EventManager.");
        pluginManager.registerEvents(eventManager, this);
        logger.info("Registered EventManager.");

        this.guiManager = new InvadedGuiManager();
        logger.info("Created instance of GuiManager.");
        pluginManager.registerEvents(guiManager, this);
        logger.info("Registered GuiManager.");
        guiManager.startUpdating(this);
        logger.info("Started updating all GUIs.");

        registerCommands();
        logger.info("Registered commands.");

        setProviderInstance(this);
        getServer().getServicesManager().register(InvadedLandsEvents.class, this, this, ServicePriority.High);
        getLogger().info("Registered InvadedLandsEvents API.");

        long endTime = System.currentTimeMillis();
        logger.info("Done enabling! (" + (endTime - startTime) + "ms)");
    }

    @Override
    public void onDisable() {
        long startTime = System.currentTimeMillis();
        Logger logger = getLogger();

        Event currentEvent = eventManager.getCurrentEvent();
        if (currentEvent != null) {
            currentEvent.forceEnd(true);
            logger.info("Force ended the active event.");
        }

        for (PlayerData data : playerDataManager.getPlayerDataMap().values()) {
            try {
                data.saveToFile();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "could not save " + data.getPlayerUUID() + " player data", e);
            }
        }
        logger.info("Saved all player data.");

        for (Gui gui : guiManager.getGuiMap().values()) {
            gui.close();
        }
        logger.info("Closed all open GUIs.");

        setProviderInstance(null);
        getServer().getServicesManager().unregisterAll(this);
        logger.info("Unregistered InvadedLandsEvents API.");

        long endTime = System.currentTimeMillis();
        logger.info("Done disabling! (" + (endTime - startTime) + "ms)");
    }

    private void registerCommands() {
        PluginCommand event = getCommand("event");
        PluginCommand eventConfig = getCommand("eventconfig");

        Validate.checkNotNull(event, "could not find event command");
        Validate.checkNotNull(eventConfig, "could not find event config command");

        EventCommand eventCommand = new EventCommand(this);
        EventConfigCommand eventConfigCommand = new EventConfigCommand(this);

        event.setExecutor(eventCommand);
        event.setTabCompleter(eventCommand);
        eventConfig.setExecutor(eventConfigCommand);
        eventConfig.setTabCompleter(eventConfigCommand);
    }

    @Override
    public FileConfiguration getConfig() {
        throw new UnsupportedOperationException(UNSUPPORTED_CONFIG_OPERATION);
    }

    @Override
    public void reloadConfig() {
        throw new UnsupportedOperationException(UNSUPPORTED_CONFIG_OPERATION);
    }

    @Override
    public void saveConfig() {
        throw new UnsupportedOperationException(UNSUPPORTED_CONFIG_OPERATION);
    }

    @Override
    public void saveDefaultConfig() {
        throw new UnsupportedOperationException(UNSUPPORTED_CONFIG_OPERATION);
    }

    @Override
    public InvadedConfigurationManager getConfigurationManager() {
        Validate.checkState(configurationManager != null, "ConfigurationManager is not ready");
        return configurationManager;
    }

    @Override
    public InvadedPlayerDataManager getPlayerDataManager() {
        Validate.checkState(playerDataManager != null, "PlayerDataManager is not ready");
        return playerDataManager;
    }

    @Override
    public InvadedEventManager getEventManager() {
        Validate.checkState(eventManager != null, "EventManager is not ready");
        return eventManager;
    }

    @Override
    public InvadedGuiManager getGuiManager() {
        Validate.checkState(guiManager != null, "GuiManager is not ready");
        return guiManager;
    }

    @Override
    public Gui createGui(Player player, String title, int size) {
        return new InvadedGui(player, title, size);
    }

    @Override
    public Button createButton(ItemStack itemStack, Callback callback) {
        return new InvadedButton(itemStack, callback);
    }

    @Override
    public Kit createKit(List<ItemStack> items, List<ItemStack> armour, ItemStack offhand) {
        return new InvadedKit(items, armour, offhand);
    }

    @Override
    public CuboidRegion createRegion(Location locationOne, Location locationTwo) {
        return new InvadedCuboidRegion(locationOne, locationTwo);
    }

    private static void setProviderInstance(InvadedLandsEvents instance) {
        try {
            Field field = InvadedLandsEventsProvider.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, instance);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("could not set provider's instance", e);
        }
    }
}
