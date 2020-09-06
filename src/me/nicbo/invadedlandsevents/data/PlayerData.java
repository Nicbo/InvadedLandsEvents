package me.nicbo.invadedlandsevents.data;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class that holds all data for an individual player
 *
 * @author Nicbo
 */

public final class PlayerData {
    private final File file;
    private final FileConfiguration config;
    private final UUID uuid;

    // Data
    private final Map<String, Long> eventTimestamps;

    private int cooldown;

    PlayerData(File file, UUID uuid) {
        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);
        this.uuid = uuid;
        this.eventTimestamps = new HashMap<>();

        ConfigurationSection generalConfig = config.getConfigurationSection("events.general");

        // Load old values from config
        ConfigurationSection timestampsSection = config.getConfigurationSection("timestamps");

        // If there are timestamps saved
        if (timestampsSection != null) {
            for (String event : timestampsSection.getKeys(false)) {
                this.eventTimestamps.put(event, timestampsSection.getLong(event));
            }
        }

        this.cooldown = generalConfig.getInt("host-seconds.value");
    }

    public void save() throws IOException {
        // Save all data here

        // Overwrite timestamps TODO: See if there is a better way to do this
        config.set("timestamps", null);

        // If the player actually has timestamps to save
        if (!eventTimestamps.isEmpty()) {
            ConfigurationSection timestampsSection = config.createSection("timestamps");

            for (String event : eventTimestamps.keySet()) {
                timestampsSection.set(event, eventTimestamps.get(event));
            }
        }

        config.save(file);
    }

    public void addTimestamp(String event) {
        eventTimestamps.put(event, System.currentTimeMillis());
    }

    public void removeTimestamp(String event) {
        eventTimestamps.remove(event);
    }

    public long getSecondsUntilHost(String event) {
        Long timestamp = eventTimestamps.get(event);

        if (timestamp == null) {
            return 0;
        }

        // Configurable cooldown time
        final int MILLISECONDS_IN_DAY = 1000 * cooldown;

        // Milliseconds in a day - how many milliseconds it's been since hosted
        long millisecondsLeft = MILLISECONDS_IN_DAY - (System.currentTimeMillis() - timestamp);

        if (millisecondsLeft <= 0) {
            eventTimestamps.remove(event);
            return 0;
        } else {
            return millisecondsLeft / 1000; // Seconds
        }
    }

    public UUID getUUID() {
        return uuid;
    }
}
