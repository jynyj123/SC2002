package rush.domain;


import java.util.List;


/**
 * Stage 3: a combo made of two existing menu items
 * The combo is built from the two items (composition), so its price and
 * preparation units always follow the items it contains
 */
public final class ComboMeal implements MenuItem {

    // A combo costs 90% of its two items' combined price
    private static final int PRICE_PERCENT = 90;

    private final MenuItem first;
    private final MenuItem second;


    // Builds a combo from two items that are already on the menu
    public ComboMeal(MenuItem first, MenuItem second) {
        this.first = first;
        this.second = second;
    }


    // e.g. "Salad + Pasta Combo", so findByName("Pasta") still only matches plain Pasta
    @Override public String getName() { return first.getName() + " + " + second.getName() + " Combo"; }


    /**
     * 90% of the combined price, rounded to the nearest cent
     * e.g. Salad + Pasta: (900 + 1800) x 0.9 = 2430 ($24.30)
     */
    @Override
    public int getPriceCents() {
        int combined = first.getPriceCents() + second.getPriceCents();
        return (combined * PRICE_PERCENT + 50) / 100;
    }


    // The Chef has to prepare both items, e.g. Salad + Pasta = 1 + 2 = 3 units
    @Override public int getPrepUnits() { return first.getPrepUnits() + second.getPrepUnits(); }


    // A combo is the one kind of menu item that is a combo
    @Override public boolean isCombo() { return true; }


    // The two items inside the combo
    public List<MenuItem> getComponents() { return List.of(first, second); }

    // Shows only the combo name when printed
    @Override public String toString() { return getName(); }
}
