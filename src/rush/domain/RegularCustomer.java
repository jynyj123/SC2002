package rush.domain;


/**
 * A normal customer who loses 8 satisfaction each turn while waiting
 * and pays the full price for their food
 */
public class RegularCustomer extends Customer {

    public static final int WAITING_LOSS = 8;


    public RegularCustomer(int id) { super(id); }


    @Override public int getWaitingLoss() { return WAITING_LOSS; }

    @Override public String getTypeName() { return "Regular"; }


    /**
     * Regular customers choose their food based on their ID
     * The order is Salad, Burger, then Pasta, repeating for later customers
     * (even IDs upgrade to a combo when the menu has one, see Customer.chooseByIdRule)
     */
    @Override
    public MenuItem chooseItem(Menu menu) {
        return chooseByIdRule(menu);
    }
}
