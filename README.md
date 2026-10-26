# Restaurant Rush

### NTU SC2002 Group Assignment - Java Console Restaurant Management Game

Take the role of a restaurant manager and coordinate a small team of staff
across 18 turns. Decide which customers to seat, whose orders to take,
which dishes to prepare, and which customers to serve first.

With only two tables and limited staff capacity, every decision matters.
Efficient service earns revenue and preserves customer satisfaction, while
delays may cause customers to leave without paying.

The project was developed through three runnable stages, adding new customer
behaviour, staff roles, and pricing policies to the original game.
All three stages are completed and available at their Git tags.

## Contents

- [Links](#links)
- [Team Members](#team-members)
- [Features and Game Rules](#features-and-game-rules)
- [How to Play](#how-to-play)
- [Build and Run](#build-and-run)
- [Tests and Reproducible Runs](#tests-and-reproducible-runs)
- [Stage Versions and Changes](#stage-versions-and-changes)
- [Presentation Video](#presentation-video)

## Links

- [GitHub repository](https://github.com/jynyj123/SC2002)
- [Class diagram](UML/Class%20Diagram%20-%20Rush.pdf)
- Sequence diagram: [SEQUENCE_DIAGRAM_LINK]
- Final report: [REPORT_LINK]
- [Design discussion](DESIGN_DECISIONS.md)
- [Test notes](TEST_NOTES.md)

## Team Members

- Lab: SCE3
- Group: Group 1

| Name | GitHub Account | Main Responsibility |
| --- | --- | --- |
| LAW JUN HAO | jwakee23 | Stage 1 game/controller, Waiter, Chef, Cashier, and ConsoleUI |
| JOEY NG | jynyj123 | Stage 1 Customer, RegularCustomer, Table, Order, and MenuItem |
| CALEB CHOO | calebb221 | Stage 2 VIPCustomer and CriticCustomer |
| BENJAMIN NG | benngsr | Stage 3 Host, ComboMeal and Happy Hour |
| SHI LISHENG | CakesnCoffee | UML class diagram and sequence diagram |

## Features and Game Rules

### Base Setting

- 18 turns, 2 tables, and 6 customers.
- Customers arrive on turns 1, 4, 7, 10, 13 and 16.
- The Waiter performs at most one task per turn.
- The Chef completes at most one preparation unit per turn.
- The Cashier automatically collects at most one eligible payment per turn.
- Stage 3 adds a Host who can seat one queued customer per turn.

Victory requires all three conditions after turn 18:

1. At least 4 paid customers.
2. Revenue of at least $45.00.
3. Average satisfaction of paid customers of at least 60.

The game always runs all 18 turns.

### Menu

| Item | Price | Preparation Units |
| --- | --- | --- |
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |
| Salad + Burger Combo | $21.60 | 2 |
| Salad + Pasta Combo | $24.30 | 3 |
| Burger + Pasta Combo | $29.70 | 3 |

Combos are available from Stage 3. Each contains two existing menu items,
costs 90% of their combined price rounded to the nearest cent, and requires
the sum of their preparation units.

### Customers and Food Selection

The manager chooses service priorities, not the food.

| Customer Type | Waiting Loss per Turn | Billing | Food Selection |
| --- | --- | --- | --- |
| Regular | 8 | Full price | Customer-ID cycle |
| VIP | 5 | 10% discount | Same customer-ID cycle |
| Critic | 12 | Full price | Always plain Pasta |

Regular and VIP customers use this repeating rule:

| Customer ID Remainder When Divided by 3 | Usual Dish | Stage 3 Combo for Even IDs |
| --- | --- | --- |
| 1 | Burger | Burger + Pasta Combo |
| 2 | Pasta | Salad + Pasta Combo |
| 0 | Salad | Salad + Burger Combo |

In Stage 3, even-ID Regular and VIP customers upgrade when the corresponding
combo is available. Odd-ID customers keep their usual dish.
Critics always order plain Pasta.

Stage 1 uses six Regular customers. Stages 2 and 3 replace the turn-4
arrival with a VIP and the turn-7 arrival with a Critic.
The remaining arrivals are Regular customers.

### Host and Happy Hour

The Host acts before the Waiter, allowing the Waiter to take the order
of a customer seated by the Host that turn. The Waiter can still seat
customers too.

Happy Hour can be activated once per game at the start of a turn:

- It lasts for the activation turn and the next turn.
- Its remaining playable duration is displayed.
- Orders taken while active receive 20% off.
- Customers still waiting for a table or food recover 15 satisfaction
  at turn start, capped at 100.
- Customers who have been served or have left are not affected.
- The VIP discount is applied first, then Happy Hour, with each step
  rounded to the nearest cent.
- Bills are locked when orders are taken.

Example:

```text
VIP Salad + Pasta Combo during Happy Hour:
$24.30 → $21.87 after VIP discount → $17.50 after Happy Hour
```

### Service Timing and Satisfaction

- The Host acts before the Waiter, and the Waiter acts before the Chef.
- The Chef may work on an order taken that turn.
- A dish completed this turn can be served next turn at the earliest.
- A customer served on turn T finishes eating on turn T+1 and can pay
  on turn T+2 at the earliest.
- The Cashier handles the oldest eligible READY_TO_PAY customer first,
  breaking ties by customer ID.
- Tables remain occupied until payment or abandonment.
- Unserved customers lose satisfaction at turn end. At 0, they leave
  without paying and their unserved orders are cancelled.
- Revenue includes only paid orders.
- Average satisfaction uses the value recorded at payment; before any
  payment, the display shows `N/A`.

## How to Play

Until Happy Hour has been used, the game asks:

```text
Switch on Happy Hour now? (y/n)
```

Type `y` or `n`.

| Staff | Input | Action |
| --- | --- | --- |
| Host | `1` | Seat a queued customer |
| Host | `0` | Wait |
| Waiter | `1` | Seat a queued customer |
| Waiter | `2` | Take a seated customer's order |
| Waiter | `3` | Serve a ready order |
| Waiter | `0` | Wait |
| Chef | `1` | Advance an order by one preparation unit |
| Chef | `0` | Wait |

Enter a customer ID when prompted. Cashier payments are automatic.

Invalid input or tasks produce an explanation and allow another choice
without using up the staff member's task.

Stages 1 and 2 do not include the Host or Happy Hour prompts.

## Build and Run

### Requirements

- JDK 17 or later, including `java` and `javac`.
- Terminal access.
- Git if cloning the repository or selecting stage versions.
- No external Java libraries are required.
- Gameplay works offline once the project and JDK are available.

The recorded Stage 3 test environment was Windows 11 with JDK 26.

### Get the Source

```bash
git clone [https://github.com/jynyj123/SC2002.git](https://github.com/jynyj123/SC2002.git)
cd SC2002
```

Run the commands below from the folder containing `src` and `test`.
The default application starts the Stage 3 game.

### Windows PowerShell

```powershell
New-Item -ItemType Directory -Force out | Out-Null

$sourceFiles = Get-ChildItem .\src -Recurse -Filter *.java |
    Select-Object -ExpandProperty FullName

javac -d out $sourceFiles
```

After successful compilation:

```powershell
java -cp out rush.Main
```

### macOS / Linux

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out rush.Main
```

### Run a Tagged Stage

With a clean working tree, select the version you want:

```bash
git switch --detach stage-1
```

Replace `stage-1` with `stage-2` or `stage-3` as needed.

Rebuild using the instructions in that snapshot. Use fresh `out` and
`testout` directories so compiled classes from different stages are not mixed.

To return to the main branch:

```bash
git switch main
```

## Tests and Reproducible Runs

Tests are plain Java programs and do not require JUnit.

### Windows PowerShell

Compile the application first, then run:

```powershell
New-Item -ItemType Directory -Force testout | Out-Null

$testFiles = Get-ChildItem .\test -Filter *.java |
    Select-Object -ExpandProperty FullName

javac -cp out -d testout $testFiles

java -cp "out;testout" StaffRulesTest
java -cp "out;testout" ControllerSmokeTest
java -cp "out;testout" RandomGamesTest
java -cp "out;testout" ConsoleUITest
java -cp "out;testout" ExtensionSeamsTest
java -cp "out;testout" Stage2CustomersTest
java -cp "out;testout" Stage3Test
```

### macOS / Linux

```bash
mkdir -p testout
javac -cp out -d testout test/*.java

java -cp out:testout StaffRulesTest
java -cp out:testout ControllerSmokeTest
java -cp out:testout RandomGamesTest
java -cp out:testout ConsoleUITest
java -cp out:testout ExtensionSeamsTest
java -cp out:testout Stage2CustomersTest
java -cp out:testout Stage3Test
```

Run tests from the project folder containing `src` and `test`.
Earlier stages contain the tests available in their snapshots.

All seven test programs passed in the recorded Stage 3 run on
26 October 2026. `Stage3Test` includes 39 checks and 3,000 random
Stage 3 games.

### Replay Victory and Defeat

`ConsoleUITest` generates `test/victory_input.txt` and
`test/defeat_input.txt`. Use replay files from the same stage as the application.

Windows PowerShell:

```powershell
cmd /c "java -cp out rush.Main < test\victory_input.txt"
cmd /c "java -cp out rush.Main < test\defeat_input.txt"
```

macOS / Linux:

```bash
java -cp out rush.Main < test/victory_input.txt
java -cp out rush.Main < test/defeat_input.txt
```

### Results

| Victory Run | Stage 1 | Stage 2 | Stage 3 |
| --- | --- | --- | --- |
| Paid customers | 4 | 4 | 5 |
| Revenue | $57.00 | $64.20 | $98.20 |
| Average paid satisfaction | 78.0 | 74.3 | 78.0 |
| Served customers | 5 | 5 | 5 |
| Unhappy departures | 0 | 0 | 0 |
| Turns used | 18 | 18 | 18 |

All documented defeat runs end after 18 turns with 0 paid customers,
$0.00 revenue, `N/A` average paid satisfaction, and 2 unhappy departures.

The Stage 3 victory replay activates Happy Hour on turn 4 and charges
VIP customer #2 $17.50 for the Salad + Pasta Combo. It also demonstrates
invalid-input handling, retrying an invalid Host choice, and
Happy Hour satisfaction recovery.

Full test coverage, replay details, and recorded results are in
[TEST_NOTES.md](TEST_NOTES.md).

## Stage Versions and Changes

| Stage | Features Added | Git Tag |
| --- | --- | --- |
| 1 | Regular customers, menu, Waiter, Chef, Cashier, full service flow and both endings | `stage-1` |
| 2 | VIP and Critic customers | `stage-2` |
| 3 | Host, ComboMeal and Happy Hour | `stage-3` |

Each tag identifies its independently runnable stage.

### Stage 1 → Stage 2

- Implementation: [PR #1](https://github.com/jynyj123/SC2002/pull/1)
- Comparison: [stage-1...stage-2](https://github.com/jynyj123/SC2002/compare/stage-1...stage-2)

| Change Type | File | Change and Reason |
| --- | --- | --- |
| Added | `src/rush/domain/VIPCustomer.java` | Adds reduced waiting loss, ID-based food choice, and 10% discounted billing |
| Added | `src/rush/domain/CriticCustomer.java` | Adds increased waiting loss and a fixed Pasta preference |
| Added | `test/Stage2CustomersTest.java` | Tests actual customer classes, arrivals, pricing and mixed games |
| Modified | `src/rush/control/ArrivalSchedule.java` | Adds the Stage 2 schedule with a VIP on turn 4 and a Critic on turn 7 |
| Modified | `src/rush/Main.java` | Starts the Stage 2 setting |
| Modified | `test/ConsoleUITest.java` | Generates and checks console replays with Stage 2 arrivals |
| Modified | `TEST_NOTES.md` | Records Stage 2 rules and replay results |

Customer differences are handled through customer methods rather than
customer-type checks in the controller. Earlier service rules remain
unchanged, with regression tests covering the Regular-only setup.

### Stage 2 → Stage 3

- Implementation: [PR #4](https://github.com/jynyj123/SC2002/pull/4)
- Comparison: [stage-2...stage-3](https://github.com/jynyj123/SC2002/compare/stage-2...stage-3)

| Change Type | File | Change and Reason |
| --- | --- | --- |
| Added | `src/rush/domain/ComboMeal.java` | Adds a two-item menu item with summed preparation units and the 90% combo price |
| Added | `src/rush/domain/HappyHour.java` | Keeps once-per-game activation and duration in one place |
| Added | `src/rush/domain/HappyHourPricing.java` | Applies Happy Hour after the customer's discount through the existing pricing interface |
| Added | `src/rush/staff/Host.java` | Adds one seating task per turn |
| Added | `src/rush/staff/Seating.java` | Shares seating checks between Host and Waiter |
| Added | `src/rush/ui/HostAction.java`, `src/rush/ui/HostChoice.java` | Represents the manager's Host decision |
| Added | `test/Stage3Test.java` | Tests Stage 3 rules and earlier-stage compatibility |
| Modified | `src/rush/control/GameController.java` | Adds Host and Happy Hour steps; retains the earlier constructor for Stage 1/2 setups |
| Modified | `src/rush/ui/GameUI.java` | Adds default Host and Happy Hour methods so earlier test UIs still work |
| Modified | `src/rush/ui/ConsoleUI.java` | Adds the Host menu and Happy Hour prompt |
| Modified | `src/rush/domain/Customer.java` | Adds satisfaction recovery and shared ID/combo selection |
| Modified | `src/rush/domain/RegularCustomer.java`, `src/rush/domain/VIPCustomer.java` | Uses the shared food-selection helper instead of duplicated logic |
| Modified | `src/rush/domain/Menu.java` | Adds the Stage 3 menu |
| Modified | `src/rush/staff/Waiter.java` | Delegates seating to the shared helper |
| Modified | `src/rush/Main.java` | Wires the Stage 3 menu, Host and Happy Hour |
| Modified | `test/ConsoleUITest.java` | Records and checks Stage 3 prompts and features |
| Modified | `test/victory_input.txt`, `test/defeat_input.txt` | Adds Host and Happy Hour inputs |
| Modified | `CONTRACT.md`, `DESIGN_DECISIONS.md`, `README.md`, `TEST_NOTES.md` | Documents the extended APIs, design choices, gameplay and test results |
| Deleted | None | Existing classes are retained |

The existing staff and customer tests still pass. `Stage3Test` also checks
the Stage 2 setup through the earlier controller constructor.

The design discussion is available in
[DESIGN_DECISIONS.md](DESIGN_DECISIONS.md).

## Presentation Video

Video: [VIDEO_LINK]

| Segment | Timestamp |
| --- | --- |
| Launch from the submitted project | [mm:ss] |
| Meaningful manager choices with constrained staff | [mm:ss] |
| READY → SERVED → payment | [mm:ss] |
| VIP or Critic behaviour | [mm:ss] |
| Stage 3 feature | [mm:ss] |
| Victory and defeat replays | [mm:ss] |
| Stage comparisons and diagrams | [mm:ss] |

This project is developed for the SC2002 Restaurant Rush group assignment.
