package me.nicbo.InvadedLandsEvents.commands;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

public class EventConfigCommand implements CommandExecutor {    private EventsMain plugin;
    private FileConfiguration config;

    public EventConfigCommand(EventsMain plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) { //make me pretty pls
        if (cmd.getName().equalsIgnoreCase("eventconfig") || cmd.getName().equalsIgnoreCase("econfig")) {
            if (!(sender instanceof Player)) sender.sendMessage("This command must be used by a player ingame. You can config from yaml file.");
            if (args.length == 0) {
                sender.sendMessage("todo help menu");
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                sender.sendMessage("reloaded");
                return true;
            }else if (GeneralUtils.containsString(EventManager.getEventNames(), args[0])) {
                switch (args[0]) {
                    case "spleef":
                        spleef(args, (Player) sender);
                        break;
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
                        player.sendMessage("/eventconfig spleef region <string>");
                    } else {
                        section.set("region", args[2]);
                    }
                    break;
                case "enabled":
                    if (preview) {
                        player.sendMessage("/eventconfig spleef enabled <boolean>");
                    } else {
                        section.set("enabled", Boolean.valueOf(args[2]));
                    }
                    break;
                case "snow-position-1":
                case "snow-position-2":
                    if (preview) {
                        player.sendMessage("Stand on the block and do /eventconfig spleef " + args[1] + " set");
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
                        player.sendMessage("/eventconfig spleef " + args[1] + " set");
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
