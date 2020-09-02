package me.nicbo.invadedlandsevents.events.type.impl;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.events.type.RoundEvent;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import me.nicbo.invadedlandsevents.scoreboard.line.Line;
import me.nicbo.invadedlandsevents.scoreboard.line.TrackLine;
import me.nicbo.invadedlandsevents.util.SpigotUtils;
import me.nicbo.invadedlandsevents.util.GeneralUtils;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Players must stand on the chosen wool
 * All players who are on the wrong block are eliminated
 * Last player standing wins
 *
 * @author Nicbo
 */

public final class WoolShuffle extends RoundEvent {
    private final Wool[] wools;
    private final String[] woolNames;

    private final Location startLoc;

    private int index;

    private boolean pvpEnabled;

    public WoolShuffle(InvadedLandsEvents plugin) {
        super(plugin, "woolshuffle", "Wool Shuffle", new int[]{15, 15, 14, 14, 13, 13, 12, 12, 11, 11, 10, 10, 9, 9, 8, 7, 7, 6, 6, 5, 5, 4, 4, 3, 2});

        this.wools = new Wool[]{
                new Wool(DyeColor.ORANGE),
                new Wool(DyeColor.YELLOW),
                new Wool(DyeColor.LIME),
                new Wool(DyeColor.PINK),
                new Wool(DyeColor.CYAN),
                new Wool(DyeColor.PURPLE),
                new Wool(DyeColor.BLUE),
        };

        this.woolNames = new String[]{
                ChatColor.GOLD + "Orange",
                ChatColor.YELLOW + "Yellow",
                ChatColor.GREEN + "Green",
                ChatColor.LIGHT_PURPLE + "Pink",
                ChatColor.DARK_AQUA + "Cyan",
                ChatColor.DARK_PURPLE + "Purple",
                ChatColor.BLUE + "Blue"
        };

        this.startLoc = getEventLocation("start");
        this.index = 0;
    }

    @Override
    protected void start() {
        super.start();
        for (Player player : getPlayersView()) {
            player.teleport(startLoc);
        }
    }

    @Override
    protected Function<Player, EventScoreboard> getScoreboardFactory() {
        return WoolShuffleSB::new;
    }

    @Override
    protected void newRound() {
        broadcastEventMessage(Message.WOOLSHUFFLE_ROUND_STARTING.get().replace("{round}", String.valueOf(getRound())));

        // Pick new wool
        int newIndex;
        do {
            newIndex = GeneralUtils.randomMinMax(0, wools.length - 1);
        } while (newIndex == index);
        index = newIndex;

        broadcastEventMessage(Message.WOOLSHUFFLE_COLOUR.get().replace("{colour}", woolNames[index]));

        // Give wool to all players
        // Random potion (speed, slow)

        PotionEffect effect = getRandomPotionEffect();
        ItemStack wool = wools[index].toItemStack(1);
        for (Player player : getPlayersView()) {
            SpigotUtils.fillPlayerHotbar(player, wool);
            if (effect != null) {
                player.addPotionEffect(effect);
            }
        }

        // Pvp toggle (only first 5 rounds)
        this.pvpEnabled = getRound() >= 6 || GeneralUtils.randomBoolean();
        broadcastEventMessage((pvpEnabled ? Message.WOOLSHUFFLE_PVP_ENABLED : Message.WOOLSHUFFLE_PVP_DISABLED).get());
    }

    @Override
    protected void eliminatePlayers() {
        List<Player> toLose = new ArrayList<>();

        for (Player player : getPlayersView()) {
            if (!isPlayerOnWool(player)) {
                toLose.add(player);
                broadcastEventMessage(Message.WOOLSHUFFLE_FAILED.get().replace("{player}", player.getName()));
                broadcastEventMessage(Message.WOOLSHUFFLE_ELIMINATED.get()
                        .replace("{player}", player.getName())
                        .replace("{remaining}", String.valueOf(getPlayersSize() - toLose.size())));
            }
        }

        loseEvent(toLose);
    }

    /**
     * Get a random potion effect to apply to the players
     * The potion effects duration is the same as the round timer
     *
     * None - 40%
     * Speed 1 - 20%
     * Speed 2 - 20%
     * Speed 4 - 10%
     * Slow 1 - 10%
     *
     * @return the potion effect (null if none)
     */
    private PotionEffect getRandomPotionEffect() {
        int rand = GeneralUtils.randomMinMax(1, 10);
        if (rand < 5) { // 1 - 4 (40%)
            return null;
        }

        PotionEffectType type = PotionEffectType.SPEED;
        int amplifier;

        if (rand < 7) { // 5 - 6 (20%)
            amplifier = 0;
        } else if (rand < 9) { // 7 - 8 (20%)
            amplifier = 1;
        } else if (rand < 10) { // 9 (10%)
            amplifier = 3;
        } else { // 10 (10%)
            type = PotionEffectType.SLOW;
            amplifier = 0;
        }

        return new PotionEffect(type, 20 * getTimer(), amplifier, true, false);
    }

    /**
     * Check if a player is standing on the current wool
     *
     * @param player the player
     * @return true if the player is standing on the correct wool
     */
    private boolean isPlayerOnWool(Player player) {
        Location loc = player.getLocation();
        loc.setY(loc.getY() - 1);
        Block block = loc.getWorld().getBlockAt(loc);
        loc.setY(loc.getY() - 1);
        Block blockUnder = loc.getWorld().getBlockAt(loc);
        return isEqual(block, wools[index]) || isEqual(blockUnder, wools[index]);
    }

    /**
     * Checks if a block is equal to wool
     *
     * @param block the block
     * @param wool  the wool
     * @return true if the block and wool are equal
     */
    private static boolean isEqual(Block block, Wool wool) {
        if (block.getType() == Material.WOOL) {
            return wool.equals(block.getState().getData());
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHitWS(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (ignoreEvent(player)) {
                return;
            }

            if (pvpEnabled) {
                event.setDamage(0);
            } else {
                event.setCancelled(true);
            }
        }
    }

    private final class WoolShuffleSB extends EventScoreboard {
        private final TrackLine roundTrack;
        private final TrackLine countDownTrack;
        private final TrackLine colourTrack;
        private final TrackLine pvpTrack;
        private final TrackLine playerCountTrack;
        private final TrackLine specCountTrack;

        private WoolShuffleSB(Player player) {
            super(getConfigName(), player);
            this.roundTrack = new TrackLine("rtWoolShuffle", "&eRound: ", "&6", "", 8);
            this.countDownTrack = new TrackLine("cdtWoolShuffle", "&eTime Remain", "ing: &6", "", 7);
            this.colourTrack = new TrackLine("ctWoolShuffle", "&eRun to: ", ChatColor.AQUA + "" + ChatColor.RESET, "", 6);
            this.pvpTrack = new TrackLine("ptWoolShuffle", ChatColor.YELLOW + "PvP: ", ChatColor.RED + "" + ChatColor.RESET, "", 5);
            Line blank = new Line("bWoolShuffle", "", ChatColor.AQUA.toString(), "", 4);
            this.playerCountTrack = new TrackLine("pctWoolShuffle", ChatColor.YELLOW + "Players: ", ChatColor.DARK_PURPLE + "" + ChatColor.GOLD, "", 3);
            this.specCountTrack = new TrackLine("sctWoolShuffle", ChatColor.YELLOW + "Spectators: ", ChatColor.LIGHT_PURPLE + "" + ChatColor.GOLD, "", 2);
            this.initLines(roundTrack, countDownTrack, colourTrack, pvpTrack, blank, playerCountTrack, specCountTrack);
        }

        @Override
        protected void refresh() {
            roundTrack.setSuffix(String.valueOf(getRound()));
            countDownTrack.setSuffix(String.valueOf(getTimer()));
            colourTrack.setSuffix(woolNames[index]);
            pvpTrack.setSuffix(pvpEnabled ? "&a&lEnabled" : "&c&lDisabled");
            playerCountTrack.setSuffix(String.valueOf(getPlayersSize()));
            specCountTrack.setSuffix(String.valueOf(getSpectatorsSize()));
        }
    }
}
