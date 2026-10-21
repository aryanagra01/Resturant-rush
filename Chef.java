//prepares one unit of one order per turn
//waiter can take an order adn chef start prparing in the same turn
//a failed task does not update lastActionTurn, hence chef can still choose another valid order
public class Chef extends Staff {
    private static final int NOT_ACTED = -1;

    private int lastActionTurn;

    public Chef(int staffId, String name) {
        super(staffId, name);
        lastActionTurn = NOT_ACTED;
    }

    //called by gamecontroller with order and current turn
    public void prepareOrder(Order order, int turn) {
        if (turn < 1) {
            throw new IllegalArgumentException("Turn must be at least 1.");
        }
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null.");
        }

        //check if chef has done somehitng this turn already
        if (!canAct(turn)) {
            throw new IllegalStateException(
                    "Chef has already completed a task this turn.");
        }

        //order must be placed or already preparing
        if (order.getStatus() != OrderStatus.PLACED
                && order.getStatus() != OrderStatus.PREPARING) {
            throw new IllegalStateException(
                    "Only a placed or preparing order can be prepared.");
        }

        //call the order object to minus 1 from its preparing and update that an action was done this turn
        order.prepareOneUnit();
        lastActionTurn = turn;
    }

    //true when this turn is valid and the chef has not prepared anything yet
    public boolean canAct(int turn) {
        return turn >= 1 && lastActionTurn != turn;
    }
}
