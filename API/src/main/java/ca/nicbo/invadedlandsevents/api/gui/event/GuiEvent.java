package ca.nicbo.invadedlandsevents.api.gui.event;

import ca.nicbo.invadedlandsevents.api.gui.Gui;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a GUI related event.
 *
 * @author Nicbo
 */
public abstract class GuiEvent extends Event {
    private final Gui gui;

    /**
     * Creates a GuiEvent.
     *
     * @param gui the GUI involved with the event
     * @throws NullPointerException if the GUI is null
     */
    protected GuiEvent(@NotNull Gui gui) {
        Validate.checkArgumentNotNull(gui, "gui");
        this.gui = gui;
    }

    /**
     * Returns the GUI involved with the event.
     *
     * @return the GUI
     */
    @NotNull
    public Gui getGui() {
        return gui;
    }
}
