//loses satisfaction more slowly and pays 10% less
public class VIPCustomer extends Customer {

    public VIPCustomer(int customerId, String name) {
        super(customerId, name);
    }

    //overrides customer (polymoroshm)
    public int getSatisfactionLoss() {
        return 5;
    }

    //10% off, still in whole cents. negative prices are rejected by Customer
    public int applyDiscount(int priceCents) {
        int fullPrice = super.applyDiscount(priceCents);
        return fullPrice - fullPrice / 10;
    }
}
