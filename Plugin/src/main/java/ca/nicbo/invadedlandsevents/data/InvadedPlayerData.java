package ca.nicbo.invadedlandsevents.data;

import ca.nicbo.invadedlandsevents.api.data.PlayerData;
import ca.nicbo.invadedlandsevents.api.data.PlayerEventData;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of {@link PlayerData}.
 *
 * @author Nicbo
 */
public class InvadedPlayerData implements PlayerData {
    private final UUID uuid;
    private final File file;
    private final FileConfiguration config;

    private final Map<EventType, PlayerEventData> eventDataMap;

    public InvadedPlayerData(UUID uuid, File file) {
        Validate.checkArgumentNotNull(uuid, "uuid");
        Validate.checkArgumentNotNull(file, "file");
        this.uuid = uuid;
        this.file = file;
        this.config = YamlConfiguration.loadConfiguration(file);
        Map<EventType, PlayerEventData> eventDataMap = new EnumMap<>(EventType.class);

        for (EventType eventType : EventType.values()) {
            final String path = eventType.getConfigName() + ".";
            final long timestamp = config.getLong(path + "timestamp");
            final int wins = config.getInt(path + "wins");
            eventDataMap.put(eventType, new InvadedPlayerEventData(eventType, timestamp, wins));
        }

        this.eventDataMap = Collections.unmodifiableMap(eventDataMap);
    }

    @Override
    public UUID getPlayerUUID() {
        return uuid;
    }

    @Override
    public PlayerEventData getEventData(EventType eventType) {
        Validate.checkArgumentNotNull(eventType, "eventType");
        return eventDataMap.get(eventType);
    }

    @Override
    public Map<EventType, PlayerEventData> getEventDataMap() {
        return eventDataMap;
    }

    @Override
    public void saveToFile() throws IOException {
        // Save event data
        for (Map.Entry<EventType, PlayerEventData> entry : eventDataMap.entrySet()) {
            final String path = entry.getKey().getConfigName() + ".";
            PlayerEventData eventData = entry.getValue();
            config.set(path + "timestamp", eventData.getHostTimestamp());
            config.set(path + "wins", eventData.getWins());
        }

        config.save(file);
    }
}
