import java.util.*;
import java.util.regex.*;
import rush.control.*; import rush.domain.*; import rush.staff.*; import rush.ui.*;


/**
 * Plays many random games to check that the program does not crash
 * It also checks that the game totals match what happened during the game
 */
public class RandomGamesTest {

    // A random player that makes random choices, including invalid ones
    static class RandomUI implements GameUI {

        final Random r;

        // Counts used to compare with the game totals
        int servedMsgs = 0, leftMsgs = 0, paidCents = 0, paidMsgs = 0;

        // Used to find the payment amount from the Cashier message
        static final Pattern PAID = Pattern.compile("collected \\$(\\d+)\\.(\\d\\d) from customer");

        RandomUI(Random r) { this.r = r; }

        public void showTurnStart(GameState s) {}
        public void showTurnSummary(GameState s) {}
        public void showFinalResult(GameState s, GameResult x) {}

        public void showMessage(String m) {
            // Count served orders
            if (m.startsWith("Served ")) servedMsgs++;

            // Count customers who left unhappy
            if (m.endsWith("left unhappy.")) leftMsgs++;

            // Find and add up payments from the Cashier messages
            Matcher k = PAID.matcher(m);
            if (k.find()) {
                paidMsgs++;
                paidCents += Integer.parseInt(k.group(1)) * 100 + Integer.parseInt(k.group(2));
            }
        }

        public WaiterChoice askWaiterChoice(GameState s) {
            // Choose a random Waiter action and customer ID
            return new WaiterChoice(WaiterAction.values()[r.nextInt(4)], r.nextInt(9) - 1);
        }

        public ChefChoice askChefChoice(GameState s) {
            // Choose a random Chef action and customer ID
            return new ChefChoice(ChefAction.values()[r.nextInt(2)], r.nextInt(9) - 1);
        }
    }


    // Counts how many rule violations were found
    static int bad = 0;

    // Records a violation if a check fails
    static void inv(boolean ok, String msg, long seed) {
        if (!ok) {
            bad++;
            if (bad <= 10) System.out.println("VIOLATION (seed " + seed + "): " + msg);
        }
    }


    public static void main(String[] args) {
        int games = 3000, victories = 0, maxPaid = 0;

        // Customer states that require a table
        EnumSet<CustomerState> needTable = EnumSet.of(CustomerState.SEATED, CustomerState.ORDERED,
                CustomerState.EATING, CustomerState.READY_TO_PAY);

        // Play many games using different random seeds
        for (long seed = 0; seed < games; seed++) {
            RandomUI ui = new RandomUI(new Random(seed));
            GameState s = new GameState(GameConfig.TABLE_COUNT);
            GameResult res;

            try {
                res = new GameController(s, ArrivalSchedule.basicSetting(), new Waiter(Menu.standard(), new StandardPricing()),
                        new Chef(), new Cashier(), ui).run();
            } catch (Throwable t) {
                System.out.println("EXCEPTION seed " + seed + ": " + t);
                bad++;
                continue;
            }

            if (res == GameResult.VICTORY) victories++;


            // Check that the game totals match the messages shown during the game
            inv(s.getServedCount() == ui.servedMsgs, "served " + s.getServedCount() + " vs msgs " + ui.servedMsgs, seed);
            inv(s.getUnhappyDepartures() == ui.leftMsgs, "departures " + s.getUnhappyDepartures() + " vs msgs " + ui.leftMsgs, seed);
            inv(s.getRevenueCents() == ui.paidCents, "revenue " + s.getRevenueCents() + " vs msgs " + ui.paidCents, seed);
            inv(s.getPaidCount() == ui.paidMsgs, "paid " + s.getPaidCount() + " vs msgs " + ui.paidMsgs, seed);


            // Check that the tables and customers match each other
            Set<Customer> seated = new HashSet<>();

            for (Table t : s.getTables()) {
                if (!t.isFree()) {
                    seated.add(t.getOccupant());
                    inv(t.getOccupant().getTable() == t, "table/customer mismatch", seed);
                }
            }

            for (Customer c : s.getAllCustomers()) {
                inv(c.getSatisfaction() >= 0 && c.getSatisfaction() <= 100, "satisfaction out of range", seed);
                inv(needTable.contains(c.getState()) == seated.contains(c), "table occupancy mismatch " + c.getState(), seed);
                inv((c.getState() == CustomerState.WAITING_IN_QUEUE) == s.getWaitingQueue().contains(c), "queue mismatch", seed);

                if (c.getState() == CustomerState.PAID)
                    inv(c.getSatisfactionAtPayment() > 0 && c.getOrder().getStatus() == OrderStatus.PAID, "bad paid customer", seed);

                if (c.getState() == CustomerState.LEFT)
                    inv(c.getSatisfaction() == 0 && (c.getOrder() == null || c.getOrder().getStatus() == OrderStatus.CANCELLED), "bad departed customer", seed);
            }

            inv(s.getPaidCount() <= 5, "more than 5 paid", seed);
            inv(s.getTurn() == GameConfig.MAX_TURNS, "did not run all turns", seed);

            maxPaid = Math.max(maxPaid, s.getPaidCount());
        }

        // Show the final test results
        System.out.println(games + " random games, violations: " + bad + " (victories " + victories + ", max paid " + maxPaid + ")");
        System.out.println(bad == 0 ? "ALL RANDOM-GAME CHECKS PASSED" : "CHECKS FAILED");
        if (bad != 0) System.exit(1);
    }
}