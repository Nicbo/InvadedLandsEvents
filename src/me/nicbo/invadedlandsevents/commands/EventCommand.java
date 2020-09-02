package me.nicbo.invadedlandsevents.commands;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.events.EventManager;
import me.nicbo.invadedlandsevents.gui.host.HostGUI;
import me.nicbo.invadedlandsevents.gui.host.MainHostGUI;
import me.nicbo.invadedlandsevents.gui.host.SumoHostGUI;
import me.nicbo.invadedlandsevents.messages.impl.ListMessage;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.permission.EventPermission;
import me.nicbo.invadedlandsevents.util.GeneralUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.StringUtil;

import java.util.*;

/**
 * Handles all commands for /event or /e
 *
 * @author Nicbo
 * @author StarZorrow
 */

public final class EventCommand implements CommandExecutor, TabCompleter, Listener {
    private final InvadedLandsEvents plugin;
    private final EventManager eventManager;

    private final List<String> firstArguments;
    private final List<String> hostArguments;

    private final Map<UUID, Long> cooldowns;

    public EventCommand(InvadedLandsEvents plugin) {
        this.plugin = plugin;
        this.eventManager = plugin.getEventManager();

        this.firstArguments = Arrays.asList("join", "leave", "spectate", "info", "forceend", "host");

        this.hostArguments = new ArrayList<>(EventManager.getEventNames());
        this.hostArguments.add("sumo");

        this.cooldowns = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (cmd.getName().equalsIgnoreCase("event")) {
            String command = args.length == 0 ? "" : args[0].toLowerCase();

            // From when they forceend from console
            switch (command) {
                case "fe":
                case "stop":
                case "forceend":
                    String forceEndMsg = eventManager.endEvent(sender);
                    sender.sendMessage(forceEndMsg);
                    return true;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You can only use /event forceend from here");
                return true;
            }

            Player player = (Player) sender;
            switch (command) {
                case "j":
                case "join":
                    if (isPlayerOnCooldown(player)) {
                        player.sendMessage(Message.JOIN_COOLDOWN.get());
                    } else {
                        String joinMsg = eventManager.joinEvent(player);

                        // Player joined
                        if (joinMsg == null) {
                            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                        } else {
                            player.sendMessage(joinMsg);
                        }
                    }
                    break;
                case "l":
                case "leave":
                    String leaveMsg = eventManager.leaveEvent(player);
                    if (leaveMsg != null) {
                        player.sendMessage(leaveMsg);
                    }
                    break;
                case "s":
                case "spec":
                case "spectate":
                    String spectateMsg = eventManager.specEvent(player);
                    player.sendMessage(spectateMsg);
                    break;
                case "i":
                case "info":
                    String infoMsg = eventManager.eventInfo(player);
                    if (infoMsg != null) {
                        player.sendMessage(infoMsg);
                    }
                    break;
                case "h":
                case "host":
                    boolean oneArg = args.length == 1;
                    if (oneArg || "sumo".equalsIgnoreCase(args[1])) {
                        if (player.hasPermission(EventPermission.HOST_EVENT)) {
                            HostGUI gui;
                            if (oneArg) {
                                gui = new MainHostGUI(plugin, player);
                            } else {
                                gui = new SumoHostGUI(plugin, player);
                            }
                            gui.open();
                        } else {
                            player.sendMessage(Message.NO_PERMISSION.get());
                        }
                    } else {
                        String hostMsg = eventManager.hostEvent(player, args[1].toLowerCase());
                        if (hostMsg != null) {
                            player.sendMessage(hostMsg);
                        }
                    }
                    break;
                default:
                    GeneralUtils.sendMessages(player, ListMessage.USAGE_MESSAGES.get());
                    break;
            }
            return true;
        }
        sender.sendMessage(Message.ERROR.get().replace("{error}", "EVENT_COMMAND"));
        return true;
    }

    private boolean isPlayerOnCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        Long cooldown = cooldowns.get(uuid);

        if (cooldown == null) {
            return false;
        }

        if ((System.currentTimeMillis() - cooldown) / 1000 >= 5) {
            cooldowns.remove(uuid);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        if (cmd.getName().equalsIgnoreCase("event")) {
            if (sender instanceof Player) {
                List<String> completions = new ArrayList<>();
                if (args.length == 1) {
                    StringUtil.copyPartialMatches(args[0], firstArguments, completions);
                    Collections.sort(completions);
                } else if (args.length == 2) {
                    if ("host".equalsIgnoreCase(args[0])) {
                        StringUtil.copyPartialMatches(args[1], hostArguments, completions);
                        Collections.sort(completions);
                    }
                }
                return completions;
            }
        }
        return null;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}
