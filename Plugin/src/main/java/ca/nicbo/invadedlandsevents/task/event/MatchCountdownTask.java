package ca.nicbo.invadedlandsevents.task.event;

import ca.nicbo.invadedlandsevents.api.util.Callback;
import ca.nicbo.invadedlandsevents.api.util.Validate;
import ca.nicbo.invadedlandsevents.task.SyncedTask;

import java.util.function.Consumer;

/**
 * Counts down for {@value #STARTING_SECONDS} seconds.
 *
 * @author Nicbo
 */
public class MatchCountdownTask extends SyncedTask {
    private static final long DELAY = 0;
    private static final long PERIOD = 20;

    private static final int STARTING_SECONDS = 6;

    private final Consumer<String> broadcaster;
    private final String starting;
    private final String counter;
    private final String started;
    private final Callback callback;

    private int timer;

    private MatchCountdownTask(Builder builder) {
        super(DELAY, PERIOD);
        this.broadcaster = builder.broadcaster;
        this.starting = builder.starting;
        this.counter = builder.counter;
        this.started = builder.started;
        this.callback = builder.callback;
        this.timer = STARTING_SECONDS;
    }

    @Override
    protected void run() {
        if (timer == STARTING_SECONDS && starting != null) {
            broadcaster.accept(starting);
        }

        if (--timer > 0 && counter != null) {
            broadcaster.accept(counter.replace("{seconds}", String.valueOf(timer)));
            return;
        }

        if (started != null) {
            broadcaster.accept(started);
        }

        if (callback != null) {
            callback.call();
        }

        stop();
    }

    public static class Builder {
        private final Consumer<String> broadcaster;
        private String starting;
        private String counter;
        private String started;
        private Callback callback;

        public Builder(Consumer<String> broadcaster) {
            Validate.checkArgumentNotNull(broadcaster, "broadcaster");
            this.broadcaster = broadcaster;
        }

        public Builder setStarting(String starting) {
            this.starting = starting;
            return this;
        }

        public Builder setCounter(String counter) {
            this.counter = counter;
            return this;
        }

        public Builder setStarted(String started) {
            this.started = started;
            return this;
        }

        public Builder setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }

        public MatchCountdownTask build() {
            return new MatchCountdownTask(this);
        }
    }
}
