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