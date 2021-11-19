package ca.nicbo.invadedlandsevents.data;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.data.PlayerData;
import ca.nicbo.invadedlandsevents.api.data.PlayerDataManager;
import ca.nicbo.invadedlandsevents.api.data.PlayerEventData;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.configuration.InvadedConfigurationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Implementation of {@link PlayerDataManager}.
 *
 * @author Nicbo
 */
public class InvadedPlayerDataManager implements PlayerDataManager, Listener {
    private final InvadedLandsEventsPlugin plugin;
    private final InvadedConfigurationManager configManager;

    private final Map<UUID, PlayerData> playerDataMap;

    public InvadedPlayerDataManager(InvadedLandsEventsPlugin plugin, InvadedConfigurationManager configManager) {
        Validate.checkArgumentNotNull(plugin, "plugin");
        Validate.checkArgumentNotNull(configManager, "configManager");
        this.plugin = plugin;
        this.configManager = configManager;
        this.playerDataMap = new HashMap<>();
    }

    public boolean createPlayerDataFolder() {
        File folder = getPlayerDataFolder();

        if (!folder.exists()) {
            boolean success = folder.mkdirs();
            Validate.checkState(success, "Could not create playerdata folder");
            return true;
        }

        return false;
    }

    private File getPlayerDataFolder() {
        return new File(plugin.getDataFolder(), "playerdata");
    }

    public void loadOnlinePlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            playerDataMap.put(uuid, load(uuid));
        }
    }

    public void scheduleSaveTask() {
        // Schedule async task to save data
        int interval = configManager.getConfigHandler().getConfigSection("general").getInteger("save-interval") * 20;
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            // Lock until saving is done
            synchronized (this) {
                for (PlayerData data : playerDataMap.values()) {
                    save(data);
                }
            }
        }, interval, interval);
    }

    private void save(PlayerData data) {
        try {
            data.saveToFile();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + data.getPlayerUUID() + " player data", e);
        }
    }

    private PlayerData load(UUID uuid) {
        final String fileName = uuid + ".yml";
        return new InvadedPlayerData(uuid, new File(plugin.getDataFolder() + File.separator + "playerdata", fileName));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        playerDataMap.put(uuid, load(uuid));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        PlayerData data = playerDataMap.remove(uuid);

        // Should never be null, if they are in the server their data will be loaded
        Validate.checkState(data != null, "%s data is not loaded", uuid);
        save(data);
    }

    @Override
    public PlayerData getPlayerData(UUID uuid) {
        Validate.checkArgumentNotNull(uuid, "uuid");
        PlayerData playerData = playerDataMap.get(uuid);
        return playerData == null ? load(uuid) : playerData;
    }

    @Override
    public Map<UUID, PlayerData> getPlayerDataMap() {
        return Collections.unmodifiableMap(playerDataMap);
    }

    @Override
    public Map<UUID, PlayerData> getGlobalPlayerDataMap() {
        Map<UUID, PlayerData> map = new HashMap<>();

        File folder = getPlayerDataFolder();
        File[] files = folder.listFiles();
        Validate.checkNotNull(files, "could not get files from the player data folder");

        for (File file : files) {
            final String fileExt = getFileExtension(file);

            if (!"yml".equalsIgnoreCase(fileExt)) {
                continue;
            }

            final String fileName = getFileNameWithoutExtension(file);
            final UUID uuid = UUID.fromString(fileName);
            map.put(uuid, new InvadedPlayerData(uuid, file));
        }

        return map;
    }

    public int getSecondsUntilHost(UUID uuid, EventType eventType) {
        PlayerEventData playerEventData = getPlayerData(uuid).getEventData(eventType);
        long timestamp = playerEventData.getHostTimestamp();

        if (timestamp == 0) {
            return 0;
        }

        final long cooldownMillis = configManager.getConfigHandler().getConfigSection("general").getInteger("host-cooldown") * 1000L;
        final long millisecondsLeft = cooldownMillis - (System.currentTimeMillis() - timestamp);
        return (int) (millisecondsLeft / 1000);
    }

    private static String getFileExtension(File file) {
        final String fileName = file.getName();
        final int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? "" : fileName.substring(dotIndex + 1);
    }

    private static String getFileNameWithoutExtension(File file) {
        final String fileName = file.getName();
        final int dotIndex = fileName.lastIndexOf('.');
        return dotIndex == -1 ? fileName : fileName.substring(0, dotIndex);
    }
}
