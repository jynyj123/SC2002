package rush.control;


import rush.domain.Customer;
import rush.staff.ActionResult;
import rush.staff.Cashier;
import rush.staff.Chef;
import rush.staff.Waiter;
import rush.ui.ChefChoice;
import rush.ui.GameUI;
import rush.ui.WaiterChoice;


/**
 * Controls the main game loop and carries out the steps for each turn
 * The user interface is handled by GameUI, while customer behaviour is handled
 * by the Customer subclasses
 */
public class GameController {

    // Stores the current game information such as customers, tables and revenue
    private final GameState state;

    // Decides which customer arrives on each turn
    private final ArrivalSchedule arrivals;

    // The three staff members used during the game
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;

    // Handles all messages and user input
    private final GameUI ui;


    // Saves the objects needed to run the game
    public GameController(GameState state, ArrivalSchedule arrivals,
                          Waiter waiter, Chef chef, Cashier cashier, GameUI ui) {
        this.state = state;
        this.arrivals = arrivals;
        this.waiter = waiter;
        this.chef = chef;
        this.cashier = cashier;
        this.ui = ui;
    }


    /**
     * Runs all 18 turns and returns whether the game was won or lost
     */
    public GameResult run() {
        for (int turn = 1; turn <= GameConfig.MAX_TURNS; turn++) {
            state.setTurn(turn);

            stepArrivals();                 // step 1
            showTurnStart();                // show the start of the turn
            stepManagerChoices();           // steps 2 and 3
            stepEatingAndPayment();         // step 4
            stepWaitingAndAbandonment();    // step 5
            ui.showTurnSummary(state);      // show what happened this turn
        }

        // Check whether the final results meet the win conditions
        GameResult result = evaluateResult();
        ui.showFinalResult(state, result);
        return result;
    }



    /**
     * Adds the scheduled customer to the queue if there is one this turn
     */
    void stepArrivals() {
        Customer arrival = arrivals.arrivalAt(state.getTurn());

        if (arrival != null) {
            state.addArrival(arrival);
        }
    }


    /**
     * Shows the current game state and the tasks each staff member can do
     */
    private void showTurnStart() {
        ui.showTurnStart(state);

        for (String task : waiter.describeAvailableTasks(state)) {
            ui.showMessage("Waiter can: " + task);
        }

        for (String task : chef.describeAvailableTasks(state)) {
            ui.showMessage("Chef can: " + task);
        }
    }

    /**
     * Gets the Waiter choice first, then the Chef choice
     * If the choice is invalid, the user is asked again
     */
    void stepManagerChoices() {
        promptUntilAccepted(this::askWaiterOnce);
        promptUntilAccepted(this::askChefOnce);
    }


    /**
     * Represents one staff choice that can either succeed or fail
     */
    private interface ChoiceAttempt {
        ActionResult attempt();
    }


    /**
     * Keeps asking for a choice until a valid one is entered
     */
    private void promptUntilAccepted(ChoiceAttempt choiceAttempt) {
        while (true) {
            ActionResult result = choiceAttempt.attempt();
            ui.showMessage(result.message());

            if (result.success()) {
                return;
            }
        }
    }


    /**
     * Asks the Waiter for a choice and carries it out
     */
    private ActionResult askWaiterOnce() {
        WaiterChoice choice = ui.askWaiterChoice(state);

        return switch (choice.action()) {
            case WAIT          -> ActionResult.ok("Waiter waits this turn.");
            case SEAT_CUSTOMER -> waiter.seatCustomer(state, choice.customerId());
            case TAKE_ORDER    -> waiter.takeOrder(state, choice.customerId());
            case SERVE_ORDER   -> waiter.serveOrder(state, choice.customerId());
        };
    }


    /**
     * Asks the Chef for a choice and carries it out
     */
    private ActionResult askChefOnce() {
        ChefChoice choice = ui.askChefChoice(state);

        return switch (choice.action()) {
            case WAIT          -> ActionResult.ok("Chef waits this turn.");
            case ADVANCE_ORDER -> chef.advanceOrder(state, choice.customerId());
        };
    }


    /**
     * Customers who finished eating become ready to pay
     * The Cashier then collects one payment if possible
     */
    void stepEatingAndPayment() {
        int turn = state.getTurn();

        // Mark customers who have finished eating as ready to pay
        for (Customer customer : state.getAllCustomers()) {
            if (customer.hasFinishedEatingOn(turn)) {
                customer.finishEating(turn);
            }
        }

        ui.showMessage(cashier.collectPayment(state).message());
    }

    /**
     * Customers who are still waiting or seated lose satisfaction
     * Anyone whose satisfaction reaches zero leaves without paying
     */
    void stepWaitingAndAbandonment() {
        for (Customer customer : state.getAllCustomers()) {
            if (!customer.losesSatisfactionAtTurnEnd()) {
                continue;
            }

            customer.loseWaitingSatisfaction();

            if (customer.hasReachedZeroSatisfaction()) {
                customer.leaveUnhappy();
                ui.showMessage("Customer " + customer.getId() + " left unhappy.");
            }
        }
    }

    // Checks whether the game meets all three win conditions
    GameResult evaluateResult() {
        boolean enoughPaid = state.getPaidCount() >= GameConfig.MIN_PAID_CUSTOMERS;
        boolean enoughRevenue = state.getRevenueCents() >= GameConfig.MIN_REVENUE_CENTS;
        boolean happyEnough = state.getAverageSatisfaction().orElse(0.0) >= GameConfig.MIN_AVG_SATISFACTION;

        return (enoughPaid && enoughRevenue && happyEnough) ? GameResult.VICTORY : GameResult.DEFEAT;
    }
}