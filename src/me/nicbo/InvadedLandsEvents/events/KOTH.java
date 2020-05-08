package me.nicbo.InvadedLandsEvents.events;

public final class KOTH extends InvadedEvent {
    private final String CAPTURING;
    private final String CAPTURING_POINTS;
    private final String LOST;

    public KOTH() {
        super("King Of The Hill", "koth");
        this.CAPTURING = getEventMessage("CAPTURING");
        this.CAPTURING_POINTS = getEventMessage("CAPTURING_POINTS");
        this.LOST = getEventMessage("LOST");
    }

    @Override
    public void init() {

    }

    @Override
    public void start() {

    }

    @Override
    public void over() {

    }
}
