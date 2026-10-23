package entities.staff;

import utility.Require;

import java.util.function.Supplier;

// Base class for every staff role.
// Every role does at most one task per turn, and a failed task must not use up
// the turn. That rule is written once here (template method) instead of being
// copied into Waiter, Chef and Cashier.
public abstract class Staff {
    private static final int NOT_ACTED = -1;

    private final int staffId;
    private final String name;
    private int lastActionTurn;

    protected Staff(int staffId, String name) {
        Require.notNegative(staffId, "Staff ID");
        Require.notBlank(name, "Staff name");

        this.staffId = staffId;
        this.name = name;
        lastActionTurn = NOT_ACTED;
    }

    public int getStaffId() {
        return staffId;
    }

    public String getName() {
        return name;
    }

    // True when this staff member has not finished a task this turn.
    public boolean canAct(int turn) {
        return turn >= 1 && lastActionTurn != turn;
    }

    // Template method for one task:
    //   1. check the turn and the once-per-turn limit
    //   2. run the role-specific task
    //   3. only if it succeeded, mark this turn as used
    // If the task throws, the turn is not used and the manager can choose again.
    protected final <T> T performTask(int turn, Supplier<T> task) {
        Require.validTurn(turn, "Turn");
        Require.state(canAct(turn),
                name + " has already completed a task this turn.");

        T result = task.get();
        lastActionTurn = turn;
        return result;
    }

    // Same as above, for tasks that return nothing.
    protected final void performTask(int turn, Runnable task) {
        performTask(turn, () -> {
            task.run();
            return null;
        });
    }
}
