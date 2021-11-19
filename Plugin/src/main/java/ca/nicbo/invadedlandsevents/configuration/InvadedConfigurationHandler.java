package ca.nicbo.invadedlandsevents.configuration;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.configuration.ConfigurationHandler;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Implementation of {@link ConfigurationHandler}.
 *
 * @author Nicbo
 */
public abstract class InvadedConfigurationHandler implements ConfigurationHandler {
    private final ConfigurationFile configurationFile;

    protected InvadedConfigurationHandler(String configFileName, InvadedLandsEventsPlugin plugin) {
        Validate.checkArgumentNotNull(configFileName, "configFileName");
        Validate.checkArgumentNotNull(plugin, "plugin");
        this.configurationFile = new ConfigurationFile(configFileName, plugin);
    }

    public FileConfiguration getInternalConfiguration() {
        return configurationFile.getConfig();
    }

    public boolean isInitial() {
        return !configurationFile.existed();
    }

    @Override
    public int getVersion() {
        return getInternalConfiguration().getInt("version");
    }

    @Override
    public void save() {
        configurationFile.save();
    }

    @Override
    public void reload() {
        configurationFile.reload();
    }
}
