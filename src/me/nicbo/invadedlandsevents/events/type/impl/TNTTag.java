package me.nicbo.invadedlandsevents.events.type.impl;

import me.nicbo.invadedlandsevents.InvadedLandsEvents;
import me.nicbo.invadedlandsevents.events.type.RoundEvent;
import me.nicbo.invadedlandsevents.messages.impl.Message;
import me.nicbo.invadedlandsevents.scoreboard.EventScoreboard;
import me.nicbo.invadedlandsevents.scoreboard.line.Line;
import me.nicbo.invadedlandsevents.scoreboard.line.TrackLine;
import me.nicbo.invadedlandsevents.util.SpigotUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Half of the players are tagged
 * Each round every player who is tagged gets eliminated
 * Hitting a player when you are tagged tags that player
 *
 * @author Nicbo
 */

public final class TNTTag extends RoundEvent {
    private final Set<Player> tagged;

    private final Location start;

    public TNTTag(InvadedLandsEvents plugin) {
        super(plugin, "tnttag", "TNT Tag", new int[]{30});
        this.tagged = new HashSet<>();

        this.start = getEventLocation("start");
    }

    @Override
    protected void start() {
        super.start();
        for (Player player : getPlayersView()) {
            player.teleport(start);
        }
    }

    @Override
    protected void newRound() {
        broadcastEventMessage(Message.TNTTAG_ROUND_STARTING.get().replace("{round}", String.valueOf(getRound())));
        shufflePlayers();
        List<Player> players = getPlayersView();

        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (i < players.size() / 2) {
                tagPlayer(player);
            } else {
                unTagPlayer(player);
            }
        }
    }

    @Override
    protected void eliminatePlayers() {
        int lost = 0;
        for (Player player : tagged) {
            broadcastEventMessage(Message.TNTTAG_ELIMINATED.get()
                    .replace("{player}", player.getName())
                    .replace("{remaining}", String.valueOf(getPlayersSize() - ++lost)));
        }
        loseEvent(tagged);
        tagged.clear();
    }

    @Override
    public void leaveEvent(Player player) {
        super.leaveEvent(player);
        tagged.remove(player);
    }

    @Override
    protected Function<Player, EventScoreboard> getScoreboardFactory() {
        return TNTTagSB::new;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerHitTT(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (ignoreEvent(player)) {
                return;
            }

            event.setDamage(0);

            if (event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();
                if (tagged.contains(damager) && !tagged.contains(player)) {
                    unTagPlayer(damager);
                    tagPlayer(player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClickTT(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (!ignoreEvent(player)) {
                event.setCancelled(true);
            }
        }
    }

    private void tagPlayer(Player player) {
        ItemStack tnt = new ItemStack(Material.TNT);
        tagged.add(player);
        player.getInventory().setHelmet(tnt);
        SpigotUtils.fillPlayerHotbar(player, tnt);
        player.sendMessage(Message.TNTTAG_TAG.get());
        player.removePotionEffect(PotionEffectType.SPEED);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true, false));
    }

    private void unTagPlayer(Player player) {
        tagged.remove(player);
        SpigotUtils.clearInventory(player);
        player.removePotionEffect(PotionEffectType.SPEED);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, true, false));
    }

    private final class TNTTagSB extends EventScoreboard {
        private final TrackLine roundTrack;
        private final TrackLine timerTrack;
        private final TrackLine playerCountTrack;
        private final TrackLine specCountTrack;

        private final Line hit1;
        private final Line hit2;
        private final Line blank1;
        private final Line blank2;

        private boolean showHit;

        private TNTTagSB(Player player) {
            super(getConfigName(), player);
            this.hit1 = new Line("h1TNTTag", "&c&lYOU", " MUST ", "HIT", 9);
            this.hit2 = new Line("h2TNTTag", "&c&lA TNTLE", "SS", " PLAYER", 8);
            this.blank1 = new Line("bTNTTag", "", "&d", "", 7);

            this.roundTrack = new TrackLine("rtTNTTag", "&eRound: ", "&6", "", 6);
            this.timerTrack = new TrackLine("ttTNTTag", "&eTime Remaining", ": &6", "", 5);
            this.blank2 = new TrackLine("b1TNTTag", "", "&8&8", "", 4);
            this.playerCountTrack = new TrackLine("pctTNTTag", "&ePlayers: ", "&b&6", "", 3);
            this.specCountTrack = new TrackLine("sctTNTTag", "&eSpectators: ", "&7&6", "", 2);

            this.initLines(roundTrack, timerTrack, blank2, playerCountTrack, specCountTrack);
        }

        @Override
        protected void refresh() {
            boolean playerTagged = tagged.contains(getPlayer());

            if (showHit && !playerTagged) {
                this.showHit = false;
                clearLines();
                this.initLines(roundTrack, timerTrack, blank2, playerCountTrack, specCountTrack);
            } else if (!showHit && playerTagged) {
                this.showHit = true;
                clearLines();
                this.initLines(hit1, hit2, blank1, roundTrack, timerTrack, blank2, playerCountTrack, specCountTrack);
            }

            this.roundTrack.setSuffix(String.valueOf(getRound()));
            this.timerTrack.setSuffix(String.valueOf(getTimer()));
            this.playerCountTrack.setSuffix(String.valueOf(getPlayersSize()));
            this.specCountTrack.setSuffix(String.valueOf(getSpectatorsSize()));
        }
    }
}
