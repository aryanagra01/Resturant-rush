package config;

import entities.menu.MenuItem;
import entities.restaurant.Restaurant;
import java.util.List;
import java.util.Objects;
import utility.Require;

/**
 * One game's settings: turns, tables, menu, arrival schedule and victory targets.
 *
 * <p>An immutable object passed to the classes that need it, instead of global
 * constants, so other difficulty settings or test setups can be built without
 * touching the game code. {@link #base()} is the required base setting from the brief.
 */
public final class GameSettings {
    // 0 means nobody arrives. The other numbers are the three customer subclasses.
    public static final int NO_ARRIVAL = 0;
    public static final int REGULAR = 1;
    public static final int VIP = 2;
    public static final int CRITIC = 3;

    private final int maxTurns;
    private final int tableCount;
    private final List<MenuItem> menu;
    // Index is the turn number. Turn 0 is unused.
    private final int[] arrivalTypes;
    private final int targetPaidCustomers;
    private final int targetRevenueCents;
    private final int targetAverageSatisfaction;

    public GameSettings(int maxTurns, int tableCount, List<MenuItem> menu,
            int[] arrivalTypes, int targetPaidCustomers,
            int targetRevenueCents, int targetAverageSatisfaction) {
        Require.validTurn(maxTurns, "Max turns");
        Require.argument(tableCount >= 1, "A game needs at least one table.");
        Objects.requireNonNull(menu, "Menu cannot be null.");
        Require.argument(!menu.isEmpty(), "A game needs at least one menu item.");
        Objects.requireNonNull(arrivalTypes, "Arrivals cannot be null.");
        Require.argument(arrivalTypes.length == maxTurns + 1,
                "Arrivals must cover turns 1 to " + maxTurns + ".");
        for (int turn = 1; turn <= maxTurns; turn++) {
            int type = arrivalTypes[turn];
            Require.argument(type == NO_ARRIVAL || type == REGULAR || type == VIP || type == CRITIC,
                    "Arrival on turn " + turn + " has an unknown customer type.");
        }
        Require.notNegative(targetPaidCustomers, "Paid customers target");
        Require.notNegative(targetRevenueCents, "Revenue target");
        Require.notNegative(targetAverageSatisfaction, "Average satisfaction target");

        this.maxTurns = maxTurns;
        this.tableCount = tableCount;
        this.menu = List.copyOf(menu);
        this.arrivalTypes = new int[arrivalTypes.length];
        for (int turn = 0; turn < arrivalTypes.length; turn++) {
            this.arrivalTypes[turn] = arrivalTypes[turn];
        }
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
        // Turns 1, 7, 13 and 16 are regular. Turn 4 is a VIP. Turn 10 is a critic.
        int[] arrivals = new int[19];
        arrivals[1] = REGULAR;
        arrivals[4] = VIP;
        arrivals[7] = REGULAR;
        arrivals[10] = CRITIC;
        arrivals[13] = REGULAR;
        arrivals[16] = REGULAR;
        return new GameSettings(
                18,
                2,
                // Order matters: Regular customers pick by index (id mod menu size).
                List.of(
                        new MenuItem("Salad", 900, 1),
                        new MenuItem("Pasta", 1800, 2),
                        new MenuItem("Burger", 1500, 1)),
                arrivals,
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

    // The customer code for this turn, or NO_ARRIVAL when nobody comes.
    public int arrivalOn(int turn) {
        if (turn < 1 || turn > maxTurns) {
            return NO_ARRIVAL;
        }
        return arrivalTypes[turn];
    }
}
