package rush.ui;

/** The manager's Waiter decision. customerId is ignored for WAIT. */
public record WaiterChoice(WaiterAction action, int customerId) {
    public static WaiterChoice waitTurn() { return new WaiterChoice(WaiterAction.WAIT, -1); }
}
