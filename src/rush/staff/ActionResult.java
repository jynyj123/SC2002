package rush.staff;


/**
 * Stores the result of a staff action
 * If the action fails, the message explains why and the manager can choose again
 */
public record ActionResult(boolean success, String message) {

    // Creates a successful result
    public static ActionResult ok(String message) { return new ActionResult(true, message); }

    // Creates a failed result
    public static ActionResult fail(String message) { return new ActionResult(false, message); }
}