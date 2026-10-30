package entities.customer;

import entities.menu.MenuItem;
import java.util.List;

// Prefers Pasta, loses satisfaction faster, and gains satisfaction for fast service.
public class CriticCustomer extends Customer {
    public static final int SATISFACTION_LOSS = 12;
    public static final String PREFERRED_DISH = "Pasta";
    public static final int FAST_SERVICE_TURNS = 2;
    public static final int FAST_SERVICE_BONUS = 10;

    public CriticCustomer(int customerId, String name) {
        super(customerId, name);
    }

    @Override
    public String getType() {
        return "Critic";
    }

    @Override
    public int getSatisfactionLoss() {
        return SATISFACTION_LOSS;
    }

    // Orders Pasta. If the menu has no Pasta, uses the regular customer's rule.
    @Override
    protected MenuItem pickMenuItem(List<MenuItem> menu) {
        for (int i = 0; i < menu.size(); i++) {
            if (menu.get(i).getName().equals(PREFERRED_DISH)) {
                return menu.get(i);
            }
        }
        return super.pickMenuItem(menu);
    }

    // Adds 10 satisfaction if served within 2 turns of ordering. Runs once, from receiveFood.
    @Override
    protected void onFoodServed(int placedTurn, int servedTurn) {
        if (servedTurn - placedTurn <= FAST_SERVICE_TURNS) {
            increaseSatisfaction(FAST_SERVICE_BONUS);
        }
    }
}
