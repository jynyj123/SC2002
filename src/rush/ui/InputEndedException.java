package rush.ui;

/** If input stream ends (e.g. Ctrl+D) before the game is finished. */
public class InputEndedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InputEndedException() {
        super("Input ended before the game finished.");
    }
}
