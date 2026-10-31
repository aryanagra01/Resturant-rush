package entities.restaurant;

import entities.customer.Customer;
import entities.customer.CustomerStatus;
import entities.customer.Order;
import entities.customer.OrderStatus;
import entities.menu.MenuItem;
import entities.pricing.HappyHour;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import utility.Require;

// Tables, waiting queue, customers, menu, statistics and Happy Hour.
// Built from the values passed in. GameController owns the staff.
// Getters return copies, so callers cannot change the restaurant's lists.
public class Restaurant {
    private final List<Table> tables;
    private final List<Customer> waitingQueue;
    private final List<Customer> customers;
    private final List<MenuItem> menu;
    private final GameStats stats;
    private final HappyHour happyHour;

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
        happyHour = new HappyHour();
    }

    // Customers waiting for a table, in arrival order.
    public List<Customer> getWaitingCustomers() {
        return List.copyOf(waitingQueue);
    }

    // Every customer who has arrived, in arrival order, including paid and departed customers.
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

    public HappyHour getHappyHour() {
        return happyHour;
    }

    public Optional<Customer> findCustomer(int customerId) {
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getCustomerId() == customerId) {
                return Optional.of(customers.get(i));
            }
        }
        return Optional.empty();
    }

    public Optional<Order> findOrder(int orderId) {
        for (int i = 0; i < customers.size(); i++) {
            Optional<Order> order = customers.get(i).getOrder();
            if (order.isPresent() && order.get().getOrderId() == orderId) {
                return order;
            }
        }
        return Optional.empty();
    }

    // The first free table, if any.
    public Optional<Table> findAvailableTable() {
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).isAvailable()) {
                return Optional.of(tables.get(i));
            }
        }
        return Optional.empty();
    }

    public Optional<Table> findTableByCustomer(Customer customer) {
        Objects.requireNonNull(customer, "Customer cannot be null.");
        for (int i = 0; i < tables.size(); i++) {
            if (tables.get(i).isOccupiedBy(customer)) {
                return Optional.of(tables.get(i));
            }
        }
        return Optional.empty();
    }

    // Customers whose order the chef can still work on, in arrival order.
    public List<Customer> getCustomersWithOrdersToPrepare() {
        ArrayList<Customer> preparing = new ArrayList<Customer>();
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).hasOrderToPrepare()) {
                preparing.add(customers.get(i));
            }
        }
        return preparing;
    }

    // Adds a newly arrived customer to the restaurant and the waiting queue.
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

    // Frees the customer's table and returns it, or empty if they had none.
    public Optional<Table> releaseTableOf(Customer customer) {
        Optional<Table> table = findTableByCustomer(customer);
        if (table.isPresent()) {
            table.get().release();
        }
        return table;
    }

    // Asks the customer to choose a dish. Each customer type has its own rule.
    public MenuItem selectMenuItem(Customer customer) {
        Objects.requireNonNull(customer, "Customer cannot be null.");
        return customer.chooseMenuItem(menu);
    }

    // Adds a completed payment to revenue. Called once, immediately after the cashier charges.
    public void recordPayment(Customer customer) {
        Objects.requireNonNull(customer, "Customer cannot be null.");
        Require.state(customer.getStatus() == CustomerStatus.PAID,
                "Only a paid customer can be added to revenue.");
        Optional<Order> found = customer.getOrder();
        if (found.isEmpty() || found.get().getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Only a paid order adds revenue.");
        }
        Order order = found.get();

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
