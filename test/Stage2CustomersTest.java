import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import rush.control.*;
import rush.domain.*;
import rush.staff.*;
import rush.ui.*;


/**
 * Stage 2 checks for VIPCustomer and CriticCustomer
 * Covers their rules on their own, through the real Waiter and GameController,
 * the Stage 2 arrival setting, and 3000 random games with mixed customers
 */
public class Stage2CustomersTest {

    // Counts how many checks failed
    static int failures = 0;

    // Prints whether a check passed or failed
    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) failures++;
    }


    /**
     * A player who makes both staff wait every turn
     * Records which customers left and on which turn
     */
    static class WaitingUI implements GameUI {
        final Map<Integer, Integer> leftOnTurn = new HashMap<>();
        int turn;

        public void showTurnStart(GameState s) { turn = s.getTurn(); }
        public void showTurnSummary(GameState s) {}
        public void showFinalResult(GameState s, GameResult r) {}

        public void showMessage(String m) {
            if (m.endsWith("left unhappy.")) {
                int id = Integer.parseInt(m.replaceAll("\\D+", ""));
                leftOnTurn.put(id, turn);
            }
        }

        public WaiterChoice askWaiterChoice(GameState s) { return WaiterChoice.waitTurn(); }
        public ChefChoice askChefChoice(GameState s) { return ChefChoice.waitTurn(); }
    }


    // Seats one customer and takes their order with the given pricing, returns the locked price
    static int orderPrice(Customer c, PricingPolicy pricing) {
        GameState s = new GameState(1);
        s.setTurn(1);
        s.addArrival(c);

        Waiter w = new Waiter(Menu.standard(), pricing);
        w.seatCustomer(s, c.getId());
        w.takeOrder(s, c.getId());

        return c.getOrder().getPriceCents();
    }


    public static void main(String[] args) throws IOException {
        Menu menu = Menu.standard();
        MenuItem salad = menu.findByName("Salad"), burger = menu.findByName("Burger"), pasta = menu.findByName("Pasta");


        // 1. Waiting loss per turn
        check("waiting loss: Regular 8, VIP 5, Critic 12",
                new RegularCustomer(1).getWaitingLoss() == 8
                && new VIPCustomer(1).getWaitingLoss() == 5
                && new CriticCustomer(1).getWaitingLoss() == 12);

        Customer v = new VIPCustomer(1), k = new CriticCustomer(2);
        v.loseWaitingSatisfaction();
        k.loseWaitingSatisfaction();
        check("after one waiting turn: VIP 95, Critic 88", v.getSatisfaction() == 95 && k.getSatisfaction() == 88);

        Customer floor = new CriticCustomer(3);
        for (int i = 0; i < 9; i++) floor.loseWaitingSatisfaction();
        check("Critic reaches 0 after 9 waiting turns and never goes below 0",
                floor.getSatisfaction() == 0 && floor.hasReachedZeroSatisfaction());


        // 2. Bills (integer cents, VIP rounded to the nearest cent)
        VIPCustomer vip = new VIPCustomer(1);
        check("VIP bills: Salad $8.10, Burger $13.50, Pasta $16.20",
                vip.billFor(salad) == 810 && vip.billFor(burger) == 1350 && vip.billFor(pasta) == 1620);
        check("VIP bill on a $24.30 item (Stage 3 combo example) = $21.87",
                vip.billFor(new BasicMenuItem("Salad + Pasta", 2430, 3)) == 2187);
        check("VIP rounding: 995 cents -> 896 (895.5 rounds up), 999 -> 899 (899.1 rounds down)",
                vip.billFor(new BasicMenuItem("x", 995, 1)) == 896 && vip.billFor(new BasicMenuItem("y", 999, 1)) == 899);
        check("Critic pays full price for every dish",
                new CriticCustomer(1).billFor(salad) == 900
                && new CriticCustomer(1).billFor(burger) == 1500
                && new CriticCustomer(1).billFor(pasta) == 1800);


        // 3. Food choice
        boolean criticAlwaysPasta = true, vipSameAsRegular = true;
        List<MenuItem> bigger = new ArrayList<>(menu.getItems());
        bigger.add(new BasicMenuItem("Burger + Pasta", 2970, 3));
        Menu bigMenu = new Menu(bigger);

        for (int id = 1; id <= 9; id++) {
            criticAlwaysPasta &= new CriticCustomer(id).chooseItem(menu).getName().equals("Pasta")
                    && new CriticCustomer(id).chooseItem(bigMenu).getName().equals("Pasta");
            vipSameAsRegular &= new VIPCustomer(id).chooseItem(menu).getName()
                    .equals(new RegularCustomer(id).chooseItem(menu).getName());
        }

        check("Critic always orders Pasta, even with a bigger menu", criticAlwaysPasta);
        check("VIP uses the same ID food cycle as Regular", vipSameAsRegular);

        boolean threw = false;
        try {
            new CriticCustomer(1).chooseItem(new Menu(List.of(salad, burger)));
        } catch (IllegalStateException e) {
            threw = true;
        }
        check("Critic with no Pasta on the menu gives a clear error", threw);


        // 4. Through the real Waiter: price is locked when the order is taken
        check("Waiter locks VIP id 2 Pasta at $16.20", orderPrice(new VIPCustomer(2), new StandardPricing()) == 1620);
        check("Waiter locks Critic Pasta at $18.00", orderPrice(new CriticCustomer(5), new StandardPricing()) == 1800);

        PricingPolicy happyHour = (c, i) -> new StandardPricing().priceFor(c, i) * 8 / 10;
        check("VIP Burger with a 20%-off wrapper = $10.80 (brief example, VIP discount first)",
                orderPrice(new VIPCustomer(1), happyHour) == 1080);


        // 5. Stage 2 arrival setting
        ArrivalSchedule stage2 = ArrivalSchedule.stage2Setting();
        List<String> arrivals = new ArrayList<>();

        for (int t = 1; t <= GameConfig.MAX_TURNS; t++) {
            Customer c = stage2.arrivalAt(t);
            if (c != null) arrivals.add(t + ":" + c.getTypeName() + "#" + c.getId());
        }

        check("Stage 2 arrivals: T1 Regular, T4 VIP, T7 Critic, then Regular on 10, 13, 16 " + arrivals,
                arrivals.equals(List.of("1:Regular#1", "4:VIP#2", "7:Critic#3", "10:Regular#4", "13:Regular#5", "16:Regular#6")));


        // 6. Through the real GameController: abandonment follows each type's own loss
        WaitingUI waiting = new WaitingUI();
        GameState s = new GameState(GameConfig.TABLE_COUNT);
        GameResult r = new GameController(s, ArrivalSchedule.stage2Setting(), new Waiter(menu, new StandardPricing()),
                new Chef(), new Cashier(), waiting).run();

        Customer vipInGame = s.findCustomer(2).orElseThrow();
        check("all-wait game: Regular #1 leaves on turn 13, Critic #3 on turn 15 " + waiting.leftOnTurn,
                waiting.leftOnTurn.equals(Map.of(1, 13, 3, 15)));
        check("all-wait game: VIP #2 waits 15 turns and stays (100 - 15 x 5 = 25)",
                vipInGame.getSatisfaction() == 25 && vipInGame.getState() == CustomerState.WAITING_IN_QUEUE);
        check("all-wait game ends in DEFEAT", r == GameResult.DEFEAT);


        // 7. 3000 random games with the Stage 2 arrivals: no crashes, totals match the actions shown
        int bad = 0;
        for (long seed = 0; seed < 3000; seed++) {
            RandomGamesTest.RandomUI ui = new RandomGamesTest.RandomUI(new Random(seed));
            GameState g = new GameState(GameConfig.TABLE_COUNT);

            try {
                new GameController(g, ArrivalSchedule.stage2Setting(), new Waiter(menu, new StandardPricing()),
                        new Chef(), new Cashier(), ui).run();
            } catch (Throwable t) {
                bad++;
                continue;
            }

            boolean ok = g.getRevenueCents() == ui.paidCents && g.getPaidCount() == ui.paidMsgs
                    && g.getServedCount() == ui.servedMsgs && g.getUnhappyDepartures() == ui.leftMsgs
                    && g.getPaidCount() <= 5;

            for (Customer c : g.getAllCustomers()) {
                ok &= c.getSatisfaction() >= 0 && c.getSatisfaction() <= 100;
                if (c.getOrder() != null) ok &= c.getOrder().getPriceCents() == c.billFor(c.getOrder().getItem());
            }

            if (!ok) bad++;
        }
        check("3000 random Stage 2 games: no crashes, revenue and counts match, bills match each customer's rule", bad == 0);


        // 8. No customer-type checks in the controller, staff or UI (brief 4.2)
        boolean noTypeChecks = true;
        for (String dir : List.of("src/rush/control", "src/rush/staff", "src/rush/ui")) {
            try (Stream<Path> files = Files.walk(Path.of(dir))) {
                for (Path p : files.filter(f -> f.toString().endsWith(".java")).toList()) {
                    String code = Files.readString(p);
                    noTypeChecks &= !code.contains("instanceof") && !code.contains("getTypeName().equals");
                }
            }
        }
        check("controller, staff and UI never check the customer type", noTypeChecks);


        System.out.println(failures == 0 ? "ALL STAGE 2 CHECKS PASSED" : failures + " CHECK(S) FAILED");
        if (failures != 0) System.exit(1);
    }
}
