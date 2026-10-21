// Starts one base-setting game. All rules live in the other classes.
public class Main {
    public static void main(String[] args) {
        Restaurant restaurant = new Restaurant();
        GameCLI cli = new GameCLI();
        GameController controller = new GameController(restaurant, cli);
        controller.startGame();
    }
}
