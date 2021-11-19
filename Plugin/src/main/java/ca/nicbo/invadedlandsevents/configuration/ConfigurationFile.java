package ca.nicbo.invadedlandsevents.configuration;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A configuration file.
 *
 * @author thehydrogen
 * @author Nicbo
 */
public class ConfigurationFile {
    private final String name;
    private final File file;
    private final boolean existed;
    private final FileConfiguration config;
    private final Logger logger;

    public ConfigurationFile(String fileName, InvadedLandsEventsPlugin plugin) {
        Validate.checkArgumentNotNull(fileName, "fileName");
        Validate.checkArgumentNotNull(plugin, "plugin");

        this.name = fileName;
        this.file = new File(plugin.getDataFolder(), fileName);
        this.existed = file.exists();

        if (!existed) {
            plugin.saveResource(fileName, false);
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        this.logger = plugin.getLogger();
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "could not save " + name, e);
        }
    }

    public void reload() {
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            logger.log(Level.SEVERE, "could not reload " + name, e);
        }
    }

    public boolean existed() {
        return existed;
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
