package control;

import config.GameSettings;
import entities.customer.Customer;
import entities.customer.CustomerType;
import entities.customer.Order;
import entities.menu.MenuItem;
import entities.restaurant.Restaurant;
import entities.restaurant.Table;
import entities.staff.Cashier;
import entities.staff.Chef;
import entities.staff.Waiter;
import java.util.List;
import java.util.Objects;
import utility.GameRuleException;
import utility.IdSequence;

/**
 * Runs the turns in the order the brief requires.
 *
 * <p>It coordinates the restaurant, the staff and the view; the domain objects
 * apply their own rules and the view does all text formatting. It owns the staff
 * and depends only on the {@link GameView} interface, not on a concrete UI. Only
 * {@link GameRuleException} is treated as a refused choice; any other exception
 * is a bug and stops the game.
 */
public class GameController {
    private static final int CHEF_WAIT = 0;

    private final GameSettings settings;
    private final Restaurant restaurant;
    private final GameView view;
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;
    private final IdSequence customerIds;
    private int currentTurn;

    /** The restaurant is built from the same settings, so the two always match. */
    public GameController(GameSettings settings, GameView view) {
        this.settings = Objects.requireNonNull(settings, "Settings cannot be null.");
        this.view = Objects.requireNonNull(view, "View cannot be null.");
        restaurant = settings.createRestaurant();

        // A fixed staff of three, as the base setting requires.

        waiter = new Waiter(1, "Waiter", new IdSequence());
        chef = new Chef(2, "Chef");
        cashier = new Cashier(3, "Cashier");
        customerIds = new IdSequence();
    }

    /** Plays every turn, then shows victory or defeat. */
    public void startGame() {
        int maxTurns = settings.getMaxTurns();
        view.displayWelcome(restaurant, maxTurns);
        for (currentTurn = 1; currentTurn <= maxTurns; currentTurn++) {
            playTurn();
        }
        view.displayFinalResults(restaurant, maxTurns);
    }

    private void playTurn() {
        processArrivals();
        view.displayGameState(restaurant, currentTurn, settings.getMaxTurns());
        retryUntilValid(this::processWaiterChoice);
        retryUntilValid(this::processChefChoice);
        processEating();
        processPayment();
        processSatisfactionAndAbandonment();
        view.displayTurnResults(restaurant, currentTurn, settings.getMaxTurns());
    }

    // Runs one manager decision. If it breaks a game rule, the reason is shown and
    // the manager chooses again; the staff task is not used up (see Staff.performTask).
    private void retryUntilValid(Runnable decision) {
        while (true) {
            try {
                decision.run();
                return;
            } catch (GameRuleException ex) {
                view.displayError(ex.getMessage());
            }
        }
    }

    // ---------- step 1: arrivals ----------

    private void processArrivals() {
        settings.arrivalOn(currentTurn).ifPresent(this::admit);
    }

    private void admit(CustomerType type) {
        int id = customerIds.next();
        Customer customer = type.create(id, "Customer " + id);
        restaurant.addCustomer(customer);
        view.showArrival(customer);
    }

    // ---------- step 2 and 3: manager choices, waiter before chef ----------

    private void processWaiterChoice() {
        WaiterAction action = view.readWaiterAction();
        Customer customer = null;
        if (action.needsCustomer()) {
            int customerId = view.readInt("Enter the customer id: ");
            customer = restaurant.findCustomer(customerId).orElseThrow(
                    () -> new GameRuleException("No customer has id " + customerId + "."));
        }
        action.perform(this, customer);
    }

    // Called by WaiterAction.WAIT.
    void waiterWaits() {
        view.showWaits(waiter);
    }

    // Called by WaiterAction.SEAT.
    void seatCustomer(Customer customer) {
        Table table = restaurant.findAvailableTable().orElseThrow(
                () -> new GameRuleException("No table is available."));
        waiter.seatCustomer(customer, table, currentTurn);
        restaurant.removeFromWaitingQueue(customer);
        view.showSeated(customer, table);
    }

    // Called by WaiterAction.TAKE_ORDER. The customer chooses the food.
    void takeCustomerOrder(Customer customer) {
        MenuItem item = restaurant.selectMenuItem(customer);
        Order order = waiter.takeOrder(customer, item, currentTurn);
        view.showOrderTaken(customer, order);
    }

    // Called by WaiterAction.SERVE.
    void serveCustomer(Customer customer) {
        waiter.serveOrder(customer, currentTurn);
        restaurant.recordService();
        view.showServed(customer);
    }

    // The chef acts after the waiter, so a dish finished now is served next turn.
    private void processChefChoice() {
        int choice = view.readChefChoice(restaurant.getCustomersWithOrdersToPrepare());
        if (choice == CHEF_WAIT) {
            view.showWaits(chef);
            return;
        }

        Order order = restaurant.findOrder(choice).orElseThrow(
                () -> new GameRuleException("No order has id " + choice + "."));
        chef.prepareOrder(order, currentTurn);
        view.showPrepared(order);
    }

    // ---------- step 4: eating and payment ----------

    private void processEating() {
        for (Customer customer : restaurant.findCustomers(c -> c.isDoneEating(currentTurn))) {
            customer.finishEating(currentTurn);
            view.showFinishedEating(customer);
        }
    }

    // Customers who became ready to pay this turn are excluded by canPayOn.
    // If a payment is refused, the next oldest customer is tried.
    private void processPayment() {
        List<Customer> candidates = restaurant.findCustomers(c -> c.canPayOn(currentTurn));

        while (!candidates.isEmpty() && cashier.canAct(currentTurn)) {
            Customer payer = cashier.chooseNextPayer(candidates);
            try {
                checkout(payer);
            } catch (GameRuleException ex) {
                view.displayError(ex.getMessage());
                candidates.remove(payer);
            }
        }
    }

    // The only place a customer is charged: the Cashier takes the payment and the
    // revenue is recorded in the same step, so one can never happen without the other.
    private void checkout(Customer payer) {
        // A ready-to-pay customer always has a table; if not, it is a bug.
        Table table = restaurant.findTableByCustomer(payer).orElseThrow(
                () -> new IllegalStateException(payer.getName() + " is not at a table."));
        cashier.processPayment(payer, table, currentTurn);
        restaurant.recordPayment(payer);
        view.showPayment(payer);
    }

    // ---------- step 5: waiting and abandonment ----------

    private void processSatisfactionAndAbandonment() {
        for (Customer customer : restaurant.findCustomers(Customer::isAwaitingService)) {
            customer.decreaseSatisfaction();
            if (customer.isAbandoning()) {
                abandonCustomer(customer);
            }
        }
    }

    private void abandonCustomer(Customer customer) {
        boolean orderCancelled = customer.cancelUnfinishedOrder();

        Table freedTable = null;
        if (customer.canBeSeated()) {
            restaurant.removeFromWaitingQueue(customer);
        } else {
            freedTable = restaurant.releaseTableOf(customer).orElse(null);
        }

        customer.leave();
        restaurant.recordAbandonment();
        view.showDeparture(customer, orderCancelled, freedTable);
    }
}
