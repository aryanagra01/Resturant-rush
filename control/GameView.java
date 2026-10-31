package control;

import entities.customer.Customer;
import entities.customer.Order;
import entities.pricing.HappyHour;
import entities.restaurant.Restaurant;
import entities.restaurant.Table;
import entities.staff.Staff;
import java.util.List;

// What the controller needs from a user interface: show the game, report events
// and read the manager's choices. GameCLI is the command-line implementation.
public interface GameView {

    // Shows the rules, customer types, victory requirements and menu.
    void displayWelcome(Restaurant restaurant, int maxTurns);

    // Shows the start-of-turn state: stats, queue, tables, customers and available tasks.
    void displayGameState(Restaurant restaurant, int turn, int maxTurns);

    // Shows the end-of-turn results.
    void displayTurnResults(Restaurant restaurant, int turn, int maxTurns);

    // Shows victory or defeat and the final statistics.
    void displayFinalResults(Restaurant restaurant, int maxTurns);

    // Waiter choices. 0 waits, 1 seats, 2 takes an order and 3 serves.
    int WAITER_WAIT = 0;
    int WAITER_SEAT = 1;
    int WAITER_TAKE_ORDER = 2;
    int WAITER_SERVE = 3;

    // Reads one waiter action, asking again until the choice is 0 to 3.
    int readWaiterAction();

    // Shows the orders the chef can work on and reads an order id. 0 means wait.
    int readChefChoice(List<Customer> customersWithOrdersToPrepare);

    // Reads a whole number, asking again if the input is not one.
    int readInt(String prompt);

    // Asks whether to switch on Happy Hour now. True means yes.
    boolean readHappyHourChoice();

    // Shows why a choice was refused.
    void displayError(String message);

    void showArrival(Customer customer);

    void showHappyHourStarted(HappyHour happyHour);

    // A waiting customer recovered satisfaction during Happy Hour.
    void showRecovered(Customer customer, int gained);

    void showWaits(Staff staff);

    void showSeated(Customer customer, Table table);

    void showOrderTaken(Customer customer, Order order);

    void showPrepared(Order order);

    void showServed(Customer customer);

    void showFinishedEating(Customer customer);

    void showPayment(Customer customer);

    // A customer left unhappy. freedTable is null if they were still in the queue.
    void showDeparture(Customer customer, boolean orderCancelled, Table freedTable);
}
