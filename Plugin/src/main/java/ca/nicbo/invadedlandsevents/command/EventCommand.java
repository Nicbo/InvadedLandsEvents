package ca.nicbo.invadedlandsevents.command;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.gui.Gui;
import ca.nicbo.invadedlandsevents.api.permission.EventPermission;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;
import ca.nicbo.invadedlandsevents.configuration.Message;
import ca.nicbo.invadedlandsevents.event.InvadedEventManager;
import ca.nicbo.invadedlandsevents.gui.host.BracketsHostGui;
import ca.nicbo.invadedlandsevents.gui.host.MainHostGui;
import ca.nicbo.invadedlandsevents.gui.host.SumoHostGui;
import ca.nicbo.invadedlandsevents.util.CollectionUtils;
import ca.nicbo.invadedlandsevents.util.SpigotUtils;
import ca.nicbo.invadedlandsevents.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles anything related to /event.
 *
 * @author Nicbo
 * @author StarZorrow
 */
public final class EventCommand implements CommandExecutor, TabCompleter {
    private static final List<String> FIRST_ARGUMENTS = CollectionUtils.unmodifiableList("join", "leave", "spectate", "info", "forceend", "host", "stats");
    private static final List<String> HOST_ARGUMENTS = Arrays.stream(EventType.values())
            .map(EventType::getConfigName)
            .collect(CollectionUtils.toUnmodifiableList());

    private static final String MUST_BE_PLAYER = "&cYou must be a player to execute this command";
    private static final String MUST_BE_PLAYER_STATS = "&cYou must be a player to view your own stats, use /event stats <player> instead.";
    private static final String MUST_BE_PLAYER_HOST = "&cYou must be a player to open the host GUI, use /event host <event> instead.";

    private final InvadedLandsEventsPlugin plugin;
    private final InvadedEventManager eventManager;

    private final Map<String, GlobalCommand> globalCommandMap;
    private final Map<String, PlayerCommand> playerCommandMap;

    public EventCommand(InvadedLandsEventsPlugin plugin) {
        Validate.checkArgumentNotNull(plugin, "plugin");

        this.plugin = plugin;
        this.eventManager = plugin.getEventManager();

        Map<String, GlobalCommand> globalCommandMap = new HashMap<>();
        globalCommandMap.put("stats", this::sendEventStats);
        globalCommandMap.put("fe", (sender, args) -> eventManager.forceEndCurrentEvent(sender));
        globalCommandMap.put("stop", (sender, args) -> eventManager.forceEndCurrentEvent(sender));
        globalCommandMap.put("forceend", (sender, args) -> eventManager.forceEndCurrentEvent(sender));
        globalCommandMap.put("h", this::hostEvent);
        globalCommandMap.put("host", this::hostEvent);
        this.globalCommandMap = Collections.unmodifiableMap(globalCommandMap);

        Map<String, PlayerCommand> playerCommandMap = new HashMap<>();
        playerCommandMap.put("j", eventManager::joinCurrentEvent);
        playerCommandMap.put("join", eventManager::joinCurrentEvent);
        playerCommandMap.put("l", eventManager::leaveCurrentEvent);
        playerCommandMap.put("leave", eventManager::leaveCurrentEvent);
        playerCommandMap.put("spec", eventManager::spectateCurrentEvent);
        playerCommandMap.put("spectate", eventManager::spectateCurrentEvent);
        playerCommandMap.put("i", eventManager::sendCurrentEventInfo);
        playerCommandMap.put("info", eventManager::sendCurrentEventInfo);
        this.playerCommandMap = Collections.unmodifiableMap(playerCommandMap);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsageMessages(sender);
            return true;
        }

        String firstArgument = args[0].toLowerCase();
        GlobalCommand globalCommand = globalCommandMap.get(firstArgument);
        if (globalCommand != null) {
            globalCommand.execute(sender, args);
        } else {
            PlayerCommand playerCommand = playerCommandMap.get(firstArgument);
            if (playerCommand == null) {
                sendUsageMessages(sender);
            } else if (!(sender instanceof Player)) {
                SpigotUtils.sendMessage(sender, MUST_BE_PLAYER);
            } else {
                playerCommand.execute((Player) sender);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], FIRST_ARGUMENTS, completions);
            Collections.sort(completions);
        } else if (args.length == 2) {
            if ("stats".equalsIgnoreCase(args[0])) {
                return null; // player names
            }

            if (StringUtils.equalsAnyIgnoreCase(args[0], "h", "host")) {
                StringUtil.copyPartialMatches(args[1], HOST_ARGUMENTS, completions);
                Collections.sort(completions);
            }
        }

        return completions;
    }

    private void sendEventStats(CommandSender sender, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        boolean specified = args.length >= 2;
        Player target = specified ? Bukkit.getPlayer(args[1]) : player;

        if (target == null) {
            if (specified) {
                sender.sendMessage(Message.PLAYER_NOT_FOUND.get().replace("{player}", args[1]));
            } else {
                SpigotUtils.sendMessage(sender, MUST_BE_PLAYER_STATS);
            }
            return;
        }

        eventManager.sendEventStats(sender, target);
    }

    private void hostEvent(CommandSender sender, String[] args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        boolean oneArg = args.length == 1;
        if (oneArg || StringUtils.equalsAnyIgnoreCase(args[1], "brackets", "sumo")) {
            if (player == null) {
                SpigotUtils.sendMessage(sender, MUST_BE_PLAYER_HOST);
                return;
            }

            if (!player.hasPermission(EventPermission.HOST)) {
                player.sendMessage(Message.NO_PERMISSION.get());
                return;
            }

            Gui gui = oneArg ? MainHostGui.create(player, plugin) : "brackets".equalsIgnoreCase(args[1]) ?
                    BracketsHostGui.create(player, plugin) :
                    SumoHostGui.create(player, plugin);
            gui.open();
        } else { // /event host <event>
            EventType eventType = getEventType(args[1]);
            if (eventType == null) {
                sender.sendMessage(Message.DOES_NOT_EXIST.get().replace("{event}", args[1]));
                return;
            }

            eventManager.hostEvent(eventType, sender);
        }
    }

    private static void sendUsageMessages(CommandSender sender) {
        for (String message : ListMessage.USAGE_MESSAGES.get()) {
            sender.sendMessage(message);
        }
    }

    private static EventType getEventType(String configName) {
        for (EventType type : EventType.values()) {
            if (type.getConfigName().equalsIgnoreCase(configName)) {
                return type;
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface GlobalCommand {
        void execute(CommandSender sender, String[] args);
    }

    @FunctionalInterface
    private interface PlayerCommand {
        void execute(Player player); // conveniently doesn't need args, allows method reference :)
    }
}
