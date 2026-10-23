package entities.customer;

import entities.menu.MenuItem;
import java.util.Objects;
import utility.Require;

/**
 * One customer's order. Created by the Waiter.
 *
 * <p>The bill is locked when the order is taken. Order lives in the customer package
 * because a customer owns their order: only {@link Customer} may serve, pay or cancel
 * it (package-private methods). The Chef only calls {@link #prepareOneUnit()}.
 */
public class Order {    
    private final int orderId;
    private final MenuItem menuItem;
    private final int billCents;
    private final int placedTurn;
    private int preparedUnits;
    private OrderStatus status;

    public Order(int orderId, MenuItem menuItem, int billCents, int placedTurn) {
        Require.notNegative(orderId, "Order ID");
        Objects.requireNonNull(menuItem, "Menu item cannot be null.");
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

    /** The bill in cents, after any discount, locked when the order was taken. */
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

    /**
     * Called by the Chef. Prepares one unit; the order becomes READY when the last
     * unit is done.
     *
     * @throws utility.GameRuleException if the order is not placed or preparing
     */
    public void prepareOneUnit() {
        Require.rule(needsPreparation(), "Only a placed or preparing order can be prepared.");

        status = OrderStatus.PREPARING;
        preparedUnits++;
        if (preparedUnits == menuItem.getPreparationUnits()) {
            status = OrderStatus.READY;
        }
    }

    // Only Customer.receiveFood calls this.
    void serve() {
        Require.status(status, OrderStatus.READY, "Order", "be served");
        status = OrderStatus.SERVED;
    }

    // Only Customer.pay calls this.
    void markPaid() {
        Require.status(status, OrderStatus.SERVED, "Order", "be paid");
        status = OrderStatus.PAID;
    }

    // Only Customer.cancelUnfinishedOrder calls this.
    void cancel() {
        Require.state(isUnfinished(), "Only an unfinished order can be cancelled.");
        status = OrderStatus.CANCELLED;
    }

    @Override
    public String toString() {
        return "Order[id=" + orderId + ", item=" + menuItem.getName()
                + ", status=" + status + ", units=" + preparedUnits + "/"
                + menuItem.getPreparationUnits() + ", billCents=" + billCents
                + ", placedTurn=" + placedTurn + "]";
    }
}
