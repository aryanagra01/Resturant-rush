package config;

import entities.customer.CustomerType;
import entities.menu.MenuItem;
import entities.restaurant.Restaurant;
import java.util.List;
import java.util.Objects;
import utility.Require;

// One game's settings: turns, tables, menu, arrivals and victory requirements.
// base() is the required setting from the assignment brief.
public final class GameSettings {
    private final int maxTurns;
    private final int tableCount;
    private final List<MenuItem> menu;
    // Index is the turn number. Turn 0 is unused. Null means nobody arrives.
    private final CustomerType[] arrivalTypes;
    private final int targetPaidCustomers;
    private final int targetRevenueCents;
    private final int targetAverageSatisfaction;

    public GameSettings(int maxTurns, int tableCount, List<MenuItem> menu,
            CustomerType[] arrivalTypes, int targetPaidCustomers,
            int targetRevenueCents, int targetAverageSatisfaction) {
        Require.validTurn(maxTurns, "Max turns");
        Require.argument(tableCount >= 1, "A game needs at least one table.");
        Objects.requireNonNull(menu, "Menu cannot be null.");
        Require.argument(!menu.isEmpty(), "A game needs at least one menu item.");
        Objects.requireNonNull(arrivalTypes, "Arrivals cannot be null.");
        Require.argument(arrivalTypes.length == maxTurns + 1,
                "Arrivals must cover turns 1 to " + maxTurns + ".");
        Require.notNegative(targetPaidCustomers, "Paid customers target");
        Require.notNegative(targetRevenueCents, "Revenue target");
        Require.notNegative(targetAverageSatisfaction, "Average satisfaction target");

        this.maxTurns = maxTurns;
        this.tableCount = tableCount;
        this.menu = List.copyOf(menu);
        this.arrivalTypes = new CustomerType[arrivalTypes.length];
        for (int turn = 0; turn < arrivalTypes.length; turn++) {
            this.arrivalTypes[turn] = arrivalTypes[turn];
        }
        this.targetPaidCustomers = targetPaidCustomers;
        this.targetRevenueCents = targetRevenueCents;
        this.targetAverageSatisfaction = targetAverageSatisfaction;
    }

    // 18 turns, 2 tables, Salad/Pasta/Burger.
    // Arrivals on turns 1, 4, 7, 10, 13 and 16. Turn 4 is a VIP and turn 10 is a Critic.
    // Victory needs 4 paid customers, $45.00 revenue and average satisfaction 60.
    public static GameSettings base() {
        // Turns 1, 7, 13 and 16 are regular. Turn 4 is a VIP. Turn 10 is a critic.
        int maxTurns = 18;
        CustomerType[] arrivals = new CustomerType[maxTurns + 1];
        arrivals[1] = CustomerType.REGULAR;
        arrivals[4] = CustomerType.VIP;
        arrivals[7] = CustomerType.REGULAR;
        arrivals[10] = CustomerType.CRITIC;
        arrivals[13] = CustomerType.REGULAR;
        arrivals[16] = CustomerType.REGULAR;
        return new GameSettings(
                maxTurns,
                2,
                // Order matters: Regular customers pick by index (id mod menu size).
                List.of(
                        new MenuItem("Salad", 900, 1),
                        new MenuItem("Pasta", 1800, 2),
                        new MenuItem("Burger", 1500, 1)),
                arrivals,
                4, 4500, 60);
    }

    // A fresh restaurant built from these settings.
    public Restaurant createRestaurant() {
        return new Restaurant(menu, tableCount, targetPaidCustomers, targetRevenueCents,
                targetAverageSatisfaction);
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public int getTableCount() {
        return tableCount;
    }

    public List<MenuItem> getMenu() {
        return menu;
    }

    // The customer type arriving this turn, or null when nobody comes.
    public CustomerType arrivalOn(int turn) {
        if (turn < 1 || turn > maxTurns) {
            return null;
        }
        return arrivalTypes[turn];
    }
}
