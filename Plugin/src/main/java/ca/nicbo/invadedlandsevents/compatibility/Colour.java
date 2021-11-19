package ca.nicbo.invadedlandsevents.compatibility;

/**
 * The different colours that some materials can have.
 *
 * @author Nicbo
 */
public enum Colour {
    WHITE(0),
    ORANGE(1),
    MAGENTA(2),
    LIGHT_BLUE(3),
    YELLOW(4),
    LIME(5),
    PINK(6),
    GREY(7),
    LIGHT_GREY(8),
    CYAN(9),
    PURPLE(10),
    BLUE(11),
    BROWN(12),
    GREEN(13),
    RED(14),
    BLACK(15);

    private final short data;

    Colour(int data) {
        this.data = (short) data;
    }

    public short getData() {
        return data;
    }
}
