# Restaurant Rush 

### NTU SC2002 Group Assignment - Java Console Restaurant Management Game

Takes the role of a restaurant manager and coordinate a small team of staff
across an 18-turn service. Decide which customers to seat, whose orders to
take, which dishes to prepare, and which customers to serve first.

With only two tables and limited staff capacity, every decision matters.
Efficient service earns revenue and preserves customer satisfaction, while
delays may cause customers to leave without paying.

The project is developed through three runnable stages to demonstrate how
an object-oriented design can support progressively richer customer
behaviour, staff roles, and pricing policies.

> Development status: Stage 1 functionality is implemented in the reviewed
> source. Build verification, test execution, and Git tag status must be
> confirmed by the team. Stage 2 and Stage 3 descriptions below are planned
> requirements unless explicitly marked as completed.

## Contents

- [Links](#links)
- [Team Members](#team-members)
- [Project Highlights](#project-highlights)
- [Features and Development Stages](#features-and-development-stages)
- [Game Rules](#game-rules)
- [How to Play](#how-to-play)
- [Requirements](#requirements)
- [Build and Run](#build-and-run)
- [Project Structure](#project-structure)
- [Architecture and Responsibilities](#architecture-and-responsibilities)
- [Testing](#testing)
- [Reproducible Victory and Defeat](#reproducible-victory-and-defeat)
- [Stage Versions and Change Evidence](#stage-versions-and-change-evidence)
- [UML Diagrams and Report](#uml-diagrams-and-report)
- [Presentation Video](#presentation-video)
- [Known Limitations and Verification Status](#known-limitations-and-verification-status)
- [Submission Checklist](#submission-checklist)
- [Acknowledgements and Declarations](#acknowledgements-and-declarations)

## Links

- GitHub repository: https://github.com/jynyj123/SC2002
- Shared integration contract: [CONTRACT.md](CONTRACT.md)
- Design discussion: [DESIGN_DECISIONS.md](DESIGN_DECISIONS.md)
- Test notes: [TEST_NOTES.md](TEST_NOTES.md)
- Final report: TODO - add the actual report location
- UML diagrams: TODO - add actual diagram locations
- Presentation video: TODO - add an accessible video link or file location

Documentation links above assume these files are at the repository root.
Adjust relative paths if the source project is stored inside a subfolder.

## Team Members

- Lab: SCE3
- Group: Group 1

| Name | GitHub Account | Main Responsibility |
| --- | --- | --- |
| LAW JUN HAO | jwakee23 | Stage 1 game/controller, Waiter, Chef, Cashier and ConsoleUI |
| JOEY NG | jynyj123 | Stage 1 Customer, RegularCustomer, Table, Order and MenuItem |
| Member 3 | TODO | Stage 2 VIPCustomer and CriticCustomer |
| Member 4 | TODO | Stage 3 Host, ComboMeal and Happy Hour |
| SHI LISHENG | CakesnCoffee | UML class diagram and sequence diagram |

## Project Highlights

### Object-Oriented Customer Behaviour

`Customer` defines shared customer state and behaviour hooks, while
`RegularCustomer` supplies the Stage 1 waiting-loss and food-selection rules.

The design provides extension points for later customer types through
methods such as:

- `getWaitingLoss()`
- `chooseItem(Menu menu)`
- `billFor(MenuItem item)`

The current controller does not select behaviour through customer-type
condition chains.

### Separation of Interface and Game Rules

`ConsoleUI` collects manager choices and displays the restaurant state.

Staff and domain objects validate and apply gameplay actions, while
`GameController` coordinates the turn sequence.

Malformed input is handled by the interface. Invalid gameplay choices
return an explanation and allow the manager to choose again.

### Deterministic and Reproducible Gameplay

Arrival turns and Regular customer food selection are deterministic.

This allows the same input sequence to reproduce a game, supporting
regression tests, recorded demonstrations, and verification by graders.

### Integer-Cent Money Representation

Prices and bills are stored as integer cents rather than floating-point
currency amounts.

For example:

```text
Salad  = 900 cents
Burger = 1500 cents
Pasta  = 1800 cents
```

The `Money` helper formats these values as dollar amounts for display.

### Order-Time Price Locking

The Waiter obtains the bill through `PricingPolicy` when taking an order.

That price is stored in the `Order`, so subsequent pricing-policy changes
do not retroactively alter an existing bill.

Stage 1 uses `StandardPricing`.

### Derived Game Statistics

`GameState` derives revenue, paid count, served count, unhappy departures,
and average paid satisfaction from customers and their orders.

This avoids maintaining separate totals that could become inconsistent
with the domain state.

### Testable Console Interface

`ConsoleUI` accepts injectable input/output streams.

Tests can replay typed input without requiring a person to interact with
the terminal.

## Features and Development Stages

Each completed stage must be independently runnable at its Git tag.

| Stage | Scope | Required Tag | Current Status |
| --- | --- | --- | --- |
| Stage 1 | Basic restaurant operation | `stage-1` | Source implemented; verification/tag status TODO |
| Stage 2 | Customer behaviour extension | `stage-2` | Planned; update after integration |
| Stage 3 | New staff role and menu policy | `stage-3` | Planned; update after integration |

### Stage 1 - Basic Restaurant Operation

Implemented in the reviewed source:

- Regular customers.
- Salad, Burger and Pasta.
- Two restaurant tables.
- Waiting queue derived from customer states.
- Waiter seating, ordering, serving and waiting.
- Chef preparation in units.
- Automatic Cashier payment.
- Customer satisfaction loss and abandonment.
- Preparation, serving, eating and payment delays.
- Revenue and satisfaction statistics.
- Victory and defeat evaluation.
- Console input validation and retry handling.

### Stage 2 - Customer Behaviour Extension

Planned requirements:

| Customer Type | Waiting Loss per Turn | Billing |
| --- | --- | --- |
| Regular | 8 satisfaction | Full price |
| VIP | 5 satisfaction | 10% discount |
| Critic | 12 satisfaction | Full price |

The Critic must also have a documented distinct food preference or service
reaction.

A reproducible base-setting run must include at least one VIP and one Critic,
while retaining six total arrivals.

TODO - document the actual Critic rule and mixed-arrival schedule once built.

### Stage 3 - New Staff Role and Menu Policy

Planned requirements:

#### Host

- Seats at most one queued customer per turn.
- Works in addition to the Waiter's task.
- Does not remove the Waiter's ability to seat customers.

#### ComboMeal

- Contains two existing menu items.
- Preparation units equal the sum of the components' units.
- Base price equals 90% of their combined price, rounded to the nearest cent.

Example:

```text
Salad + Pasta
Price: ($9.00 + $18.00) × 0.9 = $24.30
Preparation: 1 + 2 = 3 units
```

TODO - document the actual customer combo-selection rule.

#### Happy Hour

- Can be activated once per game at the start of a turn.
- Lasts for the activation turn and the following turn.
- Displays its remaining duration.
- Gives 20% off orders taken while active.
- Restores 15 satisfaction at turn start to eligible customers still
  waiting for a table or food, capped at 100.
- Applies the VIP discount before the Happy Hour discount.
- Does not change bills for orders already taken.
- Does not affect customers who have left.

Example:

```text
VIP Burger during Happy Hour:
$15.00 × 0.9 × 0.8 = $10.80
```

## Game Rules

### Required Base Setting

| Setting | Value |
| --- | --- |
| Maximum turns | 18 |
| Tables | 2 |
| Waiter | 1; at most one task per turn |
| Chef | 1; at most one preparation unit per turn |
| Cashier | 1; at most one payment per turn |
| Customer arrivals | Turns 1, 4, 7, 10, 13 and 16 |
| Total arrivals | 6 |

### Victory Conditions

After turn 18, all three conditions must be met:

1. At least 4 customers have paid.
2. Revenue is at least $45.00.
3. Average satisfaction of paid customers is at least 60.

The game runs all 18 turns. It does not end early when victory becomes
possible or impossible.

### Base Menu

| Item | Price | Preparation Units |
| --- | --- | --- |
| Salad | $9.00 | 1 |
| Burger | $15.00 | 1 |
| Pasta | $18.00 | 2 |

### Customer Food Selection

The manager chooses service priorities, not the food.

Regular customers use a deterministic customer-ID cycle:

| Customer ID | Food Choice |
| --- | --- |
| 1 | Burger |
| 2 | Pasta |
| 3 | Salad |
| 4 | Burger |
| 5 | Pasta |
| 6 | Salad |

The implementation names the base dishes explicitly, so adding an item to
the menu does not automatically change this rule.

### Turn Sequence

The Stage 1 controller performs:

1. Add any scheduled arrival.
2. Display the updated restaurant state.
3. Ask for and execute a valid Waiter task.
4. Ask for and execute a valid Chef task.
5. Advance eligible eating customers and process at most one eligible payment.
6. Apply waiting-satisfaction loss and abandonment.
7. Display the end-of-turn summary.

After turn 18, evaluate and display the final result.

The Waiter acts before the Chef, allowing the Chef to work on an order taken
during the current turn.

### Service and Payment Timing

An order completed by the Chef this turn cannot be served until a later turn.

A customer served on turn T:

- Finishes eating on turn T+1.
- Can pay on turn T+2 at the earliest.

A table stays occupied until its customer pays or leaves.

The Cashier selects the oldest eligible READY_TO_PAY customer. Equal
ready-to-pay turns are resolved by customer ID.

### Satisfaction and Abandonment

- Customers arrive with satisfaction 100.
- Regular customers lose 8 satisfaction at turn end while waiting for a
  table or food.
- Satisfaction cannot fall below 0.
- Customers at 0 satisfaction leave without paying.
- Abandonment cancels an unserved order and releases the table.
- Served, eating, and ready-to-pay customers do not lose waiting satisfaction.

Average satisfaction uses each paid customer's satisfaction recorded at
payment. If nobody has paid, the display shows `N/A`.

### Example Service Timeline

This illustrates the assignment's Pasta example, not necessarily customer 1
under this implementation's food-selection rule.

| Turn | Action | End-of-Turn Satisfaction |
| --- | --- | --- |
| 1 | Arrive and get seated | 92 |
| 2 | Order Pasta; Chef prepares unit 1 | 84 |
| 3 | Chef prepares unit 2; order becomes READY | 76 |
| 4 | Waiter serves the dish | 76 |
| 5 | Customer finishes eating and becomes READY_TO_PAY | 76 |
| 6 | Cashier collects $18.00 and releases the table | 76 recorded |

## How to Play

### Waiter Controls

| Input | Action |
| --- | --- |
| `1` | Seat a queued customer |
| `2` | Take a seated customer's order |
| `3` | Serve a ready order |
| `0` | Wait |

Enter a customer ID when prompted.

### Chef Controls

| Input | Action |
| --- | --- |
| `1` | Advance an order by one preparation unit |
| `0` | Wait |

Enter the ID of the customer whose order should be prepared.

### Cashier

No manager input is required. Payment is automatic when a customer is eligible.

### Invalid Choices

Malformed input and invalid gameplay choices produce explanatory messages.

An invalid staff choice does not consume the staff member's task.

## Requirements

- JDK 17 or later.
- Terminal access.
- Git only if cloning or inspecting stage history.
- No external Java libraries are required by the reviewed Stage 1 code.
- No network access is required for mandatory gameplay once installed.

Check your Java installation:

```text
java -version
javac -version
```

Both commands must be available.

## Build and Run

The application entry point is:

```text
src/rush/Main.java
```

Its fully qualified Java class name is:

```text
rush.Main
```

### Get the Source

```bash
git clone [https://github.com/jynyj123/SC2002.git](https://github.com/jynyj123/SC2002.git)
cd SC2002
```

All build commands below must be executed from the folder containing
`src`, `test`, and the project documentation.

If the repository contains an enclosing project folder, enter that folder
before compiling.

### Windows PowerShell

#### Compile

```powershell
New-Item -ItemType Directory -Force out | Out-Null

$sourceFiles = Get-ChildItem .\src -Recurse -Filter *.java |
    Select-Object -ExpandProperty FullName

javac -d out $sourceFiles
```

Resolve any compiler errors before proceeding.

#### Run

```powershell
java -cp out rush.Main
```

### macOS / Linux

```bash
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out rush.Main
```

### Running an Earlier Stage

First ensure your working tree is clean:

```bash
git status
```

After the stage tags exist:

```bash
git switch --detach stage-1
```

Use `stage-2` or `stage-3` to inspect a different stage.

Rebuild the selected version before running. Do not reuse compiled classes
from a different stage. Use fresh output directories or remove generated
`out` and `testout` directories first.

Return to your development branch afterwards:

```bash
git switch YOUR_BRANCH_NAME
```

Replace `YOUR_BRANCH_NAME` with your actual branch.

## Project Structure

```text
.
├── README.md
├── CONTRACT.md
├── DESIGN_DECISIONS.md
├── TEST_NOTES.md
├── src/
│   └── rush/
│       ├── Main.java
│       ├── control/
│       │   ├── ArrivalSchedule.java
│       │   ├── CustomerFactory.java
│       │   ├── GameConfig.java
│       │   ├── GameController.java
│       │   ├── GameResult.java
│       │   └── GameState.java
│       ├── domain/
│       │   ├── BasicMenuItem.java
│       │   ├── Customer.java
│       │   ├── CustomerState.java
│       │   ├── Menu.java
│       │   ├── MenuItem.java
│       │   ├── Money.java
│       │   ├── Order.java
│       │   ├── OrderStatus.java
│       │   ├── PricingPolicy.java
│       │   ├── RegularCustomer.java
│       │   ├── StandardPricing.java
│       │   └── Table.java
│       ├── staff/
│       │   ├── ActionResult.java
│       │   ├── Cashier.java
│       │   ├── Chef.java
│       │   └── Waiter.java
│       └── ui/
│           ├── ChefAction.java
│           ├── ChefChoice.java
│           ├── ConsoleUI.java
│           ├── GameUI.java
│           ├── InputEndedException.java
│           ├── WaiterAction.java
│           └── WaiterChoice.java
└── test/
    ├── ConsoleUITest.java
    ├── ControllerSmokeTest.java
    ├── ExtensionSeamsTest.java
    ├── RandomGamesTest.java
    ├── StaffRulesTest.java
    ├── victory_input.txt
    └── defeat_input.txt
```

This reflects the reviewed Stage 1 layout. Replay files are generated by
`ConsoleUITest` if not already present.

TODO - update the tree as new stages and documentation are added.

## Architecture and Responsibilities

### Main Components

| Component | Responsibility |
| --- | --- |
| `Main` | Constructs dependencies and starts the game |
| `GameController` | Coordinates arrivals, staff choices, eating, payment, abandonment and results |
| `GameState` | Stores current turn, tables and arrivals; derives queues and statistics |
| `ArrivalSchedule` | Maps turns to customer factories |
| `Customer` | Holds customer lifecycle, satisfaction, table/order references and behaviour hooks |
| `RegularCustomer` | Implements Regular customer behaviour |
| `Table` | Tracks table occupancy |
| `Order` | Stores selected item, locked bill, preparation progress and order status |
| `MenuItem` | Defines the contract for orderable items |
| `BasicMenuItem` | Represents a single base dish |
| `Menu` | Holds available menu items and provides name lookup |
| `PricingPolicy` | Determines the bill when an order is taken |
| `StandardPricing` | Delegates Stage 1 pricing to customer billing behaviour |
| `Waiter` | Validates and performs seating, ordering and serving |
| `Chef` | Validates and advances preparation |
| `Cashier` | Collects one eligible payment per controller turn |
| `GameUI` | Defines the controller/interface boundary |
| `ConsoleUI` | Implements terminal display and input |
| `ActionResult` | Reports staff-action success or failure and its explanation |

### Object-Oriented Design

- Encapsulation: customer and order data are stored in private fields and
  exposed through queries and transition methods. Additional transition
  guards remain a potential improvement in the original reviewed code.
- Inheritance: `RegularCustomer` extends `Customer`.
- Polymorphism: customer selection, waiting loss, and billing are expressed
  through customer methods; pricing is accessed through `PricingPolicy`.
- Interface-based dependencies: the controller uses `GameUI`, and ordering
  uses `MenuItem` and `PricingPolicy`.
- Object collaboration: customers, orders, tables, staff, and controller
  cooperate to complete the service lifecycle.

The final report discusses the actual design trade-offs and applies SOLID
principles only where supported by the code.

## Testing

The current tests are plain Java programs. They do not require JUnit.

### Test Coverage

| Test | Purpose |
| --- | --- |
| `StaffRulesTest` | Seating, ordering, preparation, serving delay, payment priority and cancellation |
| `ControllerSmokeTest` | Full good-play and all-wait controller runs; prints outcomes for inspection |
| `RandomGamesTest` | Runs 3,000 seeded random games and checks final-state invariants and reported totals |
| `ConsoleUITest` | Generates replay scripts and checks console input handling and victory/defeat replays |
| `ExtensionSeamsTest` | Checks test-only customer/pricing extension examples and configurable arrivals |

Test presence does not mean that tests have passed.

`ExtensionSeamsTest` contains later-stage-style test fixtures. The group
must resolve its placement to keep the `stage-1` snapshot consistent with
the brief's restriction on later-stage features.

### Compile Tests - Windows PowerShell

Compile the application first.

```powershell
New-Item -ItemType Directory -Force testout | Out-Null

$testFiles = Get-ChildItem .\test -Filter *.java |
    Select-Object -ExpandProperty FullName

javac -cp out -d testout $testFiles
```

### Run Tests - Windows PowerShell

```powershell
java -cp "out;testout" StaffRulesTest
java -cp "out;testout" ControllerSmokeTest
java -cp "out;testout" RandomGamesTest
java -cp "out;testout" ConsoleUITest
```

If included in the checked-out snapshot:

```powershell
java -cp "out;testout" ExtensionSeamsTest
```

### Compile and Run Tests - macOS / Linux

```bash
mkdir -p testout
javac -cp out -d testout test/*.java

java -cp out:testout StaffRulesTest
java -cp out:testout ControllerSmokeTest
java -cp out:testout RandomGamesTest
java -cp out:testout ConsoleUITest
```

If included in the checked-out snapshot:

```bash
java -cp out:testout ExtensionSeamsTest
```

### Verification Record

| Item | Actual Result |
| --- | --- |
| Tested commit | TODO |
| Tester | TODO |
| Test date | TODO |
| Operating system | TODO |
| JDK version | TODO |
| Application compilation | Not recorded |
| Staff rules | Not recorded |
| Controller smoke results | Not recorded |
| Random-game checks | Not recorded |
| Console UI checks | Not recorded |
| Extension checks, if applicable | Not recorded |

Record actual results and any failures in `TEST_NOTES.md`.

## Reproducible Victory and Defeat

Run `ConsoleUITest` from the project root to generate or refresh:

```text
test/victory_input.txt
test/defeat_input.txt
```

Commit the verified scripts so graders can replay the runs directly.

### Windows PowerShell

```powershell
cmd /c "java -cp out rush.Main < test\victory_input.txt"
cmd /c "java -cp out rush.Main < test\defeat_input.txt"
```

### macOS / Linux

```bash
java -cp out rush.Main < test/victory_input.txt
java -cp out rush.Main < test/defeat_input.txt
```

### Expected Stage 1 Outcomes

These are expectations documented in the supplied test notes.
They must be confirmed against the final Stage 1 commit.

| Metric | Victory Replay | Defeat Replay |
| --- | --- | --- |
| Final result | VICTORY | DEFEAT |
| Paid customers | 4 | 0 |
| Revenue | $57.00 | $0.00 |
| Average paid satisfaction | 78.0 | N/A |
| Served customers | 5 | 0 |
| Unhappy departures | 0 | 2 |
| Turns used | 18 | 18 |

### What the Replays Demonstrate

The victory replay prioritizes service sufficiently to meet the targets.
It also includes malformed input and an invalid customer-ID choice to
exercise input rejection and retry behaviour.

The defeat replay makes the Waiter and Chef wait throughout the game.
It demonstrates that the game still runs all 18 turns and that neglected
customers can abandon the restaurant.

Later stages require their own reproducible evidence after their customer
and pricing behaviour is integrated.

## Stage Versions and Change Evidence

Required tags:

```text
stage-1
stage-2
stage-3
```

Tags must point to actual independently runnable versions, not merely
documentation describing the intended stages.

### Stage Tag Record

| Tag | Commit | Verification |
| --- | --- | --- |
| `stage-1` | TODO | TODO |
| `stage-2` | TODO | TODO |
| `stage-3` | TODO | TODO |

### Stage 1 → Stage 2

Compare URL: TODO - insert the actual GitHub compare or commit link.

| Change Type | Exact File | Change | Reason |
| --- | --- | --- | --- |
| Added | TODO | TODO | TODO |
| Modified | TODO | TODO | TODO |
| Deleted | TODO, or None | TODO | TODO |

Explain:

- How VIP and Critic behaviour is dispatched through customer objects.
- What arrival/setup changes were necessary.
- What Critic-specific rule was chosen.
- How the Stage 1 service flow was preserved.
- Which regression tests or replays were executed.

### Stage 2 → Stage 3

Compare URL: TODO - insert the actual GitHub compare or commit link.

| Change Type | Exact File | Change | Reason |
| --- | --- | --- | --- |
| Added | TODO | TODO | TODO |
| Modified | TODO | TODO | TODO |
| Deleted | TODO, or None | TODO | TODO |

Explain:

- How the Host adds seating capacity without replacing the Waiter.
- How ComboMeal represents its two components.
- How combo selection is determined.
- How Happy Hour activation, duration and recovery are coordinated.
- How VIP and Happy Hour discounts are ordered and rounded.
- Why any earlier classes or interfaces needed modification.
- What tests demonstrate that earlier behaviour still works.

## UML Diagrams and Report

### Class Diagram

TODO - insert the actual diagram location or report page.

The final diagram must match Stage 3 code and show:

- Principal domain classes.
- Relevant controller and boundary classes.
- Relevant enums.
- Key attributes and operations with visibility.
- Inheritance and interface realization.
- Associations and meaningful multiplicities.
- Meaningful examples of encapsulation, polymorphism and composition.

### Sequence Diagram

TODO - insert the actual diagram location or report page.

The scenario must include:

- Customer arrival and order selection.
- Manager decisions and staff assignments.
- Preparation to READY.
- Serving to SERVED.
- Eating and payment.
- Result evaluation.
- At least one Stage 2 or Stage 3 behaviour.
- Relevant guards and loops.

Explain how a manager choice changes the outcome.

### Final PDF Report

TODO - insert the actual report location.

The report includes the final diagrams, responsibilities, design discussion,
trade-offs, an alternative design, functional evidence and reflections.

## Presentation Video

Video file or accessible download link: TODO

The group video should be approximately 15 minutes, with readable screen
capture and audible explanation. Each member explains a part they understand.

| Required Segment | Timestamp | Presenter |
| --- | --- | --- |
| Launch from the submitted project | TODO | TODO |
| Meaningful manager choices with constrained staff | TODO | TODO |
| READY → SERVED → payment | TODO | TODO |
| VIP or Critic behaviour | TODO | TODO |
| Stage 3 feature | TODO | TODO |
| Victory and defeat replays | TODO | TODO |
| Stage comparisons and diagrams | TODO | TODO |

Use actual recording timestamps, not planned timings.

## Known Limitations and Verification Status

- Only Stage 1 source has been reviewed for this README draft.
- Compilation and test results have not yet been recorded here.
- Git stage tags have not been verified in this draft.
- The original domain API relies on staff/controller validation for several
  state transitions; additional domain guards are a possible improvement.
- The placement of test-only later-stage examples must be resolved before
  finalizing the Stage 1 snapshot.
- The turn-16 arrival cannot pay within 18 turns. This is an intended rule,
  not a defect.
- The reviewed source uses console interaction; it does not include a GUI.
- No packaged JAR or launcher scripts are assumed by these instructions.

TODO - replace this section with the final, accurately verified limitations
before submission.

## Submission Checklist

- [ ] Source code included.
- [ ] Build/run commands verified from a clean checkout.
- [ ] All mandatory gameplay works offline.
- [ ] Stage 1 independently runnable at `stage-1`.
- [ ] Stage 2 independently runnable at `stage-2`.
- [ ] Stage 3 independently runnable at `stage-3`.
- [ ] Stage 1 contains no later-stage gameplay features.
- [ ] Reproducible victory and defeat evidence included.
- [ ] Both stage-transition change tables completed.
- [ ] Actual commit or compare links supplied.
- [ ] Final class and sequence diagrams match the implementation.
- [ ] PDF report included.
- [ ] Video or accessible download link included.
- [ ] Required video timestamps supplied.
- [ ] Signed Declaration of Original Work included.
- [ ] Each member's Declaration on Use of GAI Assistance included.
- [ ] All TODO placeholders resolved.
- [ ] Final group archive named according to the assignment brief.

## Acknowledgements and Declarations

This project is developed for the SC2002 Restaurant Rush group assignment.

