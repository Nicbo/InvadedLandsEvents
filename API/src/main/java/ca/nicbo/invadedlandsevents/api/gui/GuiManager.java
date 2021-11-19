package ca.nicbo.invadedlandsevents.api.gui;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Manages each player's {@link Gui}.
 *
 * @author Nicbo
 */
public interface GuiManager {
    /**
     * Returns the GUI that the player currently has open or null.
     *
     * @param player the player
     * @return the GUI
     * @throws NullPointerException if the player is null
     */
    @Nullable
    Gui getGui(@NotNull Player player);

    /**
     * Returns an unmodifiable view of the GUI map.
     *
     * @return an unmodifiable view of the GUI map
     */
    @NotNull
    Map<Player, Gui> getGuiMap();
}
