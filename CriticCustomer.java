import java.util.ArrayList;

//always orders pasta, loses satisfaction faster, and likes fast service
public class CriticCustomer extends Customer {
    //stops the fast-service reward from being applied twice
    private boolean rewardApplied;

    public CriticCustomer(int customerId, String name) {
        super(customerId, name);
    }

    //override customer
    public int getSatisfactionLoss() {
        return 12;
    }

    //overriden method
    public MenuItem chooseMenuItem(ArrayList<MenuItem> menu) {
        if (menu == null) {
            throw new IllegalArgumentException("Menu cannot be null.");
        }
        if (menu.isEmpty()) {
            throw new IllegalStateException("The menu is empty.");
        }

        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.get(i);
            if (item.getName().equals("Pasta")) {
                return item;
            }
        }

        throw new IllegalStateException("Pasta is not on the menu.");
    }

    //called after Waiter.serveOrder, once the customer is already eating
    public void onFoodServed(int placedTurn, int servedTurn) {
        if (placedTurn < 1) {
            throw new IllegalArgumentException("Placed turn must be at least 1.");
        }
        if (servedTurn < placedTurn) {
            throw new IllegalArgumentException(
                    "Served turn cannot be before the placed turn.");
        }

        Order order = getOrder();
        if (getStatus() != CustomerStatus.EATING) {
            throw new IllegalStateException(
                    "Fast service reward applies only after the customer is served.");
        }
        if (order == null || order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalStateException(
                    "Fast service reward applies only after the order is served.");
        }
        if (getServedTurn() != servedTurn) {
            throw new IllegalStateException(
                    "Served turn does not match this service.");
        }
        if (rewardApplied) {
            throw new IllegalStateException(
                    "Fast service reward has already been applied.");
        }

        rewardApplied = true;
        if (servedTurn - placedTurn <= 2) {
            increaseSatisfaction(10);
        }
    }

    // Critic is happier if their food is served quickly
    //overriden also
    public void onFoodServed(int placedTurn, int servedTurn) {
        
        if (placedTurn < 1) {
            throw new IllegalArgumentException("Placed turn must be at least 1.");
        }
        if (servedTurn < placedTurn) {
            throw new IllegalArgumentException(
                    "Served turn cannot be before the placed turn.");
        }

        
        Order order = getOrder();
        if (getStatus() != CustomerStatus.EATING) {
            throw new IllegalStateException(
                    "Critic has not been served yet.");
        }
        if (order == null || order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalStateException(
                    "Critic's order must be served first.");
        }
        if (getServedTurn() != servedTurn) {
            throw new IllegalStateException(
                    "Served turn does not match this service.");
        }

        // Satisfaction should only be adjusted once for the service
        if (rewardApplied) {
            throw new IllegalStateException(
                    "Critic's service satisfaction has already been checked.");
        }

        rewardApplied = true;

        // Critic is pleased with fast service and gains 10 satisfaction
        if (servedTurn - placedTurn <= 2) {
            increaseSatisfaction(10);
        }
    }
}
