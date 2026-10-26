import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import rush.control.*;
import rush.domain.*;
import rush.staff.*;
import rush.ui.*;


/**
 * Stage 3 checks for the Host, ComboMeal and Happy Hour
 * Covers each rule on its own, through the real staff and GameController,
 * and 3000 random games with the full Stage 3 setup
 */
public class Stage3Test {

    // Counts how many checks failed
    static int failures = 0;

    // Prints whether a check passed or failed
    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) failures++;
    }


    // Builds a Stage 3 game the same way as Main
    static GameController stage3Game(GameState state, GameUI ui, HappyHour happyHour) {
        Menu menu = Menu.stage3();
        PricingPolicy pricing = new HappyHourPricing(new StandardPricing(), happyHour, state::getTurn);

        return new GameController(state, ArrivalSchedule.stage2Setting(),
                new Host(), new Waiter(menu, pricing), new Chef(), new Cashier(), happyHour, ui);
    }


    // Happy Hour price with nearest-cent rounding, used to work out expected bills
    static int happyHourPrice(int cents) { return (cents * 80 + 50) / 100; }


    /**
     * A scripted player for the controller checks
     * The Host and Waiter seat, order and serve; the Chef cooks the lowest-id order
     * Happy Hour is switched on at a chosen turn, and the Host can try one invalid choice first
     */
    static class ScriptedUI implements GameUI {
        final int happyHourTurn;
        final int invalidHostTurn;
        final List<String> messages = new ArrayList<>();

        // Counts how often the Host was asked on each turn, and how many Host seatings succeeded
        final Map<Integer, Integer> hostAsks = new HashMap<>();
        final Map<Integer, Integer> hostSeated = new HashMap<>();

        int turn;
        boolean triedInvalidHost = false;

        ScriptedUI(int happyHourTurn, int invalidHostTurn) {
            this.happyHourTurn = happyHourTurn;
            this.invalidHostTurn = invalidHostTurn;
        }

        public void showTurnStart(GameState s) { turn = s.getTurn(); }
        public void showTurnSummary(GameState s) {}
        public void showFinalResult(GameState s, GameResult r) {}

        public void showMessage(String m) {
            messages.add("T" + turn + " " + m);
            if (m.startsWith("Host: Seated")) hostSeated.merge(turn, 1, Integer::sum);
        }

        public boolean askHappyHourActivation(GameState s) { return s.getTurn() == happyHourTurn; }

        public HostChoice askHostChoice(GameState s) {
            hostAsks.merge(s.getTurn(), 1, Integer::sum);

            if (s.getTurn() == invalidHostTurn && !triedInvalidHost) {
                triedInvalidHost = true;
                return new HostChoice(HostAction.SEAT_CUSTOMER, 999);
            }

            if (!s.getWaitingQueue().isEmpty() && s.findFreeTable().isPresent()) {
                return new HostChoice(HostAction.SEAT_CUSTOMER, s.getWaitingQueue().get(0).getId());
            }

            return HostChoice.waitTurn();
        }

        public WaiterChoice askWaiterChoice(GameState s) {
            for (Customer c : s.getAllCustomers())
                if (c.getOrder() != null && c.getOrder().isServableOn(s.getTurn())) return new WaiterChoice(WaiterAction.SERVE_ORDER, c.getId());

            for (Customer c : s.getAllCustomers())
                if (c.getState() == CustomerState.SEATED) return new WaiterChoice(WaiterAction.TAKE_ORDER, c.getId());

            if (!s.getWaitingQueue().isEmpty() && s.findFreeTable().isPresent())
                return new WaiterChoice(WaiterAction.SEAT_CUSTOMER, s.getWaitingQueue().get(0).getId());

            return WaiterChoice.waitTurn();
        }

        public ChefChoice askChefChoice(GameState s) {
            for (Customer c : s.getAllCustomers())
                if (c.getOrder() != null && c.getOrder().canBePrepared()) return new ChefChoice(ChefAction.ADVANCE_ORDER, c.getId());

            return ChefChoice.waitTurn();
        }

        boolean said(String text) { return messages.stream().anyMatch(m -> m.contains(text)); }
    }


    /**
     * Random player for Stage 3: random Host choices and a random Happy Hour switch on top of
     * the Stage 1 random player. Records what is needed to check the results afterwards
     */
    static class RandomStage3UI extends RandomGamesTest.RandomUI {
        GameState state;
        int turn, hostSeatings = 0, activations = 0, askedAfterActivation = 0;
        boolean hostTwiceInOneTurn = false;
        Set<Integer> hostSeatedTurns = new HashSet<>();

        // Turn each customer's order was taken
        final Map<Integer, Integer> orderTurn = new HashMap<>();

        // Who was waiting when Happy Hour started the turn, and who actually recovered
        Set<Integer> expectedRecovery = new HashSet<>(), actualRecovery = new HashSet<>();
        boolean recoveryMismatch = false;

        RandomStage3UI(Random r) { super(r); }

        @Override public void showTurnStart(GameState s) {
            checkRecovery();
            state = s;
            turn = s.getTurn();
        }

        @Override public void showMessage(String m) {
            super.showMessage(m);

            if (m.startsWith("Happy Hour: ACTIVE")) {
                for (Customer c : state.getAllCustomers())
                    if (c.losesSatisfactionAtTurnEnd()) expectedRecovery.add(c.getId());
            }

            if (m.contains("recovers satisfaction")) actualRecovery.add(Integer.parseInt(m.replaceAll("^Customer (\\d+).*$", "$1")));

            if (m.startsWith("Took order from customer ")) orderTurn.put(Integer.parseInt(m.replaceAll("^Took order from customer (\\d+):.*$", "$1")), turn);

            if (m.startsWith("Host: Seated")) {
                hostSeatings++;
                if (!hostSeatedTurns.add(turn)) hostTwiceInOneTurn = true;
            }
        }

        @Override public void showTurnSummary(GameState s) { checkRecovery(); }

        // Compares who recovered with who was waiting, then clears both for the next turn
        void checkRecovery() {
            if (!expectedRecovery.equals(actualRecovery)) recoveryMismatch = true;
            expectedRecovery.clear();
            actualRecovery.clear();
        }

        @Override public boolean askHappyHourActivation(GameState s) {
            if (activations > 0) askedAfterActivation++;

            boolean on = r.nextInt(8) == 0;
            if (on) activations++;
            return on;
        }

        @Override public HostChoice askHostChoice(GameState s) {
            return new HostChoice(HostAction.values()[r.nextInt(2)], r.nextInt(9) - 1);
        }
    }


    public static void main(String[] args) throws IOException {
        Menu menu = Menu.stage3();
        MenuItem salad = menu.findByName("Salad"), burger = menu.findByName("Burger"), pasta = menu.findByName("Pasta");


        // 1. ComboMeal: price is 90% of the two items (nearest cent), units are the sum
        ComboMeal saladPasta = new ComboMeal(salad, pasta);
        check("Salad + Pasta combo: $24.30 and 3 units (brief example)",
                saladPasta.getPriceCents() == 2430 && saladPasta.getPrepUnits() == 3);
        check("Salad + Burger combo: $21.60 and 2 units",
                new ComboMeal(salad, burger).getPriceCents() == 2160 && new ComboMeal(salad, burger).getPrepUnits() == 2);
        check("Burger + Pasta combo: $29.70 and 3 units",
                new ComboMeal(burger, pasta).getPriceCents() == 2970 && new ComboMeal(burger, pasta).getPrepUnits() == 3);
        check("combo price rounds to the nearest cent: (995 + 0) x 0.9 = 895.5 -> 896",
                new ComboMeal(new BasicMenuItem("x", 995, 1), new BasicMenuItem("y", 0, 1)).getPriceCents() == 896);
        check("combo is made of its two items and named after them",
                saladPasta.getComponents().equals(List.of(salad, pasta)) && saladPasta.getName().equals("Salad + Pasta Combo"));
        check("Stage 3 menu: 3 dishes + 3 combos, findByName(\"Pasta\") still finds plain Pasta",
                menu.getItems().size() == 6 && menu.findByName("Pasta").getPrepUnits() == 2
                && menu.findByName("Salad + Pasta Combo").getPriceCents() == 2430);


        // 2. Combo choice rule: even IDs upgrade their usual dish to a combo, Critic stays on Pasta
        List<String> regularChoices = new ArrayList<>();
        boolean vipSameAsRegular = true, criticAlwaysPasta = true, standardUnchanged = true;

        for (int id = 1; id <= 6; id++) {
            regularChoices.add(new RegularCustomer(id).chooseItem(menu).getName());
            vipSameAsRegular &= new VIPCustomer(id).chooseItem(menu) == new RegularCustomer(id).chooseItem(menu);
            criticAlwaysPasta &= new CriticCustomer(id).chooseItem(menu) == pasta;
            standardUnchanged &= new RegularCustomer(id).chooseItem(Menu.standard()).getName()
                    .equals(List.of("Burger", "Pasta", "Salad").get((id - 1) % 3));
        }

        check("combo rule on the Stage 3 menu " + regularChoices,
                regularChoices.equals(List.of("Burger", "Salad + Pasta Combo", "Salad",
                        "Burger + Pasta Combo", "Pasta", "Salad + Burger Combo")));
        check("VIP follows the same rule as Regular", vipSameAsRegular);
        check("Critic orders plain Pasta on the Stage 3 menu", criticAlwaysPasta);
        check("with no combos on the menu the Stage 1/2 rule is unchanged", standardUnchanged);


        // 3. Happy Hour timing: once per game, active on the activation turn and the next
        HappyHour hh = new HappyHour();
        boolean unusedOk = !hh.isUsed() && !hh.isActiveOn(1) && hh.turnsRemaining(1) == 0;
        hh.activate(5);
        check("Happy Hour starts unused and inactive", unusedOk);
        check("switched on at turn 5: active on 5 and 6 only",
                !hh.isActiveOn(4) && hh.isActiveOn(5) && hh.isActiveOn(6) && !hh.isActiveOn(7));
        check("remaining duration: turn 5 -> 2, turn 6 -> 1, turn 7 -> 0",
                hh.turnsRemaining(5) == 2 && hh.turnsRemaining(6) == 1 && hh.turnsRemaining(7) == 0);

        boolean threw = false;
        try {
            hh.activate(9);
        } catch (IllegalStateException e) {
            threw = true;
        }
        check("Happy Hour cannot be switched on a second time", threw && !hh.isActiveOn(9));


        // 4. Happy Hour price through the real Waiter: VIP first, then 20% off, locked at order time
        GameState s = new GameState(GameConfig.TABLE_COUNT);
        HappyHour priceHH = new HappyHour();
        Waiter waiter = new Waiter(menu, new HappyHourPricing(new StandardPricing(), priceHH, s::getTurn));

        Customer early = new RegularCustomer(5), vipBurger = new VIPCustomer(1);
        s.addArrival(early);
        s.addArrival(vipBurger);
        s.setTurn(3);
        waiter.seatCustomer(s, 5);
        waiter.seatCustomer(s, 1);
        waiter.takeOrder(s, 5);           // turn 3, before Happy Hour: full price Pasta

        priceHH.activate(4);
        s.setTurn(4);
        ActionResult vipOrder = waiter.takeOrder(s, 1);

        check("order taken before Happy Hour keeps its price ($18.00)", early.getOrder().getPriceCents() == 1800);
        check("VIP Burger in Happy Hour = $10.80 (brief example: 15 x 0.9 x 0.8)",
                vipBurger.getOrder().getPriceCents() == 1080 && vipOrder.message().contains("$10.80"));

        GameState s2 = new GameState(GameConfig.TABLE_COUNT);
        HappyHour hh2 = new HappyHour();
        Waiter waiter2 = new Waiter(menu, new HappyHourPricing(new StandardPricing(), hh2, s2::getTurn));
        Customer vipCombo = new VIPCustomer(2), regularCombo = new RegularCustomer(4), afterHH = new RegularCustomer(5);
        s2.addArrival(vipCombo);
        s2.addArrival(regularCombo);
        hh2.activate(1);
        s2.setTurn(2);
        waiter2.seatCustomer(s2, 2);
        waiter2.seatCustomer(s2, 4);
        waiter2.takeOrder(s2, 2);
        waiter2.takeOrder(s2, 4);

        check("VIP Salad + Pasta combo in Happy Hour: 2430 -> 2187 -> 1749.6 -> $17.50", vipCombo.getOrder().getPriceCents() == 1750);
        check("Regular Burger + Pasta combo in Happy Hour: 2970 x 0.8 = $23.76", regularCombo.getOrder().getPriceCents() == 2376);

        s2.addArrival(afterHH);
        s2.setTurn(3);
        vipCombo.leaveUnhappy();            // frees a table
        waiter2.seatCustomer(s2, 5);
        waiter2.takeOrder(s2, 5);
        check("order taken after Happy Hour ends pays full price ($18.00)", afterHH.getOrder().getPriceCents() == 1800);
        check("orders already placed keep their locked price", regularCombo.getOrder().getPriceCents() == 2376);
        check("Happy Hour rounds to the nearest cent: 1001 -> 801 (800.8), 999 -> 799 (799.2)",
                happyHourPrice(1001) == 801 && happyHourPrice(999) == 799);


        // 5. Satisfaction recovery
        Customer rec = new RegularCustomer(1);
        for (int i = 0; i < 3; i++) rec.loseWaitingSatisfaction();      // 100 - 24 = 76
        rec.recoverSatisfaction(HappyHour.RECOVERY);
        int after76 = rec.getSatisfaction();
        rec.recoverSatisfaction(HappyHour.RECOVERY);
        check("recovery: 76 + 15 = 91, then capped at 100", after76 == 91 && rec.getSatisfaction() == 100);

        // All-wait game with Happy Hour on turn 14: Regular #1 already LEFT on turn 13, VIP #2 is still queued
        ScriptedUI idle = new ScriptedUI(14, -1) {
            public HostChoice askHostChoice(GameState g) { return HostChoice.waitTurn(); }
            public WaiterChoice askWaiterChoice(GameState g) { return WaiterChoice.waitTurn(); }
            public ChefChoice askChefChoice(GameState g) { return ChefChoice.waitTurn(); }
        };
        GameState idleState = new GameState(GameConfig.TABLE_COUNT);
        stage3Game(idleState, idle, new HappyHour()).run();

        check("queued VIP recovers at turn 14 start: 100 - 10 x 5 = 50 -> 65",
                idle.messages.contains("T14 Customer 2 recovers satisfaction: 50 -> 65."));
        check("customer who already left does not recover (stays at 0, LEFT)",
                !idle.said("Customer 1 recovers") && idleState.findCustomer(1).orElseThrow().getSatisfaction() == 0
                && idleState.findCustomer(1).orElseThrow().getState() == CustomerState.LEFT);
        check("no recovery outside the two Happy Hour turns",
                idle.messages.stream().filter(m -> m.contains("recovers")).allMatch(m -> m.startsWith("T14 ") || m.startsWith("T15 ")));

        // Happy Hour on turn 2: customer 1's Burger is READY but not served (still waiting for food),
        // on turn 3 they are EATING and do not recover
        ScriptedUI eat = new ScriptedUI(2, -1);
        stage3Game(new GameState(GameConfig.TABLE_COUNT), eat, new HappyHour()).run();
        check("customer waiting for food recovers (turn 2: 92 -> 100)", eat.messages.contains("T2 Customer 1 recovers satisfaction: 92 -> 100."));
        check("customer who is eating does not recover (turn 3)", !eat.said("T3 Customer 1 recovers"));
        check("remaining duration shown on both turns",
                eat.said("T2 Happy Hour: ACTIVE, 2 turn(s) left") && eat.said("T3 Happy Hour: ACTIVE, 1 turn(s) left") && eat.said("T4 Happy Hour: used."));

        ScriptedUI late = new ScriptedUI(18, -1);
        stage3Game(new GameState(GameConfig.TABLE_COUNT), late, new HappyHour()).run();
        check("switched on at turn 18: shows 1 turn left (the game ends after turn 18)", late.said("T18 Happy Hour: ACTIVE, 1 turn(s) left"));


        // 6. Host: seats one customer in addition to the Waiter, invalid choices re-asked
        GameState hs = new GameState(GameConfig.TABLE_COUNT);
        hs.setTurn(1);
        hs.addArrival(new RegularCustomer(1));
        hs.addArrival(new RegularCustomer(2));
        hs.addArrival(new RegularCustomer(3));
        Host host = new Host();

        ActionResult hostSeat = host.seatCustomer(hs, 1);
        ActionResult waiterSeat = new Waiter(menu, new StandardPricing()).seatCustomer(hs, 2);
        check("Host and Waiter both seat a customer in the same turn",
                hostSeat.success() && waiterSeat.success() && hs.getWaitingQueue().size() == 1
                && hostSeat.message().equals("Host: Seated customer 1 at table 1."));
        check("Host invalid choices give reasons: unknown id, not queued, no free table",
                host.seatCustomer(hs, 999).message().equals("There is no customer with id 999.")
                && host.seatCustomer(hs, 1).message().equals("Customer 1 is not waiting in the queue.")
                && host.seatCustomer(hs, 3).message().equals("There is no free table."));
        check("Host task list: nothing to seat when the tables are full", host.describeAvailableTasks(hs).equals(List.of("Wait")));

        ScriptedUI hostGame = new ScriptedUI(-1, 1);
        GameResult hostResult = stage3Game(new GameState(GameConfig.TABLE_COUNT), hostGame, new HappyHour()).run();
        check("invalid Host choice is explained and re-asked in the same turn",
                hostGame.said("T1 There is no customer with id 999.") && hostGame.hostAsks.get(1) == 2
                && hostGame.said("T1 Host: Seated customer 1 at table 1."));
        check("Host is asked once per turn after a valid choice, and seats at most one customer per turn",
                hostGame.hostAsks.entrySet().stream().allMatch(e -> e.getValue() == (e.getKey() == 1 ? 2 : 1))
                && hostGame.hostSeated.values().stream().allMatch(n -> n == 1));
        check("Host acts before the Waiter: customer seated by the Host orders the same turn",
                hostGame.messages.indexOf("T1 Host: Seated customer 1 at table 1.")
                < hostGame.messages.indexOf("T1 Took order from customer 1: Burger ($15.00)."));
        check("scripted Stage 3 game ends in VICTORY", hostResult == GameResult.VICTORY);

        // Stage 1/2 controller (old constructor) never asks for the Host or Happy Hour
        ScriptedUI oldGame = new ScriptedUI(1, -1);
        new GameController(new GameState(GameConfig.TABLE_COUNT), ArrivalSchedule.stage2Setting(),
                new Waiter(Menu.standard(), new StandardPricing()), new Chef(), new Cashier(), oldGame).run();
        check("Stage 2 setup still runs without a Host or Happy Hour",
                oldGame.hostAsks.isEmpty() && !oldGame.said("Happy Hour"));


        // 7. 3000 random Stage 3 games
        int bad = 0, victories = 0, withHappyHour = 0;
        for (long seed = 0; seed < 3000; seed++) {
            RandomStage3UI ui = new RandomStage3UI(new Random(seed));
            GameState g = new GameState(GameConfig.TABLE_COUNT);
            HappyHour gameHH = new HappyHour();
            GameResult res;

            try {
                res = stage3Game(g, ui, gameHH).run();
            } catch (Throwable t) {
                System.out.println("EXCEPTION seed " + seed + ": " + t);
                bad++;
                continue;
            }

            if (res == GameResult.VICTORY) victories++;
            if (gameHH.isUsed()) withHappyHour++;

            boolean ok = g.getRevenueCents() == ui.paidCents && g.getPaidCount() == ui.paidMsgs
                    && g.getServedCount() == ui.servedMsgs && g.getUnhappyDepartures() == ui.leftMsgs
                    && g.getPaidCount() <= 6 && ui.activations <= 1 && ui.askedAfterActivation == 0
                    && !ui.hostTwiceInOneTurn && !ui.recoveryMismatch;

            for (Customer c : g.getAllCustomers()) {
                ok &= c.getSatisfaction() >= 0 && c.getSatisfaction() <= 100;

                // Price must be the customer's bill, with 20% off only if the order was taken during Happy Hour
                if (c.getOrder() != null) {
                    int bill = c.billFor(c.getOrder().getItem());
                    int expected = gameHH.isActiveOn(ui.orderTurn.get(c.getId())) ? happyHourPrice(bill) : bill;
                    ok &= c.getOrder().getPriceCents() == expected;
                }
            }

            if (!ok) {
                bad++;
                if (bad <= 5) System.out.println("VIOLATION seed " + seed);
            }
        }
        check("3000 random Stage 3 games: no crashes, totals match, Happy Hour once, prices locked correctly, "
                + "Host at most one per turn, recovery only for waiting customers (" + withHappyHour + " used Happy Hour, "
                + victories + " victories)", bad == 0);


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


        System.out.println(failures == 0 ? "ALL STAGE 3 CHECKS PASSED" : failures + " CHECK(S) FAILED");
        if (failures != 0) System.exit(1);
    }
}
