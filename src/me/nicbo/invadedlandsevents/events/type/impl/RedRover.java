package me.nicbo.invadedlandsevents.events.type.impl;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.events.type.RoundEvent;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import me.nicbo.invadedlandsevents.scoreboard.line.Line;
import me.nicbo.invadedlandsevents.scoreboard.line.TrackLine;
import me.nicbo.invadedlandsevents.util.SpigotUtils;
import me.nicbo.invadedlandsevents.util.GeneralUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


/**
 * One player is selected as the killer
 * The rest of the players are teleported to a side
 * Each round the players must run to the other side
 * Last player alive wins
 *
 * @author Nicbo
 */

public final class RedRover extends RoundEvent {
    private final ProtectedRegion blue;
    private final ProtectedRegion red;

    private final Location start;
    private final Location killerStart;

    private ItemStack[] killerArmour;

    private Player killer;

    private Side side;

    public RedRover(InvadedLandsEvents plugin) {
        super(plugin, "redrover", "Redrover", new int[]{20, 19, 19, 18, 18, 17, 17, 16, 16, 15, 15, 14, 14, 13, 13, 12, 12, 11, 11, 10});

        this.blue = getEventRegion("blue-region");
        this.red = getEventRegion("red-region");
        this.start = getEventLocation("start");
        this.killerStart = getEventLocation("killer-start");

        this.killerArmour = new ItemStack[]{
                new ItemStack(Material.DIAMOND_BOOTS),
                new ItemStack(Material.DIAMOND_LEGGINGS),
                new ItemStack(Material.DIAMOND_CHESTPLATE),
                new ItemStack(Material.DIAMOND_HELMET)
        };

        this.side = Side.BLUE; // Will be switched to red as newRound is called in start
    }

    @Override
    public void leaveEvent(Player player) {
        super.leaveEvent(player);

        if (getPlayersSize() > 1 && player.equals(killer)) {
            pickKiller();
        }
    }

    private void pickKiller() {
        this.killer = GeneralUtils.getRandom(getPlayersView());
        killer.sendMessage(Message.REDROVER_SELECTED.get());
        killer.teleport(killerStart);
        killer.getInventory().setArmorContents(killerArmour);
        killer.getInventory().setItem(0, new ItemStack(Material.DIAMOND_SWORD));
    }

    @Override
    protected void start() {
        super.start();

        pickKiller();

        for (Player player : getPlayersView()) {
            if (!player.equals(killer)) {
                player.teleport(start);
            }
        }
    }

    @Override
    protected Function<Player, EventScoreboard> getScoreboardFactory() {
        return RedRoverSB::new;
    }

    @Override
    protected void newRound() {
        side = Side.getNext(side);
        broadcastEventMessage(Message.REDROVER_ROUND_STARTING.get().replace("{round}", String.valueOf(getRound())));
        broadcastEventMessage(Message.REDROVER_RUN_TO.get().replace("{side}", side.toString()));

        PotionEffect effect = getRandomPotionEffect();

        if (effect != null) {
            killer.addPotionEffect(effect);
        }
    }

    /**
     * Get a random potion effect for the killer
     *
     * 50% - None
     * 10% - Speed 5
     * 20% - Speed 2
     * 20% - Speed 1
     *
     * @return the potion effect (null if none)
     */
    private PotionEffect getRandomPotionEffect() {
        int rand = GeneralUtils.randomMinMax(1, 10);
        if (rand < 6) { // 1 - 5 (50%)
            return null;
        }

        int amplifier;

        if (rand < 7) { // 6 (10%)
            amplifier = 4;
        } else if (rand < 8) { // 7 - 8 (20%)
            amplifier = 1;
        } else { // 9 - 10 (20%)
            amplifier = 0;
        }

        return new PotionEffect(PotionEffectType.SPEED, 20 * getTimer(), amplifier, true, false);
    }

    @Override
    protected void eliminatePlayers() {
        ProtectedRegion currentRegion = side == Side.BLUE ? blue : red;
        List<Player> toLose = new ArrayList<>();

        for (Player player : getPlayersView()) {
            if (!SpigotUtils.isLocInRegion(player.getLocation(), currentRegion) && !player.equals(killer)) {
                toLose.add(player);
                broadcastEventMessage(Message.REDROVER_ELIMINATED.get()
                        .replace("{player}", player.getName())
                        .replace("{remaining}", String.valueOf(getPlayersSize() - toLose.size())));
            }
        }

        loseEvent(toLose);

        if (getPlayersSize() == 2) {
            winEvent(getPlayersView().get(getPlayersView().get(0).equals(killer) ? 1 : 0));
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHitRR(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (ignoreEvent(player)) {
                return;
            }

            // Player hit was killer or damager was not the killer
            if (player.equals(killer) || !event.getDamager().equals(killer)) {
                event.setDamage(0); // No damage but they still get hit
            } else {
                if (player.getHealth() - event.getFinalDamage() <= 0) {
                    SpigotUtils.clearInventory(player);
                    broadcastEventMessage(Message.REDROVER_ELIMINATED_BY.get()
                            .replace("{player}", player.getName())
                            .replace("{remaining}", String.valueOf(getPlayersSize() - 1))
                            .replace("{killer}", killer.getName()));


                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        player.spigot().respawn();
                        loseEvent(player);
                    }, 1);
                }
            }
        }
    }

    private enum Side {
        BLUE(ChatColor.BLUE + "Blue"),
        RED(ChatColor.RED + "Red");

        private final String message;

        Side(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }

        public static Side getNext(Side currentSide) {
            return currentSide == BLUE ? RED : BLUE;
        }
    }

    private final class RedRoverSB extends EventScoreboard {
        private final TrackLine roundTrack;
        private final TrackLine countDownTrack;
        private final TrackLine sideTrack;
        private final TrackLine playerCountTrack;
        private final TrackLine specCountTrack;

        private RedRoverSB(Player player) {
            super(getConfigName(), player);
            this.roundTrack = new TrackLine("rtRedrover", "&eRound: ", "&6", "", 6);
            this.countDownTrack = new TrackLine("cdtRedrover", "&eCountdown: ", "&e&6", "", 5);
            this.sideTrack = new TrackLine("stRedrover", "&eRun to: ", "&b&6", "", 4);
            Line blank = new Line("bRedrover", "", "&c", "", 3);
            this.playerCountTrack = new TrackLine("pctRedrover", "&ePlayers: ", "&7&6", "", 2);
            this.specCountTrack = new TrackLine("sctRedrover", "&eSpectators: ", "&8&6", "", 1);
            this.initLines(roundTrack, countDownTrack, sideTrack, blank, playerCountTrack, specCountTrack);
        }

        @Override
        protected void refresh() {
            this.roundTrack.setSuffix(String.valueOf(getRound()));
            this.countDownTrack.setSuffix(String.valueOf(getTimer()));
            this.sideTrack.setSuffix(side.toString());
            this.playerCountTrack.setSuffix(String.valueOf(getPlayersSize()));
            this.specCountTrack.setSuffix(String.valueOf(getSpectatorsSize()));
        }
    }

    /*
    TODO:
        - Check if killer leaving works
     */
}
