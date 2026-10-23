package entities.restaurant;

import java.util.Objects;
import entities.customer.Customer;
import utility.Require;

// One restaurant table and the customer sitting at it.
// customer is null when the table is free.
public class Table {
    private final int tableId;
    private Customer customer;

    public Table(int tableId) {
        Require.notNegative(tableId, "Table ID");
        this.tableId = tableId;
        customer = null;
    }

    public int getTableId() {
        return tableId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public boolean isAvailable() {
        return customer == null;
    }

    /** True when this exact customer sits here. */
    public boolean isOccupiedBy(Customer customer) {
        return this.customer == customer && customer != null;
    }

    // Called by the Waiter when seating a customer.
    public void occupy(Customer customer) {
        Objects.requireNonNull(customer, "Customer cannot be null.");
        Require.state(isAvailable(), "Table is already occupied.");
        this.customer = customer;
    }

    // Called after payment or when the customer abandons.
    public void release() {
        Require.state(!isAvailable(), "Table is already empty.");
        customer = null;
    }
}
