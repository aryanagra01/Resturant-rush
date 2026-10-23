// The stages an order goes through.
// Each status knows whether the kitchen can still work on it and whether it is
// unfinished, so these rules live in one place.
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

    // The chef can still prepare a unit.
    public boolean needsPreparation() {
        return needsPreparation;
    }

    // Not yet served, so it can be cancelled if the customer leaves.
    public boolean isUnfinished() {
        return unfinished;
    }
}
