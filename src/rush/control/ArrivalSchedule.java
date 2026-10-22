package rush.control;


import java.util.HashMap;
import java.util.Map;
import rush.domain.Customer;
import rush.domain.RegularCustomer;


/**
 * Keeps track of which customers arrive on which turns
 * The schedule is stored as a map so different customer arrangements
 * can be tested without changing the game code
 */
public class ArrivalSchedule {

    // Maps each arrival turn to the type of customer that should arrive
    private final Map<Integer, CustomerFactory> plan;

    // Used to give every new customer a different ID
    private int nextCustomerId = 1;


    // Makes a copy of the given plan so it cannot be changed after the schedule is created
    public ArrivalSchedule(Map<Integer, CustomerFactory> plan) {
        this.plan = Map.copyOf(plan);
    }


    /**
     * Sets up the default arrangement where one Regular customer
     * arrives on each of the six arrival turns
     */
    public static ArrivalSchedule basicSetting() {
        Map<Integer, CustomerFactory> plan = new HashMap<>();

        // Add a Regular customer for every arrival turn
        for (int turn : GameConfig.ARRIVAL_TURNS) {
            plan.put(turn, RegularCustomer::new);
        }

        return new ArrivalSchedule(plan);
    }


    /**
     * Returns the customer that arrives at the start of the given turn
     * Returns null if no customer is scheduled for that turn
     */
    public Customer arrivalAt(int turn) {
        // Check if a customer is planned for this turn
        CustomerFactory factory = plan.get(turn);

        // Create the customer if there is one, otherwise nobody arrives
        return (factory == null) ? null : factory.create(nextCustomerId++);
    }
}