package entities.customer;

/**
 * The stages a customer goes through.
 * Waiting and seated customers are still waiting for service.
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

    /** True while the customer is waiting for a table or for food. */
    public boolean isAwaitingService() {
        return awaitingService;
    }
}
