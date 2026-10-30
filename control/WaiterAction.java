package control;

import entities.customer.Customer;
import java.util.Arrays;
import java.util.Optional;

/**
 * The Waiter's menu of tasks.
 *
 * <p>Each constant carries its own menu number and text and runs its own task
 * (constant-specific methods, i.e. dynamic binding), so neither the CLI nor the
 * controller needs magic numbers or an if/else chain. Menu numbers are stored
 * explicitly, so reordering the constants never changes what the player types.
 */
public enum WaiterAction {
    WAIT(0, "Wait", "wait", false) {
        @Override
        void perform(GameController game, Customer customer) {
            game.waiterWaits();
        }
    },
    SEAT(1, "Seat a waiting customer", "seat", true) {
        @Override
        void perform(GameController game, Customer customer) {
            game.seatCustomer(customer);
        }
    },
    TAKE_ORDER(2, "Take an order from a seated customer", "take an order", true) {
        @Override
        void perform(GameController game, Customer customer) {
            game.takeCustomerOrder(customer);
        }
    },
    SERVE(3, "Serve an order that is ready", "serve", true) {
        @Override
        void perform(GameController game, Customer customer) {
            game.serveCustomer(customer);
        }
    };

    private final int choice;
    private final String menuLabel;
    private final String verb;
    private final boolean needsCustomer;

    WaiterAction(int choice, String menuLabel, String verb, boolean needsCustomer) {
        this.choice = choice;
        this.menuLabel = menuLabel;
        this.verb = verb;
        this.needsCustomer = needsCustomer;
    }

    /** Runs this task through the controller. */
    abstract void perform(GameController game, Customer customer);

    /** The number the player types for this action. */
    public int getChoice() {
        return choice;
    }

    public String getMenuLabel() {
        return menuLabel;
    }

    /** True when the player must also enter a customer id. */
    public boolean needsCustomer() {
        return needsCustomer;
    }

    /** The action for a menu number, if that number is on the menu. */
    public static Optional<WaiterAction> fromChoice(int choice) {
        return Arrays.stream(values()).filter(a -> a.choice == choice).findFirst();
    }

    /** "Choose 0 to wait, 1 to seat, 2 to take an order, or 3 to serve." */
    public static String invalidChoiceMessage() {
        WaiterAction[] actions = values();
        StringBuilder text = new StringBuilder("Choose ");
        for (int i = 0; i < actions.length; i++) {
            if (i > 0) {
                text.append(i == actions.length - 1 ? ", or " : ", ");
            }
            text.append(actions[i].choice).append(" to ").append(actions[i].verb);
        }
        return text.append(".").toString();
    }
}
