# Test Notes (Stage 3)

From Stage 3 the game runs with the Host, the Stage 3 menu (three dishes + three combos) and Happy Hour.
Arrivals are still the Stage 2 setting (T1 Regular, T4 VIP, T7 Critic, then Regular on 10, 13, 16).
The Stage 1 and Stage 2 numbers are still correct at the `stage-1` and `stage-2` tags.

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
`test/victory_input.txt` is a typed session. Each turn the inputs go: Happy Hour y/n (until used), Host, Waiter, Chef.
- The first Happy Hour answer is `maybe`, which is rejected ("Please enter y or n.").
- Before the first Waiter choice it types four malformed entries (`abc`, `7`, `-1`, empty line) to show the menu
  rejecting bad input.
- On turn 2 the Host tries to seat customer 1, who is already seated, and the Waiter tries to serve customer 999.
  Both are explained and asked again without using up the task.
- Happy Hour is switched on at turn 4.

Expected ending (Stage 3): **VICTORY**. 5 paid customers, revenue $98.20, average satisfaction 78.0, 5 served,
0 unhappy departures.

| Customer | Type | Order | Bill | Satisfaction at payment |
| --- | --- | --- | --- | --- |
| #1 | Regular | Burger | $15.00 | 92 |
| #2 | VIP | Salad + Pasta Combo (taken in Happy Hour) | $17.50 (2430 -> 2187 -> 1750) | 90 |
| #3 | Critic | Pasta | $18.00 | 64 |
| #4 | Regular | Burger + Pasta Combo | $29.70 | 68 |
| #5 | Regular | Pasta | $18.00 | 76 |
| #6 | Regular | Salad + Burger Combo | not paid (arrives turn 16) | - |

On turn 5 (second Happy Hour turn) the VIP is still waiting for food and recovers 95 -> 100.

(Stage 2 at the `stage-2` tag: 4 paid, $64.20, 74.3, 5 served, 0 departures.
Stage 1 at the `stage-1` tag: 4 paid, $57.00, 78.0, 5 served, 0 departures.)

### Defeat run
```
java -cp out rush.Main < test/defeat_input.txt
```
Happy Hour is never switched on, and the Host, Waiter and Chef wait on every turn.

Expected ending (Stage 3): **DEFEAT**. 0 paid, revenue $0.00, average satisfaction N/A, 2 unhappy departures
(Regular #1 on turn 13, Critic #3 on turn 15). The VIP (#2) is still waiting at satisfaction 25. This is the same as Stage 2,
because nothing Stage 3 adds is used.

### Playing by hand
```
java -cp out rush.Main
```
Answer `y`/`n` to the Happy Hour prompt, then type the menu number for each staff member and a customer id when asked.
Ctrl+D (Ctrl+Z on Windows) ends the game early.

## Automated checks (plain Java, no libraries)
```
mkdir -p testout
javac -cp out -d testout test/*.java
java -cp out:testout StaffRulesTest        # Waiter / Chef / Cashier rules
java -cp out:testout ExtensionSeamsTest    # pricing, arrivals and food rule extend without editing Stage 1 code
java -cp out:testout ControllerSmokeTest   # full 18-turn runs through GameController
java -cp out:testout RandomGamesTest       # 3000 random games: no crashes, consistent board, totals match what the actions reported
java -cp out:testout ConsoleUITest         # typed-input replay through ConsoleUI (regenerates the two input files)
java -cp out:testout Stage2CustomersTest   # VIP/Critic rules, Stage 2 arrivals, 3000 random Stage 2 games, no type checks
java -cp out:testout Stage3Test            # combos, combo rule, Happy Hour timing/pricing/recovery, Host, 3000 random Stage 3 games
```
On Windows use `;` instead of `:` in the classpath.

Only `ConsoleUITest` changed for Stage 3 (it mirrors `Main`). The other Stage 1/2 tests pass with no edits.

### Stage 3 results (26 Oct 2026, Windows 11, JDK 26)
All seven programs pass. `ControllerSmokeTest` prints `good: VICTORY, bad: DEFEAT`.
`Stage3Test`: 39 checks. Its 3000 random games used Happy Hour in 2682 games, with no crashes or mismatches.
We also broke three rules on purpose to check the tests catch them: Happy Hour rounding truncating, recovery applied to every
customer, and Happy Hour lasting 3 turns. `Stage3Test` failed each time.
