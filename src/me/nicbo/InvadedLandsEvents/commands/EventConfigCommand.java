package me.nicbo.InvadedLandsEvents.commands;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import me.nicbo.InvadedLandsEvents.messages.EventMessage;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import me.nicbo.InvadedLandsEvents.utils.GeneralUtils;
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
import java.util.stream.Collectors;

/**
 * Event Config command class, handles commands for /eventconfig
 *
 * @author Nicbo
 * @since 2020-03-12
 */

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
        this.args0 = new ArrayList<>(Arrays.asList(EventManager.getEventNames()));
        this.args0.addAll(Arrays.asList(
                "save",
                "reload",
                "help",
                "event-world",
                "win-command",
                "spawn-location"
        ));

        this.args1 = new HashMap<>();

        for (String eventName : config.getConfigurationSection("events").getKeys(false)) {
            List<String> eventKeys = new ArrayList<>(config.getConfigurationSection("events." + eventName).getKeys(false));
            this.args1.put(eventName, eventKeys);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) { // perm check @ top
        if (cmd.getName().equalsIgnoreCase("eventconfig") || cmd.getName().equalsIgnoreCase("econfig")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
                return true;
            }

            Player player = (Player) sender;
            if (args.length == 0) { // No args, show possible args0
                sender.sendMessage(ConfigUtils.getConfigMessage(config));
                possibleArgs(player, null, true);
            } else {
                args[0] = args[0].toLowerCase();
                if (args0.contains(args[0])) { // Is an existing arg[0]
                    if (EventManager.eventExists(args[0])) { // Arg[0] is an event
                        ConfigurationSection eventConfigSection = config.getConfigurationSection("events." + args[0]);
                        if (args.length == 1) { // Event preview, send event config (probably change later it is ugly)
                            player.sendMessage(ChatColor.YELLOW + "-=-=-=-=-=-=-=-=- " + ChatColor.GOLD + args[0] + " config" + ChatColor.YELLOW + " -=-=-=-=-=-=-=-=-");
                            player.sendMessage(ConfigUtils.getConfigMessage(eventConfigSection));
                            player.sendMessage(ChatColor.YELLOW + "-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
                            return true;
                        }

                        String key = args[1].toLowerCase();
                        String path = "events." + args[0] + "." + key;

                        if (args1.get(args[0]).contains(key)) { // Arg1 exists
                            if (key.contains("location")) { // Set section to players location
                                ConfigUtils.serializeLoc(config.getConfigurationSection(path), player.getLocation(), false);
                                player.sendMessage(ChatColor.GOLD + key + ChatColor.YELLOW + " set to " + ChatColor.GOLD + "your location" + ChatColor.YELLOW + "!");
                            } else if (key.contains("position")) { // Set section to block under player
                                Location loc = player.getLocation();
                                loc.setY(loc.getBlockY() - 1);
                                ConfigUtils.serializeBlockVector(config.getConfigurationSection(path), new BlockVector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
                                player.sendMessage(ChatColor.GOLD + key + ChatColor.YELLOW + " set to " + ChatColor.GOLD + "the block under you" + ChatColor.YELLOW + "!");
                            } else if (key.contains("region")) { // Set section to region name
                                if (args.length == 2)
                                    player.sendMessage(getUsageMessage(args[0], key, "string"));
                                else {
                                    eventConfigSection.set(key, args[2].toLowerCase());
                                    player.sendMessage(ChatColor.GOLD + key + ChatColor.YELLOW + " set to '" + ChatColor.GOLD + key + ChatColor.YELLOW + "'!");
                                }
                            } else if (key.equals("enabled")) { // Set section to boolean
                                if (args.length == 2)
                                    player.sendMessage(getUsageMessage(args[0], key, "boolean"));
                                else {
                                    boolean bool = Boolean.parseBoolean(args[2].toLowerCase());
                                    eventConfigSection.set(key, bool);
                                    player.sendMessage(ChatColor.GOLD + key + ChatColor.YELLOW + " set to '" + ChatColor.GOLD + bool + ChatColor.YELLOW + "'!");
                                }
                            } else if (key.contains("int")) { // Set section to int
                                if (args.length == 2)
                                    player.sendMessage(getUsageMessage(args[0], key, "int"));
                                else {
                                    if (GeneralUtils.isInt(args[2])) {
                                        eventConfigSection.set(key, args[2]);
                                        player.sendMessage(ChatColor.GOLD + key + ChatColor.YELLOW + " set to '" + ChatColor.GOLD + args[2] + ChatColor.YELLOW + "'!");

                                    } else {
                                        player.sendMessage(getUsageMessage(args[0], key, "int"));
                                    }
                                }
                            }
                        } else { // Not an arg1
                            player.sendMessage(DOES_NOT_EXIST);
                            possibleArgs(player, args[0], false);
                        }
                    } else { // Not configuring event
                        switch (args[0]) {
                            case "save":
                                plugin.saveConfig();
                                sender.sendMessage(ChatColor.GREEN + "Event config saved");
                                plugin.reloadConfig();
                                sender.sendMessage(ChatColor.GREEN + "Event config reloaded");
                                EventsMain.getMessages().save();
                                sender.sendMessage(ChatColor.GREEN + "Event messages saved");
                                EventsMain.getMessages().reload();
                                EventMessage.reload();
                                sender.sendMessage(ChatColor.GREEN + "Event messages reloaded");
                                break;
                            case "reload":
                                plugin.reloadConfig();
                                sender.sendMessage(ChatColor.GREEN + "Event config reloaded");
                                plugin.getManagerHandler().restartEventManager();
                                sender.sendMessage(ChatColor.GREEN + "Event manager reloaded");
                                break;
                            case "event-world":
                                if (args.length == 1)
                                    player.sendMessage(USAGE + "/econfig " + args[0]  + " <string>");
                                else {
                                    config.set(args[0], args[1]);
                                    player.sendMessage(ChatColor.GOLD + args[0] + ChatColor.YELLOW + " set to '" + ChatColor.GOLD + args[1] + ChatColor.YELLOW + "'!");
                                }
                                break;
                            case "win-command":
                                if (args.length == 1) { // Send usage message, not enough arguments
                                    player.sendMessage(ChatColor.GOLD + "win-command placeholders: " + ChatColor.YELLOW + "{winner}");
                                    player.sendMessage(USAGE + "/econfig " + args[0]  + " <string>");
                                } else { // Set eventSetting to args[1]
                                    String winCommand = prepareWinCommand(args);
                                    config.set(args[0], winCommand);
                                    player.sendMessage(ChatColor.GOLD + args[0] + ChatColor.YELLOW + " set to '" + ChatColor.GOLD + winCommand + ChatColor.YELLOW + "'!");
                                }
                                break;
                            case "spawn-location":
                                ConfigUtils.serializeLoc(config.getConfigurationSection(args[0]), player.getLocation(), true);
                                player.sendMessage(ChatColor.GOLD + args[0] + ChatColor.YELLOW + " set to " + ChatColor.GOLD + "your location!");
                                break;
                        }
                    }
                } else { // not a possible args[0]
                    player.sendMessage(DOES_NOT_EXIST);
                    possibleArgs(player, args[0], true);
                }
            }
            plugin.saveConfig();
            return true;
        }
        return false;
    }

    private String prepareWinCommand(String[] args) {
        return Arrays.stream(args).skip(1).collect(Collectors.joining(" "));
    }

    /**
     * Returns usage message for event config
     * @param eventSetting Event name
     * @param key Config section key
     * @param type Type being set
     * @return String usage message
     */

    private String getUsageMessage(String eventSetting, String key, String type) {
        return USAGE + "/econfig " + eventSetting + " " + key + " <" + type + ">";
    }

    /**
     * Sends player list of possible sub commands
     * @param player Player to send message to
     * @param arg0 Used for getting args1 sub commands
     * @param checkArgs0 Boolean that decides between sub commands for args0 or args1
     */

    private void possibleArgs(Player player, String arg0, boolean checkArgs0) {
        player.sendMessage(POSSIBLE_SUB_COMMANDS);
        for (String subCommand : checkArgs0 ? args0 : args1.get(arg0)) {
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
                    List<String> possibleArgs = args1.get(args[0]);
                    if (possibleArgs != null) {
                        StringUtil.copyPartialMatches(args[1], args1.get(args[0]), completions);
                        Collections.sort(completions);
                    }
                }
                return completions;
            }
        }
        return null;
    }
}
