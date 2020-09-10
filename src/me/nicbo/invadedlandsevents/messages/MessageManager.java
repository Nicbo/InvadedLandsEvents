package me.nicbo.invadedlandsevents.messages;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.messages.impl.ListMessage;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.util.ConfigFile;
import me.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages messages
 *
 * @author Nicbo
 */

public final class MessageManager {
    private final Logger logger;
    private final ConfigFile messages;
    private final FileConfiguration messagesDefaults;

    /**
     * Creates instance of MessageManager, creates messages.yml file and loads defaults for it
     *
     * @param plugin the instance of the main class
     */
    public MessageManager(InvadedLandsEvents plugin) {
        this.logger = plugin.getLogger();
        this.messages = new ConfigFile("messages.yml", plugin);

        FileConfiguration messagesDefaultsVal = null;
        try {
            InputStreamReader messagesStreamReader = new InputStreamReader(plugin.getResource("messages.yml"), StandardCharsets.UTF_8);
            messagesDefaultsVal = YamlConfiguration.loadConfiguration(messagesStreamReader);
            logger.info("message.yml defaults loaded.");
        } catch (Exception e) {
            logger.severe("Error getting messages.yml defaults.");
            e.printStackTrace();
        }

        this.messagesDefaults = messagesDefaultsVal;
    }

    /**
     * Reloads messages
     */
    public void reload() {
        messages.reload();
        load();
    }

    /**
     * Loads messages into the message enums from config
     * If the message is missing it will attempt to get it's default
     *
     * @see Message
     * @see ListMessage
     */
    public void load() {
        logger.info("Loading messages...");
        for (Message message : Message.values()) {
            String msg = messages.getConfig().getString(message.getPath());
            if (msg == null) {
                logger.severe("Could not get " + message.getPath() + " message from messages.yml. Attempting to get default.");
                msg = messagesDefaults.getString(message.getPath());
                if (msg == null) {
                    logger.severe("Could not get default of " + message.getPath() + "!");
                    msg = "&cCOULD_NOT_DEFAULT";
                } else {
                    logger.info("Found default of " + message.getPath() + "!");
                }
            }
            message.set(StringUtils.colour(msg));
        }
        logger.info("Finished loading messages.");

        logger.info("Loading list messages...");
        for (ListMessage message : ListMessage.values()) {
            List<String> msgs = messages.getConfig().getStringList(message.getPath());
            if (msgs == null) {
                logger.severe("Could not get " + message.getPath() + " messages from messages.yml. Attempting to get defaults.");
                msgs = messagesDefaults.getStringList(message.getPath());
                if (msgs == null) {
                    logger.severe("Could not get defaults of " + message.getPath() + "!");
                    msgs = Collections.singletonList("&cCOULD_NOT_DEFAULT");
                } else {
                    logger.info("Found defaults of " + message.getPath() + "!");
                }
            }
            message.set(StringUtils.colour(msgs));
        }
        logger.info("Finished loading list messages.");
    }
}
