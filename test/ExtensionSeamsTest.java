import java.util.*;
import rush.control.*;
import rush.domain.*;
import rush.staff.*;


/**
 * Tests whether the game can be extended without changing the original code
 * It checks the food rule, pricing rules and arrival schedule
 */
public class ExtensionSeamsTest {

    // Counts how many checks failed
    static int failures = 0;

    // Prints whether a check passed or failed
    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) failures++;
    }


    /**
     * A test VIP customer used to check whether new customer types can be added
     * VIPs lose less satisfaction and get a discount
     */
    static class TestVip extends Customer {

        TestVip(int id) { super(id); }

        public int getWaitingLoss() { return 5; }

        public String getTypeName() { return "TestVIP"; }

        public MenuItem chooseItem(Menu m) { return m.findByName("Burger"); }

        // VIPs pay 90% of the normal price
        @Override public int billFor(MenuItem i) { return i.getPriceCents() * 9 / 10; }
    }


    public static void main(String[] args) {

        // 1. Check that adding a new item does not change what Regular customers order
        List<MenuItem> bigger = new ArrayList<>(Menu.standard().getItems());
        bigger.add(new BasicMenuItem("Combo", 2430, 3));
        Menu bigMenu = new Menu(bigger), base = Menu.standard();

        boolean same = true;

        for (int id = 1; id <= 9; id++) {
            same &= new RegularCustomer(id).chooseItem(bigMenu).getName()
                    .equals(new RegularCustomer(id).chooseItem(base).getName());
        }

        check("food rule unchanged by a larger menu", same);

        check("rule: id 1 Burger, id 2 Pasta, id 3 Salad",
                new RegularCustomer(1).chooseItem(base).getName().equals("Burger")
                && new RegularCustomer(2).chooseItem(base).getName().equals("Pasta")
                && new RegularCustomer(3).chooseItem(base).getName().equals("Salad"));


        // 2. Check that pricing rules can be added without changing the Waiter
        PricingPolicy happyHour = (c, i) -> new StandardPricing().priceFor(c, i) * 8 / 10;

        GameState s = new GameState(2);
        s.setTurn(1);

        Customer vip = new TestVip(10), regular = new RegularCustomer(11);
        s.addArrival(vip);
        s.addArrival(regular);

        Waiter w = new Waiter(base, happyHour);
        w.seatCustomer(s, 10);
        w.seatCustomer(s, 11);

        ActionResult vipOrder = w.takeOrder(s, 10);

        check("VIP Burger in Happy Hour = $10.80 (15 x 0.9 x 0.8)", vipOrder.message().contains("$10.80") && vip.getOrder().getPriceCents() == 1080);

        w.takeOrder(s, 11);
        check("Regular Pasta in Happy Hour = $14.40", regular.getOrder().getPriceCents() == 1440);

        // Check the VIP price without the happy hour discount
        Waiter plain = new Waiter(base, new StandardPricing());
        GameState s2 = new GameState(1);
        s2.setTurn(1);

        Customer v2 = new TestVip(12);
        s2.addArrival(v2);

        plain.seatCustomer(s2, 12);
        plain.takeOrder(s2, 12);

        check("without the decorator the VIP pays $13.50", v2.getOrder().getPriceCents() == 1350);


        // 3. Check that different customer types can be added to the arrival schedule
        Map<Integer, CustomerFactory> plan = new HashMap<>();
        plan.put(1, RegularCustomer::new);
        plan.put(4, TestVip::new);
        plan.put(7, RegularCustomer::new);

        ArrivalSchedule mixed = new ArrivalSchedule(plan);

        Customer a1 = mixed.arrivalAt(1);
        check("turn 1 Regular, id 1", a1 instanceof RegularCustomer && a1.getId() == 1);

        check("turn 2 nobody", mixed.arrivalAt(2) == null);

        Customer a4 = mixed.arrivalAt(4);
        check("turn 4 VIP, id 2", a4 instanceof TestVip && a4.getId() == 2);

        check("turn 7 Regular, id 3", mixed.arrivalAt(7).getId() == 3);


        // Check the basic arrival schedule
        ArrivalSchedule basic = ArrivalSchedule.basicSetting();
        int arrivals = 0;
        boolean allRegular = true;
        Set<Integer> ids = new HashSet<>();

        for (int t = 1; t <= GameConfig.MAX_TURNS; t++) {
            Customer c = basic.arrivalAt(t);

            if (c != null) {
                arrivals++;
                allRegular &= c instanceof RegularCustomer;
                ids.add(c.getId());
            }
        }

        check("basic setting: six Regular arrivals with unique ids", arrivals == 6 && allRegular && ids.size() == 6);


        System.out.println(failures == 0 ? "ALL SEAM CHECKS PASSED" : failures + " CHECK(S) FAILED");
        if (failures != 0) System.exit(1);
    }
}