package entities.customer;

import entities.menu.MenuItem;
import java.util.List;
import java.util.Objects;
import utility.Require;

/**
 * Base class for every customer type.
 *
 * <p>
 * It holds the shared order-and-service flow (seat, order, eat, then pay or
 * leave). Subclasses change only the parts that differ, through dynamic
 * binding:
 * <ul>
 * <li>{@link #getType()}: which type this is</li>
 * <li>{@link #getSatisfactionLoss()}: points lost per waiting turn</li>
 * <li>{@link #pickMenuItem(List)}: which dish they choose</li>
 * <li>{@link #applyDiscount(int)}: how the bill is priced</li>
 * <li>{@link #onFoodServed(int, int)}: a reaction to being served</li>
 * </ul>
 * Callers always work with {@code Customer}, never with the concrete subclass.
 */


public abstract class Customer {
    private static final int INITIAL_SATISFACTION = 100;
    private static final int MAX_SATISFACTION = 100;
    private static final int NOT_YET = -1;

    private final int customerId;
    private final String name;
    private int satisfaction;
    private CustomerStatus status;
    private Order order;
    private int servedTurn;

    protected Customer(int customerId, String name) {
        Require.notNegative(customerId, "Customer ID");
        Require.notBlank(name, "Customer name");

        this.customerId = customerId;
        this.name = name;
        satisfaction = INITIAL_SATISFACTION;
        status = CustomerStatus.WAITING;
        order = null;
        servedTurn = NOT_YET;
    }

    // ---------- behaviour each subclass defines ----------

    /** Which customer type this is, for display and descriptions. */
    public abstract CustomerType getType();

    /** Satisfaction points lost per turn while waiting for a table or food. */
    public abstract int getSatisfactionLoss();

    /**
     * Hook: the dish this customer picks from a non-empty menu. Overrides must always
     * return an item from the menu. Default: menu index = customer id mod menu size.
     */
    protected MenuItem pickMenuItem(List<MenuItem> menu) {
        return menu.get(Math.floorMod(customerId, menu.size()));
    }

    /** Hook: the bill for a menu price. Default: full price. */
    protected int applyDiscount(int priceCents) {
        return priceCents;
    }

    /** Hook: runs once, right after the customer is served. Default: nothing. */
    protected void onFoodServed(int placedTurn, int servedTurn) {
    }

    // ---------- template methods: shared checks, then the subclass hook ----------

    /** Validates the menu once, then lets the customer type pick. */
    public final MenuItem chooseMenuItem(List<MenuItem> menu) {
        Objects.requireNonNull(menu, "Menu cannot be null.");
        Require.state(!menu.isEmpty(), "The menu is empty.");
        return pickMenuItem(menu);
    }

    /** Validates the price once, then lets the customer type apply its discount. */
    public final int calculateBill(int priceCents) {
        Require.notNegative(priceCents, "Price");
        return applyDiscount(priceCents);
    }

    // ---------- getters ----------

    public int getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    /** The customer's order, or null before one is taken. */
    public Order getOrder() {
        return order;
    }

    /** The turn the customer was served, or -1 if not served yet. */
    public int getServedTurn() {
        return servedTurn;
    }

    // ---------- questions other classes ask (one source of truth) ----------

    /** Waiting for a table or for food. */
    public boolean isAwaitingService() {
        return status.isAwaitingService();
    }

    /** Satisfaction hit 0 while still waiting for service. */
    public boolean isAbandoning() {
        return satisfaction == 0 && isAwaitingService();
    }

    // ---------- state changes ----------

    /** Called by the Waiter when seating the customer. */
    public void seat() {
        Require.status(status, CustomerStatus.WAITING, "Customer", "be seated");
        status = CustomerStatus.SEATED;
    }

    /** Called by the Waiter when taking the order. */
    public void assignOrder(Order order) {
        Require.state(status == CustomerStatus.SEATED && this.order == null,
                "Customer cannot receive an order now.");
        this.order = Objects.requireNonNull(order, "Order cannot be null.");
    }

    /**
     * Called by the Waiter after the order is served. Starts eating, then runs the
     * type's {@link #onFoodServed} hook once.
     */
    public void startEating(int turn) {
        Require.status(status, CustomerStatus.SEATED, "Customer", "start eating");
        Require.state(order != null && order.getStatus() == OrderStatus.SERVED,
                "Customer can only eat after their order is served.");

        status = CustomerStatus.EATING;
        servedTurn = turn;
        onFoodServed(order.getPlacedTurn(), turn);
    }

    /** Called by the controller one full turn after being served. */
    public void finishEating(int turn) {
        Require.status(status, CustomerStatus.EATING, "Customer", "finish eating");
        Require.state(turn > servedTurn, "Customer must eat for at least one turn.");
        status = CustomerStatus.READY_TO_PAY;
    }

    /** Called by the Cashier. */
    public void markPaid() {
        Require.status(status, CustomerStatus.READY_TO_PAY, "Customer", "pay");
        status = CustomerStatus.PAID;
    }

    /** Loses {@link #getSatisfactionLoss()} points (minimum 0), only while awaiting service. */
    public void decreaseSatisfaction() {
        if (isAwaitingService()) {
            satisfaction = Math.max(0, satisfaction - getSatisfactionLoss());
        }
    }

    /** For subclasses: raises satisfaction, never above 100. */
    protected void increaseSatisfaction(int amount) {
        Require.notNegative(amount, "Satisfaction increase");
        satisfaction = Math.min(MAX_SATISFACTION, satisfaction + amount);
    }

    /** The customer abandons the restaurant. Only allowed while awaiting service. */
    public void leave() {
        Require.state(status != CustomerStatus.PAID && status != CustomerStatus.LEFT,
                "A paid or departed customer cannot leave again.");
        Require.state(isAwaitingService(),
                "A served customer cannot abandon the restaurant.");
        status = CustomerStatus.LEFT;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + customerId + ", name=" + name
                + ", status=" + status + ", satisfaction=" + satisfaction
                + ", order=" + (order == null ? "none" : order.getOrderId()) + "]";
    }
}
