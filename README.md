# Restaurant Rush

A turn-based restaurant management game for the command line, written in Java for
SC2002. You are the manager. Each turn you decide what the Waiter and the Chef do, and
you try to reach the targets by the end of turn 18.

**Repository:** https://github.com/aryanagra01/Resturant-rush

## Build and run

Requires **Java 15 or newer** (the CLI uses text blocks). No libraries and no network
access needed.

```bash
javac -d out *.java
```
```bash
java -cp out Main
```

To run a specific stage, check out its tag first:

```bash
git checkout stage-2
```

## How to play

Every turn the game shows the revenue, the average satisfaction of paid customers, the
waiting queue, the tables, every customer's satisfaction and order status, and the tasks
each staff member can do. Then:

1. **Waiter:** choose one task by number: `0` wait, `1` seat a customer, `2` take an
   order, `3` serve a ready order. Then enter the customer id.
2. **Chef:** enter an order id to prepare one unit of it, or `0` to wait.
3. **Cashier:** works automatically and takes one eligible payment per turn, oldest
   first.

An invalid choice is explained and you choose again. It does not use up the staff task.

**Win** at the end of turn 18 with all three: at least 4 paid customers, revenue of at
least $45.00, and average satisfaction of paid customers of at least 60.

### Base setting

| Setting | Value |
|---|---|
| Turns | 18 |
| Tables | 2 |
| Staff | 1 Waiter, 1 Chef, 1 Cashier |
| Arrivals | Turns 1, 4, 7, 10, 13, 16. Turn 4 is a VIP, turn 10 a Critic, the rest Regular |
| Menu | Salad $9.00 (1 unit), Pasta $18.00 (2 units), Burger $15.00 (1 unit) |

### Customer types (Stage 2)

| Type | Loss per waiting turn | Bill | Food choice | Special behaviour |
|---|---|---|---|---|
| Regular | 8 | Full price | Menu item at index *customer id mod 3* | — |
| VIP | 5 | 10% off | Same as Regular | — |
| Critic | 12 | Full price | Always Pasta | +10 satisfaction if served within 2 turns of ordering (max 100) |

Customers lose satisfaction only while waiting for a table or for food. A customer who
reaches 0 leaves without paying: any unfinished order is cancelled and the table is
freed.

## Tests

```bash
javac -d out *.java test/*.java
```
```bash
java -cp out Stage2Test
```

Expected: `37 passed, 0 failed`. The test cases and the two reproducible runs (one
victory, one defeat, both including a VIP and a Critic) are described in
[test/TEST_NOTES.md](test/TEST_NOTES.md). To replay a run by hand:

```bash
java -cp out Main < test/runs/stage2-victory.txt
```

## Project structure

| Layer | Classes | Responsibility |
|---|---|---|
| Boundary | `GameCLI` | Shows the state and reads the manager's input. No game rules |
| Control | `GameController`, `WaiterAction` | Runs the six turn steps in order and dispatches the Waiter's choice |
| Entity: restaurant | `Restaurant`, `Table`, `MenuItem`, `GameStats`, `BaseSetting` | Tables, queue, menu, staff, statistics, victory check, base setting |
| Entity: customers | `Customer` (abstract), `RegularCustomer`, `VIPCustomer`, `CriticCustomer`, `CustomerType` | Shared service flow; each type overrides its own behaviour |
| Entity: staff | `Staff` (abstract), `Waiter`, `Chef`, `Cashier` | One task per turn (in `Staff`); each role's task |
| Entity: orders | `Order`, `OrderStatus`, `CustomerStatus` | Order lifecycle and status rules |
| Utility | `Require`, `Money` | Shared validation; formatting cents as dollars |

Design notes on the refactoring and the OOP principles used are in
[docs/REFACTORING.md](docs/REFACTORING.md).

## Stages

| Stage | Adds | Tag |
|---|---|---|
| 1 | Regular customers, menu, Waiter, Chef, Cashier, full turn sequence, victory and defeat | `stage-1` |
| 2 | VIP and Critic customers | `stage-2` |
| 3 | Host, ComboMeal, Happy Hour | `stage-3` |

### Stage 1 → Stage 2

Compare: https://github.com/aryanagra01/Resturant-rush/compare/stage-1...stage-2

**Part A: adding VIP and Critic customers**

| File | Change | Reason |
|---|---|---|
| `VIPCustomer.java` | New | Loses 5 per turn and gets 10% off |
| `CriticCustomer.java` | New | Loses 12 per turn, always orders Pasta, rewards fast service |
| `Customer.java` | Changed | Added overridable methods (`getSatisfactionLoss`, `chooseMenuItem`, `applyDiscount`, `onFoodServed`) so each type's behaviour is chosen by the customer object, not by type checks in the controller |
| `Order.java` | Changed | Records `placedTurn`, needed for the Critic's fast-service rule |
| `Waiter.java` | Changed | Bill now comes from the customer's own pricing (VIP discount); notifies the customer after serving |
| `Restaurant.java` | Changed | Food choice moved from `Restaurant` to the customer object |
| `GameController.java` | Changed | Turn 4 arrival is a VIP and turn 10 a Critic; shows the discounted bill |
| `GameCLI.java` | Changed | Explains the three customer types and shows each customer's type |

**Part B: refactoring for reuse** (no change in game behaviour)

Adding Stage 2 showed repeated code across the staff classes, the customer classes, the
controller and the CLI. It was refactored before tagging the final Stage 2. All 22
recorded test inputs give identical output before and after.

| File | Change | Reason |
|---|---|---|
| `RegularCustomer.java` | New | `Customer` became abstract; regular behaviour moved to its own subclass |
| `CustomerType.java` | New | Creates each customer type, replacing the `if (turn == 4) … else if (turn == 10)` chain and the `instanceof` checks in the CLI |
| `WaiterAction.java` | New | Replaces int constants and an if/else chain for the Waiter's menu |
| `GameStats.java` | New | Statistics and the victory check, split out of `Restaurant` |
| `BaseSetting.java` | New | All base-setting values in one place |
| `Require.java`, `Money.java` | New | Shared validation and money formatting, previously copied across classes |
| `Staff.java` | Changed | Owns the once-per-turn rule (`performTask`), previously copied into all three roles |
| `Waiter.java`, `Chef.java`, `Cashier.java` | Changed | Use `performTask`; Cashier owns the oldest-first payment choice |
| `Customer.java` | Changed | Abstract, with final template methods that validate once and call the subclass hooks |
| `CustomerStatus.java`, `OrderStatus.java`, `Order.java` | Changed | Status rules (e.g. "placed or preparing") written once instead of 4 times |
| `Restaurant.java` | Changed | One generic search helper; delegates statistics to `GameStats` |
| `GameController.java` | Changed | No display text; one retry loop for invalid choices |
| `GameCLI.java` | Changed | Shared formatting helpers; no game rules; 59 `println` calls replaced by one helper |
| `Restaurant.findTable`, `Cashier.canProcessPayment` | Deleted | Unused / replaced by the inherited `Staff.canAct` |

### Stage 2 → Stage 3

Compare: https://github.com/aryanagra01/Resturant-rush/compare/stage-2...stage-3

*To be added with Stage 3.*

## Video

Link: *to be added*

| # | Segment | Timestamp | Presenter |
|---|---|---|---|
| 1 | Game launching from the submitted project | | |
| 2 | Manager choices, including a decision where staff are constrained | | |
| 3 | A dish going READY → SERVED → payment | | |
| 4 | A VIP or Critic behaviour | | |
| 5 | A Stage 3 feature | | |
| 6 | One victory and one defeat | | |
| 7 | Stage tag comparisons and the diagrams | | |
