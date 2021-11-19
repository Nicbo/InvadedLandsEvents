package ca.nicbo.invadedlandsevents.event.event.player;

import ca.nicbo.invadedlandsevents.api.event.Event;
import ca.nicbo.invadedlandsevents.api.event.event.player.EventPlayerEvent;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.util.SpigotUtils;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Called when an event player damages an event player.
 *
 * @author Nicbo
 */
public class EventPlayerDamageByEventPlayerEvent extends EventPlayerEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player damager;
    private final Projectile projectile;
    private final EntityDamageByEntityEvent originalEvent;

    private boolean applyingKnockback;

    public EventPlayerDamageByEventPlayerEvent(Event event, Player player, Player damager, EntityDamageByEntityEvent originalEvent) {
        super(event, player);
        Validate.checkArgumentNotNull(damager, "damager");
        Validate.checkArgumentNotNull(originalEvent, "originalEvent");
        this.damager = damager;
        this.projectile = originalEvent.getDamager() instanceof Projectile ? (Projectile) originalEvent.getDamager() : null;
        this.originalEvent = originalEvent;
        this.applyingKnockback = true;
    }

    public boolean isKillingBlow() {
        return getPlayer().getHealth() - getFinalDamage() <= 0;
    }

    public void doFakeDeath() {
        setDamage(0);
        setApplyingKnockback(false);
        SpigotUtils.clear(getPlayer());
    }

    public double getDamage() {
        return originalEvent.getDamage();
    }

    public void setDamage(double damage) {
        originalEvent.setDamage(damage);
    }

    public double getFinalDamage() {
        return originalEvent.getFinalDamage();
    }

    public Player getDamager() {
        return damager;
    }

    public Projectile getProjectile() {
        return projectile;
    }

    public boolean isApplyingKnockback() {
        return applyingKnockback;
    }

    public void setApplyingKnockback(boolean applyingKnockback) {
        this.applyingKnockback = applyingKnockback;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return originalEvent.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancel) {
        originalEvent.setCancelled(cancel);
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
