package entities.menu;

import entities.pricing.PricingPolicy;
import java.util.Objects;

/**
 * Two menu items served as one. It needs the preparation units of both items and
 * costs 10% less than the two items bought separately, rounded to the nearest cent.
 *
 * <p>A combo is a MenuItem (inheritance) made of two MenuItems (composition), so
 * orders, the Chef and the bill handle it like any other dish.
 */
public class ComboMeal extends MenuItem {
    public static final int DISCOUNT_PERCENT = 10;

    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(MenuItem first, MenuItem second) {
        super(nameOf(first, second),
                PricingPolicy.percentOff(first.getPriceCents() + second.getPriceCents(),
                        DISCOUNT_PERCENT),
                first.getPreparationUnits() + second.getPreparationUnits());
        this.first = first;
        this.second = second;
    }

    // Checks both items before the superclass constructor uses them.
    private static String nameOf(MenuItem first, MenuItem second) {
        Objects.requireNonNull(first, "First item cannot be null.");
        Objects.requireNonNull(second, "Second item cannot be null.");
        return first.getName() + " + " + second.getName() + " combo";
    }

    public MenuItem getFirst() {
        return first;
    }

    public MenuItem getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return "ComboMeal[" + first.getName() + " + " + second.getName()
                + ", priceCents=" + getPriceCents() + ", units=" + getPreparationUnits() + "]";
    }
}
