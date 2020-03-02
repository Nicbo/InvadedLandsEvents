package me.nicbo.InvadedLandsEvents.commands;

import me.nicbo.InvadedLandsEvents.EventsMain;
import me.nicbo.InvadedLandsEvents.manager.managers.EventManager;
import me.nicbo.InvadedLandsEvents.utils.ConfigUtils;
import org.bukkit.Bukkit;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class EventConfigCommand implements CommandExecutor, TabCompleter {
    private EventsMain plugin;
    private FileConfiguration config;
    private final String usage;
    private List<String> args0;
    private HashMap<String, List<String>> args1;

    public EventConfigCommand(EventsMain plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.usage = ChatColor.GOLD + "Usage: " + ChatColor.YELLOW;
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
            if (args.length == 0) {
                sender.sendMessage(usage + "/econfig <help|event|save|reload|setting(event-world|spawn-location)> <setting|value> <value>");
                return true;
            } else if (args[0].equalsIgnoreCase("save")) {
                plugin.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Event config saved");
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                plugin.getManagerHandler().restartEventManager();
                sender.sendMessage(ChatColor.GREEN + "Event config reloaded");
                sender.sendMessage(ChatColor.GREEN + "Event manager reloaded");
                return true;
            } else if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You must be a player to execute this command!");
                return true;
            } else {
                // Uses reflection to call event method based on args[0]
                Player player = (Player) sender;
                try {
                    Method method = EventConfigCommand.class.getDeclaredMethod(args[0].toLowerCase().replace("-", ""), String[].class, Player.class);
                    method.invoke(this, args, player);
                } catch (NoSuchMethodException e) {
                    StringBuilder eventList = new StringBuilder(ChatColor.YELLOW.toString());
                    for (String event : EventManager.getEventNames()) {
                        eventList.append("\n   - ").append(event);
                    }
                    player.sendMessage(ChatColor.YELLOW + "'" + args[0] + "'" + ChatColor.GOLD
                            + " doesn't exist! Try event-world, win-command, spawn-location or an event. \nAll events: " + eventList.toString());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
        return false;
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

    private void help(String[] args, Player player) {
        player.sendMessage(usage + "/econfig <help|event|save|setting(event-world|spawn-location)> <setting|value> <value>");
        player.sendMessage(ChatColor.GOLD + "Tutorial video: " + ChatColor.YELLOW + "youtube.com/LINK_TO_VIDEO");
    }

    private void eventworld(String[] args, Player player) {
        if (args.length == 1) {
            player.sendMessage(ChatColor.GOLD + "event-world: " + ChatColor.YELLOW + config.getString("event-world"));
            player.sendMessage(usage + "/econfig event-world <string>");
        } else {
            config.set("event-world", args[1]);
            player.sendMessage(Bukkit.getWorld(args[1]) == null ? ChatColor.RED + "Warning: Could not find world " + ChatColor.YELLOW + "'" + args[1] + "'" + ChatColor.RED + "!"
                    : ChatColor.GOLD + "event-world set to " + ChatColor.YELLOW + "'" + args[1] + "'" + ChatColor.GOLD + "!");
        }
    }

    private void wincommand(String[] args, Player player) {
        if (args.length == 1) {
            player.sendMessage(ChatColor.GOLD + "win-command: " + ChatColor.YELLOW + config.getString("win-command"));
            player.sendMessage(usage + "/econfig win-command <string[]>");
        } else {
            List<String> command = new ArrayList<>();

            for (String arg : args) {
                command.add(arg + " ");
            }

            config.set("event-world", command.toString());
            player.sendMessage(ChatColor.GOLD + "win-command set to " + ChatColor.YELLOW + "'" + command.toString() + "'" + ChatColor.GOLD + "!");
        }
    }

    private void spawnlocation(String[] args, Player player) {
        config.set("spawn-location", player.getLocation());
        player.sendMessage(ChatColor.GOLD + "spawn-location set to " + ChatColor.YELLOW + "your location" + ChatColor.GOLD + "!");
    }

    private void sumo(String[] args, Player player) {
        ConfigurationSection section = config.getConfigurationSection("events.sumo");
        if (args.length == 1) {
            player.sendMessage(ConfigUtils.configSectionToMsgs(section));
            player.sendMessage("\n" + usage + "/econfig sumo <key>");
        } else {
            switch (args[1].toLowerCase()) {
                case "enabled":
                    if (args.length > 2) {
                        boolean enable = Boolean.parseBoolean(args[2]);
                        section.set("enabled", enable);
                        player.sendMessage(ChatColor.GOLD + "enabled set to " + ChatColor.YELLOW + enable + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig sumo enabled <boolean>");
                    }
                    break;
                case "start-location-1":
                case "start-location-2":
                case "spec-location":
                    ConfigUtils.serializeLoc(player.getLocation(), section.getConfigurationSection(args[1]));
                    player.sendMessage(ChatColor.GOLD + args[1] + " set to " + ChatColor.YELLOW + "your location" + ChatColor.GOLD + "!");
                    player.sendMessage(usage + "/econfig sumo " + args[1].toLowerCase() + " set");
                    break;
            }
        }
    }

    private void brackets(String[] args, Player player) {
        ConfigurationSection section = config.getConfigurationSection("events.brackets");
        if (args.length == 1) {
            player.sendMessage(ConfigUtils.configSectionToMsgs(section));
            player.sendMessage("\n" + usage + "/econfig brackets <key>");
        } else {
            switch (args[1].toLowerCase()) {
                case "enabled":
                    if (args.length > 2) {
                        boolean enable = Boolean.parseBoolean(args[2]);
                        section.set("enabled", enable);
                        player.sendMessage(ChatColor.GOLD + "enabled set to " + ChatColor.YELLOW + enable + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig brackets enabled <boolean>");
                    }
                    break;
                case "start-location-1":
                case "start-location-2":
                case "spec-location":
                    ConfigUtils.serializeLoc(player.getLocation(), section.getConfigurationSection(args[1]));
                    player.sendMessage(ChatColor.GOLD + args[1] + " set to " + ChatColor.YELLOW + "your location" + ChatColor.GOLD + "!");
                    player.sendMessage(usage + "/econfig brackets " + args[1].toLowerCase() + " set");
                    break;
            }
        }
    }

    private void koth(String[] args, Player player) {
        ConfigurationSection section = config.getConfigurationSection("events.koth");
        if (args.length == 1) {
            player.sendMessage(ConfigUtils.configSectionToMsgs(section));
            player.sendMessage("\n" + usage + "/econfig koth <key>");
        } else {
            switch (args[1].toLowerCase()) {
                case "region":
                    if (args.length > 2) {
                        section.set("region", args[2]);
                        player.sendMessage(ChatColor.GOLD + "region set to " + ChatColor.YELLOW + "'" + args[2] + "'" + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig koth region <string>");
                    }
                    break;
                case "enabled":
                    if (args.length > 2) {
                        boolean enable = Boolean.parseBoolean(args[2]);
                        section.set("enabled", enable);
                        player.sendMessage(ChatColor.GOLD + "enabled set to " + ChatColor.YELLOW + enable + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig koth enabled <boolean>");
                    }
                    break;
                case "start-location-1":
                case "start-location-2":
                case "start-location-3":
                case "start-location-4":
                case "spec-location":
                    ConfigUtils.serializeLoc(player.getLocation(), section.getConfigurationSection(args[1]));
                    player.sendMessage(ChatColor.GOLD + args[1] + " set to " + ChatColor.YELLOW + "your location" + ChatColor.GOLD + "!");
                    player.sendMessage(usage + "/econfig koth " + args[1].toLowerCase() + " set");
                    break;
            }
        }
    }

    private void lms(String[] args, Player player) {
        ConfigurationSection section = config.getConfigurationSection("events.lms");
        if (args.length == 1) {
            player.sendMessage(ConfigUtils.configSectionToMsgs(section));
            player.sendMessage("\n" + usage + "/econfig lms <key>");
        } else {
            switch (args[1].toLowerCase()) {
                case "enabled":
                    if (args.length > 2) {
                        boolean enable = Boolean.parseBoolean(args[2]);
                        section.set("enabled", enable);
                        player.sendMessage(ChatColor.GOLD + "enabled set to " + ChatColor.YELLOW + enable + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig lms enabled <boolean>");
                    }
                    break;
                case "start-location":
                case "spec-location":
                    ConfigUtils.serializeLoc(player.getLocation(), section.getConfigurationSection(args[1]));
                    player.sendMessage(ChatColor.GOLD + args[1] + " set to " + ChatColor.YELLOW + "your location" + ChatColor.GOLD + "!");
                    player.sendMessage(usage + "/econfig lms " + args[1].toLowerCase() + " set");
                    break;
            }
        }
    }

    private void oitc(String[] args, Player player) {
        ConfigurationSection section = config.getConfigurationSection("events.oitc");
        if (args.length == 1) {
            player.sendMessage(ConfigUtils.configSectionToMsgs(section));
            player.sendMessage("\n" + usage + "/econfig oitc <key>");
        } else {
            switch (args[1].toLowerCase()) {
                case "region":
                    if (args.length > 2) {
                        section.set("region", args[2]);
                        player.sendMessage(ChatColor.GOLD + "region set to " + ChatColor.YELLOW + "'" + args[2] + "'" + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig oitc region <string>");
                    }
                    break;
                case "enabled":
                    if (args.length > 2) {
                        boolean enable = Boolean.parseBoolean(args[2]);
                        section.set("enabled", enable);
                        player.sendMessage(ChatColor.GOLD + "enabled set to " + ChatColor.YELLOW + enable + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig oitc enabled <boolean>");
                    }
                    break;
                case "start-location-1":
                case "start-location-2":
                case "start-location-3":
                case "start-location-4":
                case "start-location-5":
                case "start-location-6":
                case "start-location-7":
                case "start-location-8":
                case "spec-location":
                    ConfigUtils.serializeLoc(player.getLocation(), section.getConfigurationSection(args[1]));
                    player.sendMessage(ChatColor.GOLD + args[1] + " set to " + ChatColor.YELLOW + "your location" + ChatColor.GOLD + "!");
                    player.sendMessage(usage + "/econfig oitc " + args[1].toLowerCase() + " set");
                    break;
            }
        }
    }

    private void redrover(String[] args, Player player) {
        ConfigurationSection section = config.getConfigurationSection("events.redrover");
        if (args.length == 1) {
            player.sendMessage(ConfigUtils.configSectionToMsgs(section));
            player.sendMessage("\n" + usage + "/econfig redrover <key>");
        } else {
            switch (args[1].toLowerCase()) {
                case "blue-region":
                case "red-region":
                    if (args.length > 2) {
                        section.set(args[1].toLowerCase(), args[2]);
                        player.sendMessage(ChatColor.GOLD + args[1].toLowerCase() + " set to " + ChatColor.YELLOW + "'" + args[2] + "'" + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig redrover " + args[1].toLowerCase() + " <string>");
                    }
                    break;
                case "enabled":
                    if (args.length > 2) {
                        boolean enable = Boolean.parseBoolean(args[2]);
                        section.set("enabled", enable);
                        player.sendMessage(ChatColor.GOLD + "enabled set to " + ChatColor.YELLOW + enable + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig redrover enabled <boolean>");
                    }
                    break;
                case "start-location":
                case "spec-location":
                    ConfigUtils.serializeLoc(player.getLocation(), section.getConfigurationSection(args[1]));
                    player.sendMessage(ChatColor.GOLD + args[1] + " set to " + ChatColor.YELLOW + "your location" + ChatColor.GOLD + "!");
                    player.sendMessage(usage + "/econfig redrover " + args[1].toLowerCase() + " set");
                    break;
            }
        }
    }

    private void rod(String[] args, Player player) {
        ConfigurationSection section = config.getConfigurationSection("events.rod");
        if (args.length == 1) {
            player.sendMessage(ConfigUtils.configSectionToMsgs(section));
            player.sendMessage("\n" + usage + "/econfig rod <key>");
        } else {
            switch (args[1].toLowerCase()) {
                case "enabled":
                    if (args.length > 2) {
                        boolean enable = Boolean.parseBoolean(args[2]);
                        section.set("enabled", enable);
                        player.sendMessage(ChatColor.GOLD + "enabled set to " + ChatColor.YELLOW + enable + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig rod enabled <boolean>");
                    }
                    break;
                case "start-location":
                case "spec-location":
                    ConfigUtils.serializeLoc(player.getLocation(), section.getConfigurationSection(args[1]));
                    player.sendMessage(ChatColor.GOLD + args[1] + " set to " + ChatColor.YELLOW + "your location" + ChatColor.GOLD + "!");
                    player.sendMessage(usage + "/econfig rod " + args[1].toLowerCase() + " set");
                    break;
            }
        }
    }

    private void spleef(String[] args, Player player) {
        ConfigurationSection section = config.getConfigurationSection("events.spleef");
        if (args.length == 1) {
            player.sendMessage(ConfigUtils.configSectionToMsgs(section));
            player.sendMessage("\n" + usage + "/econfig spleef <key>");
        } else {
            switch (args[1].toLowerCase()) {
                case "region":
                    if (args.length > 2) {
                        section.set("region", args[2]);
                        player.sendMessage(ChatColor.GOLD + "region set to " + ChatColor.YELLOW + "'" + args[2] + "'" + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig spleef region <string>");
                    }
                    break;
                case "enabled":
                    if (args.length > 2) {
                        boolean enable = Boolean.parseBoolean(args[2]);
                        section.set("enabled", enable);
                        player.sendMessage(ChatColor.GOLD + "enabled set to " + ChatColor.YELLOW + enable + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig spleef enabled <boolean>");
                    }
                    break;
                case "snow-position-1":
                case "snow-position-2":
                    Location loc = player.getLocation();
                    loc.setY(loc.getBlockY() - 1);
                    ConfigUtils.blockVectorToConfig(new BlockVector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), section.getConfigurationSection(args[1]));
                    player.sendMessage(ChatColor.GOLD + args[1] + " set to " + ChatColor.YELLOW + "the block under you" + ChatColor.GOLD + "!");
                    break;
                case "start-location-1":
                case "start-location-2":
                case "spec-location":
                    ConfigUtils.serializeLoc(player.getLocation(), section.getConfigurationSection(args[1]));
                    player.sendMessage(ChatColor.GOLD + args[1] + " set to " + ChatColor.YELLOW + "your location" + ChatColor.GOLD + "!");
                    break;
            }
        }
    }

    private void tdm(String[] args, Player player) {
        ConfigurationSection section = config.getConfigurationSection("events.tdm");
        if (args.length == 1) {
            player.sendMessage(ConfigUtils.configSectionToMsgs(section));
            player.sendMessage("\n" + usage + "/econfig tdm <key>");
        } else {
            switch (args[1].toLowerCase()) {
                case "enabled":
                    if (args.length > 2) {
                        boolean enable = Boolean.parseBoolean(args[2]);
                        section.set("enabled", enable);
                        player.sendMessage(ChatColor.GOLD + "enabled set to " + ChatColor.YELLOW + enable + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig tdm enabled <boolean>");
                    }
                    break;
                case "blue-start-location":
                case "red-start-location":
                case "spec-location":
                    ConfigUtils.serializeLoc(player.getLocation(), section.getConfigurationSection(args[1]));
                    player.sendMessage(ChatColor.GOLD + args[1] + " set to " + ChatColor.YELLOW + "your location" + ChatColor.GOLD + "!");
                    player.sendMessage(usage + "/econfig tdm " + args[1].toLowerCase() + " set");
                    break;
            }
        }
    }

    private void tnttag(String[] args, Player player) {
        ConfigurationSection section = config.getConfigurationSection("events.tnttag");
        if (args.length == 1) {
            player.sendMessage(ConfigUtils.configSectionToMsgs(section));
            player.sendMessage("\n" + usage + "/econfig tnttag <key>");
        } else {
            switch (args[1].toLowerCase()) {
                case "enabled":
                    if (args.length > 2) {
                        boolean enable = Boolean.parseBoolean(args[2]);
                        section.set("enabled", enable);
                        player.sendMessage(ChatColor.GOLD + "enabled set to " + ChatColor.YELLOW + enable + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig tnttag enabled <boolean>");
                    }
                    break;
                case "start-location":
                case "spec-location":
                    ConfigUtils.serializeLoc(player.getLocation(), section.getConfigurationSection(args[1]));
                    player.sendMessage(ChatColor.GOLD + args[1] + " set to " + ChatColor.YELLOW + "your location" + ChatColor.GOLD + "!");
                    player.sendMessage(usage + "/econfig tnttag " + args[1].toLowerCase() + " set");
                    break;
            }
        }
    }

    private void waterdrop(String[] args, Player player) {
        ConfigurationSection section = config.getConfigurationSection("events.waterdrop");
        if (args.length == 1) {
            player.sendMessage(ConfigUtils.configSectionToMsgs(section));
            player.sendMessage("\n" + usage + "/econfig waterdrop <key>");
        } else {
            switch (args[1].toLowerCase()) {
                case "enabled":
                    if (args.length > 2) {
                        boolean enable = Boolean.parseBoolean(args[2]);
                        section.set("enabled", enable);
                        player.sendMessage(ChatColor.GOLD + "enabled set to " + ChatColor.YELLOW + enable + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig waterdrop enabled <boolean>");
                    }
                    break;
                case "start-location":
                case "spec-location":
                    ConfigUtils.serializeLoc(player.getLocation(), section.getConfigurationSection(args[1]));
                    player.sendMessage(ChatColor.GOLD + args[1] + " set to " + ChatColor.YELLOW + "your location" + ChatColor.GOLD + "!");
                    player.sendMessage(usage + "/econfig waterdrop " + args[1].toLowerCase() + " set");
                    break;
            }
        }
    }

    private void woolshuffle(String[] args, Player player) {
        ConfigurationSection section = config.getConfigurationSection("events.woolshuffle");
        if (args.length == 1) {
            player.sendMessage(ConfigUtils.configSectionToMsgs(section));
            player.sendMessage("\n" + usage + "/econfig woolshuffle <key>");
        } else {
            switch (args[1].toLowerCase()) {
                case "enabled":
                    if (args.length > 2) {
                        boolean enable = Boolean.parseBoolean(args[2]);
                        section.set("enabled", enable);
                        player.sendMessage(ChatColor.GOLD + "enabled set to " + ChatColor.YELLOW + enable + ChatColor.GOLD + "!");
                    } else {
                        player.sendMessage(usage + "/econfig woolshuffle enabled <boolean>");
                    }
                    break;
                case "start-location":
                case "spec-location":
                    ConfigUtils.serializeLoc(player.getLocation(), section.getConfigurationSection(args[1]));
                    player.sendMessage(ChatColor.GOLD + args[1] + " set to " + ChatColor.YELLOW + "your location" + ChatColor.GOLD + "!");
                    player.sendMessage(usage + "/econfig woolshuffle " + args[1].toLowerCase() + " set");
                    break;
            }
        }
    }

    /*
    TODO:
        - A lot
        - This entire class needs to be re-written it is a shit show
        - tbh I don't know if I'm gonna optimize this class it's really boring
     */

}
