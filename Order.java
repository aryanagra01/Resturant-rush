/**
 * Represents a customer's food order and manages its state transitions.
 */
public class Order {
    private final int orderId;
    private final MenuItem menuItem;
    private int preparedUnits;
    private OrderStatus status;
    private final int billCents;

    /**
     * Creates a placed order whose bill is locked at the supplied amount.
     *
     * @param orderId  the order's unique identifier
     * @param menuItem the ordered menu item
     * @param billCents the final bill in cents
     */
    public Order(int orderId, MenuItem menuItem, int billCents) {
        if (orderId < 0) {
            throw new IllegalArgumentException("Order ID cannot be negative.");
        }
        if (menuItem == null) {
            throw new IllegalArgumentException("Menu item cannot be null.");
        }
        if (billCents < 0) {
            throw new IllegalArgumentException("Bill cannot be negative.");
        }

        this.orderId = orderId;
        this.menuItem = menuItem;
        this.billCents = billCents;
        preparedUnits = 0;
        status = OrderStatus.PLACED;
    }

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

    /** Advances preparation by exactly one unit. */
    public void prepareOneUnit() {
        if (status != OrderStatus.PLACED
                && status != OrderStatus.PREPARING) {
            throw new IllegalStateException(
                    "Only a placed or preparing order can be prepared.");
        }

        status = OrderStatus.PREPARING;
        preparedUnits++;

        if (preparedUnits == menuItem.getPreparationUnits()) {
            status = OrderStatus.READY;
        }
    }

    public boolean isReady() {
        return status == OrderStatus.READY;
    }

    /** Marks a ready order as served. */
    public void serve() {
        requireStatus(OrderStatus.READY, "be served");
        status = OrderStatus.SERVED;
    }

    /** Marks a served order as paid. */
    public void markPaid() {
        requireStatus(OrderStatus.SERVED, "be paid");
        status = OrderStatus.PAID;
    }

    /** Cancels an order that has not yet been served. */
    public void cancel() {
        if (status != OrderStatus.PLACED
                && status != OrderStatus.PREPARING
                && status != OrderStatus.READY) {
            throw new IllegalStateException(
                    "Only an unfinished order can be cancelled.");
        }

        status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus requiredStatus, String action) {
        if (status != requiredStatus) {
            throw new IllegalStateException(
                    "Order must be " + requiredStatus + " to " + action + ".");
        }
    }
}
