# Restaurant Rush: Shared Contract (frozen at kickoff)

Java 17+. Package root `rush`. Money is **integer cents**. Customers are identified by **customer id** everywhere.

## Build / run
```
mkdir -p out
javac -d out $(find src -name "*.java")
java -cp out rush.Main
```

## Ownership (who fills in which TODO)
| Owner | Files |
|---|---|
| A: Engine | `control/GameController`, `control/ArrivalSchedule`, `control/GameConfig` |
| B: Staff | `staff/Waiter`, `staff/Chef`, `staff/Cashier` |
| C: CLI | `ui/ConsoleUI` (+ `Main` wiring) |
| D: Customers & pricing | `domain/Customer`, `RegularCustomer`, `Menu`, `MenuItem` family |
| E: Quality & docs | tests, tags, diagrams, README, change log |

Shared and frozen (change only with all-hands agreement): `CustomerState`, `OrderStatus`, `ActionResult`,
`GameUI`, `WaiterChoice`, `ChefChoice`, `GameState` method signatures (read-only queries plus `setTurn`/`addArrival`), `Customer` public methods
(constructor is `Customer(int id)`),
`PricingPolicy`, `CustomerFactory`.

## Agreed decisions
1. **Waiter then Chef (deliberate deviation from the brief's literal wording):** the Waiter choice is validated and
   applied immediately; the Chef choice is then validated against the real, updated state (so Chef can cook an order
   taken this turn). Reasoning and alternatives: see `DESIGN_DECISIONS.md`.
2. **Invalid choice** => staff returns `ActionResult.fail(reason)`; controller shows it via `ui.showMessage`
   and asks again. The task is NOT used up. The UI never checks game rules, only input format.
3. **Timing rules live in the domain objects:** `Order.isServableOn(turn)`, `Customer.hasFinishedEatingOn(turn)`,
   `Customer.canPayOn(turn)` all use "strictly earlier turn". Do not re-implement them elsewhere.
4. **Price is locked at order time:** `Waiter.takeOrder` asks its `PricingPolicy` (`pricing.priceFor(customer, item)`) and passes the result to `new Order(...)`.
   Stage 1 uses `StandardPricing` (= `customer.billFor(item)`). Later pricing rules WRAP a policy instead of editing the Waiter.
5. **Cashier ordering:** oldest READY_TO_PAY first = lowest `getReadyToPayOnTurn()`, ties by lowest customer id.
6. **Seating:** the manager picks the customer; the Waiter assigns the lowest-numbered free table.
7. **No type checks on customers** in controller/staff. Differences go in Customer subclasses.

## Who updates what
- `GameState` stores ONLY the turn, the tables and every customer who has arrived. The queue, the orders, and every total
  (revenue, paid, served, unhappy departures, average satisfaction) are DERIVED from the customers and their orders.
  There is nothing to "record": do not add counters to `GameState`.
- `Customer.pay()` stores the satisfaction at the moment of payment (`getSatisfactionAtPayment()`), which the average uses.
- Staff only change customers/orders: `Waiter` seats, orders and serves; `Chef` advances orders; `Cashier` calls `customer.pay()`.
- The controller handles arrivals, eating, satisfaction loss, abandonment and the final result.
- The start-of-turn display is shown AFTER arrivals (so the new customer is visible and choosable).

## Extension seams (how later stages plug in)
| Need | Seam | What changes |
|---|---|---|
| New customer type (VIP, Critic) | subclass `Customer`; override `getWaitingLoss`, `billFor`, `chooseItem` | new file only |
| Mixed arrivals | `new ArrivalSchedule(Map<turn, CustomerFactory>)` | wiring in `Main` (plus a new setting) |
| ComboMeal | implement `MenuItem`; add to a `Menu` | new file + `Menu` wiring |
| Happy Hour price | a `PricingPolicy` that wraps `StandardPricing` (VIP discount first, then 20% off) | new file + `Main` wiring |
| Host, Happy Hour commands | new controller step(s) and `GameUI` methods | edits to `GameController`, `GameUI`, `ConsoleUI` (document the reason) |
| Happy Hour satisfaction recovery | a new `Customer` method added in Stage 3 | edit to `Customer` (document the reason) |

`RegularCustomer` food rule: cycles Salad, Burger, Pasta by customer id (names the dishes explicitly, so a bigger menu does not change it).

## Stage 1 rule
Stage 1 contains NO Stage 2/3 features. Record every edit to an earlier file, with the reason, in the change log.
