package config;

import entities.customer.CustomerType;
import entities.menu.MenuItem;
import entities.restaurant.Restaurant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import utility.Require;

/**
 * One game's settings: turns, tables, menu, arrival schedule and victory targets.
 *
 * <p>An immutable object passed to the classes that need it, instead of global
 * constants, so other difficulty settings or test setups can be built without
 * touching the game code. {@link #base()} is the required base setting from the brief.
 */
public final class GameSettings {
    private final int maxTurns;
    private final int tableCount;
    private final List<MenuItem> menu;
    private final Map<Integer, CustomerType> arrivals;
    private final int targetPaidCustomers;
    private final int targetRevenueCents;
    private final int targetAverageSatisfaction;

    public GameSettings(int maxTurns, int tableCount, List<MenuItem> menu,
            Map<Integer, CustomerType> arrivals, int targetPaidCustomers,
            int targetRevenueCents, int targetAverageSatisfaction) {
        Require.validTurn(maxTurns, "Max turns");
        Require.argument(tableCount >= 1, "A game needs at least one table.");
        Objects.requireNonNull(menu, "Menu cannot be null.");
        Require.argument(!menu.isEmpty(), "A game needs at least one menu item.");
        Objects.requireNonNull(arrivals, "Arrivals cannot be null.");
        for (int turn : arrivals.keySet()) {
            Require.argument(turn >= 1 && turn <= maxTurns,
                    "Arrival turn " + turn + " is outside turns 1 to " + maxTurns + ".");
        }
        Require.notNegative(targetPaidCustomers, "Paid customers target");
        Require.notNegative(targetRevenueCents, "Revenue target");
        Require.notNegative(targetAverageSatisfaction, "Average satisfaction target");

        this.maxTurns = maxTurns;
        this.tableCount = tableCount;
        this.menu = List.copyOf(menu);
        this.arrivals = Map.copyOf(arrivals);
        this.targetPaidCustomers = targetPaidCustomers;
        this.targetRevenueCents = targetRevenueCents;
        this.targetAverageSatisfaction = targetAverageSatisfaction;
    }

    /**
     * The required base setting: 18 turns, 2 tables, Salad/Pasta/Burger, arrivals on
     * turns 1, 4, 7, 10, 13 and 16 (turn 4 a VIP, turn 10 a Critic), and the targets
     * 4 paid customers, $45.00 revenue, average satisfaction 60.
     */
    public static GameSettings base() {
        return new GameSettings(
                18,
                2,
                // Order matters: Regular customers pick by index (id mod menu size).
                List.of(
                        new MenuItem("Salad", 900, 1),
                        new MenuItem("Pasta", 1800, 2),
                        new MenuItem("Burger", 1500, 1)),
                Map.of(
                        1, CustomerType.REGULAR,
                        4, CustomerType.VIP,
                        7, CustomerType.REGULAR,
                        10, CustomerType.CRITIC,
                        13, CustomerType.REGULAR,
                        16, CustomerType.REGULAR),
                4, 4500, 60);
    }

    /** A fresh restaurant built from these settings. */
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

    /** The type of customer arriving on this turn, if anyone arrives. */
    public Optional<CustomerType> arrivalOn(int turn) {
        return Optional.ofNullable(arrivals.get(turn));
    }
}
