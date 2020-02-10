package me.nicbo.InvadedLandsEvents.commands;

import me.nicbo.InvadedLandsEvents.events.EventStatus;
import me.nicbo.InvadedLandsEvents.managers.EventManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventCommand implements CommandExecutor {
    private EventManager eventManager;
    public EventCommand(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (cmd.getName().toLowerCase().equalsIgnoreCase("event")) {
            if (args[0].equalsIgnoreCase("host")) {
                if (!(eventManager.hostEvent(args[1], sender.getName()))) {
                    sender.sendMessage(EventStatus.DOES_NOT_EXIST.getDescription().replace("{event}", args[1]));
                }
            } else if (args[0].equalsIgnoreCase("join")) {
                sender.sendMessage(eventManager.joinEvent((Player) sender).getDescription());
            }
        }
        return false;
    }

    /*
    TODO:
        - Add tab complete
        - Sub commands
        - Config commands
        - Right now this is just for testing I will work on actual command when events are done
     */
}
