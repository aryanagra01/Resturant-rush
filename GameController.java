import java.util.ArrayList;

// Coordinates the 18 turns. Specific objects apply their own state changes.
public class GameController {
    public static final int MAX_TURNS = 18;
    private static final int[] ARRIVAL_TURNS = {1, 4, 7, 10, 13, 16};

    private static final int WAITER_WAIT = 0;
    private static final int WAITER_SEAT = 1;
    private static final int WAITER_ORDER = 2;
    private static final int WAITER_SERVE = 3;

    private final Restaurant restaurant;
    private final GameCLI cli;
    private int currentTurn;
    private int nextCustomerId;

    public GameController(Restaurant restaurant, GameCLI cli) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null.");
        }
        if (cli == null) {
            throw new IllegalArgumentException("Game CLI cannot be null.");
        }

        this.restaurant = restaurant;
        this.cli = cli;
        nextCustomerId = 1;
    }

    // Runs the 18 turns, then displays the final result.
    public void startGame() {
        cli.displayWelcome(restaurant);
        for (currentTurn = 1; currentTurn <= MAX_TURNS; currentTurn++) {
            playTurn();
        }
        cli.displayFinalResults(restaurant);
    }

    // Processes arrivals, manager choices, kitchen work, eating and payment.
    // Then decreases satisfaction and displays the end-of-turn result.
    private void playTurn() {
        processArrivals();
        cli.displayGameState(restaurant, currentTurn);
        processManagerChoices();
        processChefChoices();
        processEatingAndPayment();
        processSatisfactionAndAbandonment();
        cli.displayTurnResults(restaurant, currentTurn);
    }

    // One scheduled customer on turns 1, 4, 7, 10, 13 and 16.
    private void processArrivals() {
        for (int i = 0; i < ARRIVAL_TURNS.length; i++) {
            if (ARRIVAL_TURNS[i] != currentTurn) {
                continue;
            }

            Customer customer = new Customer(nextCustomerId, "Customer " + nextCustomerId);
            nextCustomerId++;
            restaurant.addCustomer(customer);
            cli.displayMessage(customer.getName()
                    + " arrived and joined the waiting queue.");
        }
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
            } catch (IllegalArgumentException | IllegalStateException ex) {
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

            Order order = findOrder(choice);
            if (order == null) {
                cli.displayError("No order has id " + choice + ".");
                continue;
            }

            try {
                restaurant.getChef().prepareOrder(order, currentTurn);
                String readyNote = order.isReady() ? " The order is ready." : "";
                cli.displayMessage("The chef prepared one unit of "
                        + order.getMenuItem().getName()
                        + " for order " + order.getOrderId()
                        + " (" + order.getPreparedUnits() + "/"
                        + order.getMenuItem().getPreparationUnits() + ")."
                        + readyNote);
                return;
            } catch (IllegalArgumentException | IllegalStateException ex) {
                cli.displayError(ex.getMessage());
            }
        }
    }

    // Handles eating and payment in the correct turn order.
    // Customers who finish eating this turn can only pay next turn.
    private void processEatingAndPayment() {
        ArrayList<Customer> readyCustomers = getReadyToPayCustomers();
        processEating();
        processPayments(readyCustomers);
    }

    // Finds customers who were already ready to pay.
    private ArrayList<Customer> getReadyToPayCustomers() {
        ArrayList<Customer> readyCustomers = new ArrayList<>();
        for (int i = 0; i < restaurant.getCustomers().size(); i++) {
            Customer customer = restaurant.getCustomers().get(i);
            if (customer.getStatus() == CustomerStatus.READY_TO_PAY) {
                readyCustomers.add(customer);
            }
        }
        return readyCustomers;
    }

    // Customers served in an earlier turn finish eating.
    private void processEating() {
        for (int i = 0; i < restaurant.getCustomers().size(); i++) {
            Customer customer = restaurant.getCustomers().get(i);
            if (customer.getStatus() == CustomerStatus.EATING
                    && currentTurn > customer.getServedTurn()) {
                customer.finishEating(currentTurn);
                cli.displayMessage(customer.getName()
                        + " finished eating and is ready to pay.");
            }
        }
    }

    // Cashier processes at most one payment, oldest first.
    // If payment fails, the next oldest eligible customer is attempted.
    private void processPayments(ArrayList<Customer> readyCustomers) {
        while (!readyCustomers.isEmpty()
                && restaurant.getCashier().canProcessPayment(currentTurn)) {
            Customer oldestCustomer = readyCustomers.get(0);
            for (int i = 1; i < readyCustomers.size(); i++) {
                Customer customer = readyCustomers.get(i);
                if (customer.getServedTurn() < oldestCustomer.getServedTurn()
                        || (customer.getServedTurn() == oldestCustomer.getServedTurn()
                        && customer.getCustomerId() < oldestCustomer.getCustomerId())) {
                    oldestCustomer = customer;
                }
            }

            try {
                int satisfaction = oldestCustomer.getSatisfaction();
                int billCents = oldestCustomer.getOrder().getBillCents();
                restaurant.getCashier().processPayment(
                        oldestCustomer, restaurant, currentTurn);
                cli.displayMessage(oldestCustomer.getName() + " paid "
                        + GameCLI.formatMoney(billCents)
                        + ". Satisfaction recorded: " + satisfaction + ".");
                break;
            } catch (IllegalArgumentException | IllegalStateException ex) {
                cli.displayError(ex.getMessage());
                readyCustomers.remove(oldestCustomer);
            }
        }
    }

    // Waiting and seated customers lose satisfaction. At 0 they leave.
    private void processSatisfactionAndAbandonment() {
        for (int i = 0; i < restaurant.getCustomers().size(); i++) {
            Customer customer = restaurant.getCustomers().get(i);
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

        Customer customer = restaurant.findCustomer(customerId);
        if (customer == null) {
            throw new IllegalArgumentException("No customer has id " + customerId + ".");
        }

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
        Table table = restaurant.getAvailableTable();
        if (table == null) {
            throw new IllegalStateException("No table is available.");
        }
        restaurant.getWaiter().seatCustomer(customer, table, currentTurn);
        restaurant.removeFromWaitingQueue(customer);
        cli.displayMessage("The waiter seated " + customer.getName()
                + " at table " + table.getTableId() + ".");
    }

    // called by exectute watier choice, 
    //determine the customer's menu choic (detemirnstic thing) e and ask the waiter to create an order.
    private void takeCustomerOrder(Customer customer) {
        MenuItem item = restaurant.selectMenuItem(customer);
        Order order = restaurant.getWaiter().takeOrder(customer, item, currentTurn);
        cli.displayMessage(customer.getName() + " ordered " + item.getName()
                + " (" + GameCLI.formatMoney(item.getPriceCents())
                + ", " + item.getPreparationUnits() + " preparation unit"
                + (item.getPreparationUnits() == 1 ? "" : "s")
                + "). Order id " + order.getOrderId() + ".");
    }

    //serve a ready order and cusomter is then served
    private void serveCustomer(Customer customer) {
        restaurant.getWaiter().serveOrder(customer, currentTurn);
        restaurant.recordService();
        cli.displayMessage("The waiter served " + customer.getName()
                + ". The customer is now eating.");
    }

    
    private void abandonCustomer(Customer customer) {

        //check if order is not completed or paid and then sets boolean to cancel 
        Order order = customer.getOrder();
        boolean cancelledOrder = false;
        if (order != null
                && (order.getStatus() == OrderStatus.PLACED
                || order.getStatus() == OrderStatus.PREPARING
                || order.getStatus() == OrderStatus.READY)) {
            order.cancel();
            cancelledOrder = true;
        }


        //remove from waiting queue or the table
        String tableNote = "";
        if (customer.getStatus() == CustomerStatus.WAITING) {
            restaurant.removeFromWaitingQueue(customer);
        } else if (customer.getStatus() == CustomerStatus.SEATED) {
            Table table = restaurant.findTableByCustomer(customer);
            if (table != null) {
                int tableId = table.getTableId();
                table.release();
                tableNote = " Table " + tableId + " is free.";
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
        for (int i = 0; i < restaurant.getCustomers().size(); i++) {
            Customer customer = restaurant.getCustomers().get(i);
            Order order = customer.getOrder();
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

    // Finds an order using its ID.
    // Returns null if no matching order exists.
    private Order findOrder(int orderId) {
        for (int i = 0; i < restaurant.getCustomers().size(); i++) {
            Customer customer = restaurant.getCustomers().get(i);
            Order order = customer.getOrder();
            if (order != null && order.getOrderId() == orderId) {
                return order;
            }
        }
        return null;
    }
}
