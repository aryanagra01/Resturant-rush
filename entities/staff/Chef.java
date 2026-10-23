package entities.staff;

import entities.customer.Order;
import utility.Require;

public class Chef extends Staff {

    public Chef(int staffId, String name) {
        super(staffId, name);
    }

    // Order.prepareOneUnit checks the order status, so it is not repeated here.
    public void prepareOrder(Order order, int turn) {
        Require.notNull(order, "Order");
        performTask(turn, order::prepareOneUnit);
    }
}
