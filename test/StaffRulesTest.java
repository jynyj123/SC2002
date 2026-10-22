import rush.control.*;
import rush.domain.*;
import rush.staff.*;


/**
 * Tests the rules for the Waiter, Chef and Cashier
 * It checks seating, ordering, preparing food, serving and payment
 */
public class StaffRulesTest {

    // Counts how many checks failed
    static int failures = 0;

    // Prints whether a check passed or failed
    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) failures++;
    }


    public static void main(String[] args) {
        Menu menu = Menu.standard();
        Waiter waiter = new Waiter(menu, new StandardPricing());
        Chef chef = new Chef();
        Cashier cashier = new Cashier();
        GameState s = new GameState(2);


        Customer c1 = new RegularCustomer(1), c2 = new RegularCustomer(2), c3 = new RegularCustomer(3);
        s.addArrival(c1);
        s.addArrival(c2);
        s.addArrival(c3);
        s.setTurn(1);


        // --- Waiter: seating ---

        check("seat unknown customer fails", !waiter.seatCustomer(s, 99).success());
        check("seat customer 1 succeeds", waiter.seatCustomer(s, 1).success());
        check("seated customer is removed from queue", !s.getWaitingQueue().contains(c1));
        check("seating again fails", !waiter.seatCustomer(s, 1).success());
        check("seat customer 2 succeeds", waiter.seatCustomer(s, 2).success());
        check("no free table => seating fails", !waiter.seatCustomer(s, 3).success());
        check("failed seating leaves customer 3 in queue", s.getWaitingQueue().contains(c3));


        // --- Waiter: ordering ---

        check("order from queued customer fails", !waiter.takeOrder(s, 3).success());

        ActionResult o = waiter.takeOrder(s, 2);
        check("order succeeds and names the dish", o.success() && o.message().contains("Pasta"));
        check("ordering twice fails", !waiter.takeOrder(s, 2).success());
        check("order price locked at 1800", c2.getOrder().getPriceCents() == 1800);


        // --- Chef / serving delay ---

        check("chef cannot cook for customer without order", !chef.advanceOrder(s, 1).success());
        check("chef unit 1 ok", chef.advanceOrder(s, 2).success());
        check("order is PREPARING", c2.getOrder().getStatus() == OrderStatus.PREPARING);

        s.setTurn(2);
        ActionResult fin = chef.advanceOrder(s, 2);
        check("chef unit 2 makes READY", fin.success() && c2.getOrder().getStatus() == OrderStatus.READY);

        ActionResult early = waiter.serveOrder(s, 2);
        check("cannot serve a dish finished this turn", !early.success());
        check("failed serve leaves order READY", c2.getOrder().getStatus() == OrderStatus.READY);
        check("chef cannot cook a READY order", !chef.advanceOrder(s, 2).success());

        s.setTurn(3);
        check("serve next turn succeeds", waiter.serveOrder(s, 2).success());
        check("served count recorded", s.getServedCount() == 1);
        check("cannot serve twice", !waiter.serveOrder(s, 2).success());


        // --- Cashier: delay + oldest first ---

        c2.finishEating(4);
        s.setTurn(4);
        check("cashier does not pay customer who became ready this turn", cashier.collectPayment(s).message().contains("no payment"));

        s.setTurn(5);
        ActionResult pay = cashier.collectPayment(s);
        check("cashier pays next turn", pay.message().contains("$18.00") && c2.getState() == CustomerState.PAID);
        check("table freed after payment", s.findFreeTable().isPresent());
        check("revenue recorded", s.getRevenueCents() == 1800);
        check("satisfaction recorded at payment", s.getAverageSatisfaction().getAsDouble() == 100.0);


        // --- Cashier: oldest READY_TO_PAY first, ties by id ---

        GameState g = new GameState(3);
        Customer a = new RegularCustomer(7), b = new RegularCustomer(8), d = new RegularCustomer(9);
        g.addArrival(a);
        g.addArrival(b);
        g.addArrival(d);
        g.setTurn(1);

        // Seat, order and prepare food for all three customers
        for (Customer c : new Customer[]{a, b, d}) {
            waiter.seatCustomer(g, c.getId());
            waiter.takeOrder(g, c.getId());

            while (c.getOrder().canBePrepared()) c.getOrder().advancePreparation(1);
        }

        g.setTurn(2);
        for (Customer c : new Customer[]{a, b, d}) c.receiveDish(2);

        b.finishEating(4);
        a.finishEating(5);
        d.finishEating(5);

        g.setTurn(6);
        cashier.collectPayment(g);
        check("oldest READY_TO_PAY (b) paid first despite higher id", b.getState() == CustomerState.PAID
                && a.getState() == CustomerState.READY_TO_PAY);

        g.setTurn(7);
        cashier.collectPayment(g);
        check("tie on ready turn goes to lowest id (a)", a.getState() == CustomerState.PAID
                && d.getState() == CustomerState.READY_TO_PAY);

        check("only one payment per call", g.getPaidCount() == 2);


        // --- Customer leaving cancels order and frees the table ---

        GameState h = new GameState(1);
        Customer e = new RegularCustomer(10);
        h.addArrival(e);
        h.setTurn(1);

        waiter.seatCustomer(h, 10);
        waiter.takeOrder(h, 10);
        e.leaveUnhappy();

        check("leaving cancels order", e.getOrder().getStatus() == OrderStatus.CANCELLED);
        check("leaving frees table", h.findFreeTable().isPresent());
        check("cancelled order cannot be cooked", !chef.advanceOrder(h, 10).success());


        System.out.println(failures == 0 ? "ALL STAFF RULE CHECKS PASSED" : failures + " CHECK(S) FAILED");
        if (failures != 0) System.exit(1);
    }
}