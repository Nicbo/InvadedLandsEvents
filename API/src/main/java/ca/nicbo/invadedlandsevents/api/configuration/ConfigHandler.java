package ca.nicbo.invadedlandsevents.api.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Handles config.yml.
 *
 * @author Nicbo
 */
public interface ConfigHandler extends ConfigurationHandler {
    /**
     * Returns the config section by the provided name.
     *
     * @param name the name of the config section
     * @return the config section
     * @throws NullPointerException if the name is null
     */
    @NotNull
    ConfigSection getConfigSection(@NotNull String name);

    /**
     * Returns the config section names in config.yml.
     *
     * @return the config section names
     */
    @NotNull
    Set<String> getConfigSectionNames();
}
