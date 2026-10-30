package utility;

/**
 * Shared argument and state checks, so every class reports problems the same way.
 *
 * <p>Two kinds of check:
 * <ul>
 *   <li>{@link #rule} for game rules the manager's choice can break. Throws
 *       {@link GameRuleException}, which the controller shows and retries.</li>
 *   <li>{@link #state}, {@link #status} and the argument checks for things only a
 *       bug can break. They throw standard exceptions, so the game stops.</li>
 * </ul>
 * For null checks use {@link java.util.Objects#requireNonNull(Object, String)}.
 */
public final class Require {

    private Require() {
    }

    /** Rejects a blank or null string. */
    public static void notBlank(String value, String what) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(what + " cannot be blank.");
        }
    }

    /** Rejects a negative number. */
    public static void notNegative(int value, String what) {
        if (value < 0) {
            throw new IllegalArgumentException(what + " cannot be negative.");
        }
    }

    /** Rejects a turn number below 1 (turns are numbered from 1). */
    public static void validTurn(int turn, String what) {
        if (turn < 1) {
            throw new IllegalArgumentException(what + " must be at least 1.");
        }
    }

    /** Rejects an invalid argument with the given message. */
    public static void argument(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /** A game rule: if it is broken, the manager is told why and chooses again. */
    public static void rule(boolean condition, String message) {
        if (!condition) {
            throw new GameRuleException(message);
        }
    }

    /** An invariant that only a bug can break. */
    public static void state(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    /** Invariant check for any status enum, shared by Customer and Order. */
    public static <E extends Enum<E>> void status(E actual, E required,
            String subject, String action) {
        if (actual != required) {
            throw new IllegalStateException(
                    subject + " must be " + required + " to " + action + ".");
        }
    }
}
