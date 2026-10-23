package boundary;

import entities.customer.CriticCustomer;
import entities.customer.Customer;
import entities.customer.CustomerStatus;
import entities.customer.Order;
import entities.customer.OrderStatus;
import entities.customer.VIPCustomer;
import entities.menu.MenuItem;
import entities.restaurant.Restaurant;
import entities.restaurant.Table;
import entities.restaurant.VictoryTargets;
import java.util.ArrayList;
import java.util.Scanner;
import utility.Money;

// Boundary class. Displays the restaurant and reads manager input.
// Game rules are handled by GameController and the domain classes.
public class GameCLI {
    private final Scanner scanner;

    public GameCLI() {
        scanner = new Scanner(System.in);
    }

    // Displays the game rules, victory conditions and menu.
    public void displayWelcome(Restaurant restaurant, int maxTurns) {
        VictoryTargets targets = restaurant.getStats().getTargets();
        System.out.println("========================================");
        System.out.println("Restaurant Rush");
        System.out.println("========================================");
        System.out.println("You are the manager. The game runs for " + maxTurns + " turns.");
        System.out.println("Each turn the waiter and the chef can each do one task, or wait.");
        System.out.println("The cashier automatically takes one eligible payment per turn.");
        System.out.println("Customers choose their own food.");
        System.out.println("An invalid choice is explained, and you can choose again.");
        System.out.println();
        System.out.println("Customer types:");
        System.out.println("- Regular: loses 8 satisfaction per turn while waiting or seated, pays full price, and chooses food from their customer id.");
        System.out.println("- VIP: loses 5 satisfaction per turn while waiting or seated, and receives a 10% discount.");
        System.out.println("- Critic: loses 12 satisfaction per turn while waiting or seated, always orders Pasta, and gains 10 satisfaction if served within 2 turns of ordering. Satisfaction cannot exceed 100.");
        System.out.println();
        System.out.println("Win at the end of turn " + maxTurns + " if all three are true:");
        System.out.println("- at least " + targets.getPaidCustomers() + " paid customers");
        System.out.println("- revenue at least "
                + Money.format(targets.getRevenueCents()));
        System.out.println("- average satisfaction of paid customers at least "
                + targets.getAverageSatisfaction());
        System.out.println();
        System.out.println("Menu:");

        ArrayList<MenuItem> menu = restaurant.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.get(i);
            System.out.println("- " + i + ". " + item.getName()
                    + " " + Money.format(item.getPriceCents())
                    + ", " + item.getPreparationUnits() + " preparation unit"
                    + (item.getPreparationUnits() == 1 ? "" : "s"));
        }
        System.out.println();
    }

    // Displays the restaurant's state at the start of a turn.
    public void displayGameState(Restaurant restaurant, int turn, int maxTurns) {
        System.out.println();
        System.out.println("---------- Turn " + turn + " of "
                + maxTurns + " ----------");
        System.out.println("Revenue: " + Money.format(restaurant.getStats().getRevenueCents()));
        System.out.println("Average satisfaction of paid customers: "
                + formatAverage(restaurant));
        System.out.println("Paid: " + restaurant.getStats().getPaidCount()
                + " | Served: " + restaurant.getStats().getServedCount()
                + " | Unhappy departures: " + restaurant.getStats().getUnhappyDepartures());

        displayWaitingQueue(restaurant);
        displayTables(restaurant);
        displayCustomers(restaurant);

        System.out.println();
        System.out.println("Available staff tasks:");
        System.out.println("  Waiter: " + describeWaiterTasks(restaurant));
        System.out.println("  Chef: " + describeChefTasks(restaurant));
        System.out.println("  Cashier: Automatically collects one eligible payment per turn.");
    }

    // Displays customers waiting for a table.
    private void displayWaitingQueue(Restaurant restaurant) {
        System.out.println();
        System.out.println("Waiting queue:");
        ArrayList<Customer> waiting = restaurant.getWaitingCustomers();
        if (waiting.isEmpty()) {
            System.out.println("  (empty)");
        } else {
            for (int i = 0; i < waiting.size(); i++) {
                Customer customer = waiting.get(i);
                System.out.println("  " + customer.getName()
                        + " (" + getCustomerType(customer) + ")"
                        + " | Satisfaction: " + customer.getSatisfaction());
            }
        }
    }

    // Displays free tables and the customers occupying them.
    private void displayTables(Restaurant restaurant) {
        System.out.println();
        System.out.println("Tables:");
        ArrayList<Table> tables = restaurant.getTables();
        for (int i = 0; i < tables.size(); i++) {
            Table table = tables.get(i);
            if (table.isAvailable()) {
                System.out.println("  Table " + table.getTableId() + ": Free");
            } else {
                Customer seated = table.getCustomer();
                System.out.println("  Table " + table.getTableId() + ": "
                        + seated.getName()
                        + " (" + getCustomerType(seated) + ")"
                        + " | " + formatStatus(seated.getStatus().toString())
                        + " | Satisfaction: " + seated.getSatisfaction()
                        + " | " + describeOrder(seated.getOrder().orElse(null)));
            }
        }
    }

    // Displays all customers and their order status.
    private void displayCustomers(Restaurant restaurant) {
        System.out.println();
        System.out.println("Customers:");
        ArrayList<Customer> customers = restaurant.getCustomers();
        if (customers.isEmpty()) {
            System.out.println("  (none)");
        } else {
            for (int i = 0; i < customers.size(); i++) {
                Customer customer = customers.get(i);
                System.out.println("  " + customer.getName()
                        + " (" + getCustomerType(customer) + ")"
                        + " | " + formatStatus(customer.getStatus().toString())
                        + " | Satisfaction: " + customer.getSatisfaction()
                        + " | " + describeOrder(customer.getOrder().orElse(null)));
            }
        }
    }

    // Displays the waiter's available action types.
    public void displayWaiterActions() {
        System.out.println();
        System.out.println("Waiter actions:");
        System.out.println("0. Wait");
        System.out.println("1. Seat a waiting customer");
        System.out.println("2. Take an order from a seated customer");
        System.out.println("3. Serve an order that is ready");
    }

    // Displays the chef's numbered order choices.
    public void displayChefActions(ArrayList<String> actionLines) {
        System.out.println();
        System.out.println("Chef actions:");
        for (int i = 0; i < actionLines.size(); i++) {
            System.out.println(actionLines.get(i));
        }
    }

    // Reads the waiter's chosen action.
    public int getWaiterChoice() {
        return readInt("Choose a waiter action: ");
    }

    // Reads the chef's chosen order ID.
    public int getChefChoice() {
        return readInt("Choose a chef action (0 = wait, or an order id): ");
    }

    // Reads a whole number, asking again if the input is invalid.
    public int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            System.out.flush();
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                System.out.println();
                continue;
            }

            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException ex) {
                displayError("Please enter a whole number.");
            }
        }
    }

    // Displays an error message.
    public void displayError(String message) {
        System.out.println("Error: " + message);
    }

    // Displays a game message.
    public void displayMessage(String message) {
        System.out.println(message);
    }

    // Displays game statistics at the end of a turn.
    public void displayTurnResults(Restaurant restaurant, int turn, int maxTurns) {
        System.out.println();
        System.out.println("----- End of turn " + turn + " -----");
        System.out.println("Served: " + restaurant.getStats().getServedCount());
        System.out.println("Unhappy departures: " + restaurant.getStats().getUnhappyDepartures());
        System.out.println("Revenue: " + Money.format(restaurant.getStats().getRevenueCents()));
        System.out.println("Average satisfaction of paid customers: "
                + formatAverage(restaurant));
        System.out.println("Turns used: " + turn + " / " + maxTurns);
    }

    // Displays victory or defeat and the final statistics.
    public void displayFinalResults(Restaurant restaurant, int maxTurns) {
        VictoryTargets targets = restaurant.getStats().getTargets();
        boolean won = restaurant.hasWon();
        System.out.println();
        System.out.println("========== Final result: "
                + (won ? "VICTORY" : "DEFEAT") + " ==========");
        System.out.println("Paid customers: " + restaurant.getStats().getPaidCount()
                + " (need " + targets.getPaidCustomers() + ")");
        System.out.println("Served: " + restaurant.getStats().getServedCount());
        System.out.println("Unhappy departures: " + restaurant.getStats().getUnhappyDepartures());
        System.out.println("Revenue: " + Money.format(restaurant.getStats().getRevenueCents())
                + " (need " + Money.format(targets.getRevenueCents()) + ")");
        System.out.println("Average satisfaction of paid customers: "
                + formatAverage(restaurant)
                + " (need " + targets.getAverageSatisfaction() + ")");
        System.out.println("Turns used: " + maxTurns);
        System.out.println("Result: " + (won ? "VICTORY" : "DEFEAT"));
    }

    // Shows N/A until a customer has paid.
    private String formatAverage(Restaurant restaurant) {
        if (restaurant.getStats().getPaidCount() == 0) {
            return "N/A";
        }
        return String.format("%.1f", restaurant.getStats().getAverageSatisfaction());
    }

    // Describes an order and its preparation progress.
    private String describeOrder(Order order) {
        if (order == null) {
            return "No order";
        }
        return "Order " + order.getOrderId() + ": " + order.getMenuItem().getName()
                + " " + Money.format(order.getBillCents())
                + " (" + formatStatus(order.getStatus().toString())
                + ", " + order.getPreparedUnits() + "/"
                + order.getMenuItem().getPreparationUnits() + ")";
    }

    // Shown next to the customer name. This does not affect gameplay.
    private String getCustomerType(Customer customer) {
        if (customer instanceof VIPCustomer) {
            return "VIP";
        } else if (customer instanceof CriticCustomer) {
            return "Critic";
        } else {
            return "Regular";
        }
    }

    // Makes enum statuses easier to read.
    private String formatStatus(String status) {
        String words = status.toLowerCase().replace("_", " ");
        return words.substring(0, 1).toUpperCase() + words.substring(1);
    }

    // Describes waiter tasks based on the current restaurant state.
    private String describeWaiterTasks(Restaurant restaurant) {
        StringBuilder text = new StringBuilder("Wait");
        if (restaurant.getAvailableTable() != null) {
            ArrayList<Customer> waiting = restaurant.getWaitingCustomers();
            for (int i = 0; i < waiting.size(); i++) {
                text.append("; seat ").append(waiting.get(i).getName());
            }
        }

        ArrayList<Customer> customers = restaurant.getCustomers();
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            Order order = customer.getOrder().orElse(null);
            if (customer.getStatus() == CustomerStatus.SEATED && order == null) {
                text.append("; take order from ").append(customer.getName());
            }
            if (customer.getStatus() == CustomerStatus.SEATED
                    && order != null && order.isReady()) {
                text.append("; serve ").append(customer.getName());
            }
        }
        return text.toString();
    }

    // Describes orders that the chef can prepare.
    private String describeChefTasks(Restaurant restaurant) {
        StringBuilder text = new StringBuilder("Wait");
        ArrayList<Customer> customers = restaurant.getCustomers();
        for (int i = 0; i < customers.size(); i++) {
            Order order = customers.get(i).getOrder().orElse(null);
            if (order == null) {
                continue;
            }
            if (order.getStatus() == OrderStatus.PLACED
                    || order.getStatus() == OrderStatus.PREPARING) {
                text.append("; prepare order ").append(order.getOrderId())
                        .append(" (").append(order.getMenuItem().getName())
                        .append(" ").append(order.getPreparedUnits())
                        .append("/").append(order.getMenuItem().getPreparationUnits())
                        .append(")");
            }
        }
        return text.toString();
    }
}
