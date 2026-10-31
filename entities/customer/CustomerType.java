package entities.customer;

// The three customer types. Each constant creates its own subclass.
public enum CustomerType {
    REGULAR {
        @Override
        public Customer create(int customerId, String name) {
            return new RegularCustomer(customerId, name);
        }
    },
    VIP {
        @Override
        public Customer create(int customerId, String name) {
            return new VIPCustomer(customerId, name);
        }
    },
    CRITIC {
        @Override
        public Customer create(int customerId, String name) {
            return new CriticCustomer(customerId, name);
        }
    };

    // Creates a customer of this type.
    public abstract Customer create(int customerId, String name);
}
