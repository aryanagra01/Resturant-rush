import boundary.GameCLI;
import config.GameSettings;
import control.GameController;
import control.GameView;
import java.util.Scanner;

// Starts one base-setting game and wires the classes together.
public class Main {
    public static void main(String[] args) {
        GameSettings settings = GameSettings.base();
        GameView view = new GameCLI(new Scanner(System.in), System.out);
        GameController controller = new GameController(settings, view);
        controller.startGame();
    }
}
