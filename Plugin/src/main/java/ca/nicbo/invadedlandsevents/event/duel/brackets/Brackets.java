package ca.nicbo.invadedlandsevents.event.duel.brackets;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.event.EventType;
import ca.nicbo.invadedlandsevents.event.duel.DuelEvent;
import ca.nicbo.invadedlandsevents.event.event.player.EventPlayerDamageByEventPlayerEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.List;

/**
 * Brackets.
 *
 * @author Nicbo
 */
public abstract class Brackets extends DuelEvent {
    protected Brackets(InvadedLandsEventsPlugin plugin, EventType eventType, String hostName, List<String> description, int teamSize) {
        super(plugin, eventType, hostName, description, teamSize);
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEventPlayerDamageByEventPlayer(EventPlayerDamageByEventPlayerEvent event) {
        super.onEventPlayerDamageByEventPlayer(event);

        if (event.isCancelled()) {
            return;
        }

        if (event.isKillingBlow()) {
            event.doFakeDeath();
            eliminatePlayer(event.getPlayer(), false);
        }
    }
}
