package rush.domain;


/**
 * Stage 2: a food Critic
 * Loses 12 satisfaction each turn while waiting (a Regular loses 8)
 * and pays full price
 *
 * Special behaviour (our own rule): the Critic always orders Pasta
 * Pasta is the restaurant's signature dish: the most expensive item ($18.00)
 * and the slowest to cook (2 preparation units)
 * The Critic is the best paying customer but the hardest to keep happy,
 * so the manager has to decide whether to put them first
 */
public class CriticCustomer extends Customer {

    public static final int WAITING_LOSS = 12;

    // The dish every Critic orders
    public static final String SIGNATURE_DISH = "Pasta";


    public CriticCustomer(int id) { super(id); }


    @Override public int getWaitingLoss() { return WAITING_LOSS; }

    @Override public String getTypeName() { return "Critic"; }


    /**
     * The Critic always picks the signature dish
     * It is found by name, so adding combos to the menu does not change the choice
     */
    @Override
    public MenuItem chooseItem(Menu menu) {
        MenuItem item = menu.findByName(SIGNATURE_DISH);

        if (item == null) {
            throw new IllegalStateException("The menu has no " + SIGNATURE_DISH);
        }

        return item;
    }

    // billFor() is not overridden: Critics pay full price, like Regular customers
}
