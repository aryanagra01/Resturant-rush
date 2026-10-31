package entities.menu;

import utility.Require;

/** One dish on the menu. Its price and preparation time do not change. */
public class MenuItem {
    private final String name;
    private final int priceCents;
    private final int preparationUnits;

    public MenuItem(String name, int priceCents, int preparationUnits) {
        Require.notBlank(name, "Menu item name");
        Require.notNegative(priceCents, "Price");
        if (preparationUnits <= 0) {
            throw new IllegalArgumentException(
                    "Preparation units must be greater than zero.");
        }

        this.name = name;
        this.priceCents = priceCents;
        this.preparationUnits = preparationUnits;
    }

    public String getName() {
        return name;
    }

    /** The menu price in cents, before any discount. */
    public int getPriceCents() {
        return priceCents;
    }

    /** How many chef turns this dish needs. */
    public int getPreparationUnits() {
        return preparationUnits;
    }

    @Override
    public String toString() {
        return "MenuItem[" + name + ", priceCents=" + priceCents
                + ", units=" + preparationUnits + "]";
    }
}
