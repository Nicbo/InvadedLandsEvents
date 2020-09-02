package me.nicbo.invadedlandsevents.commands;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.permission.EventPermission;
import me.nicbo.invadedlandsevents.util.ConfigUtils;
import me.nicbo.invadedlandsevents.util.GeneralUtils;
import me.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.BlockVector;
import org.bukkit.util.StringUtil;

import java.util.*;

/**
 * Handles all commands for /eventconfig or /econfig
 *
 * @author Nicbo
 */

public final class EventConfigCommand implements CommandExecutor, TabCompleter {
    private static final String USAGE;
    private static final String VIDEO;
    private static final String LINE;
    private static final String CONFIG_RELOADED;
    private static final String MESSAGES_RELOADED;
    private static final String SET_VALUE;
    private static final String INVALID_INT;

    private final InvadedLandsEvents plugin;

    private FileConfiguration config;

    private final Map<String, Set<String>> arguments;

    private final Set<String> stringListArguments;
    private final Set<String> booleanArguments;

    private final Map<String, String> placeholders;

    static {
        USAGE = ChatColor.GOLD + "Usage: " + ChatColor.YELLOW;
        VIDEO = ChatColor.GOLD + "Click me: " + ChatColor.YELLOW + "https://www.youtube.com/watch?v=GCN_d83mG_4";
        LINE = ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "----------------------------------------------------";
        CONFIG_RELOADED = ChatColor.GREEN + "Config reloaded from disk.";
        MESSAGES_RELOADED = ChatColor.GREEN + "Messages reloaded from disk.";
        SET_VALUE = ChatColor.GOLD + "{key} " + ChatColor.YELLOW + "set to " + ChatColor.GOLD + "{value}" + ChatColor.YELLOW + ".";
        INVALID_INT = ChatColor.RED + "Invalid int provided: {value}.";
    }

    public EventConfigCommand(InvadedLandsEvents plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        this.arguments = new LinkedHashMap<>();

        for (String section : config.getConfigurationSection("events").getKeys(false)) {
            this.arguments.put(section, config.getConfigurationSection("events." + section).getKeys(false));
        }

        this.arguments.put("reload", null);
        this.arguments.put("help", null);
        this.arguments.put("info", null);

        this.stringListArguments = new LinkedHashSet<>(Arrays.asList("list", "add", "remove"));
        this.booleanArguments = new LinkedHashSet<>(Arrays.asList("true", "false"));

        this.placeholders = new LinkedHashMap<>();
        this.placeholders.put("{winner}", "Name of player that won the event");
        this.placeholders.put("{winner_uuid}", "UUID of player that won the event");
        this.placeholders.put("{winner_name}", "Display name of player that won the event (nickname)");
        this.placeholders.put("{event}", "Config name for the event won (koth)");
        this.placeholders.put("{event_name}", "Display name for the event won (King Of The Hill)");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (cmd.getName().equalsIgnoreCase("eventconfig")) {
            // Perm check
            if (!sender.hasPermission(EventPermission.ADMIN)) {
                sender.sendMessage(Message.NO_PERMISSION.get());
                return true;
            }

            // For when they edit from disk
            if (args.length > 0 && "reload".equalsIgnoreCase(args[0])) {
                plugin.reloadConfig();
                this.config = plugin.getConfig();
                sender.sendMessage(CONFIG_RELOADED);
                plugin.getMessageManager().reload();
                sender.sendMessage(MESSAGES_RELOADED);
                return true;
            }


            // Rest of commands should be in game
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You can only use /econfig reload from here");
                return true;
            }

            Player player = (Player) sender;

            player.sendMessage(LINE);

            // No arguments, show help and possible sub commands
            if (args.length == 0) {
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "EVENT CONFIG:");
                player.sendMessage("");
                player.sendMessage(ChatColor.YELLOW + "Make sure you fully config the event before you enable it or the event will break when it is hosted.");
                player.sendMessage(ChatColor.YELLOW + "Be especially careful that you configure " + ChatColor.GOLD + "spleef" + ChatColor.YELLOW + " correctly or you could end up having your map turned to snow.");
                player.sendMessage(ChatColor.YELLOW + "For " + ChatColor.GOLD + "redrover" + ChatColor.YELLOW + " the " + ChatColor.GOLD + "start" + ChatColor.YELLOW + " is always on the " + ChatColor.GOLD + "blue" + ChatColor.YELLOW + " side.");
                player.sendMessage("");
                player.sendMessage(ChatColor.YELLOW + "Do " + ChatColor.GOLD + "/econfig help" + ChatColor.YELLOW + " for a tutorial video.");
                player.sendMessage("");
                player.sendMessage(ChatColor.YELLOW + "For some reason if you change any of these values from " + ChatColor.GOLD + "config.yml" + ChatColor.YELLOW + " manually (from disk), use " + ChatColor.GOLD + "/econfig reload" + ChatColor.YELLOW + ".");
                player.sendMessage(ChatColor.YELLOW + "The same goes for any messages in " + ChatColor.GOLD + "messages.yml" + ChatColor.YELLOW + ".");
                player.sendMessage("");
                sendPossibleSubCommands(player, arguments.keySet());
                player.sendMessage("");
                player.sendMessage(USAGE + "/econfig <sub-command>");
            } else {
                String firstArgument = args[0].toLowerCase();

                // Is an existing first argument
                if (arguments.containsKey(firstArgument)) {

                    // All non config sub commands here
                    if ("help".equals(firstArgument)) {
                        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "TUTORIAL VIDEO:");
                        player.sendMessage("");
                        player.sendMessage(VIDEO);
                    } else if ("info".equals(firstArgument)) {
                        player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "PLUGIN INFO:");
                        player.sendMessage("");

                        PluginDescriptionFile description = plugin.getDescription();
                        player.sendMessage(ChatColor.GOLD + "name: " + ChatColor.YELLOW + description.getName());
                        player.sendMessage(ChatColor.GOLD + "version: " + ChatColor.YELLOW + description.getVersion());
                        player.sendMessage(ChatColor.GOLD + "description: " + ChatColor.YELLOW + description.getDescription());
                        player.sendMessage(ChatColor.GOLD + "authors: " + ChatColor.YELLOW + description.getAuthors());
                        player.sendMessage(ChatColor.GOLD + "dependencies: " + ChatColor.YELLOW + description.getDepend());
                        player.sendMessage(ChatColor.GOLD + "soft-dependencies " + ChatColor.YELLOW + description.getSoftDepend());
                    } else {
                        ConfigurationSection section = config.getConfigurationSection("events." + firstArgument);

                        // Preview config section by sending values
                        if (args.length == 1) {
                            GeneralUtils.sendMessages(player, parseSectionToStrings(section));
                        } else {
                            String secondArgument = args[1].toLowerCase();

                            // Second argument exists
                            if (arguments.get(firstArgument).contains(secondArgument)) {
                                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "CONFIGURING " + firstArgument.toUpperCase() + ":");
                                player.sendMessage("");

                                SectionType type = SectionType.valueOf(section.getString(secondArgument + ".type"));
                                Location location = player.getLocation();
                                ConfigurationSection valueSection = section.getConfigurationSection(secondArgument);

                                boolean done = true;
                                String value = null;

                                // No arguments are required for these section types
                                switch (type) {
                                    case LOCATION:
                                        ConfigUtils.serializeEventLocation(valueSection, player.getLocation());
                                        value = StringUtils.locationToString(location);
                                        break;
                                    case BLOCK_VECTOR:
                                        BlockVector blockVector = new BlockVector(location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
                                        ConfigUtils.serializeBlockVector(valueSection, blockVector);
                                        value = StringUtils.blockVectorToString(blockVector);
                                        break;
                                    case FULL_LOCATION:
                                        ConfigUtils.serializeFullLocation(valueSection, player.getLocation());
                                        value = StringUtils.fullLocationToString(location);
                                        break;
                                    default:
                                        done = false;
                                        break;
                                }

                                // Config type was above
                                if (done) {
                                    player.sendMessage(SET_VALUE
                                            .replace("{key}", secondArgument)
                                            .replace("{value}", value));
                                    plugin.saveConfig();
                                } else {
                                    if (type == SectionType.STRING_LIST) {
                                        String thirdArgument = args.length == 2 ? "" : args[2].toLowerCase();
                                        Set<String> strings = new LinkedHashSet<>(section.getStringList(secondArgument + ".value"));
                                        switch (thirdArgument) {
                                            case "list":
                                                player.sendMessage(ChatColor.GOLD + secondArgument + ":");
                                                for (String string : strings) {
                                                    player.sendMessage(ChatColor.GOLD + "- " + ChatColor.YELLOW + string);
                                                }
                                                break;
                                            case "add":
                                            case "remove":
                                                if (args.length == 3) {
                                                    player.sendMessage(USAGE + "/econfig " + firstArgument + " " + secondArgument + " " + thirdArgument + " <STRING>");
                                                } else {
                                                    boolean altered;

                                                    if ("add".equals(thirdArgument)) {
                                                        altered = strings.add(args[3]);
                                                        player.sendMessage(altered ?
                                                                ChatColor.GREEN + args[3] + " was added to the " + secondArgument + "." :
                                                                ChatColor.RED + args[3] + " was already in the " + secondArgument + ".");
                                                    } else {
                                                        altered = strings.remove(args[3]);
                                                        player.sendMessage(altered ?
                                                                ChatColor.GREEN + args[3] + " was removed from the " + secondArgument + "." :
                                                                ChatColor.RED + args[3] + " was not in the list " + secondArgument + ".");
                                                    }

                                                    if (altered) {
                                                        section.set(secondArgument + ".value", new ArrayList<>(strings));
                                                        plugin.saveConfig();
                                                    }
                                                }
                                                break;
                                            default:
                                                sendPossibleSubCommands(player, stringListArguments);
                                                player.sendMessage("");
                                                player.sendMessage(USAGE + "/econfig " + firstArgument + " " + secondArgument + " " + "<list|add|remove> (STRING)");
                                                break;
                                        }
                                    } else {
                                        if (args.length == 2) {
                                            if (secondArgument.equals("win-command")) {
                                                player.sendMessage(ChatColor.GOLD + "Placeholders: ");
                                                for (String placeholder : placeholders.keySet()) {
                                                    player.sendMessage(ChatColor.GOLD + "- " + ChatColor.YELLOW + placeholder + ChatColor.GOLD + " - " + ChatColor.YELLOW + placeholders.get(placeholder));
                                                }
                                                player.sendMessage("");
                                            }
                                            player.sendMessage(USAGE + "/econfig " + firstArgument + " " + secondArgument + " <" + type.name() + ">");
                                        } else {
                                            switch (type) {
                                                case STRING:
                                                    String string = stringFromArgs(args, 2);
                                                    valueSection.set("value", string);
                                                    player.sendMessage(SET_VALUE
                                                            .replace("{key}", secondArgument)
                                                            .replace("{value}", string));
                                                    plugin.saveConfig();
                                                    break;
                                                case INT:
                                                    try {
                                                        int num = Integer.parseInt(args[2]);
                                                        valueSection.set("value", num);
                                                        player.sendMessage(SET_VALUE
                                                                .replace("{key}", secondArgument)
                                                                .replace("{value}", String.valueOf(num)));
                                                        plugin.saveConfig();
                                                    } catch (NumberFormatException nfe) {
                                                        player.sendMessage(INVALID_INT.replace("{value}", args[2]));
                                                    }
                                                    break;
                                                case BOOLEAN:
                                                    boolean bool = Boolean.parseBoolean(args[2]);
                                                    valueSection.set("value", bool);
                                                    player.sendMessage(SET_VALUE
                                                            .replace("{key}", secondArgument)
                                                            .replace("{value}", String.valueOf(bool)));
                                                    plugin.saveConfig();
                                                    break;
                                                default:
                                                    throw new IllegalStateException("Unexpected section type: " + type);
                                            }
                                        }
                                    }
                                }
                            } else { // not a possible second argument
                                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "INVALID SECOND ARGUMENT:");
                                player.sendMessage("");
                                sendPossibleSubCommands(player, arguments.get(firstArgument));
                            }
                        }
                    }
                } else { // not a possible first argument
                    player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "INVALID FIRST ARGUMENT:");
                    player.sendMessage("");
                    sendPossibleSubCommands(player, arguments.keySet());
                }
            }
            player.sendMessage(LINE);
            return true;
        }
        sender.sendMessage(Message.ERROR.get().replace("{error}", "EVENT_CONFIG_COMMAND"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        if (cmd.getName().equalsIgnoreCase("eventconfig")) {
            if (sender instanceof Player) {
                List<String> completions = new ArrayList<>();
                switch (args.length) {
                    case 1:
                        StringUtil.copyPartialMatches(args[0], arguments.keySet(), completions);
                        Collections.sort(completions);
                        break;
                    case 2:
                        Set<String> possibleArgs = arguments.get(args[0].toLowerCase());
                        if (possibleArgs != null) {
                            StringUtil.copyPartialMatches(args[1], possibleArgs, completions);
                        }
                        Collections.sort(completions);
                        break;
                    case 3:
                        String sectionKey = args[0].toLowerCase();
                        String sectionValue = args[1].toLowerCase();

                        Set<String> secondArguments = arguments.get(sectionKey);

                        if (secondArguments != null && secondArguments.contains(sectionValue)) {
                            String typeString = config.getString("events." + sectionKey + "." + sectionValue + ".type");

                            if (typeString != null) {
                                Iterable<String> values = null;

                                // Types that have tab completes
                                switch (SectionType.valueOf(typeString)) {
                                    case BOOLEAN:
                                        values = booleanArguments;
                                        break;
                                    case STRING_LIST:
                                        values = stringListArguments;
                                        break;
                                }

                                if (values != null) {
                                    StringUtil.copyPartialMatches(args[2], values, completions);
                                }
                            }
                        }
                        break;
                }

                return completions.isEmpty() ? null : completions;
            }
        }
        return null;
    }

    /**
     * Creates string from arguments
     *
     * @param args the arguments
     * @param skip the amount of arguments to skip
     * @return the string
     * @throws IllegalArgumentException if skip is less than 0 or >= to args.length
     */
    private static String stringFromArgs(String[] args, int skip) {
        if (skip < 0 || args.length <= skip) {
            throw new IllegalArgumentException("Illegal skip amount provided");
        }

        StringBuilder builder = new StringBuilder();
        for (int i = skip; i < args.length; i++) {
            builder.append(args[i]);
            if (i != args.length - 1) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }

    /**
     * Sends possible sub commands to player
     *
     * @param player      the player to receive the messages
     * @param subCommands the possible sub commands to send
     */
    private static void sendPossibleSubCommands(Player player, Set<String> subCommands) {
        player.sendMessage(ChatColor.GOLD + "Possible sub-commands:");
        player.sendMessage("");

        StringBuilder builder = new StringBuilder();

        int i = 0;
        for (String command : subCommands) {
            builder.append(ChatColor.YELLOW).append(command).append(ChatColor.GOLD);

            if (i++ < subCommands.size() - 1) {
                builder.append(",").append(" ");
            } else {
                builder.append(".");
            }
        }

        player.sendMessage(builder.toString());
    }

    /**
     * Parses configuration section to a list of strings
     *
     * @param section the configuration section used
     * @return the list of strings containing event section values
     */
    private List<String> parseSectionToStrings(ConfigurationSection section) {
        List<String> messages = new ArrayList<>();
        messages.add(ChatColor.GOLD + "" + ChatColor.BOLD + section.getName().toUpperCase() + " CONFIG:");
        messages.add("");
        for (String key : section.getKeys(false)) {
            ConfigurationSection valueSection = section.getConfigurationSection(key);
            SectionType type = SectionType.valueOf(valueSection.getString("type"));

            String message = ChatColor.GOLD + key + ": " + ChatColor.YELLOW;
            switch (type) {
                case LOCATION:
                    message += StringUtils.locationToString(ConfigUtils.deserializeEventLocation(valueSection));
                    break;
                case FULL_LOCATION:
                    message += StringUtils.fullLocationToString(ConfigUtils.deserializeFullLocation(valueSection));
                    break;
                case BLOCK_VECTOR:
                    message += StringUtils.blockVectorToString(ConfigUtils.deserializeBlockVector(valueSection));
                    break;
                case STRING_LIST:
                    message += "Do /econfig " + section.getName() + " " + key + " list";
                    break;
                default:
                    message += valueSection.getString("value");
                    break;
            }
            messages.add(message);
        }

        messages.add("");
        messages.add(USAGE + "/econfig " + section.getName() + " <key> <value>");
        return messages;
    }

    /**
     * Different types of values in config.yml
     */
    private enum SectionType {
        LOCATION,
        FULL_LOCATION,
        BLOCK_VECTOR,
        STRING,
        INT,
        BOOLEAN,
        STRING_LIST
    }

    /*
    TODO:
        - Clean this up it's a mess (sorry if you are reading this)
     */
}