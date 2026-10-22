package rush.ui;


import rush.control.GameResult;
import rush.control.GameState;


/**
 * Handles communication between the game and the player
 * It shows game information and collects the player’s choices
 */
public interface GameUI {

    //Shows the game state at the start of a turn
    void showTurnStart(GameState state);

    //Asks the player what the Waiter should do this turn
    WaiterChoice askWaiterChoice(GameState state);

    //Asks the player what the Chef should do this turn
    ChefChoice askChefChoice(GameState state);

    //Shows messages such as action results or invalid choices
    void showMessage(String message);

    // Shows the results at the end of a turn
    void showTurnSummary(GameState state);

    //Shows whether the game was won or lost
    void showFinalResult(GameState state, GameResult result);
}