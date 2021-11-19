package ca.nicbo.invadedlandsevents.api.configuration;

import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.api.region.CuboidRegion;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Represents a section in config.yml.
 * <p>
 * The section expects the config to be in the correct state, any external editing could break functionality. Updated
 * values are automatically saved to the config file.
 *
 * @author Nicbo
 */
public interface ConfigSection {
    /**
     * Returns the name of this section.
     *
     * @return the name
     */
    @NotNull
    String getName();

    /**
     * Returns the boolean at the provided key.
     *
     * @param key the key
     * @return the boolean
     * @throws IllegalArgumentException if the key does not exist or the key's type is not {@link
     *         ConfigValueType#BOOLEAN}
     * @throws NullPointerException if the key is null
     */
    boolean getBoolean(@NotNull String key);

    /**
     * Sets the boolean at the provided key.
     *
     * @param key the key
     * @param value the boolean
     * @throws IllegalArgumentException if the key does not exist or the key's type is not {@link
     *         ConfigValueType#BOOLEAN}
     * @throws NullPointerException if the key is null
     */
    void setBoolean(@NotNull String key, boolean value);

    /**
     * Returns the integer at the provided key.
     *
     * @param key the key
     * @return the integer
     * @throws IllegalArgumentException if the key does not exist or the key's type is not {@link
     *         ConfigValueType#INTEGER}
     * @throws NullPointerException if the key is null
     */
    int getInteger(@NotNull String key);

    /**
     * Sets the integer at the provided key.
     *
     * @param key the key
     * @param value the integer
     * @throws IllegalArgumentException if the key does not exist or the key's type is not {@link
     *         ConfigValueType#INTEGER}
     * @throws NullPointerException if the key is null
     */
    void setInteger(@NotNull String key, int value);

    /**
     * Returns the kit at the provided key.
     *
     * @param key the key
     * @return the kit
     * @throws IllegalArgumentException if the key does not exist or the key's type is not {@link
     *         ConfigValueType#KIT}
     * @throws NullPointerException if the key is null
     */
    @NotNull
    Kit getKit(@NotNull String key);

    /**
     * Sets the kit at the provided key.
     *
     * @param key the key
     * @param value the kit
     * @throws IllegalArgumentException if the key does not exist or the key's type is not {@link
     *         ConfigValueType#KIT}
     * @throws NullPointerException if the key or value is null
     */
    void setKit(@NotNull String key, @NotNull Kit value);

    /**
     * Returns the location at the provided key.
     *
     * @param key the key
     * @return the location
     * @throws IllegalArgumentException if the key does not exist or the key's type is not {@link
     *         ConfigValueType#LOCATION}
     * @throws NullPointerException if the key is null
     */
    @NotNull
    Location getLocation(@NotNull String key);

    /**
     * Sets the location at the provided key.
     *
     * @param key the key
     * @param value the location
     * @throws IllegalArgumentException if the key does not exist or the key's type is not {@link
     *         ConfigValueType#LOCATION}
     * @throws NullPointerException if the key or value is null
     */
    void setLocation(@NotNull String key, @NotNull Location value);

    /**
     * Returns the region at the provided key.
     *
     * @param key the key
     * @return the region
     * @throws IllegalArgumentException if the key does not exist or the key's type is not {@link
     *         ConfigValueType#REGION}
     * @throws NullPointerException if the key is null
     */
    @NotNull
    CuboidRegion getRegion(@NotNull String key);

    /**
     * Sets the region at the provided key.
     *
     * @param key the key
     * @param value the region
     * @throws IllegalArgumentException if the key does not exist or the key's type is not {@link
     *         ConfigValueType#REGION}
     * @throws NullPointerException if the key or value is null
     */
    void setRegion(@NotNull String key, @NotNull CuboidRegion value);

    /**
     * Returns the string list at the provided key.
     *
     * @param key the key
     * @return the string list
     * @throws IllegalArgumentException if the key does not exist or the key's type is not {@link
     *         ConfigValueType#STRING_LIST}
     * @throws NullPointerException if the key is null
     */
    @NotNull
    List<String> getStringList(@NotNull String key);

    /**
     * Sets the region at the provided key.
     *
     * @param key the key
     * @param value the string list
     * @throws IllegalArgumentException if the key does not exist or the key's type is not {@link
     *         ConfigValueType#STRING_LIST}
     * @throws NullPointerException if the key or value is null
     */
    void setStringList(@NotNull String key, @NotNull List<String> value);

    /**
     * Returns the type of the provided key.
     *
     * @param key the key
     * @return the type
     * @throws IllegalArgumentException if the key does not exist
     * @throws NullPointerException if the key is null
     */
    @NotNull
    ConfigValueType getType(@NotNull String key);

    /**
     * Returns the description of the provided key.
     *
     * @param key the key
     * @return the description
     * @throws IllegalArgumentException if the key does not exist
     * @throws NullPointerException if the key is null
     */
    @NotNull
    String getDescription(@NotNull String key);

    /**
     * Returns the keys of this section.
     *
     * @return the keys
     */
    @NotNull
    Set<String> getKeys();
}
