package entities.restaurant;

import entities.customer.Customer;
import entities.customer.CustomerStatus;
import entities.customer.Order;
import entities.customer.OrderStatus;
import entities.menu.MenuItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import utility.Require;

/**
 * The place: tables, waiting queue, customers, menu and statistics.
 *
 * <p>Built from values passed in (menu, table count, targets), not from global
 * settings, so a test or another difficulty can build a different restaurant.
 * Staff are not part of the restaurant; GameController owns them. Getters return
 * read-only copies, so callers cannot change the lists.
 */
public class Restaurant {
    private final List<Table> tables;
    private final List<Customer> waitingQueue;
    private final List<Customer> customers;
    private final List<MenuItem> menu;
    private final GameStats stats;

    public Restaurant(List<MenuItem> menu, int tableCount, int targetPaidCustomers,
            int targetRevenueCents, int targetAverageSatisfaction) {
        Objects.requireNonNull(menu, "Menu cannot be null.");
        Require.notNegative(tableCount, "Table count");

        this.menu = List.copyOf(menu);
        tables = new ArrayList<>();
        for (int id = 1; id <= tableCount; id++) {
            tables.add(new Table(id));
        }
        waitingQueue = new ArrayList<>();
        customers = new ArrayList<>();
        stats = new GameStats(targetPaidCustomers, targetRevenueCents, targetAverageSatisfaction);
    }

    // ---------- getters ----------

    /** Customers waiting for a table, in arrival order. */
    public List<Customer> getWaitingCustomers() {
        return List.copyOf(waitingQueue);
    }

    /** Every customer who has arrived, in arrival order (including paid and departed). */
    public List<Customer> getCustomers() {
        return List.copyOf(customers);
    }

    public List<Table> getTables() {
        return List.copyOf(tables);
    }

    public List<MenuItem> getMenu() {
        return menu;
    }

    public GameStats getStats() {
        return stats;
    }

    // ---------- searching ----------

    private static <T> Optional<T> findFirst(List<T> items, Predicate<T> condition) {
        return items.stream().filter(condition).findFirst();
    }

    /**
     * All customers (in arrival order) that match the condition.
     * Returns a new list the caller may change.
     */
    public List<Customer> findCustomers(Predicate<Customer> condition) {
        List<Customer> matches = new ArrayList<>();
        for (Customer customer : customers) {
            if (condition.test(customer)) {
                matches.add(customer);
            }
        }
        return matches;
    }

    public Optional<Customer> findCustomer(int customerId) {
        return findFirst(customers, c -> c.getCustomerId() == customerId);
    }

    public Optional<Order> findOrder(int orderId) {
        return customers.stream()
                .flatMap(c -> c.getOrder().stream())
                .filter(order -> order.getOrderId() == orderId)
                .findFirst();
    }

    /** The first free table, if any. */
    public Optional<Table> findAvailableTable() {
        return findFirst(tables, Table::isAvailable);
    }

    public Optional<Table> findTableByCustomer(Customer customer) {
        Objects.requireNonNull(customer, "Customer cannot be null.");
        return findFirst(tables, t -> t.isOccupiedBy(customer));
    }

    /** Customers whose order the Chef can still work on. */
    public List<Customer> getCustomersWithOrdersToPrepare() {
        return findCustomers(Customer::hasOrderToPrepare);
    }

    // ---------- changes ----------

    /** Adds a newly arrived customer to the restaurant and the waiting queue. */
    public void addCustomer(Customer customer) {
        Objects.requireNonNull(customer, "Customer cannot be null.");
        Require.state(customer.canBeSeated(), "Only a waiting customer can join the queue.");
        Require.state(findCustomer(customer.getCustomerId()).isEmpty(),
                "A customer with this id is already in the restaurant.");

        customers.add(customer);
        waitingQueue.add(customer);
    }

    public void removeFromWaitingQueue(Customer customer) {
        Objects.requireNonNull(customer, "Customer cannot be null.");
        Require.state(waitingQueue.remove(customer), "Customer is not in the waiting queue.");
    }

    /** Frees the customer's table and returns it, or empty if they had none. */
    public Optional<Table> releaseTableOf(Customer customer) {
        Optional<Table> table = findTableByCustomer(customer);
        table.ifPresent(Table::release);
        return table;
    }

    /** The customer chooses (dynamic binding: each customer type has its own rule). */
    public MenuItem selectMenuItem(Customer customer) {
        Objects.requireNonNull(customer, "Customer cannot be null.");
        return customer.chooseMenuItem(menu);
    }

    /** Adds a completed payment to revenue. Called once, right after the Cashier charges. */
    public void recordPayment(Customer customer) {
        Objects.requireNonNull(customer, "Customer cannot be null.");
        Require.state(customer.getStatus() == CustomerStatus.PAID,
                "Only a paid customer can be added to revenue.");
        Order order = customer.getOrder()
                .filter(o -> o.getStatus() == OrderStatus.PAID)
                .orElseThrow(() -> new IllegalStateException("Only a paid order adds revenue."));

        stats.recordPayment(order.getBillCents(), customer.getSatisfaction());
    }

    public void recordService() {
        stats.recordService();
    }

    public void recordAbandonment() {
        stats.recordAbandonment();
    }

    public boolean hasWon() {
        return stats.hasMetTargets();
    }
}
