package entities.customer;

import entities.menu.MenuItem;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import utility.Require;

// Shared flow for every customer: seat, order, eat, then pay or leave.
// Subclasses change the type name, satisfaction loss, food choice, discount and service bonus.
// Receiving food and paying update the customer and the order together.


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

    // Short name shown next to the customer, for example "VIP".
    public abstract String getType();

    // Satisfaction points lost per turn while waiting for a table or for food.
    public abstract int getSatisfactionLoss();

    // The dish this customer picks. The default uses customer id mod menu size.
    protected MenuItem pickMenuItem(List<MenuItem> menu) {
        return menu.get(Math.floorMod(customerId, menu.size()));
    }

    // The bill for a menu price. The default is the full price.
    protected int applyDiscount(int priceCents) {
        return priceCents;
    }

    // Runs once, immediately after the customer is served. The default does nothing.
    protected void onFoodServed(int placedTurn, int servedTurn) {
    }

    // Checks the menu, then lets the customer type choose a dish.
    public final MenuItem chooseMenuItem(List<MenuItem> menu) {
        Objects.requireNonNull(menu, "Menu cannot be null.");
        Require.state(!menu.isEmpty(), "The menu is empty.");
        return pickMenuItem(menu);
    }

    // Checks the price, then lets the customer type apply its discount.
    public final int calculateBill(int priceCents) {
        Require.notNegative(priceCents, "Price");
        return applyDiscount(priceCents);
    }

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

    // The customer's order, or empty before one is taken.
    public Optional<Order> getOrder() {
        return Optional.ofNullable(order);
    }

    // The turn the customer was served, or -1 if not served yet.
    public int getServedTurn() {
        return servedTurn;
    }

    // True while waiting for a table or for food.
    public boolean isAwaitingService() {
        return status.isAwaitingService();
    }

    public boolean canBeSeated() {
        return status == CustomerStatus.WAITING;
    }

    // True when seated and no order has been taken yet.
    public boolean canPlaceOrder() {
        return status == CustomerStatus.SEATED && order == null;
    }

    // True when seated and the order is ready to serve.
    public boolean canBeServed() {
        return status == CustomerStatus.SEATED && order != null && order.isReady();
    }

    // True when the chef can still prepare this customer's order.
    public boolean hasOrderToPrepare() {
        return order != null && order.needsPreparation();
    }

    // True when the customer has been eating since an earlier turn.
    public boolean isDoneEating(int turn) {
        return status == CustomerStatus.EATING && turn > servedTurn;
    }

    // True when the customer became ready to pay on an earlier turn.
    public boolean canPayOn(int turn) {
        return status == CustomerStatus.READY_TO_PAY && turn > readyToPayTurn;
    }

    // True when satisfaction has reached 0 while still waiting for service.
    public boolean isAbandoning() {
        return satisfaction == 0 && isAwaitingService();
    }

    // Marks the customer seated. The waiter checks canBeSeated first.
    public void seat() {
        Require.status(status, CustomerStatus.WAITING, "Customer", "be seated");
        status = CustomerStatus.SEATED;
    }

    // Stores the order. The waiter checks canPlaceOrder first.
    public void assignOrder(Order order) {
        Require.state(canPlaceOrder(), "Customer cannot receive an order now.");
        this.order = Objects.requireNonNull(order, "Order cannot be null.");
    }

    // Serves the order and starts eating, then runs onFoodServed once.
    public void receiveFood(int turn) {
        Require.state(canBeServed(), "Customer can only be served a ready order.");

        order.serve();
        status = CustomerStatus.EATING;
        servedTurn = turn;
        onFoodServed(order.getPlacedTurn(), turn);
    }

    // Finishes eating one full turn after the customer was served.
    public void finishEating(int turn) {
        Require.status(status, CustomerStatus.EATING, "Customer", "finish eating");
        Require.state(turn > servedTurn, "Customer must eat for at least one turn.");
        status = CustomerStatus.READY_TO_PAY;
        readyToPayTurn = turn;
    }

    // Marks the customer and their order paid together.
    public void pay() {
        Require.status(status, CustomerStatus.READY_TO_PAY, "Customer", "pay");
        order.markPaid();
        status = CustomerStatus.PAID;
    }

    // Loses satisfaction while waiting for service. Satisfaction cannot go below 0.
    public void decreaseSatisfaction() {
        if (isAwaitingService()) {
            satisfaction = Math.max(0, satisfaction - getSatisfactionLoss());
        }
    }

    // Raises satisfaction while the customer is still waiting for a table or food
    // (Happy Hour), never above 100. Returns how much it actually went up.
    public int recover(int amount) {
        Require.state(isAwaitingService(), "Only a waiting customer can recover.");
        int before = satisfaction;
        increaseSatisfaction(amount);
        return satisfaction - before;
    }

    // Raises satisfaction for a subclass. Satisfaction cannot go above 100.
    protected void increaseSatisfaction(int amount) {
        Require.notNegative(amount, "Satisfaction increase");
        satisfaction = Math.min(MAX_SATISFACTION, satisfaction + amount);
    }

    // Cancels the order if it was not served yet. Returns true when one was cancelled.
    public boolean cancelUnfinishedOrder() {
        if (order == null || !order.isUnfinished()) {
            return false;
        }
        order.cancel();
        return true;
    }

    // The customer leaves. Only allowed while still waiting for service.
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
