import java.util.*;
import rush.control.*;
import rush.domain.*;
import rush.staff.*;
import rush.ui.*;


/**
 * Tests the GameController using the real Waiter, Chef and Cashier
 * A scripted player is used instead of typing choices manually
 */
public class ControllerSmokeTest {


    // A scripted player that makes choices automatically
    static class AutoUI implements GameUI {

        // Decides whether the player plays well or always waits
        final boolean playWell;

        // Stores messages shown during the game
        final List<String> log = new ArrayList<>();

        // Makes sure the invalid choice is only tried once
        int invalidAttemptsMade = 0;

        AutoUI(boolean playWell) { this.playWell = playWell; }


        public void showTurnStart(GameState s) { }

        public void showMessage(String m) { log.add("T" + "?" + " " + m); }

        public void showTurnSummary(GameState s) {
            System.out.printf("  end T%02d  revenue=%d paid=%d served=%d left=%d avg=%s%n", s.getTurn(),
                    s.getRevenueCents(), s.getPaidCount(), s.getServedCount(), s.getUnhappyDepartures(),
                    s.getAverageSatisfaction().isPresent() ? String.format("%.1f", s.getAverageSatisfaction().getAsDouble()) : "N/A");
        }

        public void showFinalResult(GameState s, GameResult r) { System.out.println("FINAL: " + r); }


        public WaiterChoice askWaiterChoice(GameState s) {
            if (!playWell) return WaiterChoice.waitTurn();

            // Try an invalid customer ID once to check that the game handles it
            if (s.getTurn() == 2 && invalidAttemptsMade == 0) {
                invalidAttemptsMade++;
                return new WaiterChoice(WaiterAction.SERVE_ORDER, 999);
            }

            // Serve an order if one is ready
            for (Customer c : s.getAllCustomers())
                if (c.getOrder() != null && c.getOrder().isServableOn(s.getTurn())) return new WaiterChoice(WaiterAction.SERVE_ORDER, c.getId());

            // Take an order from a seated customer
            for (Customer c : s.getAllCustomers())
                if (c.getState() == CustomerState.SEATED) return new WaiterChoice(WaiterAction.TAKE_ORDER, c.getId());

            // Seat the first waiting customer if there is a free table
            if (!s.getWaitingQueue().isEmpty() && s.findFreeTable().isPresent())
                return new WaiterChoice(WaiterAction.SEAT_CUSTOMER, s.getWaitingQueue().get(0).getId());

            return WaiterChoice.waitTurn();
        }

        public ChefChoice askChefChoice(GameState s) {
            if (!playWell) return ChefChoice.waitTurn();

            // Prepare an order if there is one that can still be prepared
            for (Customer c : s.getAllCustomers())
                if (c.getOrder() != null && c.getOrder().canBePrepared()) return new ChefChoice(ChefAction.ADVANCE_ORDER, c.getId());

            return ChefChoice.waitTurn();
        }
    }


    // Creates and runs a full game using the scripted player
    static GameResult play(boolean playWell, AutoUI ui) {
        Menu menu = Menu.standard();
        GameState state = new GameState(GameConfig.TABLE_COUNT);
        return new GameController(state, ArrivalSchedule.basicSetting(), new Waiter(menu, new StandardPricing()), new Chef(), new Cashier(), ui).run();
    }


    public static void main(String[] args) {

        // First run: the scripted player makes good choices
        System.out.println("== Run 1: player does the right thing ==");
        AutoUI good = new AutoUI(true);
        GameResult r1 = play(true, good);

        // Check that the invalid customer ID was rejected once
        long invalidMsgs = good.log.stream().filter(m -> m.contains("no customer with id 999")).count();
        System.out.println("Invalid choice rejected and re-prompted: " + (invalidMsgs == 1));


        // Second run: both staff wait every turn
        System.out.println("== Run 2: Waiter and Chef always wait ==");
        AutoUI bad = new AutoUI(false);
        GameResult r2 = play(false, bad);

        // Check how many customers left unhappy
        System.out.println("Customers who left unhappy shown in log: " + bad.log.stream().filter(m -> m.contains("left unhappy")).count());


        System.out.println("RESULTS -> good: " + r1 + ", bad: " + r2);
    }
}