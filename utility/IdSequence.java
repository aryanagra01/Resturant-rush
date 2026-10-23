package utility;

/**
 * Hands out increasing ids: 1, 2, 3, and so on.
 *
 * <p>Everyone who creates the same kind of object shares one sequence, so ids never
 * repeat. For example, all waiters share the order-id sequence.
 */
public final class IdSequence {
    private int next;

    /** A sequence starting at 1. */
    public IdSequence() {
        this(1);
    }

    public IdSequence(int first) {
        Require.notNegative(first, "First id");
        next = first;
    }

    /** Returns the next id and advances the sequence. */
    public int next() {
        return next++;
    }
}
