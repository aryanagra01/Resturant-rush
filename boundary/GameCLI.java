package boundary;

import control.GameView;
import control.WaiterAction;
import entities.customer.Customer;
import entities.customer.CustomerType;
import entities.customer.Order;
import entities.menu.MenuItem;
import entities.restaurant.GameStats;
import entities.restaurant.Restaurant;
import entities.restaurant.Table;
import entities.staff.Staff;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Command-line implementation of {@link GameView}.
 *
 * <p>It holds no game rules: it asks the domain objects (e.g.
 * {@code customer.canBeServed()}) and only turns the answers into text. Input and
 * output are passed in, so tests can drive it with a script and capture the text.
 */
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

    /** For example {@code new GameCLI(new Scanner(System.in), System.out)}. */
    public GameCLI(Scanner in, PrintStream out) {
        this.in = Objects.requireNonNull(in, "Input cannot be null.");
        this.out = Objects.requireNonNull(out, "Output cannot be null.");
    }

    // One formatted line of output. Used for every line the game prints.
    private void say(String format, Object... args) {
        out.println(String.format(format, args));
    }

    // ---------- screens ----------

    @Override
    public void displayWelcome(Restaurant restaurant, int maxTurns) {
        GameStats stats = restaurant.getStats();
        say(WELCOME, maxTurns);
        for (CustomerType type : CustomerType.values()) {
            say("- %s: %s", type.getLabel(), type.describeRules());
        }
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

        say("%nWaiting queue:");
        printCustomers(restaurant.getWaitingCustomers(), "(empty)", false);

        say("%nTables:");
        for (Table table : restaurant.getTables()) {
            say("  Table %d: %s", table.getTableId(),
                    table.getCustomer().map(c -> customerLine(c, true)).orElse("Free"));
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

    // ---------- reading the manager's choices ----------

    @Override
    public WaiterAction readWaiterAction() {
        while (true) {
            say("%nWaiter actions:");
            for (WaiterAction action : WaiterAction.values()) {
                say("%d. %s", action.getChoice(), action.getMenuLabel());
            }
            Optional<WaiterAction> action =
                    WaiterAction.fromChoice(readInt("Choose a waiter action: "));
            if (action.isPresent()) {
                return action.get();
            }
            displayError(WaiterAction.invalidChoiceMessage());
        }
    }

    @Override
    public int readChefChoice(List<Customer> customersWithOrdersToPrepare) {
        say("%nChef actions:");
        say("0. Wait");
        for (Customer customer : customersWithOrdersToPrepare) {
            Order order = orderOf(customer);
            say("%d. Prepare order %d for %s: %s (%s)", order.getOrderId(),
                    order.getOrderId(), customer.getName(), dish(order), progress(order));
        }
        return readInt("Choose a chef action (0 = wait, or an order id): ");
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

    // ---------- event messages (the controller reports, the CLI words it) ----------

    @Override
    public void displayError(String message) {
        say("Error: %s", message);
    }

    @Override
    public void showArrival(Customer c) {
        say("%s arrived and joined the waiting queue.", c.getName());
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

    // ---------- reusable formatting helpers ----------

    private void printCustomers(List<Customer> list, String emptyText, boolean details) {
        if (list.isEmpty()) {
            say("  %s", emptyText);
        }
        for (Customer customer : list) {
            say("  %s", customerLine(customer, details));
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

    // "Customer 2 (VIP) | Seated | Satisfaction: 90 | Order 2: Burger $13.50 (Placed, 0/1)"
    // The type label comes from the customer object, not from instanceof checks.
    private static String customerLine(Customer c, boolean details) {
        String name = c.getName() + " (" + c.getType().getLabel() + ")";
        String satisfaction = "Satisfaction: " + c.getSatisfaction();
        if (!details) {
            return name + " | " + satisfaction;
        }
        String orderText = c.getOrder()
                .map(order -> String.format("Order %d: %s %s (%s, %s)", order.getOrderId(),
                        dish(order), formatMoney(order.getBillCents()),
                        status(order.getStatus()), progress(order)))
                .orElse("No order");
        return String.join(" | ", name, status(c.getStatus()), satisfaction, orderText);
    }

    // The customer's order; only called when the customer is known to have one.
    private static Order orderOf(Customer c) {
        return c.getOrder().orElseThrow();
    }

    private static String dish(Order order) {
        return order.getMenuItem().getName();
    }

    // "1/2"
    private static String progress(Order order) {
        return order.getPreparedUnits() + "/" + order.getMenuItem().getPreparationUnits();
    }

    // Formats cents as dollars, e.g. 1350 becomes "$13.50".
    private static String formatMoney(int cents) {
        int dollars = cents / 100;
        int remainder = Math.abs(cents % 100);
        return String.format("$%d.%02d", dollars, remainder);
    }

    // "1 preparation unit", "2 preparation units"
    private static String units(int count) {
        return count + " preparation unit" + (count == 1 ? "" : "s");
    }

    // Works for both CustomerStatus and OrderStatus: READY_TO_PAY -> "Ready to pay".
    private static String status(Enum<?> status) {
        String words = status.name().toLowerCase().replace("_", " ");
        return Character.toUpperCase(words.charAt(0)) + words.substring(1);
    }

    // Eligibility comes from the customer objects, the same methods the Waiter
    // uses to validate, so these hints always match the rules.
    private static String describeWaiterTasks(Restaurant restaurant) {
        Stream<String> seating = restaurant.findAvailableTable().isPresent()
                ? restaurant.getWaitingCustomers().stream().map(c -> "seat " + c.getName())
                : Stream.empty();
        Stream<String> ordersAndServing = restaurant.getCustomers().stream()
                .filter(c -> c.canPlaceOrder() || c.canBeServed())
                .map(c -> (c.canPlaceOrder() ? "take order from " : "serve ") + c.getName());
        return waitOr(Stream.concat(seating, ordersAndServing));
    }

    private static String describeChefTasks(Restaurant restaurant) {
        return waitOr(restaurant.getCustomersWithOrdersToPrepare().stream()
                .map(GameCLI::orderOf)
                .map(o -> String.format("prepare order %d (%s %s)",
                        o.getOrderId(), dish(o), progress(o))));
    }

    // "Wait; task; task" - every staff member can always wait.
    private static String waitOr(Stream<String> tasks) {
        return Stream.concat(Stream.of("Wait"), tasks).collect(Collectors.joining("; "));
    }
}
