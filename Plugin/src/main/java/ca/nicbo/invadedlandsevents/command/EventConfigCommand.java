package ca.nicbo.invadedlandsevents.command;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.configuration.ConfigSection;
import ca.nicbo.invadedlandsevents.api.configuration.ConfigValueType;
import ca.nicbo.invadedlandsevents.api.configuration.WandLocationHolder;
import ca.nicbo.invadedlandsevents.api.kit.Kit;
import ca.nicbo.invadedlandsevents.api.permission.EventPermission;
import ca.nicbo.invadedlandsevents.api.region.CuboidRegion;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.configuration.InvadedConfigHandler;
import ca.nicbo.invadedlandsevents.configuration.InvadedConfigurationManager;
import ca.nicbo.invadedlandsevents.configuration.InvadedMessagesHandler;
import ca.nicbo.invadedlandsevents.configuration.Message;
import ca.nicbo.invadedlandsevents.gui.config.KitViewGui;
import ca.nicbo.invadedlandsevents.kit.InvadedKit;
import ca.nicbo.invadedlandsevents.region.InvadedCuboidRegion;
import ca.nicbo.invadedlandsevents.util.CollectionUtils;
import ca.nicbo.invadedlandsevents.util.SpigotUtils;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Handles anything related to /econfig.
 *
 * @author Nicbo
 */
public class EventConfigCommand implements CommandExecutor, TabCompleter {
    private static final String USAGE = "&6Usage: &e";
    private static final String LINE = "&e&m----------------------------------------------------";

    private static final String TUTORIAL_VIDEO_LINK = "https://www.youtube.com/watch?v=GCN_d83mG_4";
    private static final String DISCORD_LINK = "https://discord.gg/ycXggGd";

    private static final Map<ConfigValueType, ConfigValueTypeInfo> CONFIG_VALUE_TYPE_INFO_MAP;

    private final InvadedLandsEventsPlugin plugin;
    private final InvadedConfigurationManager configurationManager;
    private final InvadedConfigHandler configHandler;
    private final InvadedMessagesHandler messagesHandler;

    private final List<String> firstArguments;
    private final Map<String, SingleArgumentCommand> singleArgumentCommandMap;
    private final Map<ConfigValueType, ConfigCommand> configCommandMap;

    static {
        Map<ConfigValueType, ConfigValueTypeInfo> configValueTypeMap = new EnumMap<>(ConfigValueType.class);
        configValueTypeMap.put(ConfigValueType.BOOLEAN, new ConfigValueTypeInfo(CollectionUtils.unmodifiableList("true", "false"), "<true|false>"));
        configValueTypeMap.put(ConfigValueType.INTEGER, new ConfigValueTypeInfo(Collections.emptyList(), "<integer>"));
        configValueTypeMap.put(ConfigValueType.KIT, new ConfigValueTypeInfo(CollectionUtils.unmodifiableList("view", "give", "set", "reset"), "<view|give|set|reset>"));
        configValueTypeMap.put(ConfigValueType.LOCATION, new ConfigValueTypeInfo(Collections.singletonList("set"), "<set>"));
        configValueTypeMap.put(ConfigValueType.REGION, new ConfigValueTypeInfo(Collections.singletonList("set"), "<set>"));
        configValueTypeMap.put(ConfigValueType.STRING_LIST, new ConfigValueTypeInfo(CollectionUtils.unmodifiableList("list", "add", "remove"), "<list|add (string)|remove (integer)>"));
        CONFIG_VALUE_TYPE_INFO_MAP = Collections.unmodifiableMap(configValueTypeMap);
    }

    public EventConfigCommand(InvadedLandsEventsPlugin plugin) {
        Validate.checkArgumentNotNull(plugin, "plugin");

        this.plugin = plugin;
        this.configurationManager = plugin.getConfigurationManager();
        this.configHandler = configurationManager.getConfigHandler();
        this.messagesHandler = configurationManager.getMessagesHandler();

        List<String> firstArguments = new ArrayList<>();
        firstArguments.add("help");
        firstArguments.add("info");
        firstArguments.add("reload");
        firstArguments.add("wand");
        firstArguments.addAll(configHandler.getConfigSectionNames());
        this.firstArguments = Collections.unmodifiableList(firstArguments);

        Map<String, SingleArgumentCommand> singleArgumentCommandMap = new HashMap<>();
        singleArgumentCommandMap.put("help", this::sendHelp);
        singleArgumentCommandMap.put("info", this::sendInfo);
        singleArgumentCommandMap.put("reload", this::sendReloadConfig);
        singleArgumentCommandMap.put("wand", this::sendRegionWand);
        this.singleArgumentCommandMap = Collections.unmodifiableMap(singleArgumentCommandMap);

        Map<ConfigValueType, ConfigCommand> configCommandMap = new HashMap<>();
        configCommandMap.put(ConfigValueType.BOOLEAN, this::sendSetBoolean);
        configCommandMap.put(ConfigValueType.INTEGER, this::sendSetInteger);
        configCommandMap.put(ConfigValueType.KIT, this::sendSetKit);
        configCommandMap.put(ConfigValueType.LOCATION, this::sendSetLocation);
        configCommandMap.put(ConfigValueType.REGION, this::sendSetRegion);
        configCommandMap.put(ConfigValueType.STRING_LIST, this::sendSetStringList);
        this.configCommandMap = Collections.unmodifiableMap(configCommandMap);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(EventPermission.CONFIG)) {
            sender.sendMessage(Message.NO_PERMISSION.get());
            return true;
        }

        ConfigCommandContext context = new ConfigCommandContext(new Sender(sender));

        if (args.length == 0) {
            sendHome(context.sender);
            return true;
        }

        context.section = args[0].toLowerCase();
        if (args.length == 1) {
            SingleArgumentCommand singleArgumentCommand = singleArgumentCommandMap.get(context.section);

            if (singleArgumentCommand == null) {
                if (configHandler.getConfigSectionNames().contains(context.section)) {
                    sendSectionPreview(context);
                } else {
                    sendInvalidFirstArgument(context);
                }
            } else {
                singleArgumentCommand.execute(context.sender);
            }

            return true;
        }

        context.key = args[1].toLowerCase();
        ConfigSection configSection = configHandler.getConfigSection(context.section);
        if (!configSection.getKeys().contains(context.key)) {
            sendInvalidSecondArgument(context);
            return true;
        }

        ConfigValueType type = configSection.getType(context.key);
        if (args.length == 2) {
            sendValueUsage(context);
            return true;
        }

        context.value = args[2];
        ConfigValueTypeInfo info = getConfigValueTypeInfo(type);
        if (!info.args.isEmpty() && !info.args.contains(context.value.toLowerCase())) {
            sendInvalidThirdArgument(context);
            return true;
        }

        ConfigCommand configCommand = configCommandMap.get(type);
        Validate.checkSupported(configCommand != null, "can't run command for unsupported config value type: %s", type);
        if (args.length > 3) {
            context.overflow = Arrays.copyOfRange(args, 3, args.length);
        }

        configCommand.execute(context);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(EventPermission.CONFIG)) {
            return null;
        }

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], firstArguments, completions);
        } else {
            String section = args[0].toLowerCase();
            if (configHandler.getConfigSectionNames().contains(section)) {
                ConfigSection configSection = configHandler.getConfigSection(section);
                if (args.length == 2) {
                    StringUtil.copyPartialMatches(args[1], configSection.getKeys(), completions);
                } else if (args.length == 3) {
                    String key = args[1].toLowerCase();
                    if (configSection.getKeys().contains(key)) {
                        ConfigValueType type = configSection.getType(key);
                        ConfigValueTypeInfo info = getConfigValueTypeInfo(type);
                        StringUtil.copyPartialMatches(args[2], info.args, completions);
                    }
                }
            }
        }

        return completions;
    }

    private void sendHome(Sender sender) {
        sender.sendColouredMessage(LINE);
        sender.sendColouredMessage("&6&lEVENT CONFIG");
        sender.sendColouredMessage("");
        sender.sendColouredMessage("&eMake sure you &6fully configure &ethe event before you enable it or the event will break when it is hosted.");
        sender.sendColouredMessage("&eBe especially careful that you configure &6spleef &ecorrectly or you could end up having your map turned to snow.");
        sender.sendColouredMessage("");
        sender.sendColouredMessage("&eDo &6/econfig help &efor help.");
        sender.sendColouredMessage("");
        sender.sendColouredMessage("&eFor some reason if you change any of these values in &6config.yml &emanually (from disk), use &6/econfig reload&e.");
        sender.sendColouredMessage("&eThe same goes for any messages in &6messages.yml&e.");
        sender.sendColouredMessage("");
        sender.sendColouredMessage("&eIf you have any &6conflicting plugins (scoreboard, combat tag, etc.) &eyou will need to &6disable &ethem in the &6event world&e.");
        sender.sendColouredMessage("");
        sender.sendColouredMessage("&6Subcommands: " + StringUtils.formatStringList(firstArguments));
        sender.sendColouredMessage("");
        sender.sendColouredMessage(USAGE + "/econfig <subcommand> <key> <value>");
        sender.sendColouredMessage(LINE);
    }

    private void sendRegionWand(Sender sender) {
        if (!sender.isPlayer()) {
            sender.sendColouredMessage("&eYou must be a player to use the region wand.");
            return;
        }

        sender.sendColouredMessage(LINE);
        sender.sendColouredMessage("&6&lREGION WAND");
        sender.sendColouredMessage("");
        sender.player.getInventory().addItem(InvadedConfigurationManager.WAND);
        sender.sendColouredMessage("&eYou were given the &6InvadedLandsEvents Region Wand&e.");
        sender.sendColouredMessage(LINE);
    }

    private void sendHelp(Sender sender) {
        sender.sendColouredMessage(LINE);
        sender.sendColouredMessage("&6&lEVENT CONFIG HELP");
        sender.sendColouredMessage("");
        sender.sendColouredMessage("&6Tutorial Video: &e" + TUTORIAL_VIDEO_LINK);
        sender.sendColouredMessage("&6Support Discord Server: &e" + DISCORD_LINK);
        sender.sendColouredMessage(LINE);
    }

    private void sendReloadConfig(Sender sender) {
        sender.sendColouredMessage(LINE);
        sender.sendColouredMessage("&6&lRELOADING CONFIG");
        sender.sendColouredMessage("");
        configHandler.reload();
        sender.sendColouredMessage("&eReloaded &6config.yml &efrom disk.");
        messagesHandler.reload();
        sender.sendColouredMessage("&eReloaded &6messages.yml &efrom disk.");
        sender.sendColouredMessage(LINE);
    }

    private void sendInfo(Sender sender) {
        sender.sendColouredMessage(LINE);
        sender.sendColouredMessage("&6&lPLUGIN INFO");
        sender.sendColouredMessage("");
        PluginDescriptionFile description = plugin.getDescription();
        sender.sendColouredMessage("&6name: &e" + description.getName());
        sender.sendColouredMessage("&6version: &e" + description.getVersion());
        sender.sendColouredMessage("&6description: &e" + description.getDescription());
        sender.sendColouredMessage("&6authors: &e" + description.getAuthors());
        sender.sendColouredMessage("&6website: &e" + description.getWebsite());
        sender.sendColouredMessage("&6dependencies: &e" + description.getDepend());
        sender.sendColouredMessage("&6soft-dependencies: &e" + description.getSoftDepend());
        sender.sendColouredMessage(LINE);
    }

    private void sendSectionPreview(ConfigCommandContext context) {
        ConfigSection section = configHandler.getConfigSection(context.section);

        context.sender.sendColouredMessage(LINE);
        context.sender.sendColouredMessage("&6&l" + context.section.toUpperCase());
        context.sender.sendColouredMessage("");

        for (String key : section.getKeys()) {
            String value = getFriendlyValue(section, key);
            final String message = ChatColor.GOLD + key + ": " + ChatColor.YELLOW + value;
            context.sender.sendMessage(message);
        }

        context.sender.sendColouredMessage("");
        context.sender.sendColouredMessage(USAGE + "/econfig " + context.section + " <key> <value>");
        context.sender.sendColouredMessage(LINE);
    }

    private void sendValueUsage(ConfigCommandContext context) {
        ConfigSection section = configHandler.getConfigSection(context.section);
        ConfigValueType type = section.getType(context.key);
        ConfigValueTypeInfo info = getConfigValueTypeInfo(type);
        context.sender.sendColouredMessage(LINE);
        context.sender.sendColouredMessage("&6&l" + context.section.toUpperCase() + " " + context.key.toUpperCase());
        context.sender.sendColouredMessage("");
        context.sender.sendColouredMessage("&6Type: &e" + type);
        context.sender.sendColouredMessage("&6Description: &e" + section.getDescription(context.key));
        context.sender.sendMessage(ChatColor.GOLD + "Value: " + ChatColor.YELLOW + getFriendlyValue(section, context.key));
        context.sender.sendColouredMessage("");
        context.sender.sendColouredMessage(USAGE + "/econfig " + context.section + " " + context.key + " " + info.usage);
        context.sender.sendColouredMessage(LINE);
    }

    private void sendSetLocation(ConfigCommandContext context) {
        if (!context.sender.isPlayer()) {
            sendSetValueError(context, ChatColor.YELLOW + "You must be a player to configure locations.");
            return;
        }

        ConfigSection section = configHandler.getConfigSection(context.section);
        Location location = context.sender.player.getLocation();
        section.setLocation(context.key, location);
        sendSetValue(context, StringUtils.locationToString(location));
    }

    private void sendSetRegion(ConfigCommandContext context) {
        if (!context.sender.isPlayer()) {
            sendSetValueError(context, ChatColor.YELLOW + "You must be a player to configure regions.");
            return;
        }

        WandLocationHolder holder = configurationManager.getWandLocationHolder(context.sender.player);
        Location locationOne = holder.getLocationOne();
        Location locationTwo = holder.getLocationTwo();

        // Validate locations for region
        final String error;
        if (locationOne == null) {
            error = "&eYou are missing &6location one&e.";
        } else if (locationTwo == null) {
            error = "&eYou are missing &6location two&e.";
        } else if (locationOne.getWorld() == null) {
            error = "&eThe world is &6null &efor &6location one&e.";
        } else if (locationTwo.getWorld() == null) {
            error = "&eThe world is &6null &efor &6location two&e.";
        } else if (!locationOne.getWorld().equals(locationTwo.getWorld())) {
            error = "&eThe location's worlds must be the same &6" + holder + "&e.";
        } else {
            // Set new region
            ConfigSection section = configHandler.getConfigSection(context.section);
            CuboidRegion region = new InvadedCuboidRegion(holder.getLocationOne(), holder.getLocationTwo());
            section.setRegion(context.key, region);
            sendSetValue(context, region.toString());
            return;
        }

        sendSetValueError(context, StringUtils.colour(error));
    }

    private void sendSetBoolean(ConfigCommandContext context) {
        ConfigSection section = configHandler.getConfigSection(context.section);
        boolean bool = Boolean.parseBoolean(context.value);
        section.setBoolean(context.key, bool);
        sendSetValue(context, String.valueOf(bool));
    }

    private void sendSetInteger(ConfigCommandContext context) {
        ConfigSection section = configHandler.getConfigSection(context.section);
        final int num;
        try {
            num = Integer.parseInt(context.value);
        } catch (NumberFormatException nfe) {
            sendSetValueError(context, ChatColor.GOLD + "Invalid Integer: " + ChatColor.YELLOW + context.value);
            return;
        }

        section.setInteger(context.key, num);
        sendSetValue(context, String.valueOf(num));
    }

    // context.value here is the operation (list, add, remove), the value is in context.overflow
    private void sendSetStringList(ConfigCommandContext context) {
        ConfigSection section = configHandler.getConfigSection(context.section);
        List<String> strings = section.getStringList(context.key);
        final String title = "&6&l" + context.section.toUpperCase() + " " + context.key.toUpperCase();

        if ("list".equalsIgnoreCase(context.value)) {
            context.sender.sendColouredMessage(LINE);
            context.sender.sendColouredMessage(title);
            context.sender.sendColouredMessage("");
            for (int i = 0; i < strings.size(); i++) {
                context.sender.sendMessage(ChatColor.GOLD + String.valueOf(i + 1) + ". " + ChatColor.YELLOW + strings.get(i));
            }
            context.sender.sendColouredMessage(LINE);
            return;
        }

        if (context.overflow == null) {
            sendValueUsage(context);
            return;
        }

        String realValue = String.join(" ", context.overflow);
        if ("add".equalsIgnoreCase(context.value)) {
            strings.add(realValue);
            context.sender.sendColouredMessage(LINE);
            context.sender.sendColouredMessage(title);
            context.sender.sendColouredMessage("");
            context.sender.sendMessage(ChatColor.GOLD + realValue + ChatColor.YELLOW + " was added to the " + ChatColor.GOLD + context.key + ChatColor.YELLOW + ".");
            context.sender.sendColouredMessage(LINE);
        } else if ("remove".equalsIgnoreCase(context.value)) { // remove
            final int index;
            try {
                index = Integer.parseInt(realValue);
            } catch (NumberFormatException nfe) {
                sendSetValueError(context, ChatColor.GOLD + "Invalid Integer: " + ChatColor.YELLOW + realValue);
                return;
            }

            // We do 1 based indexing
            if (strings.isEmpty() || index <= 0 || index > strings.size()) {
                sendSetValueError(context, ChatColor.GOLD + "Invalid Index: " + ChatColor.YELLOW + realValue);
                return;
            }

            String removed = strings.remove(index - 1);
            context.sender.sendColouredMessage(LINE);
            context.sender.sendColouredMessage(title);
            context.sender.sendColouredMessage("");
            context.sender.sendMessage(ChatColor.GOLD + removed + ChatColor.YELLOW + " was removed from the " + ChatColor.GOLD + context.key + ChatColor.YELLOW + ".");
            context.sender.sendColouredMessage(LINE);
        } else {
            throw new IllegalArgumentException("unhandled value: " + context.value);
        }

        section.setStringList(context.key, strings);
    }

    private void sendSetKit(ConfigCommandContext context) {
        ConfigSection section = configHandler.getConfigSection(context.section);

        if ("view".equalsIgnoreCase(context.value)) {
            Kit kit = section.getKit(context.key);
            if (context.sender.isPlayer()) {
                KitViewGui gui = KitViewGui.create(context.sender.player, "VIEWING " + context.section.toUpperCase() + " " + context.key.toUpperCase(), kit);
                gui.open();
            } else {
                // Remove colours in item
                context.sender.sendMessage("&e" + kit.toString().replace(String.valueOf(ChatColor.COLOR_CHAR), "&"));
            }
            return;
        }

        if ("give".equalsIgnoreCase(context.value)) {
            Kit kit = section.getKit(context.key);
            if (context.sender.isPlayer()) {
                kit.apply(context.sender.player);
            } else {
                sendSetValueError(context, ChatColor.YELLOW + "You must be a player to get kits.");
            }
            return;
        }

        final Kit kit;
        final String stringValue;
        if ("set".equalsIgnoreCase(context.value)) {
            if (!context.sender.isPlayer()) {
                sendSetValueError(context, ChatColor.YELLOW + "You must be a player to set kits.");
                return;
            }

            kit = InvadedKit.from(context.sender.player.getInventory());
            stringValue = "inventory";
        } else if ("reset".equalsIgnoreCase(context.value)) { // reset
            kit = configHandler.getDefaultKitMap().get(context.section + "." + context.key);
            stringValue = "default";
        } else {
            throw new IllegalArgumentException("unhandled value: " + context.value);
        }

        section.setKit(context.key, kit);
        sendSetValue(context, stringValue);
    }

    private void sendSetValue(ConfigCommandContext context, String value) {
        context.sender.sendColouredMessage(LINE);
        context.sender.sendColouredMessage("&6&l" + context.section.toUpperCase() + " " + context.key.toUpperCase());
        context.sender.sendColouredMessage("");
        context.sender.sendMessage(ChatColor.GOLD + "Value Set To: " + ChatColor.YELLOW + value);
        context.sender.sendColouredMessage(LINE);
    }

    private void sendSetValueError(ConfigCommandContext context, String error) {
        context.sender.sendColouredMessage(LINE);
        context.sender.sendColouredMessage("&6&l" + context.section.toUpperCase() + " " + context.key.toUpperCase() + " ERROR");
        context.sender.sendColouredMessage("");
        context.sender.sendMessage(error);
        context.sender.sendColouredMessage(LINE);
    }

    private void sendInvalidFirstArgument(ConfigCommandContext context) {
        context.sender.sendColouredMessage(LINE);
        context.sender.sendColouredMessage("&6&lINVALID FIRST ARGUMENT");
        context.sender.sendColouredMessage("");
        context.sender.sendColouredMessage("&6Alternative Subcommands: " + StringUtils.formatStringList(firstArguments));
        context.sender.sendColouredMessage(LINE);
    }

    private void sendInvalidSecondArgument(ConfigCommandContext context) {
        context.sender.sendColouredMessage(LINE);
        context.sender.sendColouredMessage("&6&lINVALID SECOND ARGUMENT");
        context.sender.sendColouredMessage("");
        context.sender.sendColouredMessage("&6Alternative Subcommands: " + StringUtils.formatStringList(new ArrayList<>(configHandler.getConfigSection(context.section).getKeys())));
        context.sender.sendColouredMessage(LINE);
    }

    private void sendInvalidThirdArgument(ConfigCommandContext context) {
        ConfigValueType type = configHandler.getConfigSection(context.section).getType(context.key);
        ConfigValueTypeInfo info = getConfigValueTypeInfo(type);
        context.sender.sendColouredMessage(LINE);
        context.sender.sendColouredMessage("&6&lINVALID THIRD ARGUMENT");
        context.sender.sendColouredMessage("");
        context.sender.sendColouredMessage("&6Alternative Subcommands: " + StringUtils.formatStringList(info.args));
        context.sender.sendColouredMessage(LINE);
    }

    public String getFriendlyValue(ConfigSection section, String key) {
        ConfigValueType type = section.getType(key); // calls checkContainsKey
        switch (type) {
            case BOOLEAN:
                return ChatColor.YELLOW + String.valueOf(section.getBoolean(key));
            case INTEGER:
                return ChatColor.YELLOW + String.valueOf(section.getInteger(key));
            case KIT:
                Kit kit = section.getKit(key);

                // Resets are needed to prevent weird chatcolor bug with newlines in console
                return ChatColor.RESET + "\n  " + ChatColor.GOLD + "items: " + formatItemStackList(kit.getItems(), false, true) + ChatColor.RESET +
                        "\n  " + ChatColor.GOLD + "armour: " + formatItemStackList(kit.getArmour(), true, false) + ChatColor.RESET +
                        "\n  " + ChatColor.GOLD + "offhand: " + formatItemStackList(Collections.singletonList(kit.getOffhand()), false, false);
            case LOCATION:
                return ChatColor.YELLOW + StringUtils.locationToString(section.getLocation(key));
            case REGION:
                return ChatColor.YELLOW + section.getRegion(key).toString();
            case STRING_LIST:
                return StringUtils.formatStringList(section.getStringList(key));
            default:
                throw new UnsupportedOperationException("unsupported config value type: " + type);
        }
    }

    private static String formatItemStackList(List<ItemStack> originalItems, boolean reverse, boolean removeNulls) {
        List<ItemStack> items = reverse ? CollectionUtils.reversedCopy(originalItems) : new ArrayList<>(originalItems);

        if (removeNulls) {
            items.removeIf(Objects::isNull);
        }

        List<String> strings = new ArrayList<>();
        for (ItemStack item : items) {
            strings.add(formatItemStack(item));
        }

        if (strings.isEmpty()) {
            strings.add("null"); // In case of empty items list
        }

        return StringUtils.formatStringList(strings);
    }

    private static String formatItemStack(ItemStack item) {
        return item == null ? "null" : item.getType().name();
    }

    private static ConfigValueTypeInfo getConfigValueTypeInfo(ConfigValueType type) {
        ConfigValueTypeInfo typeInfo = CONFIG_VALUE_TYPE_INFO_MAP.get(type);
        Validate.checkSupported(typeInfo != null, "can't get info for unsupported config value type: %s", type);
        return typeInfo;
    }

    @FunctionalInterface
    private interface SingleArgumentCommand {
        void execute(Sender sender);
    }

    @FunctionalInterface
    private interface ConfigCommand {
        void execute(ConfigCommandContext context);
    }

    private static class Sender {
        private final CommandSender sender; // the original command sender
        private final Player player; // will be null if the sender is not a player

        private Sender(CommandSender sender) {
            this.sender = sender;
            this.player = sender instanceof Player ? (Player) sender : null;
        }

        private boolean isPlayer() {
            return player != null;
        }

        // Should be used whenever user input is being displayed
        private void sendMessage(String message) {
            sender.sendMessage(message);
        }

        private void sendColouredMessage(String message) {
            SpigotUtils.sendMessage(sender, message);
        }
    }

    private static class ConfigCommandContext {
        private final Sender sender; // who executed /econfig
        private String section; // /econfig <general>
        private String key; // /econfig general <spawn>
        private String value; // /econfig general spawn <set> | can either be an operation or a value
        private String[] overflow; // econfig general spawn set <something else> | used for types like STRING_LIST

        private ConfigCommandContext(Sender sender) {
            this.sender = sender;
        }
    }

    private static class ConfigValueTypeInfo {
        private final List<String> args;
        private final String usage;

        private ConfigValueTypeInfo(List<String> args, String usage) {
            this.args = args;
            this.usage = usage;
        }
    }
}
