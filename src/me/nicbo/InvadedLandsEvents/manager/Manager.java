package me.nicbo.InvadedLandsEvents.manager;

/**
 * All managers extend this class so they can have a handler
 *
 * @author StarZorroww
 * @since 2020-03-12
 */

public class Manager {
    protected ManagerHandler handler;

    public Manager(ManagerHandler handler) {
        this.handler = handler;
    }
}