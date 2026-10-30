package entities.restaurant;

import utility.Require;

/**
 * Running totals and the victory check.
 *
 * <p>Split out of Restaurant so Restaurant only manages tables and customers. Only
 * Restaurant records changes (package-private methods), after checking the
 * customer's state.
 */
public class GameStats {
    // The three victory requirements. Paid customers, revenue in cents, then average satisfaction.
    private final int targetPaidCustomers;
    private final int targetRevenueCents;
    private final int targetAverageSatisfaction;
    private int revenueCents;
    private int paidCount;
    private int servedCount;
    private int unhappyDepartures;
    private int totalPaidSatisfaction;

    public GameStats(int targetPaidCustomers, int targetRevenueCents, int targetAverageSatisfaction) {
        Require.notNegative(targetPaidCustomers, "Paid customers target");
        Require.notNegative(targetRevenueCents, "Revenue target");
        Require.notNegative(targetAverageSatisfaction, "Average satisfaction target");
        this.targetPaidCustomers = targetPaidCustomers;
        this.targetRevenueCents = targetRevenueCents;
        this.targetAverageSatisfaction = targetAverageSatisfaction;
    }

    void recordPayment(int billCents, int satisfaction) {
        revenueCents += billCents;
        paidCount++;
        totalPaidSatisfaction += satisfaction;
    }

    void recordService() {
        servedCount++;
    }

    void recordAbandonment() {
        unhappyDepartures++;
    }

    public int getTargetPaidCustomers() {
        return targetPaidCustomers;
    }

    public int getTargetRevenueCents() {
        return targetRevenueCents;
    }

    public int getTargetAverageSatisfaction() {
        return targetAverageSatisfaction;
    }

    /** Revenue from paid orders only, in cents. */
    public int getRevenueCents() {
        return revenueCents;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public int getServedCount() {
        return servedCount;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }

    public boolean hasPaidCustomers() {
        return paidCount > 0;
    }

    /**
     * Average of the satisfaction recorded when each customer paid.
     *
     * @throws IllegalStateException if nobody has paid; check {@link #hasPaidCustomers()}
     */
    public double getAverageSatisfaction() {
        Require.state(hasPaidCustomers(), "No customer has paid yet.");
        return (double) totalPaidSatisfaction / paidCount;
    }

    /** True when all three targets are met. Needs at least one paid customer. */
    public boolean hasMetTargets() {
        return hasPaidCustomers()
                && paidCount >= targetPaidCustomers
                && revenueCents >= targetRevenueCents
                && getAverageSatisfaction() >= targetAverageSatisfaction;
    }

    @Override
    public String toString() {
        return "GameStats[revenueCents=" + revenueCents + ", paid=" + paidCount
                + ", served=" + servedCount + ", unhappy=" + unhappyDepartures
                + ", totalPaidSatisfaction=" + totalPaidSatisfaction + "]";
    }
}
