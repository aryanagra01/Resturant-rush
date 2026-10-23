package entities.staff;

import java.util.function.Supplier;
import utility.Require;

/**
 * Base class for every staff role.
 *
 * <p>Every role does at most one task per turn, and a failed task must not use up
 * the turn. That rule is written once here ({@link #performTask}, a template method)
 * instead of being copied into Waiter, Chef and Cashier.
 */
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

    /** True when this staff member has not finished a task this turn. */
    public boolean canAct(int turn) {
        return turn >= 1 && lastActionTurn != turn;
    }

    /**
     * Template method for one task:
     * <ol>
     *   <li>check the turn and the once-per-turn limit,</li>
     *   <li>run the role-specific task,</li>
     *   <li>only if it succeeded, mark this turn as used.</li>
     * </ol>
     * If the task throws, the turn is not used and the manager can choose again.
     *
     * @throws utility.GameRuleException if this staff member already acted this turn
     */
    protected final <T> T performTask(int turn, Supplier<T> task) {
        Require.validTurn(turn, "Turn");
        Require.rule(canAct(turn), name + " has already completed a task this turn.");

        T result = task.get();
        lastActionTurn = turn;
        return result;
    }

    /** Same as {@link #performTask(int, Supplier)}, for tasks that return nothing. */
    protected final void performTask(int turn, Runnable task) {
        performTask(turn, () -> {
            task.run();
            return null;
        });
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + staffId + ", name=" + name
                + ", lastActionTurn=" + lastActionTurn + "]";
    }
}
