package me.nicbo.invadedlandsevents.data;

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

    PlayerData(File file, UUID uuid, boolean existed) {
        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);
        this.uuid = uuid;
        this.eventTimestamps = new HashMap<>();

        // Load old values from config
        if (existed) {
            for (String event : config.getConfigurationSection("timestamps").getKeys(false)) {
                this.eventTimestamps.put(event, config.getLong("timestamps." + event));
            }
        } else {
            config.createSection("timestamps");
        }
    }

    public void save() throws IOException {
        // Save all data here
        for (String event : eventTimestamps.keySet()) {
            config.set("timestamps." + event, eventTimestamps.get(event));
        }

        config.save(file);
    }

    public void addTimestamp(String event) {
        eventTimestamps.put(event, System.currentTimeMillis());
    }

    public void clearTimestamps() {
        eventTimestamps.clear();
    }

    public long getSecondsUntilHost(String event) {
        Long timestamp = eventTimestamps.get(event);

        if (timestamp == null) {
            return 0;
        }

        final int MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;

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
