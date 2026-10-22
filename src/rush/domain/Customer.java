package rush.domain;


/**
 * Base class for all customers
 * Different customer types can change how much satisfaction they lose,
 * what they order and how much they pay
 */
public abstract class Customer {

    // Highest satisfaction a customer can have
    public static final int MAX_SATISFACTION = 100;


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