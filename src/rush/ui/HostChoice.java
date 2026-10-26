package rush.ui;

/** Stage 3: the manager's Host decision. customerId is ignored for WAIT. */
public record HostChoice(HostAction action, int customerId) {
    public static HostChoice waitTurn() { return new HostChoice(HostAction.WAIT, -1); }
}
