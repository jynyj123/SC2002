package rush.staff;


import java.util.ArrayList;
import java.util.List;
import rush.control.GameState;
import rush.domain.Customer;
import rush.domain.CustomerState;
import rush.domain.Menu;
import rush.domain.MenuItem;
import rush.domain.Money;
import rush.domain.Order;
import rush.domain.PricingPolicy;
import rush.domain.Table;


/**
 * Handles customer seating, taking orders and serving food
 * Waiter can only do one of these tasks each turn
 */
public class Waiter {

    private final Menu menu;
    private final PricingPolicy pricing;


    // Sets the menu and pricing rules used when taking orders
    public Waiter(Menu menu, PricingPolicy pricing) {
        this.menu = menu;
        this.pricing = pricing;
    }


    /**
     * Seats a waiting customer at the first free table
     */
    public ActionResult seatCustomer(GameState state, int customerId) {
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


    /**
     * Takes an order from a seated customer
     * The price is decided using the pricing policy and saved in the order
     */
    public ActionResult takeOrder(GameState state, int customerId) {
        Customer customer = state.findCustomer(customerId).orElse(null);

        // Check that the customer exists
        if (customer == null) {
            return ActionResult.fail("There is no customer with id " + customerId + ".");
        }

        // Check that the customer has not already ordered
        if (customer.getState() == CustomerState.ORDERED) {
            return ActionResult.fail("Customer " + customerId + " has already ordered.");
        }

        // Check that the customer is seated
        if (customer.getState() != CustomerState.SEATED) {
            return ActionResult.fail("Customer " + customerId + " is not seated at a table.");
        }

        // Let the customer choose their food and calculate the price
        MenuItem item = customer.chooseItem(menu);
        int lockedPrice = pricing.priceFor(customer, item);

        // Create and save the order
        Order order = new Order(customer, item, lockedPrice);
        customer.placeOrder(order);

        return ActionResult.ok("Took order from customer " + customerId + ": "
                + item.getName() + " (" + Money.format(lockedPrice) + ").");
    }


    /**
     * Serves a customer’s order if it is ready
     */
    public ActionResult serveOrder(GameState state, int customerId) {
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

        int turn = state.getTurn();

        // Check that the order can be served this turn
        if (!order.isServableOn(turn)) {
            if (order.getStatus() == rush.domain.OrderStatus.READY) {
                return ActionResult.fail("Customer " + customerId
                        + "'s dish only became READY this turn; it can be served next turn.");
            }

            return ActionResult.fail("Customer " + customerId + "'s order is not READY (it is "
                    + order.getStatus() + ").");
        }

        customer.receiveDish(turn);
        return ActionResult.ok("Served " + order.getItem().getName() + " to customer " + customerId + ".");
    }


    /**
     * Returns the tasks the Waiter can do this turn
     */
    public List<String> describeAvailableTasks(GameState state) {
        List<String> tasks = new ArrayList<>();
        int turn = state.getTurn();
        boolean freeTable = state.findFreeTable().isPresent();

        for (Customer customer : state.getAllCustomers()) {
            switch (customer.getState()) {
                case WAITING_IN_QUEUE -> {
                    if (freeTable) tasks.add("Seat customer " + customer.getId());
                }
                case SEATED -> tasks.add("Take order from customer " + customer.getId());
                case ORDERED -> {
                    if (customer.getOrder().isServableOn(turn)) {
                        tasks.add("Serve " + customer.getOrder().getItem().getName()
                                + " to customer " + customer.getId());
                    }
                }
                default -> { }
            }
        }

        tasks.add("Wait");
        return tasks;
    }
}