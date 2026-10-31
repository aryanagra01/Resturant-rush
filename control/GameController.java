package control;

import config.GameSettings;
import entities.customer.Customer;
import entities.customer.CustomerType;
import entities.customer.Order;
import entities.menu.MenuItem;
import entities.pricing.HappyHour;
import entities.restaurant.Restaurant;
import entities.restaurant.Table;
import entities.staff.Cashier;
import entities.staff.Chef;
import entities.staff.Waiter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import utility.GameRuleException;

// Runs each turn in the order required by the assignment.
// The domain objects apply their own rules and the view formats the text.
// Only GameRuleException is a refused choice. Any other exception stops the game.
public class GameController {
    private static final int CHEF_WAIT = 0;

    private final GameSettings settings;
    private final Restaurant restaurant;
    private final GameView view;
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;
    // Customer ids start at 1 and increase by one for each arrival.
    private int nextCustomerId = 1;
    private int currentTurn;

    // The restaurant is built from these settings, so the two always match.
    public GameController(GameSettings settings, GameView view) {
        this.settings = Objects.requireNonNull(settings, "Settings cannot be null.");
        this.view = Objects.requireNonNull(view, "View cannot be null.");
        restaurant = settings.createRestaurant();

        // The base setting uses one waiter, one chef and one cashier.
        // The waiter applies Happy Hour to every order it takes.
        waiter = new Waiter(1, "Waiter", restaurant.getHappyHour());
        chef = new Chef(2, "Chef");
        cashier = new Cashier(3, "Cashier");
    }

    // Plays every turn, then shows victory or defeat.
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
        offerHappyHour();
        retryUntilValid(this::processWaiterChoice);
        retryUntilValid(this::processChefChoice);
        processEating();
        processPayment();
        processSatisfactionAndAbandonment();
        view.displayTurnResults(restaurant, currentTurn, settings.getMaxTurns());
        restaurant.getHappyHour().endTurn();
    }

    /**
     * Runs one manager decision. If it breaks a game rule, the reason is shown and
     * the manager chooses again. The staff task is not used up (see Staff.performTask).
     */
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

    private void processArrivals() {
        CustomerType type = settings.arrivalOn(currentTurn);
        if (type != null) {
            admit(type);
        }
    }

    private void admit(CustomerType type) {
        int id = nextCustomerId;
        nextCustomerId++;
        Customer customer = type.create(id, "Customer " + id);
        restaurant.addCustomer(customer);
        view.showArrival(customer);
    }

    // At the start of a turn the manager may switch on Happy Hour, once per game.
    // The game state is shown first, so the manager can see the queue before deciding.
    private void offerHappyHour() {
        HappyHour happyHour = restaurant.getHappyHour();
        if (happyHour.isAvailable() && view.readHappyHourChoice()) {
            happyHour.activate();
            view.showHappyHourStarted(happyHour);
        }
    }

    private void processWaiterChoice() {
        int choice = view.readWaiterAction();
        if (choice == GameView.WAITER_WAIT) {
            waiterWaits();
            return;
        }
        if (choice != GameView.WAITER_SEAT
                && choice != GameView.WAITER_TAKE_ORDER
                && choice != GameView.WAITER_SERVE) {
            throw new GameRuleException(
                    "Choose 0 to wait, 1 to seat, 2 to take an order, or 3 to serve.");
        }

        int customerId = view.readInt("Enter the customer id: ");
        Optional<Customer> found = restaurant.findCustomer(customerId);
        if (found.isEmpty()) {
            throw new GameRuleException("No customer has id " + customerId + ".");
        }
        Customer customer = found.get();

        if (choice == GameView.WAITER_SEAT) {
            seatCustomer(customer);
        } else if (choice == GameView.WAITER_TAKE_ORDER) {
            takeCustomerOrder(customer);
        } else if (choice == GameView.WAITER_SERVE) {
            serveCustomer(customer);
        } else {
            throw new GameRuleException(
                    "Choose 0 to wait, 1 to seat, 2 to take an order, or 3 to serve.");
        }
    }

    // Waiter choice 0: the waiter waits.
    void waiterWaits() {
        view.showWaits(waiter);
    }

    // Waiter choice 1: seat a waiting customer.
    void seatCustomer(Customer customer) {
        Optional<Table> freeTable = restaurant.findAvailableTable();
        if (freeTable.isEmpty()) {
            throw new GameRuleException("No table is available.");
        }
        Table table = freeTable.get();
        waiter.seatCustomer(customer, table, currentTurn);
        restaurant.removeFromWaitingQueue(customer);
        view.showSeated(customer, table);
    }

    // Waiter choice 2: take an order. The customer chooses the food.
    void takeCustomerOrder(Customer customer) {
        MenuItem item = restaurant.selectMenuItem(customer);
        Order order = waiter.takeOrder(customer, item, currentTurn);
        view.showOrderTaken(customer, order);
    }

    // Waiter choice 3: serve an order that is ready.
    void serveCustomer(Customer customer) {
        waiter.serveOrder(customer, currentTurn);
        restaurant.recordService();
        view.showServed(customer);
    }

    // The chef acts after the waiter, so food finished now is served on a later turn.
    private void processChefChoice() {
        int choice = view.readChefChoice(restaurant.getCustomersWithOrdersToPrepare());
        if (choice == CHEF_WAIT) {
            view.showWaits(chef);
            return;
        }

        Optional<Order> found = restaurant.findOrder(choice);
        if (found.isEmpty()) {
            throw new GameRuleException("No order has id " + choice + ".");
        }
        Order order = found.get();
        chef.prepareOrder(order, currentTurn);
        view.showPrepared(order);
    }

    private void processEating() {
        List<Customer> customers = restaurant.getCustomers();
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            if (customer.isDoneEating(currentTurn)) {
                customer.finishEating(currentTurn);
                view.showFinishedEating(customer);
            }
        }
    }

    // Customers who became ready to pay this turn cannot pay yet.
    // If a payment is refused, the next oldest customer is tried.
    private void processPayment() {
        List<Customer> customers = restaurant.getCustomers();
        ArrayList<Customer> candidates = new ArrayList<Customer>();
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            if (customer.canPayOn(currentTurn)) {
                candidates.add(customer);
            }
        }

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

    // The only place a customer is charged. Payment and revenue are recorded together.
    private void checkout(Customer payer) {
        // A customer who is ready to pay should still be at a table. If not, it is a bug.
        Optional<Table> found = restaurant.findTableByCustomer(payer);
        if (found.isEmpty()) {
            throw new IllegalStateException(payer.getName() + " is not at a table.");
        }
        Table table = found.get();
        cashier.processPayment(payer, table, currentTurn);
        restaurant.recordPayment(payer);
        view.showPayment(payer);
    }

    // Updates satisfaction for customers waiting for service, then removes anyone at 0.
    private void processSatisfactionAndAbandonment() {
        List<Customer> customers = restaurant.getCustomers();
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            if (customer.isAwaitingService()) {
                customer.decreaseSatisfaction();
                if (customer.isAbandoning()) {
                    abandonCustomer(customer);
                }
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
