package rush.control;


import rush.domain.Customer;
import rush.domain.HappyHour;
import rush.staff.ActionResult;
import rush.staff.Cashier;
import rush.staff.Chef;
import rush.staff.Host;
import rush.staff.Waiter;
import rush.ui.ChefChoice;
import rush.ui.GameUI;
import rush.ui.HostChoice;
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

    // The staff members used during the game
    // host is null in the Stage 1 and 2 game, which has no Host
    private final Host host;
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;

    // Stage 3 Happy Hour, null in the Stage 1 and 2 game
    // The same object is inside the Waiter's HappyHourPricing, so switching it on here changes prices
    private final HappyHour happyHour;

    // Handles all messages and user input
    private final GameUI ui;


    /**
     * Stage 1 and 2 game: Waiter, Chef and Cashier only, no Host or Happy Hour
     * Kept so the earlier stages and their tests still run unchanged
     */
    public GameController(GameState state, ArrivalSchedule arrivals,
                          Waiter waiter, Chef chef, Cashier cashier, GameUI ui) {
        this(state, arrivals, null, waiter, chef, cashier, null, ui);
    }


    /**
     * Stage 3 game: adds the Host and Happy Hour
     */
    public GameController(GameState state, ArrivalSchedule arrivals, Host host,
                          Waiter waiter, Chef chef, Cashier cashier, HappyHour happyHour, GameUI ui) {
        this.state = state;
        this.arrivals = arrivals;
        this.host = host;
        this.waiter = waiter;
        this.chef = chef;
        this.cashier = cashier;
        this.happyHour = happyHour;
        this.ui = ui;
    }


    /**
     * Runs all 18 turns and returns whether the game was won or lost
     */
    public GameResult run() {
        for (int turn = 1; turn <= GameConfig.MAX_TURNS; turn++) {
            state.setTurn(turn);

            stepArrivals();                 // step 1
            ui.showTurnStart(state);        // show the start of the turn
            stepHappyHour();                // Stage 3: offer Happy Hour, recovery while it is active
            showAvailableTasks();           // what each staff member can do
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
     * Stage 3: at the start of the turn, offers Happy Hour if it has not been used,
     * then shows how long it has left
     * While it is active, customers still waiting for a table or food recover satisfaction
     */
    void stepHappyHour() {
        if (happyHour == null) {
            return;
        }

        int turn = state.getTurn();

        // Offer Happy Hour until it has been used
        if (!happyHour.isUsed()) {
            ui.showMessage("Happy Hour: available (once per game, lasts this turn and the next).");

            if (ui.askHappyHourActivation(state)) {
                happyHour.activate(turn);
            }
        }

        if (!happyHour.isActiveOn(turn)) {
            if (happyHour.isUsed()) {
                ui.showMessage("Happy Hour: used.");
            }
            return;
        }

        // The game ends after the last turn, so do not count turns past it
        int turnsLeft = Math.min(happyHour.turnsRemaining(turn), GameConfig.MAX_TURNS - turn + 1);
        ui.showMessage("Happy Hour: ACTIVE, " + turnsLeft + " turn(s) left including this one. Orders taken now get 20% off.");

        // Customers still waiting for a table or food are the ones who lose satisfaction at turn end
        for (Customer customer : state.getAllCustomers()) {
            if (!customer.losesSatisfactionAtTurnEnd()) {
                continue;
            }

            int before = customer.getSatisfaction();
            customer.recoverSatisfaction(HappyHour.RECOVERY);

            ui.showMessage("Customer " + customer.getId() + " recovers satisfaction: "
                    + before + " -> " + customer.getSatisfaction() + ".");
        }
    }


    /**
     * Shows the tasks each staff member can do
     */
    private void showAvailableTasks() {
        if (host != null) {
            for (String task : host.describeAvailableTasks(state)) {
                ui.showMessage("Host can: " + task);
            }
        }

        for (String task : waiter.describeAvailableTasks(state)) {
            ui.showMessage("Waiter can: " + task);
        }

        for (String task : chef.describeAvailableTasks(state)) {
            ui.showMessage("Chef can: " + task);
        }
    }

    /**
     * Gets the Host choice first (Stage 3), then the Waiter choice, then the Chef choice
     * The Host goes first so the Waiter can take the order of a customer the Host just seated
     * If the choice is invalid, the user is asked again
     */
    void stepManagerChoices() {
        if (host != null) {
            promptUntilAccepted(this::askHostOnce);
        }
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
     * Stage 3: asks the Host for a choice and carries it out
     */
    private ActionResult askHostOnce() {
        HostChoice choice = ui.askHostChoice(state);

        return switch (choice.action()) {
            case WAIT          -> ActionResult.ok("Host waits this turn.");
            case SEAT_CUSTOMER -> host.seatCustomer(state, choice.customerId());
        };
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