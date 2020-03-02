package me.nicbo.InvadedLandsEvents.commands;

import me.nicbo.InvadedLandsEvents.EventMessage;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.*;

public class EventCommand implements CommandExecutor, TabCompleter {
    private EventManager eventManager;
    private List<String> args0;
    private List<String> events;

    public EventCommand(EventManager eventManager) {
        this.eventManager = eventManager;
        this.args0 = new ArrayList<>();
        this.args0.add("join");
        this.args0.add("leave");
        this.args0.add("host");
        this.args0.add("spectate");
        this.args0.add("info");
        this.args0.add("forceend");

        this.events = new ArrayList<>();
        this.events.addAll(Arrays.asList(EventManager.getEventNames()));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (cmd.getName().toLowerCase().equalsIgnoreCase("event")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                switch (args[0].toLowerCase()) {
                    case "j":
                    case "join": {
                        EventMessage joinMsg = eventManager.joinEvent(player);
                        if (joinMsg != null) player.sendMessage(joinMsg.getDescription());
                        break;
                    }
                    case "l":
                    case "leave": {
                        EventMessage leaveMsg = eventManager.leaveEvent(player);
                        player.sendMessage(leaveMsg.getDescription());
                        break;
                    }
                    case "host": {
                        EventMessage hostMsg = eventManager.hostEvent(args[1], player.getName());
                        if (hostMsg != null) player.sendMessage(hostMsg.getDescription().replace("{event}", args[1]));
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        if (cmd.getName().equalsIgnoreCase("event") || cmd.getName().equalsIgnoreCase("e")) {
            if (sender instanceof Player) {
                List<String> completions = new ArrayList<>();
                if (args.length == 1) {
                    StringUtil.copyPartialMatches(args[0], args0, completions);
                    Collections.sort(completions);
                } else if (args.length == 2 && args[0].equalsIgnoreCase("host")) {
                    StringUtil.copyPartialMatches(args[1], events, completions);
                    Collections.sort(completions);
                }
                return completions;
            }
        }
        return null;
    }

    /*
    TODO:
        - Add tab complete
        - Sub commands
        - Right now this is just for testing I will work on actual command when events are done
     */
}
