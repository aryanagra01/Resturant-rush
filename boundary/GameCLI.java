package boundary;

import control.GameView;
import entities.customer.CriticCustomer;
import entities.customer.Customer;
import entities.customer.Order;
import entities.customer.RegularCustomer;
import entities.customer.VIPCustomer;
import entities.menu.MenuItem;
import entities.pricing.HappyHour;
import entities.restaurant.GameStats;
import entities.restaurant.Restaurant;
import entities.restaurant.Table;
import entities.staff.Staff;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

// Command-line implementation of GameView.
// It does not apply game rules. It asks the domain objects and turns the answers into text.
public class GameCLI implements GameView {
    private static final String WELCOME = """
            ========================================
            Restaurant Rush
            ========================================
            You are the manager. The game runs for %d turns.
            Each turn the waiter and the chef can each do one task, or wait.
            The cashier automatically takes one eligible payment per turn.
            Customers choose their own food.
            An invalid choice is explained, and you can choose again.

            Customer types:""";

    private static final String TARGETS = """

            Win at the end of turn %d if all three are true:
            - at least %d paid customers
            - revenue at least %s
            - average satisfaction of paid customers at least %d

            Menu:""";

    private final Scanner in;
    private final PrintStream out;

    // For example, new GameCLI(new Scanner(System.in), System.out).
    public GameCLI(Scanner in, PrintStream out) {
        this.in = Objects.requireNonNull(in, "Input cannot be null.");
        this.out = Objects.requireNonNull(out, "Output cannot be null.");
    }

    // Prints one formatted line. Used for every line the game prints.
    private void say(String format, Object... args) {
        out.println(String.format(format, args));
    }

    @Override
    public void displayWelcome(Restaurant restaurant, int maxTurns) {
        GameStats stats = restaurant.getStats();
        say(WELCOME, maxTurns);
        say("- Regular: loses %d satisfaction per turn while waiting or seated, pays full price, and chooses food from their customer id.",
                RegularCustomer.SATISFACTION_LOSS);
        say("- VIP: loses %d satisfaction per turn while waiting or seated, and receives a %d%% discount.",
                VIPCustomer.SATISFACTION_LOSS, VIPCustomer.DISCOUNT_PERCENT);
        say("- Critic: loses %d satisfaction per turn while waiting or seated, prefers %s, and gains %d satisfaction if served within %d turns of ordering. Satisfaction cannot exceed 100.",
                CriticCustomer.SATISFACTION_LOSS, CriticCustomer.PREFERRED_DISH,
                CriticCustomer.FAST_SERVICE_BONUS, CriticCustomer.FAST_SERVICE_TURNS);
        say("%nHappy Hour: once per game, at the start of a turn, you can switch it on for"
                + " that turn and the next. Orders taken then get %d%% off, after any VIP"
                + " discount.", HappyHour.DISCOUNT_PERCENT);
        say(TARGETS, maxTurns, stats.getTargetPaidCustomers(),
                formatMoney(stats.getTargetRevenueCents()), stats.getTargetAverageSatisfaction());
        List<MenuItem> menu = restaurant.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.get(i);
            say("- %d. %s %s, %s", i, item.getName(),
                    formatMoney(item.getPriceCents()), units(item.getPreparationUnits()));
        }
        say("");
    }

    @Override
    public void displayGameState(Restaurant restaurant, int turn, int maxTurns) {
        GameStats stats = restaurant.getStats();
        say("%n---------- Turn %d of %d ----------", turn, maxTurns);
        printRevenueAndAverage(stats, false);
        say("Paid: %d | Served: %d | Unhappy departures: %d", stats.getPaidCount(),
                stats.getServedCount(), stats.getUnhappyDepartures());
        say("Happy Hour: %s", happyHourStatus(restaurant.getHappyHour()));

        say("%nWaiting queue:");
        printCustomers(restaurant.getWaitingCustomers(), "(empty)", false);

        say("%nTables:");
        List<Table> tables = restaurant.getTables();
        for (int i = 0; i < tables.size(); i++) {
            Table table = tables.get(i);
            String occupant = "Free";
            Optional<Customer> seated = table.getCustomer();
            if (seated.isPresent()) {
                occupant = customerLine(seated.get(), true);
            }
            say("  Table %d: %s", table.getTableId(), occupant);
        }

        say("%nCustomers:");
        printCustomers(restaurant.getCustomers(), "(none)", true);

        say("%nAvailable staff tasks:");
        say("  Waiter: %s", describeWaiterTasks(restaurant));
        say("  Chef: %s", describeChefTasks(restaurant));
        say("  Cashier: Automatically collects one eligible payment per turn.");
    }

    @Override
    public void displayTurnResults(Restaurant restaurant, int turn, int maxTurns) {
        say("%n----- End of turn %d -----", turn);
        printServiceCounts(restaurant.getStats());
        printRevenueAndAverage(restaurant.getStats(), false);
        say("Turns used: %d / %d", turn, maxTurns);
    }

    @Override
    public void displayFinalResults(Restaurant restaurant, int maxTurns) {
        GameStats stats = restaurant.getStats();
        String result = restaurant.hasWon() ? "VICTORY" : "DEFEAT";
        say("%n========== Final result: %s ==========", result);
        say("Paid customers: %d (need %d)", stats.getPaidCount(),
                stats.getTargetPaidCustomers());
        printServiceCounts(stats);
        printRevenueAndAverage(stats, true);
        say("Turns used: %d", maxTurns);
        say("Result: %s", result);
    }

    @Override
    public int readWaiterAction() {
        while (true) {
            say("%nWaiter actions:");
            say("%d. Wait", WAITER_WAIT);
            say("%d. Seat a waiting customer", WAITER_SEAT);
            say("%d. Take an order from a seated customer", WAITER_TAKE_ORDER);
            say("%d. Serve an order that is ready", WAITER_SERVE);
            int choice = readInt("Choose a waiter action: ");
            if (choice == WAITER_WAIT || choice == WAITER_SEAT
                    || choice == WAITER_TAKE_ORDER || choice == WAITER_SERVE) {
                return choice;
            }
            displayError("Choose 0 to wait, 1 to seat, 2 to take an order, or 3 to serve.");
        }
    }

    @Override
    public int readChefChoice(List<Customer> customersWithOrdersToPrepare) {
        say("%nChef actions:");
        say("0. Wait");
        for (int i = 0; i < customersWithOrdersToPrepare.size(); i++) {
            Customer customer = customersWithOrdersToPrepare.get(i);
            Order order = orderOf(customer);
            say("%d. Prepare order %d for %s: %s (%s)", order.getOrderId(),
                    order.getOrderId(), customer.getName(), dish(order), progress(order));
        }
        return readInt("Choose a chef action (0 = wait, or an order id): ");
    }

    @Override
    public boolean readHappyHourChoice() {
        while (true) {
            int choice = readInt("Start Happy Hour now? (1 = yes, 0 = no): ");
            if (choice == 0 || choice == 1) {
                return choice == 1;
            }
            displayError("Enter 1 to start Happy Hour or 0 to keep it for later.");
        }
    }

    @Override
    public int readInt(String prompt) {
        while (true) {
            out.print(prompt);
            out.flush();
            String line = in.nextLine().trim();
            if (line.isEmpty()) {
                say("");
                continue;
            }
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException ex) {
                displayError("Please enter a whole number.");
            }
        }
    }

    @Override
    public void displayError(String message) {
        say("Error: %s", message);
    }

    @Override
    public void showArrival(Customer c) {
        say("%s arrived and joined the waiting queue.", c.getName());
    }

    @Override
    public void showHappyHourStarted(HappyHour happyHour) {
        say("Happy Hour is on for this turn and the next: new orders get %d%% off.",
                HappyHour.DISCOUNT_PERCENT);
    }

    @Override
    public void showWaits(Staff staff) {
        say("The %s waits.", staff.getName().toLowerCase());
    }

    @Override
    public void showSeated(Customer c, Table table) {
        say("The waiter seated %s at table %d.", c.getName(), table.getTableId());
    }

    @Override
    public void showOrderTaken(Customer c, Order order) {
        say("%s ordered %s (%s, %s). Order id %d.", c.getName(), dish(order),
                formatMoney(order.getBillCents()),
                units(order.getMenuItem().getPreparationUnits()), order.getOrderId());
    }

    @Override
    public void showPrepared(Order order) {
        say("The chef prepared one unit of %s for order %d (%s).%s", dish(order),
                order.getOrderId(), progress(order),
                order.isReady() ? " The order is ready." : "");
    }

    @Override
    public void showServed(Customer c) {
        say("The waiter served %s. The customer is now eating.", c.getName());
    }

    @Override
    public void showFinishedEating(Customer c) {
        say("%s finished eating and is ready to pay.", c.getName());
    }

    @Override
    public void showPayment(Customer c) {
        say("%s paid %s. Satisfaction recorded: %d.", c.getName(),
                formatMoney(orderOf(c).getBillCents()), c.getSatisfaction());
    }

    @Override
    public void showDeparture(Customer c, boolean orderCancelled, Table freedTable) {
        String orderNote = orderCancelled
                ? " Order " + orderOf(c).getOrderId() + " was cancelled." : "";
        String tableNote = freedTable != null
                ? " Table " + freedTable.getTableId() + " is free." : "";
        say("%s left unhappy (satisfaction 0).%s%s", c.getName(), orderNote, tableNote);
    }

    private void printCustomers(List<Customer> list, String emptyText, boolean details) {
        if (list.isEmpty()) {
            say("  %s", emptyText);
        }
        for (int i = 0; i < list.size(); i++) {
            say("  %s", customerLine(list.get(i), details));
        }
    }

    private void printServiceCounts(GameStats stats) {
        say("Served: %d", stats.getServedCount());
        say("Unhappy departures: %d", stats.getUnhappyDepartures());
    }

    private void printRevenueAndAverage(GameStats stats, boolean showTargets) {
        String average = stats.hasPaidCustomers()
                ? String.format("%.1f", stats.getAverageSatisfaction()) : "N/A";
        say("Revenue: %s%s", formatMoney(stats.getRevenueCents()), showTargets
                ? " (need " + formatMoney(stats.getTargetRevenueCents()) + ")" : "");
        say("Average satisfaction of paid customers: %s%s", average, showTargets
                ? " (need " + stats.getTargetAverageSatisfaction() + ")" : "");
    }

    // Example: Customer 2 (VIP) | Seated | Satisfaction: 90 | Order 2: Burger $13.50 (Placed, 0/1)
    // The type name comes from the customer object.
    private static String customerLine(Customer c, boolean details) {
        String name = c.getName() + " (" + c.getType() + ")";
        String satisfaction = "Satisfaction: " + c.getSatisfaction();
        if (!details) {
            return name + " | " + satisfaction;
        }
        String orderText = "No order";
        Optional<Order> found = c.getOrder();
        if (found.isPresent()) {
            Order order = found.get();
            orderText = String.format("Order %d: %s %s (%s, %s)", order.getOrderId(),
                    dish(order), formatMoney(order.getBillCents()),
                    status(order.getStatus()), progress(order));
        }
        return String.join(" | ", name, status(c.getStatus()), satisfaction, orderText);
    }

    // The customer's order. Only called when the customer is known to have one.
    private static Order orderOf(Customer c) {
        return c.getOrder().orElseThrow();
    }

    // For example, "available (can be used once)", "on, last turn" or "used".
    private static String happyHourStatus(HappyHour happyHour) {
        if (happyHour.isActive()) {
            return happyHour.getTurnsLeft() == 1 ? "on, last turn"
                    : "on, " + happyHour.getTurnsLeft() + " turns left (this turn and the next)";
        }
        return happyHour.isAvailable() ? "available (can be used once)" : "used";
    }

    private static String dish(Order order) {
        return order.getMenuItem().getName();
    }

    // For example, "1/2".
    private static String progress(Order order) {
        return order.getPreparedUnits() + "/" + order.getMenuItem().getPreparationUnits();
    }

    // Formats cents as dollars. For example, 1350 becomes "$13.50".
    private static String formatMoney(int cents) {
        int dollars = cents / 100;
        int remainder = Math.abs(cents % 100);
        return String.format("$%d.%02d", dollars, remainder);
    }

    // For example, "1 preparation unit" or "2 preparation units".
    private static String units(int count) {
        return count + " preparation unit" + (count == 1 ? "" : "s");
    }

    // Turns READY_TO_PAY into "Ready to pay". Used for both customer and order status.
    private static String status(Enum<?> status) {
        String words = status.name().toLowerCase().replace("_", " ");
        return Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }

    // Uses the same customer checks as the waiter, so the hints match the rules.
    // Example: Wait; seat Customer 1; take order from Customer 2
    private static String describeWaiterTasks(Restaurant restaurant) {
        StringBuilder text = new StringBuilder("Wait");
        if (restaurant.findAvailableTable().isPresent()) {
            List<Customer> waiting = restaurant.getWaitingCustomers();
            for (int i = 0; i < waiting.size(); i++) {
                text.append("; seat ").append(waiting.get(i).getName());
            }
        }
        List<Customer> customers = restaurant.getCustomers();
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            if (customer.canPlaceOrder()) {
                text.append("; take order from ").append(customer.getName());
            } else if (customer.canBeServed()) {
                text.append("; serve ").append(customer.getName());
            }
        }
        return text.toString();
    }

    // Example: Wait; prepare order 1 (Pasta 1/2)
    private static String describeChefTasks(Restaurant restaurant) {
        StringBuilder text = new StringBuilder("Wait");
        List<Customer> preparing = restaurant.getCustomersWithOrdersToPrepare();
        for (int i = 0; i < preparing.size(); i++) {
            Order order = orderOf(preparing.get(i));
            text.append("; prepare order ").append(order.getOrderId())
                    .append(" (").append(dish(order)).append(" ")
                    .append(progress(order)).append(")");
        }
        return text.toString();
    }
}
