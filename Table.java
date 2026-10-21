//one restaurant table and the customer sitting at it
//customer is null when the table is free
public class Table {
    private final int tableId;
    private Customer customer;

    public Table(int tableId) {
        if (tableId < 0) {
            throw new IllegalArgumentException("Table ID cannot be negative.");
        }

        this.tableId = tableId;
        customer = null;
    }

    //getters
    public int getTableId() {
        return tableId;
    }

    public Customer getCustomer() {
        return customer;
    }

    //true when nobody is sitting here
    public boolean isAvailable() {
        return customer == null;
    }

    //called by waiter when seating a customer
    //rejects a null customer or a table that is already taken
    public void occupy(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        if (!isAvailable()) {
            throw new IllegalStateException("Table is already occupied.");
        }

        this.customer = customer;
    }

    //called by cashier after payment, or later by gamecontroller when a customer abandons
    //rejects a table that is already empty
    public void release() {
        if (isAvailable()) {
            throw new IllegalStateException("Table is already empty.");
        }

        customer = null;
    }
}
