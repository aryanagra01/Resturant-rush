//processes one payment per turn
//gamecontroller chooses the oldest eligible customer
//a failed payment does not update lastPaymentTurn, hence can process someone else

public class Cashier extends Staff {
    private static final int NOT_PAID = -1;

    private int lastPaymentTurn;

    public Cashier(int staffId, String name) {
        super(staffId, name);
        lastPaymentTurn = NOT_PAID;
    }

    //called by gamecontroller
    //customer must already have been ready to pay before this turn
    public void processPayment(Customer customer, Restaurant restaurant, int turn) {
        if (turn < 1) {
            throw new IllegalArgumentException("Turn must be at least 1.");
        }
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null.");
        }
        if (!canProcessPayment(turn)) {
            throw new IllegalStateException(
                    "Cashier has already processed a payment this turn.");
        }
        if (customer.getStatus() != CustomerStatus.READY_TO_PAY) {
            throw new IllegalStateException(
                    "Only a customer who is ready to pay can pay.");
        }

        //eating finishes on servedTurn + 1(ready to pay), so the earliest payment is servedTurn + 2
        if (turn < customer.getServedTurn() + 2) {
            throw new IllegalStateException(
                    "Customer cannot pay on the turn they became ready to pay.");
        }

        Order order = customer.getOrder();
        if (order == null || order.getStatus() != OrderStatus.SERVED) {
            throw new IllegalStateException("Only a served order can be paid.");
        }

        Table table = restaurant.findTableByCustomer(customer);
        if (table == null || table.getCustomer() != customer) {
            throw new IllegalStateException("Customer is not seated at a table.");
        }

        //if all good, then cusomter and order become paid, revenue recorded by resrutant, table is releaed and turn is updated of cashier last movement
        customer.markPaid();
        order.markPaid();
        restaurant.recordPayment(customer);
        table.release();
        lastPaymentTurn = turn;
    }

    //true when this turn is valid and no payment has been taken yet
    public boolean canProcessPayment(int turn) {
        return turn >= 1 && lastPaymentTurn != turn;
    }
}
