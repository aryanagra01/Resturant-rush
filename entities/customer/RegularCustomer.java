package entities.customer;

/** The default customer: standard satisfaction loss, full price, default food rule. */
public class RegularCustomer extends Customer {
    public static final int SATISFACTION_LOSS = 8;

    public RegularCustomer(int customerId, String name) {
        super(customerId, name);
    }

    @Override
    public CustomerType getType() {
        return CustomerType.REGULAR;
    }

    @Override
    public int getSatisfactionLoss() {
        return SATISFACTION_LOSS;
    }
}
