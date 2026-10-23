package entities.customer;

/**
 * The stages an order goes through.
 *
 * <p>Each status knows whether the kitchen can still work on it and whether it is
 * unfinished, so these rules live in one place.
 */
public enum OrderStatus {
    PLACED(true, true),
    PREPARING(true, true),
    READY(false, true),
    SERVED(false, false),
    PAID(false, false),
    CANCELLED(false, false);

    private final boolean needsPreparation;
    private final boolean unfinished;

    OrderStatus(boolean needsPreparation, boolean unfinished) {
        this.needsPreparation = needsPreparation;
        this.unfinished = unfinished;
    }

    /** True when the Chef can still prepare a unit. */
    public boolean needsPreparation() {
        return needsPreparation;
    }

    /** True when not yet served, so it is cancelled if the customer leaves. */
    public boolean isUnfinished() {
        return unfinished;
    }
}
