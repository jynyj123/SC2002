package rush.domain;


/**
 * Represents one customer's order
 * The price is fixed when the order is created so it cannot change later
 */
public class Order {

    private final Customer customer;
    private final MenuItem item;

    // Price that was locked in when the order was placed
    private final int priceCents;

    // Number of preparation units left before the order is ready
    private int unitsRemaining;

    private OrderStatus status;

    // Records the turn when the order became ready
    private int readyOnTurn = -1;


    // Creates an order and sets the number of preparation units needed
    public Order(Customer customer, MenuItem item, int lockedPriceCents) {
        this.customer = customer;
        this.item = item;
        this.priceCents = lockedPriceCents;
        this.unitsRemaining = item.getPrepUnits();
        this.status = OrderStatus.PENDING;
    }


    public Customer getCustomer() { return customer; }

    public MenuItem getItem() { return item; }

    public int getPriceCents() { return priceCents; }

    public int getUnitsRemaining() { return unitsRemaining; }

    public OrderStatus getStatus() { return status; }

    public int getReadyOnTurn() { return readyOnTurn; }


    /**
     * Checks if the Chef can still work on this order
     */
    public boolean canBePrepared() {
        return status == OrderStatus.PENDING || status == OrderStatus.PREPARING;
    }


    /**
     * Completes one preparation unit
     * Order becomes ready once all units are done
     */
    public void advancePreparation(int currentTurn) {
        if (!canBePrepared()) throw new IllegalStateException("Order not in preparation: " + status);

        unitsRemaining--;

        if (unitsRemaining == 0) {
            status = OrderStatus.READY;
            readyOnTurn = currentTurn;
        } else {
            status = OrderStatus.PREPARING;
        }
    }


    /**
     * Checks if the order can be served this turn
     * It must have become ready on an earlier turn
     */
    public boolean isServableOn(int currentTurn) {
        return status == OrderStatus.READY && readyOnTurn < currentTurn;
    }


    public void markServed() { status = OrderStatus.SERVED; }

    public void markPaid() { status = OrderStatus.PAID; }

    public void cancel() { status = OrderStatus.CANCELLED; }
}