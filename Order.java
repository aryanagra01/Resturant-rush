// Created by the Waiter when a customer orders.
// The order owns its own status rules; staff only ask it to change.
public class Order {
    private final int orderId;
    private final MenuItem menuItem;
    private final int billCents;
    private final int placedTurn;
    private int preparedUnits;
    private OrderStatus status;

    public Order(int orderId, MenuItem menuItem, int billCents, int placedTurn) {
        Require.notNegative(orderId, "Order ID");
        Require.notNull(menuItem, "Menu item");
        Require.notNegative(billCents, "Bill");
        Require.validTurn(placedTurn, "Placed turn");

        this.orderId = orderId;
        this.menuItem = menuItem;
        this.billCents = billCents;
        this.placedTurn = placedTurn;
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

    public int getPlacedTurn() {
        return placedTurn;
    }

    public boolean needsPreparation() {
        return status.needsPreparation();
    }

    public boolean isUnfinished() {
        return status.isUnfinished();
    }

    public boolean isReady() {
        return status == OrderStatus.READY;
    }

    // Called by the Chef. Becomes READY when the last unit is done.
    public void prepareOneUnit() {
        Require.state(needsPreparation(),
                "Only a placed or preparing order can be prepared.");

        status = OrderStatus.PREPARING;
        preparedUnits++;
        if (preparedUnits == menuItem.getPreparationUnits()) {
            status = OrderStatus.READY;
        }
    }

    // Called by the Waiter.
    public void serve() {
        Require.status(status, OrderStatus.READY, "Order", "be served");
        status = OrderStatus.SERVED;
    }

    // Called by the Cashier.
    public void markPaid() {
        Require.status(status, OrderStatus.SERVED, "Order", "be paid");
        status = OrderStatus.PAID;
    }

    // Called when the customer abandons the restaurant.
    public void cancel() {
        Require.state(isUnfinished(), "Only an unfinished order can be cancelled.");
        status = OrderStatus.CANCELLED;
    }
}
