package entities.staff;

import entities.customer.Customer;
import entities.customer.CustomerStatus;
import entities.restaurant.Table;
import java.util.List;
import java.util.Objects;
import utility.Require;

// Takes at most one payment per turn, oldest customer first.
// The controller records the revenue after a successful payment.
public class Cashier extends Staff {

    public Cashier(int staffId, String name) {
        super(staffId, name);
    }

    // Selects the oldest eligible customer. The smaller customer id breaks a tie.
    public Customer chooseNextPayer(List<Customer> candidates) {
        Objects.requireNonNull(candidates, "Candidates cannot be null.");
        if (candidates.isEmpty()) {
            throw new IllegalStateException("No customer is ready to pay.");
        }

        Customer oldest = candidates.get(0);
        for (int i = 1; i < candidates.size(); i++) {
            Customer candidate = candidates.get(i);
            if (candidate.getServedTurn() < oldest.getServedTurn()) {
                oldest = candidate;
            } else if (candidate.getServedTurn() == oldest.getServedTurn()
                    && candidate.getCustomerId() < oldest.getCustomerId()) {
                oldest = candidate;
            }
        }
        return oldest;
    }

    // Charges the customer and frees their table. Fails if they cannot pay this turn.
    public void processPayment(Customer customer, Table table, int turn) {
        performTask(turn, () -> {
            Objects.requireNonNull(customer, "Customer cannot be null.");
            Objects.requireNonNull(table, "Table cannot be null.");
            Require.rule(customer.getStatus() == CustomerStatus.READY_TO_PAY,
                    "Only a customer who is ready to pay can pay.");
            Require.rule(customer.canPayOn(turn),
                    "Customer cannot pay on the turn they became ready to pay.");
            Require.state(table.isOccupiedBy(customer), "Customer is not seated at this table.");

            customer.pay();
            table.release();
        });
    }
}
