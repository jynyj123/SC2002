import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import rush.control.*;
import rush.domain.*;
import rush.staff.*;
import rush.ui.*;


/**
 * Tests the ConsoleUI by playing through the game
 * It saves the player inputs into files, then replays them to check that the game works correctly
 */
public class ConsoleUITest {

    // Counts how many checks failed
    static int failures = 0;

    // Prints whether a check passed or failed
    static void check(String name, boolean ok) {
        System.out.println((ok ? "PASS  " : "FAIL  ") + name);
        if (!ok) failures++;
    }


    /**
     * Records the choices made by the automatic player
     * The choices are saved as the inputs ConsoleUI would receive
     */
    static class RecordingUI implements GameUI {

        final ControllerSmokeTest.AutoUI player = new ControllerSmokeTest.AutoUI(true);
        final List<String> lines = new ArrayList<>();

        public void showTurnStart(GameState s) {}
        public void showMessage(String m) {}
        public void showTurnSummary(GameState s) {}
        public void showFinalResult(GameState s, GameResult r) {}

        public WaiterChoice askWaiterChoice(GameState s) {
            WaiterChoice c = player.askWaiterChoice(s);

            // Save the Waiter choice in the same format a player would type
            switch (c.action()) {
                case WAIT -> lines.add("0");
                case SEAT_CUSTOMER -> { lines.add("1"); lines.add("" + c.customerId()); }
                case TAKE_ORDER -> { lines.add("2"); lines.add("" + c.customerId()); }
                case SERVE_ORDER -> { lines.add("3"); lines.add("" + c.customerId()); }
            }

            return c;
        }

        public ChefChoice askChefChoice(GameState s) {
            ChefChoice c = player.askChefChoice(s);

            // Save the Chef choice in the same format a player would type
            if (c.action() == ChefAction.WAIT) lines.add("0");
            else { lines.add("1"); lines.add("" + c.customerId()); }

            return c;
        }
    }


    // Creates a new game state with the normal number of tables
    static GameState newState() { return new GameState(GameConfig.TABLE_COUNT); }


    // Creates a game using the standard menu, pricing and staff
    static GameController game(GameState state, GameUI ui) {
        Menu menu = Menu.standard();
        return new GameController(state, ArrivalSchedule.stage2Setting(), new Waiter(menu, new StandardPricing()), new Chef(), new Cashier(), ui);
    }


    /**
     * Runs the game using the given input and returns everything printed by ConsoleUI
     */
    static String replay(String input, GameResult[] resultOut) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        ConsoleUI ui = new ConsoleUI(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), out);
        resultOut[0] = game(newState(), ui).run();

        return buffer.toString(StandardCharsets.UTF_8);
    }


    public static void main(String[] args) throws Exception {

        // Record the inputs for a winning game
        RecordingUI recorder = new RecordingUI();
        game(newState(), recorder).run();

        // Add some invalid inputs at the start to test input checking
        List<String> victory = new ArrayList<>(List.of("abc", "7", "-1", ""));
        victory.addAll(recorder.lines);
        String victoryInput = String.join("\n", victory) + "\n";


        // Create a losing game where both staff wait every turn
        List<String> defeat = new ArrayList<>();
        for (int i = 0; i < 36; i++) defeat.add("0");
        String defeatInput = String.join("\n", defeat) + "\n";


        // Save both sets of inputs into files
        Files.writeString(Path.of("test/victory_input.txt"), victoryInput);
        Files.writeString(Path.of("test/defeat_input.txt"), defeatInput);


        // Replay the winning inputs through ConsoleUI and check the results
        GameResult[] r = new GameResult[1];
        String v = replay(victoryInput, r);

        check("victory script ends in VICTORY", r[0] == GameResult.VICTORY && v.contains("GAME OVER: VICTORY"));
        check("malformed menu input is rejected with a message", v.contains("Please enter a number from 0 to 3."));
        check("rule-invalid choice (unknown id 999) explained and re-asked", v.contains("There is no customer with id 999."));
        check("N/A shown before anyone has paid", v.contains("Avg satisfaction (paid): N/A"));
        check("chosen dish is shown when order is placed", v.contains("Took order from customer 1: Burger ($15.00)."));
        check("turn summary shows turns used", v.contains("Turns used: 18"));


        // Replay the losing inputs and check the results
        String d = replay(defeatInput, r);

        check("defeat script ends in DEFEAT", r[0] == GameResult.DEFEAT && d.contains("GAME OVER: DEFEAT"));
        check("unhappy departures reported", d.contains("left unhappy"));


        // Check that invalid customer IDs are rejected and the game stops cleanly when input runs out
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);
        boolean ended = false;

        try {
            game(newState(), new ConsoleUI(new ByteArrayInputStream("1\nabc\n".getBytes(StandardCharsets.UTF_8)), out)).run();
        } catch (InputEndedException e) {
            ended = true;
        }

        String partial = buf.toString(StandardCharsets.UTF_8);

        check("non-numeric customer id is rejected", partial.contains("Please enter a whole number."));
        check("end of input throws InputEndedException (no endless loop)", ended);


        // Show the final test result
        System.out.println(failures == 0 ? "ALL CONSOLE UI CHECKS PASSED" : failures + " CHECK(S) FAILED");
        if (failures != 0) System.exit(1);
    }
}