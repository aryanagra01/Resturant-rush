package entities.staff;

import entities.customer.Order;
import java.util.Objects;

/**
 * Prepares one unit of one order per turn (see Staff).
 * The Chef may work on an order the Waiter took this turn.
 */
public class Chef extends Staff {

    public Chef(int staffId, String name) {
        super(staffId, name);
    }

    /**
     * Prepares one unit of the order. Order.prepareOneUnit checks the order status.
     *
     * @throws utility.GameRuleException if the Chef already acted or the order cannot be prepared
     */
    public void prepareOrder(Order order, int turn) {
        checkCanAct(turn);
        Objects.requireNonNull(order, "Order cannot be null.");
        order.prepareOneUnit();
        recordAction(turn);
    }
}
