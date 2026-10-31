package entities.customer;

/**
 * The stages an order goes through.
 * Each status knows whether the chef can still prepare it and whether leaving cancels it.
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

    /** True when the chef can still prepare a unit. */
    public boolean needsPreparation() {
        return needsPreparation;
    }

    /** True when the order has not been served, so leaving cancels it. */
    public boolean isUnfinished() {
        return unfinished;
    }
}
