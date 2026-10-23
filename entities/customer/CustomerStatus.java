package entities.customer;

/**
 * The stages a customer goes through.
 *
 * <p>Each status knows whether the customer is still waiting for service, so the
 * "WAITING or SEATED" check is written once here.
 */
public enum CustomerStatus {
    WAITING(true),
    SEATED(true),
    EATING(false),
    READY_TO_PAY(false),
    PAID(false),
    LEFT(false);

    private final boolean awaitingService;

    CustomerStatus(boolean awaitingService) {
        this.awaitingService = awaitingService;
    }

    /** Waiting for a table or for food: loses satisfaction and may abandon. */
    public boolean isAwaitingService() {
        return awaitingService;
    }
}
