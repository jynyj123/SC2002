package rush.domain;

public enum OrderStatus {
    PENDING,     // taken by Waiter, no preparation unit done yet
    PREPARING,   // at least one unit done, not finished
    READY,       // all units done, waiting for Waiter to serve
    SERVED,
    PAID,
    CANCELLED    // customer left before the order was finished/paid
}
