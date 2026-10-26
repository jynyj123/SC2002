package rush.domain;


/**
 * Stage 2: a VIP customer
 * Loses 5 satisfaction each turn while waiting (a Regular loses 8)
 * and gets 10% off the bill
 * A VIP picks food the same way a Regular customer does, so the only
 * differences are patience and price
 */
public class VIPCustomer extends Customer {

    public static final int WAITING_LOSS = 5;

    // VIPs pay 90% of the normal price
    private static final int BILL_PERCENT = 90;


    public VIPCustomer(int id) { super(id); }


    @Override public int getWaitingLoss() { return WAITING_LOSS; }

    @Override public String getTypeName() { return "VIP"; }


    /**
     * Same food rule as RegularCustomer: Salad, Burger, Pasta by customer ID,
     * with even IDs upgrading to a combo when the menu has one
     */
    @Override
    public MenuItem chooseItem(Menu menu) {
        return chooseByIdRule(menu);
    }


    /**
     * VIPs get 10% off, rounded to the nearest cent
     * e.g. Burger 1500 -> 1350, Salad + Pasta combo 2430 -> 2187
     */
    @Override
    public int billFor(MenuItem item) {
        return (item.getPriceCents() * BILL_PERCENT + 50) / 100;
    }
}
