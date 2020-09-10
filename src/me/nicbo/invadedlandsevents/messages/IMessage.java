package me.nicbo.invadedlandsevents.messages;

/**
 * Message that is in messages.yml
 *
 * @author Nicbo
 * @param <T> the type of message
 */

public interface IMessage<T> {
    T get();

    void set(T message);

    String getPath();
}
