package entities.restaurant;

import entities.customer.Customer;
import entities.customer.CustomerStatus;
import entities.customer.Order;
import entities.customer.OrderStatus;
import entities.menu.MenuItem;
import entities.staff.Cashier;
import entities.staff.Chef;
import entities.staff.Waiter;

import java.util.ArrayList;
import java.util.List;
import utility.IdSequence;

// Stores tables, the waiting queue, customers, staff, the menu and statistics.
// GameController decides when these change. This class does not read console input.
public class Restaurant {

    //resturatn attibutes
    private final ArrayList<Table> tables;
    private final ArrayList<Customer> waitingQueue;
    private final ArrayList<Customer> customers;
    private final ArrayList<MenuItem> menu;

    //staff
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;

    //game stats and the victory check
    private final GameStats stats;

    // Built from values passed in (see GameSettings), not from global constants.
    public Restaurant(List<MenuItem> menu, int tableCount, VictoryTargets targets) {
        tables = new ArrayList<>();
        for (int id = 1; id <= tableCount; id++) {
            tables.add(new Table(id));
        }

        waitingQueue = new ArrayList<>();
        customers = new ArrayList<>();
        this.menu = new ArrayList<>(menu);

        waiter = new Waiter(1, "Waiter", new IdSequence());
        chef = new Chef(2, "Chef");
        cashier = new Cashier(3, "Cashier");

        stats = new GameStats(targets);
    }

    //called by gamecontroler for phase 1 of each step
    // Rejects duplicate customer IDs
    public void addCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        if (customer.getStatus() != CustomerStatus.WAITING) {
            throw new IllegalStateException(
                    "Only a waiting customer can join the queue.");
        }
        if (findCustomer(customer.getCustomerId()) != null) {
            throw new IllegalStateException(
                    "A customer with this id is already in the restaurant.");
        }

        customers.add(customer);
        waitingQueue.add(customer);
    }

    //getters
    public ArrayList<Customer> getWaitingCustomers() {
        return new ArrayList<>(waitingQueue);
    }

    public ArrayList<Customer> getCustomers() {
        return new ArrayList<>(customers);
    }

    public ArrayList<Table> getTables() {
        return new ArrayList<>(tables);
    }

    public ArrayList<MenuItem> getMenu() {
        return new ArrayList<>(menu);
    }

    public Waiter getWaiter() {
        return waiter;
    }

    public Chef getChef() {
        return chef;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public GameStats getStats() {
        return stats;
    }

    // Return the first available table, or null if all are occupied
    public Table getAvailableTable() {
        for (int i = 0; i < tables.size(); i++) {
            Table table = tables.get(i);
            if (table.isAvailable()) {
                return table;
            }
        }
        return null;
    }

    // Find a customer by ID
    public Customer findCustomer(int id) {
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            if (customer.getCustomerId() == id) {
                return customer;
            }
        }
        return null;
    }

    // Find a table by ID
    public Table findTable(int id) {
        for (int i = 0; i < tables.size(); i++) {
            Table table = tables.get(i);
            if (table.getTableId() == id) {
                return table;
            }
        }
        return null;
    }

    // Find the table occupied by a specific customer
    public Table findTableByCustomer(Customer customer) {
        if (customer == null) {
            return null;
        }

        for (int i = 0; i < tables.size(); i++) {
            Table table = tables.get(i);
            //This checks whether both references point to the exact same Customer object.
            if (table.getCustomer() == customer) {
                return table;
            }
        }
        return null;
    }

    // The customer chooses the item. Rejects a null customer and an empty menu.
    public MenuItem selectMenuItem(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        if (menu.isEmpty()) {
            throw new IllegalStateException("The menu is empty.");
        }

        return customer.chooseMenuItem(menu);
    }

    // Called by Cashier after a successful payment
    // Record a successful payment and update the restaurant's statistics.
    public void recordPayment(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        if (customer.getStatus() != CustomerStatus.PAID) {
            throw new IllegalStateException(
                    "Only a paid customer can be added to revenue.");
        }

        Order order = customer.getOrder().orElse(null);
        if (order == null || order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Only a paid order adds revenue.");
        }

        stats.recordPayment(order.getBillCents(), customer.getSatisfaction());
    }

    //counter methods
    public void recordService() {
        stats.recordService();
    }

    public void recordAbandonment() {
        stats.recordAbandonment();
    }

    // Called by GameController after seating or abandonment
    // Removes the customer from the waiting queue
    public void removeFromWaitingQueue(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        if (!waitingQueue.remove(customer)) {
            throw new IllegalStateException(
                    "Customer is not in the waiting queue.");
        }
    }

    // Called by GameController after turn 18
    // Checks whether all three victory conditions are met
    public boolean hasWon() {
        return stats.hasMetTargets();
    }
}
