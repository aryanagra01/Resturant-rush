package utility;

/** Money is stored as integer cents everywhere. This class only formats it for display. */
public final class Money {

    private Money() {
    }

    /** Formats cents as dollars, e.g. 1350 becomes "$13.50". */
    public static String format(int cents) {
        int dollars = cents / 100;
        int remainder = Math.abs(cents % 100);
        return String.format("$%d.%02d", dollars, remainder);
    }
}
