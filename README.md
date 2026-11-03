**NTU SC2002 Group Project: Java console restaurant management game**

The game was built in three runnable stages, each saved as a Git tag: `stage-1`, `stage-2` and `stage-3`.

## Contents

[Links](#links) | [Team](#team) | [Game Rules](#game-rules) | [How to Play](#how-to-play) | [Build and Run](#build-and-run) | [Tests and Results](#tests-and-results) | [Stage Changes](#stage-changes) | [AI History](#ai-history) | [Video](#presentation-video)

## Links

| Item | Link |
| --- | --- |
| Repository | https://github.com/jynyj123/SC2002 |
| Class diagrams (Stage 1, 2, 3) | [UML folder](https://github.com/jynyj123/SC2002/tree/main/UML) |
| Sequence diagram (Stage 3: VIP, combo and Happy Hour) | [UML folder](https://github.com/jynyj123/SC2002/tree/main/UML) |
| Final report (PDF) | [REPORT_LINK] |
| Design decisions | [DESIGN_DECISIONS.md](DESIGN_DECISIONS.md) |
| Test notes | [TEST_NOTES.md](TEST_NOTES.md) |

## Team

Lab: SCE3 | Group: Group 1

| GitHub account |
| --- |
| jwakee23 |
| jynyj123 |
| calebb221 |
| benngsr |
| CakesnCoffee |

## Game Rules

**Setting:** 18 turns, 2 tables, 6 customers arriving on turns 1, 4, 7, 10, 13 and 16. The Waiter does one task per turn, the Chef one preparation unit, and the Cashier collects one eligible payment automatically. Stage 3 adds a Host who seats one customer per turn.

**Victory** after turn 18: at least 4 paid customers, revenue of at least $45.00, and average satisfaction of paid customers of at least 60. The game always runs all 18 turns.

| Item | Price | Prep units | Stage |
| --- | --- | --- | --- |
| Salad | $9.00 | 1 | 1+ |
| Burger | $15.00 | 1 | 1+ |
| Pasta | $18.00 | 2 | 1+ |
| Salad + Burger Combo | $21.60 | 2 | 3 |
| Salad + Pasta Combo | $24.30 | 3 | 3 |
| Burger + Pasta Combo | $29.70 | 3 | 3 |

A combo costs 90% of its two items (rounded to the nearest cent) and needs the sum of their preparation units.

| Customer | Waiting loss per turn | Billing | Food |
| --- | --- | --- | --- |
| Regular | 8 | Full price | By customer ID |
| VIP | 5 | 10% off | By customer ID |
| Critic | 12 | Full price | Always plain Pasta |

Regular and VIP customers choose by ID mod 3 (0 Salad, 1 Burger, 2 Pasta). In Stage 3, even-ID customers order the combo containing their usual dish. Stage 1 has six Regular customers. Stages 2 and 3 make the turn-4 arrival a VIP and the turn-7 arrival a Critic.

**Timing**
- Staff act in this order: Host (Stage 3), Waiter, Chef. The Chef can work on an order taken that turn.
- A dish finished this turn can be served next turn at the earliest.
- A customer served on turn T finishes eating on T+1 and can pay on T+2.
- The Cashier pays the oldest ready customer first, ties by lowest ID.
- Unserved customers lose satisfaction at turn end. At 0 they leave and their order is cancelled.

**Happy Hour (Stage 3).** The manager can switch it on once, at the start of a turn. It lasts that turn and the next. Orders taken during it get 20% off after the customer's own discount, and customers still waiting for a table or food recover 15 satisfaction (max 100). Bills are locked when the order is taken. Example: VIP Salad + Pasta Combo, $24.30, then $21.87, then $17.50.

## How to Play

Until Happy Hour is used, the game asks `Switch on Happy Hour now? (y/n)`. Then each staff member is asked for a choice:

| Staff | Input | Action |
| --- | --- | --- |
| Host | 1 / 0 | Seat a queued customer / Wait |
| Waiter | 1 / 2 / 3 / 0 | Seat a customer / Take an order / Serve a ready order / Wait |
| Chef | 1 / 0 | Advance an order by one unit / Wait |

Enter a customer ID when asked. Invalid input or tasks give an explanation and let you choose again without using up the task. Stages 1 and 2 have no Host or Happy Hour prompts.

## Build and Run

Requirements: JDK 17 or later and a terminal. No external libraries, works offline. The recorded Stage 3 test environment was Windows 11 with JDK 26.

```bash
git clone https://github.com/jynyj123/SC2002.git
cd SC2002
```

Run from the folder containing `src` and `test`. The default application starts Stage 3.

**Windows PowerShell**

```powershell
New-Item -ItemType Directory -Force out | Out-Null
$sourceFiles = Get-ChildItem .\src -Recurse -Filter *.java | Select-Object -ExpandProperty FullName
javac -d out $sourceFiles
java -cp out rush.Main
```

**macOS / Linux**

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out rush.Main
```

**Run an earlier stage:** with a clean working tree, run `git switch --detach stage-1` (or `stage-2`), then rebuild into fresh `out` and `testout` folders so classes from different stages are not mixed. Return with `git switch main`.

## Tests and Results

Tests are plain Java programs (no JUnit). Compile the application first, then run from the same folder.

**Windows PowerShell**

```powershell
New-Item -ItemType Directory -Force testout | Out-Null
$testFiles = Get-ChildItem .\test -Filter *.java | Select-Object -ExpandProperty FullName
javac -cp out -d testout $testFiles
foreach ($t in "StaffRulesTest","ControllerSmokeTest","RandomGamesTest","ConsoleUITest","ExtensionSeamsTest","Stage2CustomersTest","Stage3Test") { java -cp "out;testout" $t }
```

**macOS / Linux**

```bash
mkdir -p testout
javac -cp out -d testout test/*.java
for t in StaffRulesTest ControllerSmokeTest RandomGamesTest ConsoleUITest ExtensionSeamsTest Stage2CustomersTest Stage3Test; do java -cp out:testout $t; done
```

All seven programs passed in the recorded Stage 3 run on 26 October 2026. `Stage3Test` has 39 checks and 3,000 random games. Earlier stages contain only the tests that existed at their tags.

**Replay a victory and a defeat.** `ConsoleUITest` creates `test/victory_input.txt` and `test/defeat_input.txt`. Use the replay files from the same stage as the application.

```bash
java -cp out rush.Main < test/victory_input.txt
java -cp out rush.Main < test/defeat_input.txt
```

In Windows PowerShell, use `cmd /c "java -cp out rush.Main < test\victory_input.txt"`.

| Victory run | Stage 1 | Stage 2 | Stage 3 |
| --- | --- | --- | --- |
| Paid customers | 4 | 4 | 5 |
| Revenue | $57.00 | $64.20 | $98.20 |
| Average paid satisfaction | 78.0 | 74.3 | 78.0 |
| Served / unhappy departures | 5 / 0 | 5 / 0 | 5 / 0 |
| Turns used | 18 | 18 | 18 |

Every documented defeat run ends after 18 turns with 0 paid customers, $0.00 revenue, `N/A` average satisfaction and 2 unhappy departures. The Stage 3 victory replay switches on Happy Hour on turn 4, charges VIP #2 $17.50 for the Salad + Pasta Combo, and shows invalid-input handling and Happy Hour recovery. Details are in [TEST_NOTES.md](TEST_NOTES.md).

## Stage Changes

| Stage | Features added | Tag |
| --- | --- | --- |
| 1 | Regular customers, menu, Waiter, Chef, Cashier, full turn sequence, both endings | `stage-1` |
| 2 | VIP and Critic customers | `stage-2` |
| 3 | Host, ComboMeal, Happy Hour | `stage-3` |

### Stage 1 to Stage 2

[PR #1](https://github.com/jynyj123/SC2002/pull/1) | [Compare stage-1...stage-2](https://github.com/jynyj123/SC2002/compare/stage-1...stage-2)

| Change | File | Reason |
| --- | --- | --- |
| Added | `src/rush/domain/VIPCustomer.java` | Waiting loss 5, ID food rule, 10% discount |
| Added | `src/rush/domain/CriticCustomer.java` | Waiting loss 12, always plain Pasta |
| Added | `test/Stage2CustomersTest.java` | Tests the customer classes, arrivals, pricing and mixed games |
| Modified | `src/rush/control/ArrivalSchedule.java` | Adds the Stage 2 plan (VIP turn 4, Critic turn 7) |
| Modified | `src/rush/Main.java` | Starts the Stage 2 setting |
| Modified | `test/ConsoleUITest.java`, `test/victory_input.txt` | Replay with Stage 2 arrivals |
| Modified | `TEST_NOTES.md` | Records Stage 2 rules and results |
| Deleted | None | |

Customer differences are handled inside the customer classes, with no type checks in the controller. The Stage 1 tests still pass.

### Stage 2 to Stage 3

[PR #4](https://github.com/jynyj123/SC2002/pull/4) (feature) and [PR #5](https://github.com/jynyj123/SC2002/pull/5) (refinement) | [Compare stage-2...stage-3](https://github.com/jynyj123/SC2002/compare/stage-2...stage-3)

| Change | File | Reason |
| --- | --- | --- |
| Added | `src/rush/domain/ComboMeal.java` | Two-item menu item, summed units, 90% price |
| Added | `src/rush/domain/HappyHour.java` | Once-per-game activation and duration |
| Added | `src/rush/domain/HappyHourPricing.java` | Applies Happy Hour after the customer discount |
| Added | `src/rush/staff/Host.java`, `Seating.java` | One seating task per turn, with checks shared with the Waiter |
| Added | `src/rush/ui/HostAction.java`, `HostChoice.java` | The Host decision |
| Added | `test/Stage3Test.java` | Stage 3 rules and Stage 2 compatibility |
| Added | `UML/Class Diagram - Rush.pdf`, `UML/exported_Gamev2.vpp` | Diagram files at this tag (replaced later by the final diagrams on `main`) |
| Modified | `src/rush/control/GameController.java` | Host and Happy Hour steps, older constructor kept |
| Modified | `src/rush/ui/GameUI.java`, `ConsoleUI.java` | Default Host/Happy Hour methods, Host menu and prompt |
| Modified | `src/rush/domain/Customer.java` | Satisfaction recovery and shared ID/combo selection |
| Modified | `src/rush/domain/RegularCustomer.java`, `VIPCustomer.java` | Use the shared selection helper |
| Modified | `src/rush/domain/Menu.java`, `MenuItem.java` | Stage 3 menu, combo identification |
| Modified | `src/rush/staff/Waiter.java` | Uses `Seating` |
| Modified | `src/rush/Main.java` | Wires the Stage 3 objects |
| Modified | `test/ConsoleUITest.java`, `victory_input.txt`, `defeat_input.txt` | Stage 3 prompts and inputs |
| Modified | `CONTRACT.md`, `DESIGN_DECISIONS.md`, `README.md`, `TEST_NOTES.md` | Documentation |
| Deleted | None | |

The older six-argument `GameController` constructor still runs Stage 1 and 2 games, and `Stage3Test` checks this. See [DESIGN_DECISIONS.md](DESIGN_DECISIONS.md) for the reasons behind these changes.


## Presentation Video

Video: [VIDEO_LINK]

| # | Segment | Presenter | Timestamp |
| --- | --- | --- | --- |
| 0 | Game overview and the three stages | [GitHub account] | [mm:ss] |
| 1 | The game launching from the submitted project | [GitHub account] | [mm:ss] |
| 2 | Meaningful manager choices, including a decision where staff are constrained | [GitHub account] | [mm:ss] |
| 3 | A dish going READY → SERVED → payment | [GitHub account] | [mm:ss] |
| 4 | A VIP or Critic behaviour | [GitHub account] | [mm:ss] |
| 5 | A Stage 3 feature | [GitHub account] | [mm:ss] |
| 6 | One victory and one defeat (separate reproducible runs) | [GitHub account] | [mm:ss] |
| 7 | The stage tag comparisons and the diagrams (class diagrams for Stages 1 to 3) | [GitHub account] | [mm:ss] |
| 8 | The selected use case and sequence diagram | [GitHub account] | [mm:ss] |
| 9 | Individual contributions and key learning | All members | [mm:ss] |
