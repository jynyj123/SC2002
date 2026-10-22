package rush.ui;


import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import rush.control.GameConfig;
import rush.control.GameResult;
import rush.control.GameState;
import rush.domain.Customer;
import rush.domain.Money;
import rush.domain.Order;
import rush.domain.Table;


/**
 * Displays the game in the command line and reads the user’s choices
 * It does not check game rules, it only shows information and passes choices to the controller
 */
public class ConsoleUI implements GameUI {

    private final Scanner in;
    private final PrintStream out;


    // Uses normal keyboard input and console output
    public ConsoleUI() {
        this(System.in, System.out);
    }


    // Allows different input and output streams to be used, which is useful for testing
    public ConsoleUI(InputStream input, PrintStream output) {
        this.in = new Scanner(input);
        this.out = output;
    }


    // ------------------------------------------------------------------ display


    /**
     * Shows the information at the start of each turn
     */
    @Override
    public void showTurnStart(GameState state) {
        out.println();
        out.println("=============== TURN " + state.getTurn() + " of " + GameConfig.MAX_TURNS + " ===============");
        out.println("Revenue: " + Money.format(state.getRevenueCents())
                + "  |  Avg satisfaction (paid): " + averageText(state)
                + "  |  Paid customers: " + state.getPaidCount());


        out.println("Waiting queue: " + queueText(state));
        out.println("Tables:");

        // Show whether each table is free or who is sitting there
        for (Table table : state.getTables()) {
            if (table.isFree()) {
                out.println("  Table " + table.getId() + ": free");
            } else {
                Customer c = table.getOccupant();
                out.println("  Table " + table.getId() + ": customer #" + c.getId()
                        + " (" + c.getTypeName() + "), satisfaction " + c.getSatisfaction()
                        + ", " + c.getState() + ", order: " + orderText(c));
            }
        }
    }


    /**
     * Prints a message to the console
     */
    @Override
    public void showMessage(String message) {
        out.println("  " + message);
    }


    /**
     * Shows the results at the end of each turn
     */
    @Override
    public void showTurnSummary(GameState state) {
        out.println("--- End of turn " + state.getTurn() + " ---");
        out.println("Served: " + state.getServedCount()
                + "  |  Unhappy departures: " + state.getUnhappyDepartures()
                + "  |  Revenue: " + Money.format(state.getRevenueCents())
                + "  |  Avg satisfaction (paid): " + averageText(state)
                + "  |  Turns used: " + state.getTurn());
    }


    /**
     * Shows the final game results and whether the win conditions were met
     */
    @Override
    public void showFinalResult(GameState state, GameResult result) {
        out.println();
        out.println("=============== GAME OVER: " + result + " ===============");
        out.println("Paid customers:       " + state.getPaidCount()
                + " (target " + GameConfig.MIN_PAID_CUSTOMERS + "+)");
        out.println("Revenue:              " + Money.format(state.getRevenueCents())
                + " (target " + Money.format(GameConfig.MIN_REVENUE_CENTS) + "+)");
        out.println("Avg satisfaction:     " + averageText(state)
                + " (target " + (int) GameConfig.MIN_AVG_SATISFACTION + "+)");
        out.println("Served: " + state.getServedCount()
                + "  |  Unhappy departures: " + state.getUnhappyDepartures()
                + "  |  Turns used: " + state.getTurn());
    }


    // ------------------------------------------------------------------ input


    /**
     * Asks the user what the Waiter should do this turn
     */
    @Override
    public WaiterChoice askWaiterChoice(GameState state) {
        out.println("Waiter task:");
        out.println("  1) Seat a queued customer");
        out.println("  2) Take a seated customer's order");
        out.println("  3) Serve a READY order");
        out.println("  0) Wait");

        int option = readInt("Choice: ", 0, 3);

        WaiterAction action = switch (option) {
            case 1 -> WaiterAction.SEAT_CUSTOMER;
            case 2 -> WaiterAction.TAKE_ORDER;
            case 3 -> WaiterAction.SERVE_ORDER;
            default -> WaiterAction.WAIT;
        };

        if (action == WaiterAction.WAIT) {
            return WaiterChoice.waitTurn();
        }

        return new WaiterChoice(action, readInt("Customer id: ", Integer.MIN_VALUE, Integer.MAX_VALUE));
    }


    /**
     * Asks the user what the Chef should do this turn
     */
    @Override
    public ChefChoice askChefChoice(GameState state) {
        out.println("Chef task:");
        out.println("  1) Advance an order by one unit");
        out.println("  0) Wait");

        int option = readInt("Choice: ", 0, 1);

        if (option == 0) {
            return ChefChoice.waitTurn();
        }

        return new ChefChoice(ChefAction.ADVANCE_ORDER,
                readInt("Customer id: ", Integer.MIN_VALUE, Integer.MAX_VALUE));
    }


    /**
     * Reads a whole number from the user
     * Asks again if the input is not a valid number or is outside the allowed range
     */
    private int readInt(String prompt, int min, int max) {
        boolean anyNumber = (min == Integer.MIN_VALUE && max == Integer.MAX_VALUE);

        while (true) {
            out.print(prompt);
            out.flush();
            String line = readLine().trim();

            try {
                int value = Integer.parseInt(line);

                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Input was not a number, so ask again
            }

            out.println(anyNumber
                    ? "  Please enter a whole number."
                    : "  Please enter a number from " + min + " to " + max + ".");
        }
    }


    /**
     * Reads one line of input from the user
     */
    private String readLine() {
        if (!in.hasNextLine()) {
            throw new InputEndedException();
        }
        return in.nextLine();
    }


    // ------------------------------------------------------------------ formatting helpers


    /**
     * Formats the average satisfaction of paid customers
     * Shows N/A if nobody has paid yet
     */
    private String averageText(GameState state) {
        return state.getAverageSatisfaction().isPresent()
                ? String.format("%.1f", state.getAverageSatisfaction().getAsDouble())
                : "N/A";
    }


    /**
     * Formats the list of customers waiting in the queue
     */
    private String queueText(GameState state) {
        if (state.getWaitingQueue().isEmpty()) {
            return "empty";
        }

        List<String> entries = new ArrayList<>();

        for (Customer c : state.getWaitingQueue()) {
            entries.add("#" + c.getId() + " " + c.getTypeName() + " (satisfaction " + c.getSatisfaction() + ")");
        }

        return String.join(", ", entries);
    }


    /**
     * Formats the customer’s current order for display
     */
    private String orderText(Customer customer) {
        Order order = customer.getOrder();

        if (order == null) {
            return "none";
        }

        String text = order.getItem().getName() + " - " + order.getStatus();

        if (order.canBePrepared()) {
            text += ", " + order.getUnitsRemaining() + " unit(s) left";
        }

        return text;
    }
}