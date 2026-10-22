# Stage 1 Test Notes

## Build
```
mkdir -p out
javac -d out $(find src -name "*.java")
```

## Reproducible runs (the game is deterministic: fixed arrival turns and a fixed food rule)

### Victory run
```
java -cp out rush.Main < test/victory_input.txt
```
`test/victory_input.txt` is a typed session. It starts with four malformed entries (`abc`, `7`, `-1`, empty line)
to show the menu rejecting bad input, then plays well. On turn 2 it also sends a deliberately invalid Waiter choice
(serve customer 999) to show the explanation and re-prompt without using up the task.

Expected ending: **VICTORY**. 4 paid customers, revenue $57.00, average satisfaction 78.0, 5 served, 0 unhappy departures.

### Defeat run
```
java -cp out rush.Main < test/defeat_input.txt
```
Waiter and Chef wait on every turn.

Expected ending: **DEFEAT**. 0 paid, revenue $0.00, average satisfaction N/A, 2 unhappy departures (turns 13 and 16).

### Playing by hand
```
java -cp out rush.Main
```
Type the menu number, then a customer id when asked. Ctrl+D (Ctrl+Z on Windows) ends the game early.

## Automated checks (plain Java, no libraries)
```
mkdir -p testout
javac -cp out -d testout test/*.java
java -cp out:testout StaffRulesTest        # Waiter / Chef / Cashier rules
java -cp out:testout ExtensionSeamsTest    # pricing, arrivals and food rule extend without editing Stage 1 code
java -cp out:testout ControllerSmokeTest   # full 18-turn runs through GameController
java -cp out:testout RandomGamesTest       # 3000 random games: no crashes, consistent board, totals match what the actions reported
java -cp out:testout ConsoleUITest         # typed-input replay through ConsoleUI (regenerates the two input files)
```
On Windows use `;` instead of `:` in the classpath.
