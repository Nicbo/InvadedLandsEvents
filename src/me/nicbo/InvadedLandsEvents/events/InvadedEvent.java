package me.nicbo.InvadedLandsEvents.events;

public abstract class InvadedEvent {
    public abstract void start();
    public abstract void stop();

    public void startCountDown() {

    }
    /*
    TODO:
        - Add what every event is using Arraylist of players, countdown for starting event, itemstack for event preview?
     */
}
