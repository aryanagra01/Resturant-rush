package entities.customer;

import entities.menu.MenuItem;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
 * The customer also keeps its order in step with itself:
 * {@link #receiveFood(int)}
 * and {@link #pay()} change both, so staff never update the two separately.
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
    private int readyToPayTurn;

    protected Customer(int customerId, String name) {
        Require.notNegative(customerId, "Customer ID");
        Require.notBlank(name, "Customer name");

        this.customerId = customerId;
        this.name = name;
        satisfaction = INITIAL_SATISFACTION;
        status = CustomerStatus.WAITING;
        order = null;
        servedTurn = NOT_YET;
        readyToPayTurn = NOT_YET;
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

    /** The customer's order, or empty before one is taken. */
    public Optional<Order> getOrder() {
        return Optional.ofNullable(order);
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

    public boolean canBeSeated() {
        return status == CustomerStatus.WAITING;
    }

    /** Seated and has not ordered yet. */
    public boolean canPlaceOrder() {
        return status == CustomerStatus.SEATED && order == null;
    }

    /** Seated and their order is READY. */
    public boolean canBeServed() {
        return status == CustomerStatus.SEATED && order != null && order.isReady();
    }

    /** Has an order the Chef can still work on. */
    public boolean hasOrderToPrepare() {
        return order != null && order.needsPreparation();
    }

    /** Served on an earlier turn, so one full turn of eating is done. */
    public boolean isDoneEating(int turn) {
        return status == CustomerStatus.EATING && turn > servedTurn;
    }

    /** Became READY_TO_PAY on an earlier turn, so the Cashier may charge now. */
    public boolean canPayOn(int turn) {
        return status == CustomerStatus.READY_TO_PAY && turn > readyToPayTurn;
    }

    /** Satisfaction hit 0 while still waiting for service. */
    public boolean isAbandoning() {
        return satisfaction == 0 && isAwaitingService();
    }

    // ---------- state changes ----------

    /** Called by the Waiter after checking {@link #canBeSeated()}. */
    public void seat() {
        Require.status(status, CustomerStatus.WAITING, "Customer", "be seated");
        status = CustomerStatus.SEATED;
    }

    /** Called by the Waiter after checking {@link #canPlaceOrder()}. */
    public void assignOrder(Order order) {
        Require.state(canPlaceOrder(), "Customer cannot receive an order now.");
        this.order = Objects.requireNonNull(order, "Order cannot be null.");
    }

    /**
     * Called by the Waiter after checking {@link #canBeServed()}. Serves the order,
     * starts eating, then runs the type's {@link #onFoodServed} hook once.
     */
    public void receiveFood(int turn) {
        Require.state(canBeServed(), "Customer can only be served a ready order.");

        order.serve();
        status = CustomerStatus.EATING;
        servedTurn = turn;
        onFoodServed(order.getPlacedTurn(), turn);
    }

    /** Called by the controller one full turn after being served. */
    public void finishEating(int turn) {
        Require.status(status, CustomerStatus.EATING, "Customer", "finish eating");
        Require.state(turn > servedTurn, "Customer must eat for at least one turn.");
        status = CustomerStatus.READY_TO_PAY;
        readyToPayTurn = turn;
    }

    /** Called by the Cashier: marks the customer and their order PAID together. */
    public void pay() {
        Require.status(status, CustomerStatus.READY_TO_PAY, "Customer", "pay");
        order.markPaid();
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

    /** Cancels the order if it was not served yet. Returns true when one was cancelled. */
    public boolean cancelUnfinishedOrder() {
        if (order == null || !order.isUnfinished()) {
            return false;
        }
        order.cancel();
        return true;
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
