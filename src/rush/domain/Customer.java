package rush.domain;


/**
 * Base class for all customers
 * Different customer types can change how much satisfaction they lose,
 * what they order and how much they pay
 */
public abstract class Customer {

    // Highest satisfaction a customer can have
    public static final int MAX_SATISFACTION = 100;


    /**
     * Food rule shared by Regular and VIP customers: Salad, Burger, Pasta by customer ID
     * (Stage 3 moved this here, it used to be copied in both subclasses)
     */
    private static final String[] DISH_CYCLE = {"Salad", "Burger", "Pasta"};

    // Stage 3: the combo each dish is upgraded to, in the same positions as DISH_CYCLE
    private static final String[] COMBO_CYCLE = {"Salad + Burger Combo", "Burger + Pasta Combo", "Salad + Pasta Combo"};


    private final int id;

    // Starts at full satisfaction and decreases while waiting
    private int satisfaction = MAX_SATISFACTION;

    // Satisfaction is saved when the customer pays
    private int satisfactionAtPayment = -1;

    // Keeps track of what the customer is currently doing
    private CustomerState state = CustomerState.WAITING_IN_QUEUE;

    private Table table;
    private Order order;

    // Records when the customer was served and when they finished eating
    private int servedOnTurn = -1;
    private int readyToPayOnTurn = -1;


    // Every customer needs an ID
    protected Customer(int id) {
        this.id = id;
    }

    /**
     * How much satisfaction the customer loses at the end of each turn
     * while waiting or waiting for food
     */
    public abstract int getWaitingLoss();


    /**
     * Returns the customer type to show in the user interface
     */
    public abstract String getTypeName();


    /**
     * Lets the customer choose an item from the menu
     */
    public abstract MenuItem chooseItem(Menu menu);


    /**
     * Returns the price the customer pays for an item
     * By default, customers pay the full price
     */
    public int billFor(MenuItem item) { return item.getPriceCents(); }


    /**
     * The ID food rule used by Regular and VIP customers
     * The usual dish is DISH_CYCLE[id % 3]: id 1 Burger, id 2 Pasta, id 3 Salad, and so on
     * Stage 3: a customer with an even ID upgrades to the combo containing their usual dish,
     * if the menu has it (id 2 Salad + Pasta, id 4 Burger + Pasta, id 6 Salad + Burger)
     * With no combos on the menu, everyone gets their usual dish, as in Stage 1 and 2
     */
    protected final MenuItem chooseByIdRule(Menu menu) {
        int slot = getId() % DISH_CYCLE.length;

        if (getId() % 2 == 0) {
            MenuItem combo = menu.findByName(COMBO_CYCLE[slot]);
            if (combo != null) return combo;
        }

        MenuItem dish = menu.findByName(DISH_CYCLE[slot]);

        if (dish == null) {
            throw new IllegalStateException("The menu has no " + DISH_CYCLE[slot]);
        }

        return dish;
    }


    public int getId() { return id; }

    public int getSatisfaction() { return satisfaction; }


    /**
     * Returns the satisfaction the customer had when they paid
     */
    public int getSatisfactionAtPayment() { return satisfactionAtPayment; }

    public CustomerState getState() { return state; }

    public Table getTable() { return table; }

    public Order getOrder() { return order; }

    public int getReadyToPayOnTurn() { return readyToPayOnTurn; }


    /**
     * Checks if the customer should lose satisfaction at the end of the turn
     * This happens while they are waiting, seated or waiting for food
     */
    public boolean losesSatisfactionAtTurnEnd() {
        return state == CustomerState.WAITING_IN_QUEUE
                || state == CustomerState.SEATED
                || state == CustomerState.ORDERED;
    }


    /**
     * Checks if the customer has finished eating
     * They must have been served on an earlier turn
     */
    public boolean hasFinishedEatingOn(int currentTurn) {
        return state == CustomerState.EATING && servedOnTurn < currentTurn;
    }


    /**
     * Checks if the customer can pay this turn
     * They must have finished eating on an earlier turn
     */
    public boolean canPayOn(int currentTurn) {
        return state == CustomerState.READY_TO_PAY && readyToPayOnTurn < currentTurn;
    }


    /**
     * Checks if the customer’s satisfaction has reached zero
     */
    public boolean hasReachedZeroSatisfaction() { return satisfaction <= 0; }


    /**
     * Reduces satisfaction by the customer’s waiting loss
     * Satisfaction cannot go below zero
     */
    public void loseWaitingSatisfaction() {
        satisfaction = Math.max(0, satisfaction - getWaitingLoss());
    }


    /**
     * Stage 3: raises satisfaction (used by Happy Hour)
     * Satisfaction cannot go above the maximum
     */
    public void recoverSatisfaction(int amount) {
        satisfaction = Math.min(MAX_SATISFACTION, satisfaction + amount);
    }


    /**
     * Seats the customer at a table
     */
    public void seatAt(Table t) {
        t.seat(this);
        this.table = t;
        this.state = CustomerState.SEATED;
    }


    /**
     * Records the customer’s order
     */
    public void placeOrder(Order o) {
        this.order = o;
        this.state = CustomerState.ORDERED;
    }


    /**
     * Marks the order as served and the customer as eating
     */
    public void receiveDish(int currentTurn) {
        order.markServed();
        this.servedOnTurn = currentTurn;
        this.state = CustomerState.EATING;
    }


    /**
     * Marks the customer as ready to pay after eating
     */
    public void finishEating(int currentTurn) {
        this.readyToPayOnTurn = currentTurn;
        this.state = CustomerState.READY_TO_PAY;
    }


    /**
     * Records the customer’s satisfaction, marks the order as paid
     * and frees the table
     */
    public int pay() {
        satisfactionAtPayment = satisfaction;
        order.markPaid();
        this.state = CustomerState.PAID;
        table.release();
        return order.getPriceCents();
    }


    /**
     * Removes the customer from the game without payment
     * Any unfinished order is cancelled and the table is freed
     */
    public void leaveUnhappy() {
        if (order != null) order.cancel();
        if (table != null) table.release();
        this.state = CustomerState.LEFT;
    }
}