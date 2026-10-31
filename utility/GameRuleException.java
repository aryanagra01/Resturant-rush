package utility;

// A game rule refused the manager's choice, for example "No table is available."
// The controller shows this message and lets the manager choose again.
// Other exceptions are bugs and stop the game.
public class GameRuleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public GameRuleException(String message) {
        super(message);
    }
}
