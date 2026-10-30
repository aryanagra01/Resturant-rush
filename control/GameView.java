package control;

import entities.customer.Customer;
import entities.customer.Order;
import entities.restaurant.Restaurant;
import entities.restaurant.Table;
import entities.staff.Staff;
import java.util.List;

/**
 * What the controller needs from a user interface: show the game, report events,
 * and read the manager's choices.
 *
 * <p>The controller depends on this interface, not on a concrete UI (dependency
 * inversion). It lives in {@code control} because the controller owns it; the
 * boundary implements it ({@link boundary.GameCLI}), so {@code control} never
 * depends on {@code boundary}.
 */
public interface GameView {

    // ---------- screens ----------

    /** Rules, customer types, victory targets and menu. */
    void displayWelcome(Restaurant restaurant, int maxTurns);

    /** Start-of-turn display: stats, queue, tables, customers and available tasks. */
    void displayGameState(Restaurant restaurant, int turn, int maxTurns);

    /** End-of-turn results. */
    void displayTurnResults(Restaurant restaurant, int turn, int maxTurns);

    /** Victory or defeat and the final statistics. */
    void displayFinalResults(Restaurant restaurant, int maxTurns);

    // ---------- reading the manager's choices ----------

    /** Reads one waiter action, asking again until it is on the menu. */
    int readWaiterAction();

    /** Shows the orders the chef can work on and reads an order id (0 = wait). */
    int readChefChoice(List<Customer> customersWithOrdersToPrepare);

    /** Reads a whole number, asking again if the input is not one. */
    int readInt(String prompt);

    // ---------- events ----------

    /** Shows why a choice was refused. */
    void displayError(String message);

    void showArrival(Customer customer);

    void showWaits(Staff staff);

    void showSeated(Customer customer, Table table);

    void showOrderTaken(Customer customer, Order order);

    void showPrepared(Order order);

    void showServed(Customer customer);

    void showFinishedEating(Customer customer);

    void showPayment(Customer customer);

    /**
     * A customer left unhappy.
     *
     * @param freedTable the table they left, or null if they were still in the queue
     */
    void showDeparture(Customer customer, boolean orderCancelled, Table freedTable);
}
