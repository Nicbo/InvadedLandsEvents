package me.nicbo.invadedlandsevents.util;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.logging.Logger;

/**
 * @author thehydrogen
 */

public final class ConfigFile {
    private final File file;
    private FileConfiguration config;
    private final String name;
    private final Logger logger;

    public ConfigFile(String fileName, Plugin plugin) {
        this.file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(fileName, false);
        }

        this.name = fileName;
        this.logger = plugin.getLogger();

        try {
            this.config = new YamlConfiguration();
            config.load(file);
        } catch (Exception e) {
            logger.warning("Could not create the config file " + fileName);
        }
    }

    public void save() {
        try {
            config.save(file);
            reload();
        } catch (Exception e) {
            logger.info("Could not save the config file " + name);
        }
    }

    public void reload() {
        try {
            config = YamlConfiguration.loadConfiguration(file);
        } catch (Exception e) {
            logger.info("Could not reload the config file " + name);
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
