package entities.customer;

import entities.menu.MenuItem;
import java.util.List;

/** Always orders Pasta, loses satisfaction faster, and rewards fast service. */
public class CriticCustomer extends Customer {
    public static final int SATISFACTION_LOSS = 12;
    public static final String PREFERRED_DISH = "Pasta";
    public static final int FAST_SERVICE_TURNS = 2;
    public static final int FAST_SERVICE_BONUS = 10;

    public CriticCustomer(int customerId, String name) {
        super(customerId, name);
    }

    @Override
    public CustomerType getType() {
        return CustomerType.CRITIC;
    }

    @Override
    public int getSatisfactionLoss() {
        return SATISFACTION_LOSS;
    }

    /**
     * Always orders Pasta. The menu was already checked by chooseMenuItem.
     *
     * @throws IllegalStateException if the menu has no Pasta
     */
    @Override
    protected MenuItem pickMenuItem(List<MenuItem> menu) {
        for (MenuItem item : menu) {
            if (item.getName().equals(PREFERRED_DISH)) {
                return item;
            }
        }
        throw new IllegalStateException(PREFERRED_DISH + " is not on the menu.");
    }

    /** +10 satisfaction if served within 2 turns of ordering. Runs once, when served. */
    @Override
    protected void onFoodServed(int placedTurn, int servedTurn) {
        if (servedTurn - placedTurn <= FAST_SERVICE_TURNS) {
            increaseSatisfaction(FAST_SERVICE_BONUS);
        }
    }
}
