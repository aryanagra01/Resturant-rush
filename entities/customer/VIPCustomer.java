package entities.customer;

/** Loses satisfaction more slowly and pays 10% less. */
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

    /** 10% off in whole cents. The price was already validated by calculateBill. */
    @Override
    protected int applyDiscount(int priceCents) {
        return priceCents - priceCents * DISCOUNT_PERCENT / 100;
    }
}
