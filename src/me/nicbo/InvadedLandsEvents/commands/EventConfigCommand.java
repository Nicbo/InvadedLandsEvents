package me.nicbo.InvadedLandsEvents.commands;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

public class EventConfigCommand implements CommandExecutor {
    private EventsMain plugin;
    private FileConfiguration config;
    private final String usage;

    public EventConfigCommand(EventsMain plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.usage = ChatColor.GOLD + "Usage: " + ChatColor.YELLOW;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) { //make me pretty pls
        if (cmd.getName().equalsIgnoreCase("eventconfig") || cmd.getName().equalsIgnoreCase("econfig")) {
            if (args.length == 0) {
                sender.sendMessage(usage + "/econfig <event|reload|setting> <setting|value> <value>");
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "Event config reloaded");
                return true;
            } else if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
                return true;
            } else {
                switch (args[0]) {
                    case "spleef":
                        spleef(args, (Player) sender);
                        break;
                    default:
                        StringBuilder eventList = new StringBuilder(ChatColor.YELLOW.toString());
                        for (String event : EventManager.getEventNames()) {
                            eventList.append("\n   - ").append(event);
                        }
                        sender.sendMessage(ChatColor.YELLOW + "'" + args[0] + "'" + ChatColor.GOLD + " doesn't exist! \nAll events: " + eventList.toString());
                }
                return true;
            }
        }
        return false;
    }

    private void spleef(String[] args, Player player) {
        ConfigurationSection section = config.getConfigurationSection("events.spleef");
        if (args.length == 1) player.sendMessage(ConfigUtils.configSectionToMsgs(section));
        else {
            boolean preview = args.length == 2;
            switch (args[1].toLowerCase()) {
                case "region":
                    if (preview) {
                        player.sendMessage(usage + "/eventconfig spleef region <string>");
                    } else {
                        section.set("region", args[2]);
                    }
                    break;
                case "enabled":
                    if (preview) {
                        player.sendMessage(usage + "/eventconfig spleef enabled <boolean>");
                    } else {
                        section.set("enabled", Boolean.valueOf(args[2]));
                    }
                    break;
                case "snow-position-1":
                case "snow-position-2":
                    if (preview) {
                        player.sendMessage(usage + "/eventconfig spleef " + args[1] + " set (while standing on block)");
                    } else {
                        Location loc = player.getLocation();
                        loc.setY(loc.getBlockY() - 1);
                        ConfigUtils.blockVectorToConfig(new BlockVector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), section.getConfigurationSection(args[1]));
                    }
                    break;
                case "start-location-1":
                case "start-location-2":
                case "spec-location":
                    if (preview) {
                        player.sendMessage(usage + "/eventconfig spleef " + args[1] + " set");
                    } else if (args[2].equalsIgnoreCase("set")) {
                        ConfigUtils.locToConfig(player.getLocation(), section.getConfigurationSection(args[1]));
                    }
                    break;
            }
        }
    }
    /*
    TODO:
        - A lot
        - This entire class needs to be re-written it is a shit show
        - Tab complete
     */

}
