package entities.staff;

import entities.customer.Customer;
import entities.restaurant.Table;
import java.util.Objects;
import utility.Require;

/**
 * Staff who can seat customers: the Waiter and the Host. Seating is written once
 * here and counts as that staff member's task for the turn (see Staff).
 *
 * <p>Seating is not in Staff, so the Chef and the Cashier do not inherit a task
 * they must never do.
 */
public abstract class FrontOfHouseStaff extends Staff {

    protected FrontOfHouseStaff(int staffId, String name) {
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
}
