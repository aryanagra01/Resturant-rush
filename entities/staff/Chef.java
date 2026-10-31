package entities.staff;

import entities.customer.Order;
import java.util.Objects;

// Prepares one unit of one order per turn.
// The chef may work on an order the waiter took this turn.
public class Chef extends Staff {

    public Chef(int staffId, String name) {
        super(staffId, name);
    }

    // Prepares one unit. Fails if the chef already acted or the order is not being prepared.
    public void prepareOrder(Order order, int turn) {
        Objects.requireNonNull(order, "Order cannot be null.");
        performTask(turn, order::prepareOneUnit);
    }
}
