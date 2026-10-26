# Design Decisions (Stage 1)

Short, report-ready notes on choices where the brief left room, with the alternatives we rejected.

## 1. Waiter and Chef: interleaved choice and action (deliberate deviation)
**Brief:** step 2 collects the manager's choices, step 3 has the Waiter act before the Chef. Read literally, both
choices are collected first and then carried out.

**What we built:** the Waiter's choice is validated and carried out immediately (re-asking if invalid), then the Chef's
choice is asked, validated and carried out.

**Why:** the brief says the Chef may work on an order the Waiter took this turn. That order does not exist when both
choices are collected up front, so the literal model must validate against an imagined future state ("assume the Waiter's
choice succeeds"). That is complicated, and it conflicts with the rule that an invalid choice must be explained and
re-asked without using up the task. Validating against real state is simpler and always correct.

**Effect on play:** none for the rules or outcomes. The only difference is information: the manager sees the Waiter's
result before choosing the Chef's task.

**Rejected:** (B) collect both then execute, with simulated validation: more code and edge cases for little gain.
(C) hybrid, collecting both choices but validating the Chef's after the Waiter acts: still shows the Waiter's result, with more
confusing prompts.

**Sequence diagram:** show Waiter choice -> Waiter acts -> Chef choice -> Chef acts, inside the controller's manager step.

## 2. State is derived, never duplicated
`GameState` stores only the turn, the tables and all arrived customers. The queue, orders and all totals (revenue, paid,
served, unhappy departures, average satisfaction) are computed from the customers and orders on demand.
**Why:** one source of truth. No class has to remember to "record" an event, so adding the Host or Happy Hour cannot make
the totals drift from reality. **Cost:** recomputation on each query (negligible at this size).
Customer stores `satisfactionAtPayment` so the average uses the value recorded when each customer paid, as the brief requires.

## 3. Pricing has one home
`Waiter.takeOrder` asks a `PricingPolicy` for the price and locks it into the `Order`. Stage 1 uses `StandardPricing`
(which delegates to `Customer.billFor`, so a VIP discount is polymorphic). A later Happy Hour wraps the existing policy
(decorator), giving "VIP discount first, then Happy Hour" with no change to the Waiter.
Proved in `ExtensionSeamsTest` (VIP Burger under a 20%-off wrapper = $10.80, the brief's example).

## 4. Arrivals are data
`ArrivalSchedule` takes a plan (turn -> `CustomerFactory`). The base setting is six Regular arrivals; a Stage 2 setting is
a different plan, not different code.

## 5. Food rule names the dishes
Regular customers cycle Salad, Burger, Pasta by customer id. The rule names dishes rather than indexing into the menu, so
adding combos later does not change what Regular customers order.

## 6. Where the UI stops
`ConsoleUI` rejects malformed input (not a number, out of range) and re-asks. It never applies game rules: whether a seat,
order or serve is allowed is decided by the staff objects, which return an `ActionResult` with the reason.
Staff results carry display text; this is a known trade-off (simple, but wording lives outside the UI).

# Design Decisions (Stage 3)

## 7. Happy Hour is a pricing decorator
**What we built:** `HappyHourPricing implements PricingPolicy` wraps another policy (in `Main`, `StandardPricing`). It asks
the inner policy for the normal bill, then takes 20% off if Happy Hour is active on the current turn. A small `HappyHour`
object remembers the activation turn and answers `isActiveOn(turn)` and `turnsRemaining(turn)`. The controller switches it
on, and the pricing reads it. Both hold the same object.

**Why:** "VIP discount first, then Happy Hour" comes for free, because the VIP discount is already inside `billFor`, which
the inner policy calls. The Waiter is unchanged (OCP: new behaviour through a new class, not an edit), and orders already
taken keep their price because `Order` stores the locked price. The pricing gets the turn through an `IntSupplier`
(`state::getTurn` in `Main`), so `domain` does not depend on `control.GameState`.

**Rounding:** each discount rounds to the nearest cent in integer cents: `(price * 80 + 50) / 100`. VIP Salad + Pasta Combo:
2430 -> 2187 -> 1749.6 -> 1750. The old lambda in `ExtensionSeamsTest` truncates (`* 8 / 10`). That was only a seam demo,
and the real class rounds.

**Rejected:** (A) a `happyHour` flag inside `Waiter.takeOrder`. It mixes a pricing rule into staff code and breaks the
"later pricing wraps a policy" agreement in `CONTRACT.md`. (B) a `HappyHourVIPCustomer` style subclass: discounts would
multiply with customer types.

## 8. New `GameUI` methods are default methods
`askHostChoice` and `askHappyHourActivation` were added to `GameUI` with default bodies: the Host waits, and Happy Hour is
not switched on. `ConsoleUI` overrides both.
**Why:** four test UIs implement `GameUI`. Abstract methods would break all of them, even though Stage 1/2 games never
need a Host. Default methods (Ch 10, Modern Java) let the interface grow without editing every implementer.
**Cost:** a UI author can forget to override and silently get "Host always waits". `ConsoleUITest` checks the console
prompts, so the real UI is covered.
**Rejected:** a separate `Stage3UI extends GameUI` interface. The controller would need a cast or a second UI reference
for the Stage 3 prompts.

## 9. Turn order in Stage 3
arrivals -> board shown -> Happy Hour offer (if unused) -> recovery (if active) -> task lists -> Host -> Waiter -> Chef ->
eating and payment -> waiting loss and abandonment -> summary.
- The brief only says Happy Hour starts "at the start of a turn". The board is shown first so the manager can see the queue
  before deciding. Recovery still happens before any staff action.
- **Host before Waiter:** a customer the Host seats can order from the Waiter in the same turn, which is the point of having a
  second person seating. It follows the same idea as decision 1 (choose, act, then the next staff member sees the real state).
- **Rejected:** Host after the Waiter. The Host would then mostly seat people who can only order next turn, which makes the
  Host far less useful for no rule reason.

## 10. Seating logic has one home
The seating checks (customer exists, is in the queue, a table is free, lowest free table) moved from `Waiter.seatCustomer`
into a package-private `staff/Seating` class. `Waiter.seatCustomer` and `Host.seatCustomer` both call it. The Waiter's public
method and messages are unchanged, and `StaffRulesTest` passes without edits.
**Rejected:** `Host extends Waiter`. A Host is not a Waiter (it cannot take orders or serve), so it would inherit actions it
must not have (LSP). We also rejected copying the checks into `Host`.

## 11. Combo-selection rule lives in `Customer`
Even-ID Regular and VIP customers upgrade their usual dish to the combo containing it. The rule is a protected
`Customer.chooseByIdRule(Menu)` that both subclasses call from `chooseItem`. The Critic keeps its own `chooseItem` (Pasta).
**Why:** Regular and VIP had identical copies of `DISH_CYCLE`. Adding the combo upgrade to both copies would have doubled the
duplication. The rule looks combos up by name and falls back to the usual dish, so a menu without combos gives exactly the
Stage 1/2 choices. `ExtensionSeamsTest` and `Stage2CustomersTest` pass unchanged.
**Rejected:** (A) a new `Customer.chooseOrder` template method called by the Waiter. It would mean an extra public method on
a frozen class and an edit to the Waiter. (B) a new "combo customer" type. The brief says a customer chooses a combo, not
that a new kind of customer is needed.

## 12. The Stage 1/2 controller still exists
`GameController` keeps its 6-argument constructor, which runs without a Host or Happy Hour (those fields are `null` and
their steps are skipped). The 8-argument constructor adds them. This keeps every Stage 1/2 test unchanged and shows the
earlier behaviour still works. **Cost:** two `null` checks in the controller. A "null object" Host that always waits would
remove them, but it would still print Host prompts in a Stage 2 game.
