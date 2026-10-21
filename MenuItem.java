//shows the menu items
public class MenuItem {
    private final String name;
    private final int priceCents;
    private final int preparationUnits;


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

    //getters 
    //no setters once the item is finalised then no change
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
