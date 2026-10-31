package entities.pricing;

import utility.Require;

/**
 * A pricing rule applied when an order is taken, after the customer's own discount.
 * The result is fixed in the order, so a later change of policy never changes it.
 *
 * <p>Works like a superclass for pricing rules: the Waiter only knows this
 * interface, and {@link #apply(int)} runs the version of whichever policy it was
 * given (for example {@link HappyHour}).
 */
public interface PricingPolicy {

    /** No change: the bill stays as it is. */
    PricingPolicy NONE = billCents -> billCents;

    /** The bill to charge, given the bill after the customer's own discount. */
    int apply(int billCents);

    /**
     * Takes a percentage off, rounded to the nearest cent. Every discount in the game
     * uses this (combos, VIP and Happy Hour), so they all round the same way.
     * For example, 20% off 1350 is 1080, and 10% off 2700 is 2430.
     */
    static int percentOff(int cents, int percent) {
        Require.notNegative(cents, "Amount");
        Require.argument(percent >= 0 && percent <= 100, "Percent must be between 0 and 100.");
        return (int) Math.round(cents * (100 - percent) / 100.0);
    }
}
