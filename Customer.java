public class Customer {
    //constants shared by all customers
    private static final int INITIAL_SATISFACTION = 100;
    private static final int SATISFACTION_LOSS = 8;
    private static final int NOT_SERVED = -1;

    //name and id are final
    private final int customerId;
    private final String name;
    private int satisfaction;
    private CustomerStatus status;
    private Order order;
    private int servedTurn;

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

    //getters and setters
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

    // called by cashier uses this to reject payment on the same turn eating finished
    public int getServedTurn() {
        return servedTurn;
    }

    //order can only be taken when seated and no order is taken 
    //null order cant be throw as a parameter
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

    //customer should be waiting before they can be seated
    public void seat() {
        requireStatus(CustomerStatus.WAITING, "be seated");
        status = CustomerStatus.SEATED;
    }

    
    //servedTurn is set to the turn the order was given and status is updated
    //called by waiter
    public void startEating(int turn) {
        requireStatus(CustomerStatus.SEATED, "start eating");
        if (order == null || order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalStateException(
                    "Customer can only eat after their order is served.");
        }

        status = CustomerStatus.EATING;
        servedTurn = turn;
    }

    //called by game controller
    public void finishEating(int turn) {
        requireStatus(CustomerStatus.EATING, "finish eating");
        if (turn <= servedTurn) {
            throw new IllegalStateException(
                    "Customer must eat for at least one turn.");
        }

        status = CustomerStatus.READY_TO_PAY;
    }

    //from ready to pay to paid called by cashier
    public void markPaid() {
        requireStatus(CustomerStatus.READY_TO_PAY, "pay");
        status = CustomerStatus.PAID;
    }

    //called by gamecontroller
    //only waiting or seated customers lose satisfaction
    public void decreaseSatisfaction() {
        if (status == CustomerStatus.WAITING
                || status == CustomerStatus.SEATED) {
            satisfaction = Math.max(0, satisfaction - SATISFACTION_LOSS);
        }
    }

    //called by gamecontroller to check when satisfaction has sit zero while waiting or seated
    public boolean isAbandoning() {
        return satisfaction == 0
                && (status == CustomerStatus.WAITING
                        || status == CustomerStatus.SEATED);
    }

    //called by gamecontroller to update status to left
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

    //status chcker called by functions within this class
    private void requireStatus(CustomerStatus requiredStatus, String action) {
        if (status != requiredStatus) {
            throw new IllegalStateException(
                    "Customer must be " + requiredStatus + " to " + action + ".");
        }
    }
}
