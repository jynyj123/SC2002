package rush.domain;


import java.util.function.IntSupplier;


/**
 * Stage 3: Happy Hour price rule
 * Wraps another pricing policy (decorator), so the inner policy works out the
 * normal bill first (including the VIP discount) and this class takes 20% off it
 * e.g. VIP Burger: 1500 -> 1350 (VIP) -> 1080 (Happy Hour)
 * The Waiter does not change: it is given this policy instead of StandardPricing
 */
public class HappyHourPricing implements PricingPolicy {

    // Orders taken during Happy Hour pay 80% of the normal bill
    private static final int BILL_PERCENT = 80;

    private final PricingPolicy inner;
    private final HappyHour happyHour;

    // Gives the current turn, so the price can be checked against the Happy Hour turns
    private final IntSupplier currentTurn;


    public HappyHourPricing(PricingPolicy inner, HappyHour happyHour, IntSupplier currentTurn) {
        this.inner = inner;
        this.happyHour = happyHour;
        this.currentTurn = currentTurn;
    }


    /**
     * Normal price, with 20% off (rounded to the nearest cent) while Happy Hour is active
     * The Waiter locks this price into the order, so a later change of turn does not affect it
     */
    @Override
    public int priceFor(Customer customer, MenuItem item) {
        int normalPrice = inner.priceFor(customer, item);

        if (!happyHour.isActiveOn(currentTurn.getAsInt())) {
            return normalPrice;
        }

        return (normalPrice * BILL_PERCENT + 50) / 100;
    }
}
