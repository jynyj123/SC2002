package rush;


import rush.control.ArrivalSchedule;
import rush.control.GameConfig;
import rush.control.GameController;
import rush.control.GameState;
import rush.domain.HappyHour;
import rush.domain.HappyHourPricing;
import rush.domain.Menu;
import rush.domain.PricingPolicy;
import rush.domain.StandardPricing;
import rush.staff.Cashier;
import rush.staff.Chef;
import rush.staff.Host;
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
        // Set up the menu (with Stage 3 combos), game state and user interface
        Menu menu = Menu.stage3();
        GameState state = new GameState(GameConfig.TABLE_COUNT);
        GameUI ui = new ConsoleUI();

        // Stage 3: Happy Hour wraps the normal pricing, so the VIP discount is applied first
        HappyHour happyHour = new HappyHour();
        PricingPolicy pricing = new HappyHourPricing(new StandardPricing(), happyHour, state::getTurn);

        // Connect all the parts of the game together
        GameController game = new GameController(state, ArrivalSchedule.stage2Setting(),
                new Host(), new Waiter(menu, pricing), new Chef(), new Cashier(), happyHour, ui);

        try {
            game.run();
        } catch (InputEndedException e) {
            System.out.println();
            System.out.println("Input ended before the game finished. Goodbye!");
        }
    }
}