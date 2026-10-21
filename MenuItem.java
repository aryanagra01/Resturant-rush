/**
 * Represents one food item available on the restaurant's menu.
 */
public class MenuItem {
    private final String name;
    private final int priceCents;
    private final int preparationUnits;

    /**
     * Creates a menu item.
     *
     * @param name             the item's display name
     * @param priceCents       the item's price in cents
     * @param preparationUnits the number of preparation units required
     */
    public MenuItem(String name, int priceCents, int preparationUnits) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Menu item name cannot be blank.");
        }
        if (priceCents < 0) {
            throw new IllegalArgumentException("Price cannot be negative.");
        }
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

    public int getPriceCents() {
        return priceCents;
    }

    public int getPreparationUnits() {
        return preparationUnits;
    }
}
