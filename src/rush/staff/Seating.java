package rush.staff;


import rush.control.GameState;
import rush.domain.Customer;
import rush.domain.CustomerState;
import rush.domain.Table;


/**
 * The seating rule shared by the Waiter and the Host (Stage 3 moved it here from Waiter)
 * Both staff seat customers the same way, so the checks are written once
 */
final class Seating {

    // Only used through its static method
    private Seating() {}


    /**
     * Seats a waiting customer at the lowest-numbered free table
     */
    static ActionResult seatCustomer(GameState state, int customerId) {
        Customer customer = state.findCustomer(customerId).orElse(null);

        // Check that the customer exists
        if (customer == null) {
            return ActionResult.fail("There is no customer with id " + customerId + ".");
        }

        // Check that the customer is waiting in the queue
        if (customer.getState() != CustomerState.WAITING_IN_QUEUE) {
            return ActionResult.fail("Customer " + customerId + " is not waiting in the queue.");
        }

        // Check that there is a free table
        Table table = state.findFreeTable().orElse(null);
        if (table == null) {
            return ActionResult.fail("There is no free table.");
        }

        customer.seatAt(table);
        return ActionResult.ok("Seated customer " + customerId + " at table " + table.getId() + ".");
    }
}
