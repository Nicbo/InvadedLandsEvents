package me.nicbo.InvadedLandsEvents.commands;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.manager.managers.EventManager;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.StringUtil;

import java.util.*;

public class EventConfigCommand implements CommandExecutor, TabCompleter {
    private EventsMain plugin;
    private FileConfiguration config;
    private List<String> args0;
    private HashMap<String, List<String>> args1;

    private final String DOES_NOT_EXIST = ChatColor.RED + "Sub command does not exist!";
    private final String POSSIBLE_SUB_COMMANDS = ChatColor.GOLD + "Possible sub commands: ";
    private final String USAGE = ChatColor.GOLD + "Usage: " + ChatColor.YELLOW;

    public EventConfigCommand(EventsMain plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.args0 = new ArrayList<>();
        this.args0.addAll(Arrays.asList(EventManager.getEventNames()));
        this.args0.add("save");
        this.args0.add("reload");
        this.args0.add("help");
        this.args0.add("event-world");
        this.args0.add("win-command");
        this.args0.add("spawn-location");

        this.args1 = new HashMap<>();

        for (String event : config.getConfigurationSection("events").getKeys(false)) {
            List<String> keys = new ArrayList<>(config.getConfigurationSection("events." + event).getKeys(false));
            this.args1.put(event, keys);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) { // perm check @ top
        if (cmd.getName().equalsIgnoreCase("eventconfig") || cmd.getName().equalsIgnoreCase("econfig")) {
            sender.sendMessage("\n");

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
                return true;
            }

            Player player = (Player) sender;
            if (args.length == 0) { // No arguments show first key values
                sender.sendMessage(ConfigUtils.getConfigMessage(config));
                possibleArgs(player, null, true, false);
            } else {
                String eventSetting = args[0].toLowerCase();

                if (args0.contains(eventSetting)) { // Is an existing arg[0]
                    if (EventManager.eventExists(eventSetting)) { // Arg[0] is an event
                        ConfigurationSection section = config.getConfigurationSection("events." + eventSetting);
                        if (args.length == 1) { // Event preview, send event config
                            player.sendMessage(ConfigUtils.getConfigMessage(section));
                            return true;
                        }

                        String path = eventSetting + "." + args[1].toLowerCase();
                        String key = args[1].toLowerCase();

                        if (args[1].contains("location")) { // Event config target is a location
                            try { // set section to player's location
                                ConfigUtils.serializeLoc(config.getConfigurationSection("events." + path), player.getLocation(), false);
                                player.sendMessage(ChatColor.GOLD + key + ChatColor.YELLOW + " set to " + ChatColor.GOLD + "your location" + ChatColor.YELLOW + "!");
                            } catch (NullPointerException npe) { // had location in string but config section does not exist
                                possibleArgs(player, eventSetting, false, true);
                            }
                        } else if (args[1].contains("position")) {
                            try { // set section to player's block vector under them
                                Location loc = player.getLocation();
                                loc.setY(loc.getBlockY() - 1);
                                ConfigUtils.serializeBlockVector(config.getConfigurationSection("events." + path), new BlockVector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                                player.sendMessage(ChatColor.GOLD + key + ChatColor.YELLOW + " set to " + ChatColor.GOLD + "the block under you" + ChatColor.YELLOW + "!");
                            } catch (NullPointerException npe) { // had position in string but config section does not exist
                                possibleArgs(player, eventSetting, false, true);
                            }
                        } else if (args[1].contains("region")) { // Event config target is a region
                            if (args.length == 2) // Send usage message, not enough arguments
                                player.sendMessage(USAGE + "/econfig " + eventSetting + " " + key + " <string>");
                            else { // set region to args[2]
                                try {
                                    section.set(key, args[2].toLowerCase());
                                    player.sendMessage(ChatColor.GOLD + key + ChatColor.YELLOW + " set to '" + ChatColor.GOLD + key + ChatColor.YELLOW + "'!");
                                } catch (NullPointerException npe) { // had region in string but config section does not exist
                                    possibleArgs(player, eventSetting, false, true);
                                }
                            }
                        } else if (args[1].equalsIgnoreCase("enabled")) { // Event config target is enabled
                            if (args.length == 2) // Send usage message, not enough arguments
                                player.sendMessage(USAGE + "/econfig " + eventSetting + " " + key + " <boolean>");
                            else { // set enabled to parsed args[2]
                                boolean bool = Boolean.parseBoolean(args[2].toLowerCase());
                                section.set(key, bool);
                                player.sendMessage(ChatColor.GOLD + key + ChatColor.YELLOW + " set to '" + ChatColor.GOLD + bool + ChatColor.YELLOW + "'!");
                            }
                        } else { // not a possible arg[1]
                            possibleArgs(player, eventSetting, false, true);
                        }
                    } else { // Not configuring event
                        switch (eventSetting) {
                            case "save":
                                plugin.saveConfig();
                                sender.sendMessage(ChatColor.GREEN + "Event config saved");
                                plugin.reloadConfig();
                                sender.sendMessage(ChatColor.GREEN + "Event config reloaded");
                                break;
                            case "reload":
                                plugin.reloadConfig();
                                sender.sendMessage(ChatColor.GREEN + "Event config reloaded");
                                plugin.getManagerHandler().restartEventManager();
                                sender.sendMessage(ChatColor.GREEN + "Event manager reloaded");
                                break;
                            case "event-world":
                            case "win-command":
                                if (args.length == 1) { // Send usage message, not enough arguments
                                    if (eventSetting.equalsIgnoreCase("win-command"))
                                        player.sendMessage(ChatColor.GOLD + "win-command placeholders: " + ChatColor.YELLOW + "{winner}");
                                    player.sendMessage(USAGE + "/econfig " + eventSetting  + " <string>");
                                } else { // Set eventSetting to args[1]
                                    config.set(eventSetting, args[1]);
                                    player.sendMessage(ChatColor.GOLD + eventSetting + ChatColor.YELLOW + " set to '" + ChatColor.GOLD + args[1] + ChatColor.YELLOW + "'!");
                                }
                                break;
                            case "spawn-location":
                                ConfigUtils.serializeLoc(config.getConfigurationSection(eventSetting), player.getLocation(), true);
                                player.sendMessage(ChatColor.GOLD + eventSetting + ChatColor.YELLOW + " set to " + ChatColor.GOLD + "your location!");
                                break;
                        }
                    }
                } else { // not a possible args[0]
                    possibleArgs(player, eventSetting, true, true);
                }
            }
            plugin.saveConfig();
            return true;
        }
        return false;
    }

    private void possibleArgs(Player player, String eventSetting, boolean first, boolean error) {
        if (error)
            player.sendMessage(DOES_NOT_EXIST);
        player.sendMessage(POSSIBLE_SUB_COMMANDS);
        for (String subCommand : first ? args0 : args1.get(eventSetting)) {
            player.sendMessage(ChatColor.YELLOW + "- " + subCommand);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        if (cmd.getName().equalsIgnoreCase("eventconfig") || cmd.getName().equalsIgnoreCase("econfig")) {
            if (sender instanceof Player) {
                List<String> completions = new ArrayList<>();
                if (args.length == 1) {
                    StringUtil.copyPartialMatches(args[0], args0, completions);
                    Collections.sort(completions);
                } else if (!args[0].equals("") && args.length == 2) {
                    StringUtil.copyPartialMatches(args[1], args1.get(args[0]), completions);
                    Collections.sort(completions);
                }
                return completions;
            }
        }
        return null;
    }
}
