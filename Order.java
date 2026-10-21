//created by the wiater object when a order is ordered
public class Order {
    private final int orderId;
    private final MenuItem menuItem;
    private int preparedUnits;
    private OrderStatus status;
    private final int billCents;
    private final int placedTurn;

    public Order(int orderId, MenuItem menuItem, int billCents, int placedTurn) {
        if (orderId < 0) {
            throw new IllegalArgumentException("Order ID cannot be negative.");
        }
        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item cannot be null.");
        }
        if (billCents < 0) {
            throw new IllegalArgumentException("Bill cannot be negative.");
        }
        if (placedTurn < 1) {
            throw new IllegalArgumentException("Placed turn must be at least 1.");
        }

        this.orderId = orderId;
        this.menuItem = menuItem;
        this.billCents = billCents;
        this.placedTurn = placedTurn;
        preparedUnits = 0;
        status = OrderStatus.PLACED;
    }

    // temporary so Waiter still compiles. removed in prompt 3
    // once Waiter passes the real placement turn.
    public Order(int orderId, MenuItem menuItem, int billCents) {
        this(orderId, menuItem, billCents, 1);
    }

    //getters and setters
    public int getOrderId() {
        return orderId;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getPreparedUnits() {
        return preparedUnits;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getBillCents() {
        return billCents;
    }

    public int getPlacedTurn() {
        return placedTurn;
    }

    /** Advances preparation by exactly one unit. */
    //called by chef to advance the order by one unit
    //only when order is placed or preparing
    public void prepareOneUnit() {
        if (status != OrderStatus.PLACED
                && status != OrderStatus.PREPARING) {
            throw new IllegalStateException(
                    "Only a placed or preparing order can be prepared.");
        }

        //one turn to go from placed order to preparing
        status = OrderStatus.PREPARING;
        preparedUnits++;

        //update status when ready
        if (preparedUnits == menuItem.getPreparationUnits()) {
            status = OrderStatus.READY;
        }
    }

    //check function called by waiter
    public boolean isReady() {
        return status == OrderStatus.READY;
    }

    //called by waiter to update status
    public void serve() {
        requireStatus(OrderStatus.READY, "be served");
        status = OrderStatus.SERVED;
    }

    //called by cashhier to update satus to paid
    public void markPaid() {
        requireStatus(OrderStatus.SERVED, "be paid");
        status = OrderStatus.PAID;
    }

    // caled by gamecontroller to cancel an unfinished order when customer abandons
    public void cancel() {
        if (status != OrderStatus.PLACED
                && status != OrderStatus.PREPARING
                && status != OrderStatus.READY) {
            throw new IllegalStateException(
                    "Only an unfinished order can be cancelled.");
        }

        status = OrderStatus.CANCELLED;
    }

    //check fucntion used by class functions
    private void requireStatus(OrderStatus requiredStatus, String action) {
        if (status != requiredStatus) {
            throw new IllegalStateException(
                    "Order must be " + requiredStatus + " to " + action + ".");
        }
    }
}
