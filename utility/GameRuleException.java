package utility;

/**
 * A game rule refused the manager's choice, for example "No table is available."
 *
 * <p>The controller catches only this exception, shows its message and lets the
 * manager choose again. Programming errors (null arguments, broken invariants, JDK
 * exceptions) use the standard exceptions instead, so they crash the game rather
 * than being shown as a normal "choose again" message.
 */
public class GameRuleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public GameRuleException(String message) {
        super(message);
    }
}
