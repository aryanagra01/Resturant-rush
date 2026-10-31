package entities.staff;

import utility.Require;

// Every role does at most one successful task per turn. A failed task does not use the turn.
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

    // Rejects a bad turn, or a second task after one already succeeded this turn.
    protected void checkCanAct(int turn) {
        Require.validTurn(turn, "Turn");
        Require.rule(canAct(turn), name + " has already completed a task this turn.");
    }

    // Records the staff action only after it succeeds.
    protected void recordAction(int turn) {
        lastActionTurn = turn;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[id=" + staffId + ", name=" + name
                + ", lastActionTurn=" + lastActionTurn + "]";
    }
}
