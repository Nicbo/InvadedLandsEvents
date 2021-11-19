package ca.nicbo.invadedlandsevents.task;

import ca.nicbo.invadedlandsevents.InvadedLandsEventsPlugin;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import org.bukkit.scheduler.BukkitTask;

/**
 * Wraps a synchronized {@link BukkitTask}.
 * <p>
 * Note that start/stop functionality might not be supported by every subclass.
 *
 * @author Nicbo
 */
public abstract class SyncedTask {
    private final long delay;
    private final long period;

    private BukkitTask bukkitTask;

    protected SyncedTask(long delay, long period) {
        this.delay = delay;
        this.period = period;
    }

    public void start(InvadedLandsEventsPlugin plugin) {
        Validate.checkArgumentNotNull(plugin, "plugin");
        Validate.checkState(!isRunning(), "task is already running");
        onStart();
        this.bukkitTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::run, delay, period);
    }

    protected void onStart() {
    }

    public void stop() {
        Validate.checkState(isRunning(), "task is not running");
        onStop();
        this.bukkitTask.cancel();
        this.bukkitTask = null;
    }

    protected void onStop() {
    }

    protected abstract void run();

    public boolean isRunning() {
        return bukkitTask != null;
    }
}
