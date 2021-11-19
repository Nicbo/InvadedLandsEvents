package ca.nicbo.invadedlandsevents.configuration;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.configuration.MessagesHandler;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Implementation of {@link MessagesHandler}.
 *
 * @author Nicbo
 */
public class InvadedMessagesHandler extends InvadedConfigurationHandler implements MessagesHandler {
    private static final String CONFIG_FILE_NAME = "messages.yml";

    public InvadedMessagesHandler(InvadedLandsEventsPlugin plugin) {
        super(CONFIG_FILE_NAME, plugin);
    }

    @Override
    public void reload() {
        super.reload();
        load();
    }

    public void load() {
        FileConfiguration config = getInternalConfiguration();
        Message.setConfig(config);
        ListMessage.setConfig(config);
    }
}
