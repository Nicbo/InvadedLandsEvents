package me.nicbo.invadedlandsevents.events.type.impl;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.events.type.TimerEvent;
import me.nicbo.invadedlandsevents.events.util.MatchCountdown;
import me.nicbo.invadedlandsevents.messages.impl.ListMessage;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import me.nicbo.invadedlandsevents.scoreboard.line.TrackLine;
import me.nicbo.invadedlandsevents.util.SpigotUtils;
import me.nicbo.invadedlandsevents.util.StringUtils;
import me.nicbo.invadedlandsevents.util.item.Enchant;
import me.nicbo.invadedlandsevents.util.item.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Function;

/**
 * All players start with a kit
 * When a player dies they are eliminated
 * The last player standing wins the event
 *
 * @author Nicbo
 * @author StarZorrow
 */

public final class LMS extends TimerEvent {
    private final ItemStack[] armour;
    private final ItemStack[] kit;

    private final Location start1;
    private final Location start2;

    private final MatchCountdown matchCountdown;

    public LMS(InvadedLandsEvents plugin) {
        super(plugin, "Last Man Standing", "lms");

        this.armour = new ItemStack[]{
                new ItemBuilder(Material.IRON_BOOTS).setEnchants(new Enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2)).build(),
                new ItemBuilder(Material.IRON_LEGGINGS).setEnchants(new Enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2)).build(),
                new ItemBuilder(Material.IRON_CHESTPLATE).setEnchants(new Enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2)).build(),
                new ItemBuilder(Material.IRON_HELMET).setEnchants(new Enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2)).build()
        };

        this.kit = new ItemStack[]{
                new ItemBuilder(Material.IRON_SWORD).setEnchants(new Enchant(Enchantment.DAMAGE_ALL, 1)).build(),
                new ItemStack(Material.BOW),
                new ItemStack(Material.GOLDEN_APPLE, 10),
                new ItemStack(Material.ARROW, 32)
        };

        this.start1 = getEventLocation("start-1");
        this.start2 = getEventLocation("start-2");
        this.matchCountdown = new MatchCountdown(this::broadcastEventMessage, Message.LMS_MATCH_COUNTER, Message.LMS_MATCH_STARTED);
    }

    @Override
    protected void start() {
        super.start();

        List<Player> players = getPlayersView();
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            player.teleport(i % 2 == 0 ? start1 : start2);
            player.getInventory().setArmorContents(armour);
            player.getInventory().setContents(kit);
        }

        broadcastEventMessage(Message.LMS_MATCH_STARTING.get());
        matchCountdown.start(plugin);
    }

    @Override
    protected void over() {
        super.over();
        if (matchCountdown.isCounting()) {
            matchCountdown.cancel();
        }
    }

    @Override
    protected Function<Player, EventScoreboard> getScoreboardFactory() {
        return LMSSB::new;
    }

    @Override
    protected List<String> getDescriptionMessage() {
        return ListMessage.LMS_DESCRIPTION.get();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHitLMS(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (ignoreEvent(player)) {
                return;
            }

            if (matchCountdown.isCounting()) {
                event.setCancelled(true);
            } else if (player.getHealth() - event.getFinalDamage() <= 0) {
                SpigotUtils.clearInventory(player);
                broadcastEventMessage(Message.LMS_ELIMINATED.get()
                        .replace("{player}", player.getName())
                        .replace("{remaining}", String.valueOf(getPlayersSize() - 1)));
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    player.spigot().respawn();
                    loseEvent(player);
                }, 1);
            }
        }
    }

    private final class LMSSB extends EventScoreboard {
        private final TrackLine playerCountTrack;
        private final TrackLine specCountTrack;
        private final TrackLine timeRemainingTrack;

        private LMSSB(Player player) {
            super(getConfigName(), player);
            this.playerCountTrack = new TrackLine("pctLMS", "&ePlayers: ", "&5&6", "", 4);
            this.specCountTrack = new TrackLine("sctLMS", "&eSpectators: ", "&d&6", "", 3);
            this.timeRemainingTrack = new TrackLine("trtLMS", "&eTime Remain", "ing: &6", "", 2);
            this.initLines(playerCountTrack, specCountTrack, timeRemainingTrack);
        }

        @Override
        protected void refresh() {
            playerCountTrack.setSuffix(String.valueOf(getPlayersSize()));
            specCountTrack.setSuffix(String.valueOf(getSpectatorsSize()));
            timeRemainingTrack.setSuffix(StringUtils.formatSeconds(getTimeLeft()));
        }
    }
}
