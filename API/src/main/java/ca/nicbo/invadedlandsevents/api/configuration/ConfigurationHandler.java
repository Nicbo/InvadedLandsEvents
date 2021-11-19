package ca.nicbo.invadedlandsevents.api.configuration;

/**
 * Handles a configuration file.
 *
 * @author Nicbo
 */
public interface ConfigurationHandler {
    /**
     * Returns the version of the configuration file.
     *
     * @return the version
     */
    int getVersion();

    /**
     * Saves the configuration file to disk.
     */
    void save();

    /**
     * Reloads the configuration file from disk.
     */
    void reload();
}
