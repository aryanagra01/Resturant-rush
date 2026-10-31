package entities.customer;

// Pays full price, loses 8 satisfaction per waiting turn, and picks food from the customer id.
public class RegularCustomer extends Customer {
    public static final int SATISFACTION_LOSS = 8;

    public RegularCustomer(int customerId, String name) {
        super(customerId, name);
    }

    @Override
    public String getType() {
        return "Regular";
    }

    @Override
    public int getSatisfactionLoss() {
        return SATISFACTION_LOSS;
    }
}
