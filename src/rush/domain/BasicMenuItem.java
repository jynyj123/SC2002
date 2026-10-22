package rush.domain;


/**
 * Represents a simple menu item with one dish
 * Once created, its details cannot be changed
 */
public final class BasicMenuItem implements MenuItem {

    private final String name;
    private final int priceCents;
    private final int prepUnits;


    // Sets te name, price, and preparation units for menu item
    public BasicMenuItem(String name, int priceCents, int prepUnits) {
        this.name = name;
        this.priceCents = priceCents;
        this.prepUnits = prepUnits;
    }


    @Override public String getName() { return name; }

    @Override public int getPriceCents() { return priceCents; }

    @Override public int getPrepUnits() { return prepUnits; }

    // Shows only the item name when printed
    @Override public String toString() { return name; }
}