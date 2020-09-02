package me.nicbo.invadedlandsevents.messages;

/**
 * Message that is in messages.yml
 *
 * @author Nicbo
 * @param <T> the type of message
 */

public interface IMessage<T> {
    /**
     * Gets the message
     *
     * @return the message
     */
    T get();

    /**
     * Set the message
     *
     * @param message the message
     */
    void set(T message);

    /**
     * Gets the path of the message
     *
     * @return the path
     */
    String getPath();
}
