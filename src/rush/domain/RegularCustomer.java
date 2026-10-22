package rush.domain;


/**
 * A normal customer who loses 8 satisfaction each turn while waiting
 * and pays the full price for their food
 */
public class RegularCustomer extends Customer {

    public static final int WAITING_LOSS = 8;


    /**
     * Regular customers choose their food based on their ID
     * The order is Salad, Burger, then Pasta, repeating for later customers
     */
    private static final String[] DISH_CYCLE = {"Salad", "Burger", "Pasta"};


    public RegularCustomer(int id) { super(id); }


    @Override public int getWaitingLoss() { return WAITING_LOSS; }

    @Override public String getTypeName() { return "Regular"; }


    /**
     * Chooses the dish assigned to this customer
     */
    @Override
    public MenuItem chooseItem(Menu menu) {
        String dish = DISH_CYCLE[getId() % DISH_CYCLE.length];
        MenuItem item = menu.findByName(dish);

        if (item == null) {
            throw new IllegalStateException("The menu has no " + dish);
        }

        return item;
    }
}