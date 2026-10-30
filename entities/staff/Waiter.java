package entities.staff;

import entities.customer.Customer;
import entities.customer.CustomerStatus;
import entities.customer.Order;
import entities.menu.MenuItem;
import entities.restaurant.Table;
import java.util.Objects;
import utility.Require;

/** Seats customers, takes orders and serves ready food. One task per turn (see Staff). */
public class Waiter extends Staff {
    // Order ids start at 1 and go up by one for each order this waiter takes.
    private int nextOrderId = 1;

    public Waiter(int staffId, String name) {
        super(staffId, name);
    }

    /**
     * Seats a waiting customer at a free table. GameController removes the customer
     * from the waiting queue afterwards.
     *
     * @throws utility.GameRuleException if the customer is not waiting or the table is taken
     */
    public void seatCustomer(Customer customer, Table table, int turn) {
        performTask(turn, () -> {
            Objects.requireNonNull(customer, "Customer cannot be null.");
            Objects.requireNonNull(table, "Table cannot be null.");
            Require.rule(customer.canBeSeated(), "Only a waiting customer can be seated.");
            Require.rule(table.isAvailable(), "Table is already occupied.");

            table.occupy(customer);
            customer.seat();
        });
    }

    /**
     * Takes a seated customer's order. The bill is priced by the customer object
     * (dynamic binding: VIPs get a discount) and locked in the new order.
     *
     * @throws utility.GameRuleException if the customer is not seated or already ordered
     */
    public Order takeOrder(Customer customer, MenuItem item, int turn) {
        return performTask(turn, () -> {
            Objects.requireNonNull(customer, "Customer cannot be null.");
            Objects.requireNonNull(item, "Menu item cannot be null.");
            Require.rule(customer.getStatus() == CustomerStatus.SEATED,
                    "Only a seated customer can place an order.");
            Require.rule(customer.canPlaceOrder(), "Customer already has an order.");

            int billCents = customer.calculateBill(item.getPriceCents());
            Order order = new Order(nextOrderId, item, billCents, turn);
            nextOrderId++;
            customer.assignOrder(order);
            return order;
        });
    }

    /**
     * Serves a READY order. GameController runs the Waiter before the Chef, so food
     * that becomes ready this turn is served on a later turn at the earliest.
     *
     * @throws utility.GameRuleException if the customer is not seated or the order is not ready
     */
    public void serveOrder(Customer customer, int turn) {
        performTask(turn, () -> {
            Objects.requireNonNull(customer, "Customer cannot be null.");
            Require.rule(customer.getStatus() == CustomerStatus.SEATED,
                    "Only a seated customer can be served.");
            Require.rule(customer.canBeServed(),
                    "Customer can only be served a ready order.");

            customer.receiveFood(turn);
        });
    }
}
