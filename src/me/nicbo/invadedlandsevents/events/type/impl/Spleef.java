package me.nicbo.invadedlandsevents.events.type.impl;

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * All players start with shovels
 * Thrown snowballs break whatever block is hit
 * If a player falls under the snow they are eliminated
 * Last player standing wins the event
 *
 * @author Nicbo
 * @author StarZorrow
 */

public final class Spleef extends TimerEvent {
    private final MatchCountdown matchCountdown;
    private final BukkitRunnable heightCheck;

    private final int MIN_Y;
    private final Location start1;
    private final Location start2;

    private final ProtectedRegion region;

    public Spleef(InvadedLandsEvents plugin) {
        super(plugin, "Spleef", "spleef");

        this.start1 = getEventLocation("start-1");
        this.start2 = getEventLocation("start-2");

        BlockVector pos1 = getEventBlockVector("snow-1");
        BlockVector pos2 = getEventBlockVector("snow-2");

        this.MIN_Y = (int) Math.min(pos1.getY(), pos2.getY());

        this.matchCountdown = new MatchCountdown(this::broadcastEventMessage, Message.SPLEEF_MATCH_COUNTER, Message.SPLEEF_MATCH_STARTED);

        this.heightCheck = new BukkitRunnable() {
            @Override
            public void run() {
                List<Player> toLose = new ArrayList<>();
                for (Player player : getPlayersView()) {
                    if (player.getLocation().getY() < MIN_Y - 1) {
                        toLose.add(player);
                        broadcastEventMessage(Message.SPLEEF_ELIMINATED.get()
                                .replace("{player}", player.getName())
                                .replace("{remaining}", String.valueOf(getPlayersSize() - toLose.size())));
                    }
                }
                loseEvent(toLose);
            }
        };

        this.region = getEventRegion("break-region");

        // Just to avoid IDE warnings, region can not be null if event is valid
        if (isValid() && region != null) {
            buildSnow(pos1, pos2);

            // Allow players to break blocks
            region.setFlag(DefaultFlag.BUILD, StateFlag.State.ALLOW);
            region.setFlag(DefaultFlag.BLOCK_BREAK, StateFlag.State.ALLOW);
        }
    }

    @Override
    protected void start() {
        super.start();

        List<Player> players = getPlayersView();
        ItemStack shovel = new ItemBuilder(Material.DIAMOND_SPADE)
                .setEnchants(new Enchant(Enchantment.DIG_SPEED, 5))
                .build();

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            SpigotUtils.clearInventory(player);
            player.teleport(i % 2 == 0 ? start1 : start2);
            player.getInventory().setItem(0, shovel);
        }

        heightCheck.runTaskTimer(plugin, 0, 5);
        matchCountdown.start(plugin);
    }

    @Override
    protected void over() {
        super.over();
        if (matchCountdown.isCounting()) {
            matchCountdown.cancel();
        }

        heightCheck.cancel();
    }

    @Override
    protected Function<Player, EventScoreboard> getScoreboardFactory() {
        return SpleefSB::new;
    }

    @Override
    protected List<String> getDescriptionMessage() {
        return ListMessage.SPLEEF_DESCRIPTION.get();
    }

    private void buildSnow(BlockVector pos1, BlockVector pos2) {
        int minX = (int) Math.min(pos1.getX(), pos2.getX());
        int minZ = (int) Math.min(pos1.getZ(), pos2.getZ());
        int maxX = (int) Math.max(pos1.getX(), pos2.getX());
        int maxY = (int) Math.max(pos1.getY(), pos2.getY());
        int maxZ = (int) Math.max(pos1.getZ(), pos2.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = MIN_Y; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = getEventWorld().getBlockAt(x, y, z);
                    if (block.getType() != Material.SNOW_BLOCK) {
                        block.setType(Material.SNOW_BLOCK);
                    }
                }
            }
        }
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (getSpectatorsView().contains(player)) {
            event.setCancelled(true);
        } else if (getPlayersView().contains(player)) {
            if (isRunning() && !matchCountdown.isCounting() && block.getType() == Material.SNOW_BLOCK && SpigotUtils.isLocInRegion(block.getLocation(), region)) {
                block.setType(Material.AIR);
                player.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 4));
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSnowballHitSnowSpleef(ProjectileHitEvent event) {
        ProjectileSource shooter = event.getEntity().getShooter();
        if (!(shooter instanceof Player) || ignoreEvent((Player) shooter)) {
            return;
        }

        if (event.getEntity() instanceof Snowball) {
            Snowball snowball = (Snowball) event.getEntity();
            BlockIterator iterator = new BlockIterator(snowball.getWorld(),
                    snowball.getLocation().toVector(),
                    snowball.getVelocity().normalize(), 0, 4);

            while (iterator.hasNext()) {
                Block block = iterator.next();
                if (block.getType() == Material.SNOW_BLOCK && SpigotUtils.isLocInRegion(block.getLocation(), region)) {
                    block.setType(Material.AIR);
                    break;
                }
            }
        }
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && isPlayerParticipating((Player) event.getEntity())) {
            event.setCancelled(true);
        }
    }

    private final class SpleefSB extends EventScoreboard {
        private final TrackLine playerCountTrack;
        private final TrackLine specCountTrack;
        private final TrackLine timeRemainingTrack;

        private SpleefSB(Player player) {
            super(getConfigName(), player);
            this.playerCountTrack = new TrackLine("pctSpleef", "&ePlayers: ", "&c&6", "", 4);
            this.specCountTrack = new TrackLine("sctSpleef", "&eSpectators: ", "&b&6", "", 3);
            this.timeRemainingTrack = new TrackLine("trtSpleef", "&eTime Remain", "ing: &6", "", 2);
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
