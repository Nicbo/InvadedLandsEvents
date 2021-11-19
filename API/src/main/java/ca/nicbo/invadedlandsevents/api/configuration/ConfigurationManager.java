package ca.nicbo.invadedlandsevents.api.configuration;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Manages the configuration.
 *
 * @author Nicbo
 */
public interface ConfigurationManager {
    /**
     * Returns the handler for config.yml.
     *
     * @return the config handler
     */
    @NotNull
    ConfigHandler getConfigHandler();

    /**
     * Returns the handler for messages.yml.
     *
     * @return the messages handler
     */
    @NotNull
    MessagesHandler getMessagesHandler();

    /**
     * Returns the wand location holder for the provided player.
     *
     * @param player the player
     * @return the wand location holder
     * @throws NullPointerException if the player is null
     */
    @NotNull
    WandLocationHolder getWandLocationHolder(@NotNull Player player);

    /**
     * Returns an unmodifiable view on the wand location holder map.
     *
     * @return an unmodifiable view on the wand location holder map
     */
    @NotNull
    Map<Player, WandLocationHolder> getWandLocationHolderMap();
}
