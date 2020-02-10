package me.nicbo.InvadedLandsEvents.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class EventConfigCommand implements CommandExecutor {
    private FileConfiguration config;
    private String[] eventNames;

    public EventConfigCommand(FileConfiguration config) {
        this.config = config;
        this.eventNames = EventManager.getEventNames();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (cmd.getName().equalsIgnoreCase("eventconfig") || cmd.getName().equalsIgnoreCase("econfig")) {
            if (args[0] == null) {
                sender.sendMessage("todo help menu");
                return true;
            }
        }
        return false;
    }
}
