package entities.restaurant;

import entities.customer.Customer;
import java.util.Objects;
import java.util.Optional;
import utility.Require;

/** One restaurant table and the customer sitting at it, if any. */
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

    /** The customer at this table, or empty when it is free. */
    public Optional<Customer> getCustomer() {
        return Optional.ofNullable(customer);
    }

    public boolean isAvailable() {
        return customer == null;
    }

    /** True when this exact customer sits here. */
    public boolean isOccupiedBy(Customer customer) {
        return this.customer == customer && customer != null;
    }

    /** Seats a customer. The table must already be free. */
    public void occupy(Customer customer) {
        Objects.requireNonNull(customer, "Customer cannot be null.");
        Require.state(isAvailable(), "Table is already occupied.");
        this.customer = customer;
    }

    /** Frees the table after payment or when the customer leaves. */
    public void release() {
        Require.state(!isAvailable(), "Table is already empty.");
        customer = null;
    }

    @Override
    public String toString() {
        return "Table[id=" + tableId + ", "
                + (isAvailable() ? "free" : "customer=" + customer.getName()) + "]";
    }
}
