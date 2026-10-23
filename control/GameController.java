package control;

import boundary.GameCLI;
import config.GameSettings;
import entities.customer.Customer;
import entities.customer.CustomerStatus;
import entities.customer.CustomerType;
import entities.customer.Order;
import entities.customer.OrderStatus;
import entities.menu.MenuItem;
import entities.restaurant.Restaurant;
import entities.restaurant.Table;
import entities.staff.Cashier;
import entities.staff.Chef;
import entities.staff.Waiter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import utility.GameRuleException;
import utility.IdSequence;
import utility.Money;

// Coordinates the turns. Specific objects apply their own state changes.
public class GameController {

    private static final int WAITER_WAIT = 0;
    private static final int WAITER_SEAT = 1;
    private static final int WAITER_ORDER = 2;
    private static final int WAITER_SERVE = 3;

    private final GameSettings settings;
    private final Restaurant restaurant;
    private final GameCLI cli;
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;
    private final IdSequence customerIds;
    private int currentTurn;

    // The restaurant is built from the same settings, so the two always match.
    public GameController(GameSettings settings, GameCLI cli) {
        this.settings = Objects.requireNonNull(settings, "Settings cannot be null.");
        this.cli = Objects.requireNonNull(cli, "Game CLI cannot be null.");
        restaurant = settings.createRestaurant();

        // A fixed staff of three, as the base setting requires.
        waiter = new Waiter(1, "Waiter", new IdSequence());
        chef = new Chef(2, "Chef");
        cashier = new Cashier(3, "Cashier");
        customerIds = new IdSequence();
    }

    // Runs every turn, then displays the final result.
    public void startGame() {
        int maxTurns = settings.getMaxTurns();
        cli.displayWelcome(restaurant, maxTurns);
        for (currentTurn = 1; currentTurn <= maxTurns; currentTurn++) {
            playTurn();
        }
        cli.displayFinalResults(restaurant, maxTurns);
    }

    // Processes arrivals, manager choices, kitchen work, eating and payment.
    // Then decreases satisfaction and displays the end-of-turn result.
    private void playTurn() {
        processArrivals();
        cli.displayGameState(restaurant, currentTurn, settings.getMaxTurns());
        processManagerChoices();
        processChefChoices();
        processEatingAndPayment();
        processSatisfactionAndAbandonment();
        cli.displayTurnResults(restaurant, currentTurn, settings.getMaxTurns());
    }

    // The settings say who arrives on each turn; CustomerType creates the right subclass.
    private void processArrivals() {
        settings.arrivalOn(currentTurn).ifPresent(this::admit);
    }

    private void admit(CustomerType type) {
        int id = customerIds.next();
        Customer customer = type.create(id, "Customer " + id);
        restaurant.addCustomer(customer);
        cli.displayMessage(customer.getName()
                + " arrived and joined the waiting queue.");
    }

    // Reads and performs one waiter task before the chef is asked.
    // If the action fails, the manager can choose again without losing the turn.
    private void processManagerChoices() {
        while (true) {
            int action = readWaiterChoice();
            int customerId = -1;

            if (action != WAITER_WAIT) {
                customerId = cli.readInt("Enter the customer id: ");
            }

            try {
                executeWaiterChoice(action, customerId);
                return;
            } catch (IllegalArgumentException | IllegalStateException | GameRuleException ex) {
                cli.displayError(ex.getMessage());
            }
        }
    }

    // The chef acts after the waiter, so a newly placed order can be prepared.
    // An order that becomes READY here cannot be served until a later turn.
    private void processChefChoices() {
        while (true) {
            cli.displayChefActions(chefActionLines());
            int choice = cli.getChefChoice();

            if (choice == 0) {
                cli.displayMessage("The chef waits.");
                return;
            }

            Order order = restaurant.findOrder(choice).orElse(null);
            if (order == null) {
                cli.displayError("No order has id " + choice + ".");
                continue;
            }

            try {
                chef.prepareOrder(order, currentTurn);
                String readyNote = order.isReady() ? " The order is ready." : "";
                cli.displayMessage("The chef prepared one unit of "
                        + order.getMenuItem().getName()
                        + " for order " + order.getOrderId()
                        + " (" + order.getPreparedUnits() + "/"
                        + order.getMenuItem().getPreparationUnits() + ")."
                        + readyNote);
                return;
            } catch (IllegalArgumentException | IllegalStateException | GameRuleException ex) {
                cli.displayError(ex.getMessage());
            }
        }
    }

    // Handles eating and payment in the correct turn order.
    // Customers who finish eating this turn can only pay next turn (see canPayOn).
    private void processEatingAndPayment() {
        processEating();
        processPayments();
    }

    // Customers served in an earlier turn finish eating.
    private void processEating() {
        List<Customer> customers = restaurant.getCustomers();
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            if (customer.isDoneEating(currentTurn)) {
                customer.finishEating(currentTurn);
                cli.displayMessage(customer.getName()
                        + " finished eating and is ready to pay.");
            }
        }
    }

    // The Cashier takes at most one payment, oldest first.
    // If a payment is refused, the next oldest customer is tried.
    private void processPayments() {
        ArrayList<Customer> candidates = new ArrayList<>();
        for (Customer customer : restaurant.getCustomers()) {
            if (customer.canPayOn(currentTurn)) {
                candidates.add(customer);
            }
        }

        while (!candidates.isEmpty() && cashier.canAct(currentTurn)) {
            Customer payer = cashier.chooseNextPayer(candidates);
            try {
                checkout(payer);
            } catch (GameRuleException ex) {
                cli.displayError(ex.getMessage());
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
        cli.displayMessage(payer.getName() + " paid "
                + Money.format(payer.getOrder().orElseThrow().getBillCents())
                + ". Satisfaction recorded: " + payer.getSatisfaction() + ".");
    }

    // Waiting and seated customers lose satisfaction. At 0 they leave.
    private void processSatisfactionAndAbandonment() {
        List<Customer> customers = restaurant.getCustomers();
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            if (customer.getStatus() != CustomerStatus.WAITING
                    && customer.getStatus() != CustomerStatus.SEATED) {
                continue;
            }
            customer.decreaseSatisfaction();
            if (customer.isAbandoning()) {
                abandonCustomer(customer);
            }
        }
    }

    // Displays the waiter actions and returns the selected choice by calling cli
    // Keeps asking until the manager enters a valid number from 0 to 3.
    private int readWaiterChoice() {
        while (true) {
            cli.displayWaiterActions();
            int choice = cli.getWaiterChoice();
            if (choice >= WAITER_WAIT && choice <= WAITER_SERVE) {
                return choice;
            }
            cli.displayError(
                    "Choose 0 to wait, 1 to seat, 2 to take an order, or 3 to serve.");
        }
    }

    // Executes the selected waiter action on the chosen customer by finding them
    // Throws an error if the customer does not exist or the action is invalid.
    private void executeWaiterChoice(int action, int customerId) {
        if (action == WAITER_WAIT) {
            cli.displayMessage("The waiter waits.");
            return;
        }

        Customer customer = restaurant.findCustomer(customerId).orElseThrow(
                () -> new GameRuleException("No customer has id " + customerId + "."));

        if (action == WAITER_SEAT) {
            seatCustomer(customer);
        } else if (action == WAITER_ORDER) {
            takeCustomerOrder(customer);
        } else if (action == WAITER_SERVE) {
            serveCustomer(customer);
        }
    }

    //called by exectute watier choice, 
    // find avialble table, seat cuomser and remove them from waiting queue
    private void seatCustomer(Customer customer) {
        Table table = restaurant.findAvailableTable().orElseThrow(
                () -> new GameRuleException("No table is available."));
        waiter.seatCustomer(customer, table, currentTurn);
        restaurant.removeFromWaitingQueue(customer);
        cli.displayMessage("The waiter seated " + customer.getName()
                + " at table " + table.getTableId() + ".");
    }

    // called by exectute watier choice, 
    //determine the customer's menu choic (detemirnstic thing) e and ask the waiter to create an order.
    private void takeCustomerOrder(Customer customer) {
        MenuItem item = restaurant.selectMenuItem(customer);
        Order order = waiter.takeOrder(customer, item, currentTurn);
        cli.displayMessage(customer.getName() + " ordered " + item.getName()
                + " (" + Money.format(order.getBillCents())
                + ", " + item.getPreparationUnits() + " preparation unit"
                + (item.getPreparationUnits() == 1 ? "" : "s")
                + "). Order id " + order.getOrderId() + ".");
    }

    //serve a ready order and cusomter is then served
    private void serveCustomer(Customer customer) {
        waiter.serveOrder(customer, currentTurn);
        restaurant.recordService();
        cli.displayMessage("The waiter served " + customer.getName()
                + ". The customer is now eating.");
    }

    
    private void abandonCustomer(Customer customer) {

        //check if order is not completed or paid and then sets boolean to cancel 
        Order order = customer.getOrder().orElse(null);
        boolean cancelledOrder = customer.cancelUnfinishedOrder();


        //remove from waiting queue or the table
        String tableNote = "";
        if (customer.getStatus() == CustomerStatus.WAITING) {
            restaurant.removeFromWaitingQueue(customer);
        } else if (customer.getStatus() == CustomerStatus.SEATED) {
            Table table = restaurant.releaseTableOf(customer).orElse(null);
            if (table != null) {
                tableNote = " Table " + table.getTableId() + " is free.";
            }
        }

        //updates the 2 statuses
        customer.leave();
        restaurant.recordAbandonment();

        //finally display
        String orderNote = "";
        if (cancelledOrder) {
            orderNote = " Order " + order.getOrderId() + " was cancelled.";
        }
        cli.displayMessage(customer.getName()
                + " left unhappy (satisfaction 0)." + orderNote + tableNote);
    }

    // Builds the list of chef actions for orders needing preparation.
    // Only placed or preparing orders are displayed.
    private ArrayList<String> chefActionLines() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("0. Wait");
        List<Customer> customers = restaurant.getCustomers();
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            Order order = customer.getOrder().orElse(null);
            if (order == null) {
                continue;
            }
            if (order.getStatus() != OrderStatus.PLACED
                    && order.getStatus() != OrderStatus.PREPARING) {
                continue;
            }
            lines.add(order.getOrderId() + ". Prepare order " + order.getOrderId()
                    + " for " + customer.getName()
                    + ": " + order.getMenuItem().getName()
                    + " (" + order.getPreparedUnits() + "/"
                    + order.getMenuItem().getPreparationUnits() + ")");
        }
        return lines;
    }
}
