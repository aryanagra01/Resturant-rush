package entities.staff;

/**
 * Seats at most one waiting customer per turn, in addition to the Waiter's task.
 * The Waiter can still seat customers too.
 */
public class Host extends FrontOfHouseStaff {

    public Host(int staffId, String name) {
        super(staffId, name);
    }
}
