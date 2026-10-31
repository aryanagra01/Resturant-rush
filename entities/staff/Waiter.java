package entities.staff;

import entities.customer.Customer;
import entities.customer.CustomerStatus;
import entities.customer.Order;
import entities.menu.MenuItem;
import entities.restaurant.Table;
import java.util.Objects;
import utility.Require;

// Seats customers, takes orders and serves ready food. One task per turn.
public class Waiter extends Staff {
    // Order ids start at 1 and increase by one for each order this waiter takes.
    private int nextOrderId = 1;

    public Waiter(int staffId, String name) {
        super(staffId, name);
    }

    // Seats a waiting customer at a free table. The controller then removes them from the queue.
    public void seatCustomer(Customer customer, Table table, int turn) {
        checkCanAct(turn);
        Objects.requireNonNull(customer, "Customer cannot be null.");
        Objects.requireNonNull(table, "Table cannot be null.");
        Require.rule(customer.canBeSeated(), "Only a waiting customer can be seated.");
        Require.rule(table.isAvailable(), "Table is already occupied.");

        table.occupy(customer);
        customer.seat();
        recordAction(turn);
    }

    // Takes a seated customer's order. The customer prices the bill, including any VIP discount.
    public Order takeOrder(Customer customer, MenuItem item, int turn) {
        checkCanAct(turn);
        Objects.requireNonNull(customer, "Customer cannot be null.");
        Objects.requireNonNull(item, "Menu item cannot be null.");
        Require.rule(customer.getStatus() == CustomerStatus.SEATED,
                "Only a seated customer can place an order.");
        Require.rule(customer.canPlaceOrder(), "Customer already has an order.");

        int billCents = customer.calculateBill(item.getPriceCents());
        Order order = new Order(nextOrderId, item, billCents, turn);
        customer.assignOrder(order);
        nextOrderId++;
        recordAction(turn);
        return order;
    }

    // Serves a ready order. Food finished this turn is served on a later turn at the earliest.
    public void serveOrder(Customer customer, int turn) {
        checkCanAct(turn);
        Objects.requireNonNull(customer, "Customer cannot be null.");
        Require.rule(customer.getStatus() == CustomerStatus.SEATED,
                "Only a seated customer can be served.");
        Require.rule(customer.canBeServed(),
                "Customer can only be served a ready order.");

        customer.receiveFood(turn);
        recordAction(turn);
    }
}
