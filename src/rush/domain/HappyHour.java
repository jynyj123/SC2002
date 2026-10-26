package rush.domain;


/**
 * Stage 3: keeps track of the Happy Hour pricing policy
 * The manager can switch it on once per game, at the start of a turn
 * It is active on that turn and the next one
 * The price change itself is done by HappyHourPricing
 */
public class HappyHour {

    // Active on the activation turn and the turn after it
    public static final int DURATION_TURNS = 2;

    // Satisfaction recovered at the start of each active turn by customers still waiting
    public static final int RECOVERY = 15;


    // Turn Happy Hour was switched on, -1 if it has not been used yet
    private int activationTurn = -1;


    /**
     * Checks if Happy Hour has already been switched on this game
     */
    public boolean isUsed() { return activationTurn != -1; }


    /**
     * Switches Happy Hour on, starting from the given turn
     * It can only be used once per game
     */
    public void activate(int turn) {
        if (isUsed()) {
            throw new IllegalStateException("Happy Hour was already used on turn " + activationTurn);
        }
        activationTurn = turn;
    }


    /**
     * Checks if Happy Hour is active on the given turn
     */
    public boolean isActiveOn(int turn) {
        return isUsed() && turn >= activationTurn && turn < activationTurn + DURATION_TURNS;
    }


    /**
     * Number of active turns left, counting the given turn
     * e.g. switched on at turn 5: turn 5 -> 2, turn 6 -> 1, turn 7 -> 0
     */
    public int turnsRemaining(int turn) {
        return isActiveOn(turn) ? activationTurn + DURATION_TURNS - turn : 0;
    }
}
