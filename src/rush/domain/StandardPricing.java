package rush.domain;

/** Stage 1 pricing: whatever the customer's own billing rule says (full price for Regular customers). */
public class StandardPricing implements PricingPolicy {
    @Override
    public int priceFor(Customer customer, MenuItem item) {
        return customer.billFor(item);
    }
}
