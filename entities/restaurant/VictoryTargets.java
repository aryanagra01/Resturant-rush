package entities.restaurant;

import utility.Require;

/** The three targets the manager must meet by the last turn. Immutable. */
public final class VictoryTargets {
    private final int paidCustomers;
    private final int revenueCents;
    private final int averageSatisfaction;

    public VictoryTargets(int paidCustomers, int revenueCents, int averageSatisfaction) {
        Require.notNegative(paidCustomers, "Paid customers target");
        Require.notNegative(revenueCents, "Revenue target");
        Require.notNegative(averageSatisfaction, "Average satisfaction target");
        this.paidCustomers = paidCustomers;
        this.revenueCents = revenueCents;
        this.averageSatisfaction = averageSatisfaction;
    }

    public int getPaidCustomers() {
        return paidCustomers;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getAverageSatisfaction() {
        return averageSatisfaction;
    }

    /**
     * True when all three targets are met. Only call it once at least one customer
     * has paid, since the average is undefined before that.
     */
    public boolean areMetBy(int paid, int revenueCents, double averageSatisfaction) {
        return paid >= paidCustomers
                && revenueCents >= this.revenueCents
                && averageSatisfaction >= this.averageSatisfaction;
    }

    @Override
    public String toString() {
        return "VictoryTargets[paid=" + paidCustomers + ", revenueCents=" + revenueCents
                + ", averageSatisfaction=" + averageSatisfaction + "]";
    }
}
