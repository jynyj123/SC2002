package rush.ui;

public record ChefChoice(ChefAction action, int customerId) {
    public static ChefChoice waitTurn() { return new ChefChoice(ChefAction.WAIT, -1); }
}
