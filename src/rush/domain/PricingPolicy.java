package rush.domain;


/**
 * Decides how much a customer pays for an item
 * The Waiter uses this when taking an order and saves the price in the Order
 */
@FunctionalInterface
public interface PricingPolicy {

    /**
     * Returns the price in cents for the customer ordering the item
     */
    int priceFor(Customer customer, MenuItem item);
}