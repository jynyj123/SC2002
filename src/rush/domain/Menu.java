package rush.domain;


import java.util.Collections;
import java.util.List;


/**
 * Stores the items available in the restaurant
 */
public class Menu {

    private final List<MenuItem> items;


    // Creates a menu using the items given
    public Menu(List<MenuItem> items) {
        this.items = List.copyOf(items);
    }


    /**
     * Creates the default menu used in the game
     */
    public static Menu standard() {
        return new Menu(List.of(
                new BasicMenuItem("Salad", 900, 1),
                new BasicMenuItem("Burger", 1500, 1),
                new BasicMenuItem("Pasta", 1800, 2)));
    }


    /**
     * Stage 3 menu: the three standard dishes plus every combo of two different dishes
     * The combos are built from the same dish objects, so they follow the dishes' prices
     * Salad + Burger $21.60 (2 units), Salad + Pasta $24.30 (3 units), Burger + Pasta $29.70 (3 units)
     */
    public static Menu stage3() {
        MenuItem salad = new BasicMenuItem("Salad", 900, 1);
        MenuItem burger = new BasicMenuItem("Burger", 1500, 1);
        MenuItem pasta = new BasicMenuItem("Pasta", 1800, 2);

        return new Menu(List.of(
                salad, burger, pasta,
                new ComboMeal(salad, burger),
                new ComboMeal(salad, pasta),
                new ComboMeal(burger, pasta)));
    }


    // Returns all menu items, but does not allow the list to be changed from outside
    public List<MenuItem> getItems() { return Collections.unmodifiableList(items); }


    /**
     * Finds a menu item using its name (dont need to match capitalisation)
     */
    public MenuItem findByName(String name) {
        for (MenuItem item : items) {
            if (item.getName().equalsIgnoreCase(name)) return item;
        }
        return null;
    }
}