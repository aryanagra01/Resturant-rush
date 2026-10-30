# Restaurant Rush

A turn-based restaurant management game for the command line, written in Java for
SC2002. You are the manager. Each turn you decide what the Waiter and the Chef do, and
you try to reach the targets by the end of turn 18.

**Repository:** https://github.com/aryanagra01/Resturant-rush

The current Stage 2 code is on branch `staeg-2-cleanup`. It has 22 Java source files.
The tags `stage-1` and `stage-2` are earlier snapshots. The cleanup on this branch is
not part of the `stage-2` tag.

## Build and run

Developed and tested with **Java 17**. The program uses text blocks and has no
libraries or network access. Run the commands from the project folder.

The commands in the first block use a Unix-style shell, such as bash:

```bash
javac --release 17 -d out \
  Main.java \
  boundary/*.java \
  config/*.java \
  control/*.java \
  entities/customer/*.java \
  entities/menu/*.java \
  entities/restaurant/*.java \
  entities/staff/*.java \
  utility/*.java
java -cp out Main
```

Windows PowerShell, from the same folder:

```powershell
javac --release 17 -d out Main.java boundary\*.java config\*.java control\*.java entities\customer\*.java entities\menu\*.java entities\restaurant\*.java entities\staff\*.java utility\*.java
java -cp out Main
```

To look at an earlier snapshot, check out its tag, for example `git checkout stage-2`.
That tag does not include the later cleanup on `staeg-2-cleanup`.

## How to play

Every turn the game shows the revenue, the average satisfaction of paid customers, the
waiting queue, the tables, every customer's satisfaction and order status, and the tasks
each staff member can do. Anyone scheduled for that turn arrives before the choices.
Then:

1. **Waiter:** choose one task by number: `0` wait, `1` seat a customer, `2` take an
   order, `3` serve a ready order. For 1, 2 or 3, enter the customer id.
2. **Chef:** enter an order id to prepare one unit of it, or `0` to wait. Food finished
   this turn can be served on a later turn.
3. **Cashier:** works automatically and takes one eligible payment per turn. The oldest
   ready customer is chosen first: the earliest turn served, then the smaller customer
   id.

After the Chef, a customer who has been eating since an earlier turn finishes eating.
The Cashier can take that customer's payment on a later turn. Customers who are still
waiting for a table or for food then lose satisfaction.

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
| Critic | 12 | Full price | Pasta (on a menu without Pasta, the Regular rule) | +10 satisfaction if served within 2 turns of ordering (max 100) |

Customers lose satisfaction only while waiting for a table or for food. A customer who
reaches 0 leaves without paying: any unfinished order is cancelled and the table is
freed.

## Tests

There is no automated test class in this repository. Stage 2 was checked by compiling
with Java 17 and replaying two local input scripts, one victory and one defeat. Each
run's console output was compared byte for byte with a saved log. Those scripts and
logs are shared with the group separately. They are not part of this repository.

To repeat that check, compile as above, then run `Main` with a shared script as
standard input and compare the output with the shared log.

Unix-style shell:

```bash
java -cp out Main < /path/to/stage2-victory.txt > victory-output.txt
cmp victory-output.txt /path/to/victory-expected.txt
```

PowerShell does not support `<`. From PowerShell, use Command Prompt redirection:

```powershell
cmd /c "java -cp out Main < C:\path\to\stage2-victory.txt > C:\path\to\victory-output.txt"
fc /b C:\path\to\victory-output.txt C:\path\to\victory-expected.txt
```

Repeat the same steps with the shared defeat script. `cmp` and `fc /b` report no
differences when the output matches the saved log.

## Project structure

The program uses Boundary–Control–Entity separation. The boundary reads input and
prints output. The control package runs the turns. The entities hold the restaurant,
customers, orders and staff. Settings and shared checks sit beside those three.

| Package | Classes | Responsibility |
|---|---|---|
| `boundary` | `GameCLI` | Command-line implementation of `GameView`. Shows the state and reads the manager's input. It does not apply game rules |
| `control` | `GameController`, `GameView` | `GameController` runs each turn, owns the Waiter, Chef and Cashier, and retries a refused choice. `GameView` is the interface a user interface implements |
| `config` | `GameSettings` | One game's settings. `GameSettings.base()` is the required base setting, including arrivals and victory targets |
| `entities.customer` | `Customer` (abstract), `RegularCustomer`, `VIPCustomer`, `CriticCustomer`, `CustomerStatus`, `Order`, `OrderStatus` | Shared seat, order, eat and pay flow. Subclasses override the type name, satisfaction loss, food choice, discount and service bonus. `Order` stores the dish, bill and the turn it was placed |
| `entities.menu` | `MenuItem` | A dish: name, price in cents, and preparation units |
| `entities.restaurant` | `Restaurant`, `Table`, `GameStats` | `Restaurant` manages the tables, customers, waiting queue and statistics. `GameStats` stores the totals and checks victory |
| `entities.staff` | `Staff` (abstract), `Waiter`, `Chef`, `Cashier` | `Staff` allows one successful task per turn. Each subclass performs that role's task and records it only after it succeeds |
| `utility` | `Require`, `GameRuleException` | `Require` is the shared validation. `GameRuleException` is an invalid gameplay choice, which the controller shows and then asks again |
| (root) | `Main` | Builds the base setting, the command-line view and the controller, then starts the game |

`Customer` and `Staff` are abstract. Callers use a `Customer` reference, and the
subclass methods supply the Regular, VIP or Critic behaviour. `GameCLI` is the only
`GameView` implementation.

## Design diagrams

To be added.

## Stages

| Stage | Adds | Tag |
|---|---|---|
| 1 | Regular customers, menu, Waiter, Chef, Cashier, full turn sequence, victory and defeat | `stage-1` |
| 2 | VIP and Critic customers | `stage-2` |
| 3 | Host, ComboMeal, Happy Hour | Not tagged yet |

`stage-1` and `stage-2` are unchanged snapshots. Stage 3 is planned and has not been
implemented. The current cleanup is on `staeg-2-cleanup`, after the `stage-2` tag.

### Stage 1 → Stage 2

Compare: https://github.com/aryanagra01/Resturant-rush/compare/stage-1...stage-2

**Adding VIP and Critic customers**

| File | Change | Reason |
|---|---|---|
| `VIPCustomer.java` | New | Loses 5 per turn and gets 10% off through `applyDiscount` |
| `CriticCustomer.java` | New | Loses 12 per turn, prefers Pasta in `pickMenuItem`, and `onFoodServed` adds 10 satisfaction for fast service |
| `Customer.java` | Changed | Subclasses override `getType`, `getSatisfactionLoss`, `pickMenuItem`, `applyDiscount` and `onFoodServed`. Shared methods such as `chooseMenuItem` and `calculateBill` call those, so the controller does not branch on the customer type |
| `Order.java` | Changed | Records `placedTurn`, which the Critic uses for the fast-service rule |
| `Waiter.java` | Changed | The bill comes from `calculateBill`, so the VIP discount is applied by the customer. Serving calls `receiveFood`, which runs `onFoodServed` |
| `Restaurant.java` | Changed | Food choice goes through `selectMenuItem`, which asks the customer |
| `GameController.java` | Changed | Creates `RegularCustomer`, `VIPCustomer` or `CriticCustomer` for each arrival. The base setting uses a VIP on turn 4 and a Critic on turn 10 |
| `GameCLI.java` | Changed | Shows each customer's type from `getType()` and prints the bill, including a VIP discount |

The `stage-2` tag remains that earlier runnable snapshot. This branch later simplified
the same behaviour. The cleanup removed helper classes that were no longer needed
(`VictoryTargets`, `IdSequence`, `Money`, `WaiterAction` and `CustomerType`). Staff
check that they can act, perform the task, and record it only when it succeeds.
Searching and the task descriptions use ordinary loops. Customer-specific behaviour
stays in the overridden methods. Shared validation stays in `Require`, and the
Boundary–Control–Entity split is unchanged.

### Stage 2 → Stage 3

To be added with Stage 3. There is no `stage-3` tag yet.

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
