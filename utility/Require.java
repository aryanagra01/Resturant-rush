package utility;

/**
 * Shared checks so every class reports problems the same way.
 * rule() is a choice the manager can retry. The other checks are bugs and stop the game.
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

    /** Rejects a turn number below 1. Turns are numbered from 1. */
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

    /** A broken game rule. The manager is told why and chooses again. */
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

    /** Checks that a status is the one required before an action. */
    public static <E extends Enum<E>> void status(E actual, E required,
            String subject, String action) {
        if (actual != required) {
            throw new IllegalStateException(
                    subject + " must be " + required + " to " + action + ".");
        }
    }
}
