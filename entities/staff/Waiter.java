package entities.staff;

import entities.customer.Customer;
import entities.customer.CustomerStatus;
import entities.customer.Order;
import entities.menu.MenuItem;
import entities.pricing.PricingPolicy;
import java.util.Objects;
import utility.Require;

// Seats customers (see FrontOfHouseStaff), takes orders and serves ready food.
// One task per turn.
public class Waiter extends FrontOfHouseStaff {
    // Order ids start at 1 and increase by one for each order this waiter takes.
    private int nextOrderId = 1;
    private final PricingPolicy pricing;

    /** A waiter that charges the customer's own price, with no extra discount. */
    public Waiter(int staffId, String name) {
        this(staffId, name, PricingPolicy.NONE);
    }

    /**
     * @param pricing applied to every bill after the customer's own discount, for
     *                example Happy Hour
     */
    public Waiter(int staffId, String name, PricingPolicy pricing) {
        super(staffId, name);
        this.pricing = Objects.requireNonNull(pricing, "Pricing cannot be null.");
    }

    // Takes a seated customer's order. The customer's own discount (VIP) comes first,
    // then the pricing policy (Happy Hour). The bill is then fixed in the order.
    public Order takeOrder(Customer customer, MenuItem item, int turn) {
        return performTask(turn, () -> {
            Objects.requireNonNull(customer, "Customer cannot be null.");
            Objects.requireNonNull(item, "Menu item cannot be null.");
            Require.rule(customer.getStatus() == CustomerStatus.SEATED,
                    "Only a seated customer can place an order.");
            Require.rule(customer.canPlaceOrder(), "Customer already has an order.");

            int billCents = pricing.apply(customer.calculateBill(item.getPriceCents()));
            Order order = new Order(nextOrderId, item, billCents, turn);
            customer.assignOrder(order);
            nextOrderId++;
            return order;
        });
    }

    // Serves a ready order. Food finished this turn is served on a later turn at the earliest.
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
