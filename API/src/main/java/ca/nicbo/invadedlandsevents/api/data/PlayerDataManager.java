package ca.nicbo.invadedlandsevents.api.data;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Manages the {@link PlayerData} for the online players.
 * <p>
 * All player data is stored in the playerdata folder at the root of the plugin's data folder.
 *
 * @author Nicbo
 */
public interface PlayerDataManager {
    /**
     * Returns the player data associated with the provided player UUID.
     *
     * @param uuid the player's UUID
     * @return the player data
     * @throws NullPointerException if the uuid is null
     */
    @NotNull
    PlayerData getPlayerData(@NotNull UUID uuid);

    /**
     * Returns an unmodifiable view of the online player's data map.
     *
     * @return an unmodifiable view of the online player's data map
     */
    @NotNull
    Map<UUID, PlayerData> getPlayerDataMap();

    /**
     * Returns an unmodifiable map of all the data in the player data folder. Note that this operation could take a long
     * time depending on how many players have logged in to the server.
     *
     * @return an unmodifiable map of all the data in the player data folder
     */
    @NotNull
    Map<UUID, PlayerData> getGlobalPlayerDataMap();
}
