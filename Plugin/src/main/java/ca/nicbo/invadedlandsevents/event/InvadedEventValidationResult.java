package ca.nicbo.invadedlandsevents.event;

import ca.nicbo.invadedlandsevents.api.util.Validate;

/**
 * The result of validating an {@link InvadedEvent}.
 *
 * @author Nicbo
 */
public class InvadedEventValidationResult {
    private static final String DEFAULT_VALID_MESSAGE = "Event is valid";
    private static final String DEFAULT_INVALID_MESSAGE = "Event is invalid";

    private final boolean valid;
    private final String message;

    public InvadedEventValidationResult(boolean valid) {
        this(valid, valid ? DEFAULT_VALID_MESSAGE : DEFAULT_INVALID_MESSAGE);
    }

    public InvadedEventValidationResult(boolean valid, String message) {
        Validate.checkArgumentNotNull(message, "message");
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMessage() {
        return message;
    }
}
