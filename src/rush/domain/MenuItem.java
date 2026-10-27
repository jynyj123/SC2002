package rush.domain;


/**
 * Represents something a customer can order
 * New types of menu items can be added without changing the other classes
 */
public interface MenuItem {

    String getName();

    // Price of the item in cents
    int getPriceCents();

    // Number of preparation units the Chef needs for this item
    int getPrepUnits();

    // True only for combos; lets a Menu tell whether it offers combos without a type check
    default boolean isCombo() { return false; }
}