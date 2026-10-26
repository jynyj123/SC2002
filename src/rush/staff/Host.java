package rush.staff;


import java.util.ArrayList;
import java.util.List;
import rush.control.GameState;
import rush.domain.Customer;


/**
 * Stage 3: seats queued customers
 * The Host can seat at most one customer each turn, in addition to the Waiter's task
 * The Waiter can still seat customers as well
 */
public class Host {


    /**
     * Seats a waiting customer at the first free table
     * Uses the same rule as the Waiter
     */
    public ActionResult seatCustomer(GameState state, int customerId) {
        ActionResult result = Seating.seatCustomer(state, customerId);

        return result.success() ? ActionResult.ok("Host: " + result.message()) : result;
    }


    /**
     * Returns the tasks the Host can do this turn
     */
    public List<String> describeAvailableTasks(GameState state) {
        List<String> tasks = new ArrayList<>();

        if (state.findFreeTable().isPresent()) {
            for (Customer customer : state.getWaitingQueue()) {
                tasks.add("Seat customer " + customer.getId());
            }
        }

        tasks.add("Wait");
        return tasks;
    }
}
