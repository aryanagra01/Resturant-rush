package entities.customer;

import entities.pricing.PricingPolicy;

// Loses satisfaction more slowly and pays 10% less.
public class VIPCustomer extends Customer {
    public static final int SATISFACTION_LOSS = 5;
    public static final int DISCOUNT_PERCENT = 10;

    public VIPCustomer(int customerId, String name) {
        super(customerId, name);
    }

    @Override
    public String getType() {
        return "VIP";
    }

    @Override
    public int getSatisfactionLoss() {
        return SATISFACTION_LOSS;
    }

    // Takes 10% off the price, rounded to the nearest cent like every other discount.
    @Override
    protected int applyDiscount(int priceCents) {
        return PricingPolicy.percentOff(priceCents, DISCOUNT_PERCENT);
    }
}
