package rush.domain;


public enum CustomerState {
    WAITING_IN_QUEUE,   // arrived, not yet seated
    SEATED,             // at a table, order not yet taken
    ORDERED,            // order taken, food not yet served
    EATING,             // served, eating
    READY_TO_PAY,       // finished eating, waiting for Cashier
    PAID,               // paid, table freed
    LEFT                // abandoned (satisfaction hit 0), no payment
}
