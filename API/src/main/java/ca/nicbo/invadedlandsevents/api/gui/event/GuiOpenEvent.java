package ca.nicbo.invadedlandsevents.api.gui.event;

import ca.nicbo.invadedlandsevents.api.gui.Gui;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a GUI is opened.
 *
 * @author Nicbo
 */
public class GuiOpenEvent extends GuiEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * Creates a GuiOpenEvent.
     *
     * @param gui the GUI that was opened
     * @throws NullPointerException if the GUI is null
     */
    public GuiOpenEvent(@NotNull Gui gui) {
        super(gui);
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
