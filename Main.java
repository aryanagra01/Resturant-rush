import boundary.GameCLI;
import config.GameSettings;
import control.GameController;

// Starts one base-setting game. All rules live in the other classes.
public class Main {
    public static void main(String[] args) {
        GameController controller = new GameController(GameSettings.base(), new GameCLI());
        controller.startGame();
    }
}
