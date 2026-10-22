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
