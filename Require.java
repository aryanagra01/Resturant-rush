// Shared argument and state checks.
// Every class reports bad input the same way, so the checks are written once here
// instead of being copied into each constructor and staff method.
public final class Require {

    private Require() {
    }

    // Rejects null and returns the value, so it can be used inline.
    public static <T> T notNull(T value, String what) {
        if (value == null) {
            throw new IllegalArgumentException(what + " cannot be null.");
        }
        return value;
    }

    public static void notBlank(String value, String what) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(what + " cannot be blank.");
        }
    }

    public static void notNegative(int value, String what) {
        if (value < 0) {
            throw new IllegalArgumentException(what + " cannot be negative.");
        }
    }

    // Turns are numbered from 1.
    public static void validTurn(int turn, String what) {
        if (turn < 1) {
            throw new IllegalArgumentException(what + " must be at least 1.");
        }
    }

    // A rule of the game is broken: the action is refused and the message is shown.
    public static void state(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    // Works for any status enum, so Customer and Order share one status check.
    public static <E extends Enum<E>> void status(E actual, E required,
            String subject, String action) {
        if (actual != required) {
            throw new IllegalStateException(
                    subject + " must be " + required + " to " + action + ".");
        }
    }
}
