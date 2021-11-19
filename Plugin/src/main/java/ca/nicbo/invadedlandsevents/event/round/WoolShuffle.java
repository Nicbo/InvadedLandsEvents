package ca.nicbo.invadedlandsevents.event.round;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.api.event.event.EventPostStartEvent;
import ca.nicbo.invadedlandsevents.compatibility.Colour;
import ca.nicbo.invadedlandsevents.compatibility.CompatibleMaterial;
import ca.nicbo.invadedlandsevents.configuration.ListMessage;
import ca.nicbo.invadedlandsevents.configuration.Message;
import ca.nicbo.invadedlandsevents.event.event.player.EventPlayerDamageByEventPlayerEvent;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import ca.nicbo.invadedlandsevents.scoreboard.EventScoreboardLine;
import ca.nicbo.invadedlandsevents.util.CollectionUtils;
import ca.nicbo.invadedlandsevents.util.RandomUtils;
import ca.nicbo.invadedlandsevents.util.RandomWeightedCollection;
import ca.nicbo.invadedlandsevents.util.SpigotUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Wool Shuffle.
 *
 * @author Nicbo
 */
public class WoolShuffle extends RoundEvent {
    private static final int[] TIMES = {15, 15, 14, 14, 13, 13, 12, 12, 11, 11, 10, 10, 9, 9, 8, 7, 7, 6, 6, 5, 5, 4, 4, 3, 2};

    private final RandomWeightedCollection<Supplier<PotionEffect>> potionEffectSuppliers;
    private final List<Wool> wools;

    private final Location startLocation;

    private Wool currentWool;
    private boolean pvpEnabled;

    public WoolShuffle(InvadedLandsEventsPlugin plugin, String hostName) {
        super(plugin, EventType.WOOL_SHUFFLE, hostName, ListMessage.WOOLSHUFFLE.get(), TIMES);

        this.potionEffectSuppliers = new RandomWeightedCollection<Supplier<PotionEffect>>()
                .add(() -> null, 40)
                .add(() -> new PotionEffect(PotionEffectType.SPEED, getTimer() * 20, 0, true, false), 20)
                .add(() -> new PotionEffect(PotionEffectType.SPEED, getTimer() * 20, 1, true, false), 20)
                .add(() -> new PotionEffect(PotionEffectType.SPEED, getTimer() * 20, 3, true, false), 10)
                .add(() -> new PotionEffect(PotionEffectType.SLOW, getTimer() * 20, 0, true, false), 10);

        this.wools = CollectionUtils.unmodifiableList(
                new Wool(ChatColor.GOLD + "Orange", CompatibleMaterial.ORANGE_WOOL.createItemStack(), Colour.ORANGE),
                new Wool(ChatColor.YELLOW + "Yellow", CompatibleMaterial.YELLOW_WOOL.createItemStack(), Colour.YELLOW),
                new Wool(ChatColor.GREEN + "Lime", CompatibleMaterial.LIME_WOOL.createItemStack(), Colour.LIME),
                new Wool(ChatColor.LIGHT_PURPLE + "Pink", CompatibleMaterial.PINK_WOOL.createItemStack(), Colour.PINK),
                new Wool(ChatColor.DARK_AQUA + "Cyan", CompatibleMaterial.CYAN_WOOL.createItemStack(), Colour.CYAN),
                new Wool(ChatColor.DARK_PURPLE + "Purple", CompatibleMaterial.PURPLE_WOOL.createItemStack(), Colour.PURPLE),
                new Wool(ChatColor.BLUE + "Blue", CompatibleMaterial.BLUE_WOOL.createItemStack(), Colour.BLUE),
                new Wool(ChatColor.RED + "Red", CompatibleMaterial.RED_WOOL.createItemStack(), Colour.RED)
        );

        this.startLocation = getEventConfig().getLocation("start");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEventPostStart(EventPostStartEvent event) {
        super.onEventPostStart(event);

        for (Player player : getPlayers()) {
            player.teleport(startLocation);
        }
    }

    @Override
    protected void onStartRound() {
        broadcastMessage(Message.WOOLSHUFFLE_ROUND_STARTING.get().replace("{round}", String.valueOf(getRound())));

        this.currentWool = currentWool == null ? RandomUtils.randomElement(wools) : RandomUtils.randomElementNotEqual(wools, currentWool);

        broadcastMessage(Message.WOOLSHUFFLE_COLOR.get().replace("{color}", currentWool.getName()));

        // Give wool to all players
        // Random potion (speed, slow)
        PotionEffect effect = potionEffectSuppliers.next().get();
        ItemStack wool = currentWool.getItemStack();
        for (Player player : getPlayers()) {
            SpigotUtils.fillHotbar(player, wool);
            if (effect != null) {
                player.addPotionEffect(effect);
            }
        }

        // Pvp toggle (only first 5 rounds)
        this.pvpEnabled = getRound() < 6 && RandomUtils.randomBoolean();
        broadcastMessage((pvpEnabled ? Message.WOOLSHUFFLE_PVP_ENABLED : Message.WOOLSHUFFLE_PVP_DISABLED).get());
    }

    @Override
    protected void onEndRound() {
        List<Player> toLose = new ArrayList<>();

        for (Player player : getPlayers()) {
            if (!isPlayerOnCurrentWool(player)) {
                toLose.add(player);
                broadcastMessage(Message.WOOLSHUFFLE_FAILED.get().replace("{player}", player.getName()));
                broadcastMessage(Message.WOOLSHUFFLE_ELIMINATED.get()
                        .replace("{player}", player.getName())
                        .replace("{remaining}", String.valueOf(getPlayersSize() - toLose.size())));
            }
        }

        lose(toLose);
    }

    @Override
    protected EventScoreboard createEventScoreboard(Player player) {
        return new WoolShuffleScoreboard(player);
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEventPlayerDamageByEventPlayer(EventPlayerDamageByEventPlayerEvent event) {
        super.onEventPlayerDamageByEventPlayer(event);

        if (event.isCancelled()) {
            return;
        }

        if (pvpEnabled) {
            event.setDamage(0);
        } else {
            event.setCancelled(true);
        }
    }

    // ---------- Getters for Plugin module users ----------

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public Wool getCurrentWool() {
        return currentWool;
    }

    // -----------------------------------------------------

    private boolean isPlayerOnCurrentWool(Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();
        loc.setY(loc.getY() - 1);
        Block block = world.getBlockAt(loc);
        loc.setY(loc.getY() - 1);
        Block blockUnder = world.getBlockAt(loc);
        return isBlockCurrentWool(block) || isBlockCurrentWool(blockUnder);
    }

    @SuppressWarnings("deprecation") // Compatibility
    private boolean isBlockCurrentWool(Block block) {
        return currentWool.getColour().getData() == block.getData();
    }

    private class WoolShuffleScoreboard extends EventScoreboard {
        private final EventScoreboardLine roundLine;
        private final EventScoreboardLine countdownLine;
        private final EventScoreboardLine colourLine;
        private final EventScoreboardLine pvpEnabledLine;
        private final EventScoreboardLine playerCountLine;
        private final EventScoreboardLine spectatorCountLine;

        public WoolShuffleScoreboard(Player player) {
            super(player, Message.TITLE_WOOLSHUFFLE.get(), getConfigName());
            this.roundLine = new EventScoreboardLine(8);
            this.countdownLine = new EventScoreboardLine(7);
            this.colourLine = new EventScoreboardLine(6);
            this.pvpEnabledLine = new EventScoreboardLine(5);
            EventScoreboardLine blankLine = new EventScoreboardLine(4);
            this.playerCountLine = new EventScoreboardLine(3);
            this.spectatorCountLine = new EventScoreboardLine(2);
            this.setLines(roundLine, countdownLine, colourLine, pvpEnabledLine, blankLine, playerCountLine, spectatorCountLine);
        }

        @Override
        protected void refresh() {
            roundLine.setText("&eRound: &6" + getRound());
            countdownLine.setText("&eTime Remaining: &6" + getTimer());
            colourLine.setText("&eRun to: " + (currentWool == null ? "None" : currentWool.getName()));
            pvpEnabledLine.setText("&ePvP: " + (pvpEnabled ? "&a&lEnabled" : "&c&lDisabled"));
            playerCountLine.setText("&ePlayers: &6" + getPlayersSize());
            spectatorCountLine.setText("&eSpectators: &6" + getSpectatorsSize());
        }
    }

    public static class Wool {
        private final String name;
        private final ItemStack itemStack;
        private final Colour colour;

        public Wool(String name, ItemStack itemStack, Colour colour) {
            this.name = name;
            this.itemStack = itemStack;
            this.colour = colour;
        }

        public String getName() {
            return name;
        }

        public ItemStack getItemStack() {
            return itemStack.clone();
        }

        public Colour getColour() {
            return colour;
        }
    }
}
