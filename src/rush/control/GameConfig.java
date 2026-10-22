package rush.control;


import java.util.Set;


/**
 * Stores the main values used in the base setting
 * Keeping them here means the rest of the code uses the same values
 */
public final class GameConfig {

    // Stops this class from being created as an object
    private GameConfig() {}


    // Total number of turns in the game
    public static final int MAX_TURNS = 18;

    // Number of tables available in the restaurant
    public static final int TABLE_COUNT = 2;

    // Turns where a customer arrives
    public static final Set<Integer> ARRIVAL_TURNS = Set.of(1, 4, 7, 10, 13, 16);


    // Minimum number of customers that must pay for the game to be successful
    public static final int MIN_PAID_CUSTOMERS = 4;

    // Minimum total revenue needed, stored in cents
    public static final int MIN_REVENUE_CENTS = 4500;

    // Minimum average customer satisfaction needed
    public static final double MIN_AVG_SATISFACTION = 60.0;
}
