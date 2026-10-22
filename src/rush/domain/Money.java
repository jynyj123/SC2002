package rush.domain;

/** Formatting,money is stored as integer cents everywhere, but displays in dollars. */
public final class Money {
    private Money() {}

    /** 1800 -> "$18.00". */
    public static String format(int cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }
}
