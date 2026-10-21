public class Customer {
    private static final int INITIAL_SATISFACTION = 100;
    private static final int SATISFACTION_LOSS = 8;
    private static final int NOT_SERVED = -1;

    private final int customerId;
    private final String name;
    private int satisfaction;
    private CustomerStatus status;
    private Order order;
    private int servedTurn;

    /**
     * Creates a waiting customer with full satisfaction and no order.
     *
     * @param customerId the customer's unique identifier
     * @param name       the customer's display name
     */
    public Customer(int customerId, String name) {
        if (customerId < 0) {
            throw new IllegalArgumentException("Customer ID cannot be negative.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer name cannot be blank.");
        }

        this.customerId = customerId;
        this.name = name;
        satisfaction = INITIAL_SATISFACTION;
        status = CustomerStatus.WAITING;
        order = null;
        servedTurn = NOT_SERVED;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public Order getOrder() {
        return order;
    }

    /** Associates the customer's first order with them. */
    public void assignOrder(Order order) {
        if (status != CustomerStatus.SEATED) {
            throw new IllegalStateException(
                    "Only a seated customer can receive an order.");
        }
        if (this.order != null) {
            throw new IllegalStateException("Customer already has an order.");
        }
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }

        this.order = order;
    }

    /** Moves the customer from the waiting queue to a table. */
    public void seat() {
        requireStatus(CustomerStatus.WAITING, "be seated");
        status = CustomerStatus.SEATED;
    }

    /** Records that food was served and starts the customer's meal. */
    public void startEating(int turn) {
        requireStatus(CustomerStatus.SEATED, "start eating");
        if (order == null || order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalStateException(
                    "Customer can only eat after their order is served.");
        }

        status = CustomerStatus.EATING;
        servedTurn = turn;
    }

    /** Finishes the meal on a turn after the food was served. */
    public void finishEating(int turn) {
        requireStatus(CustomerStatus.EATING, "finish eating");
        if (turn <= servedTurn) {
            throw new IllegalStateException(
                    "Customer must eat for at least one turn.");
        }

        status = CustomerStatus.READY_TO_PAY;
    }

    /** Marks a customer who is ready to pay as paid. */
    public void markPaid() {
        requireStatus(CustomerStatus.READY_TO_PAY, "pay");
        status = CustomerStatus.PAID;
    }

    /** Reduces satisfaction by eight points without going below zero. */
    public void decreaseSatisfaction() {
        satisfaction = Math.max(0, satisfaction - SATISFACTION_LOSS);
    }

    /**
     * Returns whether the customer should leave before receiving service.
     */
    public boolean isAbandoning() {
        return satisfaction == 0
                && (status == CustomerStatus.WAITING
                        || status == CustomerStatus.SEATED);
    }

    /** Makes an unpaid customer leave the restaurant. */
    public void leave() {
        if (status == CustomerStatus.PAID || status == CustomerStatus.LEFT) {
            throw new IllegalStateException(
                    "A paid or departed customer cannot leave again.");
        }
        if (status == CustomerStatus.EATING
                || status == CustomerStatus.READY_TO_PAY) {
            throw new IllegalStateException(
                    "A served customer cannot abandon the restaurant.");
        }

        status = CustomerStatus.LEFT;
    }

    private void requireStatus(CustomerStatus requiredStatus, String action) {
        if (status != requiredStatus) {
            throw new IllegalStateException(
                    "Customer must be " + requiredStatus + " to " + action + ".");
        }
    }
}
