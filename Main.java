import boundary.GameCLI;
import config.GameSettings;
import control.GameController;
import control.GameView;
import java.util.Scanner;

/**
 * Starts one base-setting game. All rules live in the other classes; this is the
 * only place that chooses concrete classes and wires them together.
 */
public class Main {
    public static void main(String[] args) {
        GameSettings settings = GameSettings.base();
        GameView view = new GameCLI(new Scanner(System.in), System.out);
        GameController controller = new GameController(settings, view);
        controller.startGame();
    }
}
