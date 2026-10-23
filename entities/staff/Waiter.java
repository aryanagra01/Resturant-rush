package entities.staff;

import entities.customer.Customer;
import entities.customer.CustomerStatus;
import entities.customer.Order;
import entities.menu.MenuItem;
import entities.restaurant.Table;

//seats customers, takes orders and serves ready food
//one successful task per turn
//a failed task does not update lastActionTurn like the chef and cashier
public class Waiter extends Staff {
    private static final int NOT_ACTED = -1;

    private int lastActionTurn;
    
    // Tracks the ID to assign to the next order
    private int nextOrderId;

    public Waiter(int staffId, String name) {
        super(staffId, name);
        lastActionTurn = NOT_ACTED;
        nextOrderId = 1;
    }

    //called by gamecontroller
    //customer must be waiting and the table must be free
    //does not remove the customer from the waiting queue, gamecontroller handles that 
    public void seatCustomer(Customer customer, Table table, int turn) {
        if (turn < 1) {
            throw new IllegalArgumentException("Turn must be at least 1.");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null.");
        }
        if (!canAct(turn)) {
            throw new IllegalStateException(
                    "Waiter has already completed a task this turn.");
        }
        if (customer.getStatus() != CustomerStatus.WAITING) {
            throw new IllegalStateException(
                    "Only a waiting customer can be seated.");
        }
        if (!table.isAvailable()) {
            throw new IllegalStateException("Table is already occupied.");
        }

        table.occupy(customer);
        customer.seat();
        lastActionTurn = turn;
    }

    //called by gamecontroller
    //creates the order, gives it to the seated customer, and returns it
    //the bill is the menu price after the customer's discount (only CIP has discount)
    public Order takeOrder(Customer customer, MenuItem item, int turn) {
        if (turn < 1) {
            throw new IllegalArgumentException("Turn must be at least 1.");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        if (item == null) {
            throw new IllegalArgumentException("Menu item cannot be null.");
        }
        if (!canAct(turn)) {
            throw new IllegalStateException(
                    "Waiter has already completed a task this turn.");
        }
        if (customer.getStatus() != CustomerStatus.SEATED) {
            throw new IllegalStateException(
                    "Only a seated customer can place an order.");
        }
        if (customer.getOrder().orElse(null) != null) {
            throw new IllegalStateException("Customer already has an order.");
        }

        int priceCents = item.getPriceCents();
        int billCents = customer.calculateBill(priceCents);
        Order order = new Order(nextOrderId, item, billCents, turn);
        customer.assignOrder(order);
        nextOrderId++;
        lastActionTurn = turn;
        return order;
    }

    //called by gamecontroller
    //order must already be ready before this is called
    //gamecontroller must run the waiter before the chef, so food that becomes ready this turn is not served until a later turn
    public void serveOrder(Customer customer, int turn) {
        if (turn < 1) {
            throw new IllegalArgumentException("Turn must be at least 1.");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        if (!canAct(turn)) {
            throw new IllegalStateException(
                    "Waiter has already completed a task this turn.");
        }
        if (customer.getStatus() != CustomerStatus.SEATED) {
            throw new IllegalStateException(
                    "Only a seated customer can be served.");
        }

        Order order = customer.getOrder().orElse(null);
        if (order == null || !order.isReady()) {
            throw new IllegalStateException(
                    "Customer can only be served a ready order.");
        }

        customer.receiveFood(turn);
        lastActionTurn = turn;
    }

    //true when this turn is valid and the waiter has not finished a task yet
    public boolean canAct(int turn) {
        return turn >= 1 && lastActionTurn != turn;
    }
}
