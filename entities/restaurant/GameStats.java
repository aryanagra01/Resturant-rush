package entities.restaurant;

import utility.Require;

// Running totals and the victory check.
// Only Restaurant updates these, after checking the customer's state.
public class GameStats {
    // The three victory requirements: paid customers, revenue in cents, and average satisfaction.
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

    // Revenue from paid orders only, in cents.
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

    // Average satisfaction of customers who have paid. Fails if nobody has paid yet.
    public double getAverageSatisfaction() {
        Require.state(hasPaidCustomers(), "No customer has paid yet.");
        return (double) totalPaidSatisfaction / paidCount;
    }

    // True when all three victory requirements are met. Needs at least one paid customer.
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
