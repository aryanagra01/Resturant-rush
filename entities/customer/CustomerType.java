package entities.customer;

/**
 * The catalogue of customer types.
 *
 * <p>Each constant creates its own subclass (factory method), so the arrival code
 * never needs an if/else chain to decide which class to build. Adding a type means
 * adding a subclass and one constant here.
 */
public enum CustomerType {
    REGULAR("Regular", RegularCustomer.SATISFACTION_LOSS,
            ", pays full price, and chooses food from their customer id.") {
        @Override
        public Customer create(int customerId, String name) {
            return new RegularCustomer(customerId, name);
        }
    },
    VIP("VIP", VIPCustomer.SATISFACTION_LOSS,
            ", and receives a " + VIPCustomer.DISCOUNT_PERCENT + "% discount.") {
        @Override
        public Customer create(int customerId, String name) {
            return new VIPCustomer(customerId, name);
        }
    },
    CRITIC("Critic", CriticCustomer.SATISFACTION_LOSS,
            ", prefers " + CriticCustomer.PREFERRED_DISH
                    + ", and gains " + CriticCustomer.FAST_SERVICE_BONUS
                    + " satisfaction if served within "
                    + CriticCustomer.FAST_SERVICE_TURNS
                    + " turns of ordering. Satisfaction cannot exceed 100.") {
        @Override
        public Customer create(int customerId, String name) {
            return new CriticCustomer(customerId, name);
        }
    };

    private final String label;
    private final int satisfactionLoss;
    private final String specialRules;

    CustomerType(String label, int satisfactionLoss, String specialRules) {
        this.label = label;
        this.satisfactionLoss = satisfactionLoss;
        this.specialRules = specialRules;
    }

    /** Creates a new customer of this type. */
    public abstract Customer create(int customerId, String name);

    /** Short name shown next to the customer, e.g. "VIP". */
    public String getLabel() {
        return label;
    }

    /** The rules text for the welcome screen, built from the subclass constants. */
    public String describeRules() {
        return "loses " + satisfactionLoss
                + " satisfaction per turn while waiting or seated" + specialRules;
    }
}
