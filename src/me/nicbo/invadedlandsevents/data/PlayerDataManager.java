package me.nicbo.invadedlandsevents.data;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all player data and saves it
 *
 * @author Nicbo
 */

public final class PlayerDataManager implements Listener {
    private final InvadedLandsEvents plugin;

    private final Map<UUID, PlayerData> playerDataMap;

    public PlayerDataManager(InvadedLandsEvents plugin) {
        this.plugin = plugin;
        this.playerDataMap = new HashMap<>();

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        File dataFolder = new File(plugin.getDataFolder(), "playerdata");

        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        // 20 minutes
        final int delay = 20 * 60 * 20;

        // Schedule async task to save data every 20 minutes
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            plugin.getLogger().info("Saving player data...");
            // Lock until saving is done
            synchronized (this) {
                saveAll();
            }
            plugin.getLogger().info("Player data saved.");
        }, delay, delay);

        // Load all online players (in case of reload)
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            playerDataMap.put(uuid, load(uuid));
        }

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public PlayerData getData(UUID uuid) {
        PlayerData playerData = playerDataMap.get(uuid);

        if (playerData != null) {
            return playerData;
        } else {
            return load(uuid);
        }
    }

    private void save(UUID uuid) {
        try {
            playerDataMap.get(uuid).save();
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving player data for " + uuid);
            e.printStackTrace();
        }
    }

    public void saveAll() {
        for (UUID uuid : playerDataMap.keySet()) {
            save(uuid);
        }
    }

    private PlayerData load(UUID uuid) {
        File file = new File(plugin.getDataFolder() + File.separator + "playerdata", uuid + ".yml");

        boolean exists = file.exists();

        if (!exists) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create player data config for " + uuid);
                e.printStackTrace();
            }
        }

        return new PlayerData(file, uuid, exists);
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        playerDataMap.put(uuid, load(uuid));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLeave(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        save(uuid);
        playerDataMap.remove(uuid);
    }

    /*
    TODO:
        - Better error handling, if anything is wrong with file it should give warning and overwrite?
     */
}

