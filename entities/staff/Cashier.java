package entities.staff;

import entities.customer.Customer;
import entities.customer.CustomerStatus;
import entities.restaurant.Table;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import utility.Require;

/**
 * Processes at most one payment per turn (see Staff), oldest first.
 *
 * <p>It only needs the customer and their table, not the whole restaurant. The
 * controller records the revenue after a successful payment.
 */
public class Cashier extends Staff {
    /** Served earliest first; ties broken by customer id. */
    private static final Comparator<Customer> OLDEST_FIRST =
            Comparator.comparingInt(Customer::getServedTurn)
                    .thenComparingInt(Customer::getCustomerId);

    public Cashier(int staffId, String name) {
        super(staffId, name);
    }

    /** Picks who should pay next from a non-empty list of customers allowed to pay. */
    public Customer chooseNextPayer(List<Customer> candidates) {
        Objects.requireNonNull(candidates, "Candidates cannot be null.");
        return candidates.stream().min(OLDEST_FIRST)
                .orElseThrow(() -> new IllegalStateException("No customer is ready to pay."));
    }

    /**
     * Charges the customer (customer and order become PAID) and frees their table.
     *
     * @param table the table the customer sits at
     * @throws utility.GameRuleException if the customer is not allowed to pay this turn
     */
    public void processPayment(Customer customer, Table table, int turn) {
        checkCanAct(turn);
        Objects.requireNonNull(customer, "Customer cannot be null.");
        Objects.requireNonNull(table, "Table cannot be null.");
        Require.rule(customer.getStatus() == CustomerStatus.READY_TO_PAY,
                "Only a customer who is ready to pay can pay.");
        Require.rule(customer.canPayOn(turn),
                "Customer cannot pay on the turn they became ready to pay.");
        Require.state(table.isOccupiedBy(customer), "Customer is not seated at this table.");

        customer.pay();
        table.release();
        recordAction(turn);
    }
}
