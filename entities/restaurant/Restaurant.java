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

// Stores tables, the waiting queue, customers, staff, the menu and statistics.
// GameController decides when these change. This class does not read console input.
public class Restaurant {

    //victory conditions
    public static final int TARGET_PAID_CUSTOMERS = 4;
    public static final int TARGET_REVENUE_CENTS = 4500;
    public static final int TARGET_AVERAGE_SATISFACTION = 60;

    //resturatn attibutes
    private final ArrayList<Table> tables;
    private final ArrayList<Customer> waitingQueue;
    private final ArrayList<Customer> customers;
    private final ArrayList<MenuItem> menu;

    //staff
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;

    //game stats
    private int revenueCents;
    private int paidCount;
    private int servedCount;
    private int unhappyDepartures;
    private int totalPaidSatisfaction;

    // Initialise the base restaurant with 2 tables, 3 menu items and 3 staff members
    public Restaurant() {
        tables = new ArrayList<>();
        tables.add(new Table(1));
        tables.add(new Table(2));

        waitingQueue = new ArrayList<>();
        customers = new ArrayList<>();

        menu = new ArrayList<>();
        menu.add(new MenuItem("Salad", 900, 1));
        menu.add(new MenuItem("Pasta", 1800, 2));
        menu.add(new MenuItem("Burger", 1500, 1));

        waiter = new Waiter(1, "Waiter");
        chef = new Chef(2, "Chef");
        cashier = new Cashier(3, "Cashier");
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

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }

    public int getTotalPaidSatisfaction() {
        return totalPaidSatisfaction;
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

        Order order = customer.getOrder();
        if (order == null || order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Only a paid order adds revenue.");
        }

        revenueCents += order.getBillCents();
        paidCount++;
        totalPaidSatisfaction += customer.getSatisfaction();
    }

    //counter methods
    public void recordService() {
        servedCount++;
    }

    public void recordAbandonment() {
        unhappyDepartures++;
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

    // Calculates average satisfaction of paid customers
    // Used for game statistics and victory checks
    public double getAverageSatisfaction() {
        if (paidCount == 0) {
            throw new IllegalStateException("No customer has paid yet.");
        }
        return (double) totalPaidSatisfaction / paidCount;
    }

    // Called by GameController after turn 18
    // Checks whether all three victory conditions are met
    public boolean hasWon() {
        if (paidCount < TARGET_PAID_CUSTOMERS
                || revenueCents < TARGET_REVENUE_CENTS) {
            return false;
        }
        return getAverageSatisfaction() >= TARGET_AVERAGE_SATISFACTION;
    }
}
