package rush.staff;


import java.util.ArrayList;
import java.util.List;
import rush.control.GameState;
import rush.domain.Customer;
import rush.domain.Order;
import rush.domain.OrderStatus;


/**
 * Prepares customer orders
 * The Chef can work on one order each turn
 */
public class Chef {


    /**
     * Advances the chosen customer’s order by one preparation unit
     * Returns a message saying whether the action worked
     */
    public ActionResult advanceOrder(GameState state, int customerId) {
        Customer customer = state.findCustomer(customerId).orElse(null);

        // Check that the customer exists
        if (customer == null) {
            return ActionResult.fail("There is no customer with id " + customerId + ".");
        }

        Order order = customer.getOrder();

        // Check that the customer has placed an order
        if (order == null) {
            return ActionResult.fail("Customer " + customerId + " has not ordered yet.");
        }

        // Check that the order can still be prepared
        if (!order.canBePrepared()) {
            return ActionResult.fail("Customer " + customerId + "'s order cannot be prepared (it is "
                    + order.getStatus() + ").");
        }

        // Prepare one unit of the order
        order.advancePreparation(state.getTurn());
        String dish = order.getItem().getName();

        // The order is ready once all preparation units are done
        if (order.getStatus() == OrderStatus.READY) {
            return ActionResult.ok("Chef finished " + dish + " for customer " + customerId
                    + ". It is READY and can be served next turn.");
        }

        return ActionResult.ok("Chef prepared one unit of " + dish + " for customer " + customerId
                + " (" + order.getUnitsRemaining() + " unit(s) left).");
    }


    /**
     * Returns the orders the Chef can work on this turn
     */
    public List<String> describeAvailableTasks(GameState state) {
        List<String> tasks = new ArrayList<>();

        for (Order order : state.getOrders()) {
            if (order.canBePrepared()) {
                tasks.add("Advance " + order.getItem().getName() + " for customer "
                        + order.getCustomer().getId() + " (" + order.getUnitsRemaining() + " unit(s) left)");
            }
        }

        tasks.add("Wait");
        return tasks;
    }
}
