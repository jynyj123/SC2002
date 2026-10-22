package rush;


import rush.control.ArrivalSchedule;
import rush.control.GameConfig;
import rush.control.GameController;
import rush.control.GameState;
import rush.domain.Menu;
import rush.domain.StandardPricing;
import rush.staff.Cashier;
import rush.staff.Chef;
import rush.staff.Waiter;
import rush.ui.ConsoleUI;
import rush.ui.GameUI;
import rush.ui.InputEndedException;


/**
 * Starts the restaurant game
 * Creates the objects needed and runs the game
 */
public class Main {

    public static void main(String[] args) {
        // Set up the menu, game state and user interface
        Menu menu = Menu.standard();
        GameState state = new GameState(GameConfig.TABLE_COUNT);
        GameUI ui = new ConsoleUI();

        // Connect all the parts of the game together
        GameController game = new GameController(
                state, ArrivalSchedule.basicSetting(), new Waiter(menu, new StandardPricing()), new Chef(), new Cashier(), ui);

        try {
            game.run();
        } catch (InputEndedException e) {
            System.out.println();
            System.out.println("Input ended before the game finished. Goodbye!");
        }
    }
}