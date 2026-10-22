package rush.staff;


import rush.control.GameState;
import rush.domain.Customer;
import rush.domain.Money;


/**
 * Collects payment from customers who are ready to pay
 * Only one payment can be collected each turn
 */
public class Cashier {


    /**
     * Finds the customer who has been ready to pay the longest and collects their payment
     * If no customer can pay, nothing happens
     */
    public ActionResult collectPayment(GameState state) {
        int turn = state.getTurn();
        Customer next = null;

        // Look through all customers to find who should pay first
        for (Customer customer : state.getAllCustomers()) {
            if (!customer.canPayOn(turn)) {
                continue;
            }

            // Choose the customer who became ready to pay earliest
            // If they became ready on the same turn, choose the lower customer ID
            if (next == null
                    || customer.getReadyToPayOnTurn() < next.getReadyToPayOnTurn()
                    || (customer.getReadyToPayOnTurn() == next.getReadyToPayOnTurn()
                        && customer.getId() < next.getId())) {
                next = customer;
            }
        }

        // Nobody is ready to pay this turn
        if (next == null) {
            return ActionResult.ok("Cashier: no payment this turn.");
        }

        // Collect the payment from the chosen customer
        int bill = next.pay();
        return ActionResult.ok("Cashier collected " + Money.format(bill) + " from customer " + next.getId() + ".");
    }
}