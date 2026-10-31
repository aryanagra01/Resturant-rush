# Restaurant Rush

A turn-based restaurant management game for the command line, written in Java for
SC2002. You are the manager. Each turn you decide what the Host, the Waiter and the
Chef do, and when to use Happy Hour, and you try to reach the targets by the end of
turn 18.

**Repository:** https://github.com/aryanagra01/Resturant-rush

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
  entities/pricing/*.java \
  entities/restaurant/*.java \
  entities/staff/*.java \
  utility/*.java
java -cp out Main
```

Windows PowerShell, from the same folder:

```powershell
javac --release 17 -d out Main.java boundary\*.java config\*.java control\*.java entities\customer\*.java entities\menu\*.java entities\pricing\*.java entities\restaurant\*.java entities\staff\*.java utility\*.java
java -cp out Main
```

To look at an earlier snapshot, check out its tag, for example `git checkout stage-2`.
That tag does not include the later Stage 2 cleanup.

## How to play

Every turn the game shows the revenue, the average satisfaction of paid customers, the
waiting queue, the tables, every customer's satisfaction and order status, and the tasks
each staff member can do. It also shows whether Happy Hour is available, on (and for
how long) or used. Anyone scheduled for that turn arrives before the choices. Then:

1. **Happy Hour** (until it is used): `1` to switch it on now, `0` to keep it for later.
2. **Host:** enter the number next to a waiting customer to seat them, or `0` to wait.
   Only asked when someone is waiting and a table is free.
3. **Waiter:** choose one task by number: `0` wait, `1` seat a customer, `2` take an
   order, `3` serve a ready order. For 1, 2 or 3, enter the customer id.
4. **Chef:** enter the number next to an order to prepare one unit of it, or `0` to
   wait. Food finished this turn can be served on a later turn.
5. **Cashier:** works automatically and takes one eligible payment per turn. The oldest
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
| Staff | 1 Waiter, 1 Chef, 1 Cashier, plus 1 Host from Stage 3 |
| Arrivals | Turns 1, 4, 7, 10, 13, 16. Turn 4 is a VIP, turn 10 a Critic, the rest Regular |
| Menu | Salad $9.00 (1 unit), Pasta $18.00 (2 units), Burger $15.00 (1 unit), then three combos from Stage 3 (see below) |

### Customer types (Stage 2)

| Type | Loss per waiting turn | Bill | Food choice | Special behaviour |
|---|---|---|---|---|
| Regular | 8 | Full price | Menu item at index *customer id mod menu size* (3 items in Stage 2, 6 in Stage 3) | — |
| VIP | 5 | 10% off | Same as Regular | — |
| Critic | 12 | Full price | Pasta (on a menu without Pasta, the Regular rule) | +10 satisfaction if served within 2 turns of ordering (max 100) |

Customers lose satisfaction only while waiting for a table or for food. A customer who
reaches 0 leaves without paying: any unfinished order is cancelled and the table is
freed.

### Stage 3: Host, combo meals and Happy Hour

| Feature | Rules |
|---|---|
| Host | Seats at most one waiting customer per turn, before the Waiter acts and in addition to the Waiter's task. The Waiter can still seat customers too |
| Combo meals | A combo is two menu items served together. Preparation units = the sum of both; price = 90% of both prices together, rounded to the nearest cent. The menu has Salad + Pasta ($24.30, 3 units), Salad + Burger ($21.60, 2 units) and Pasta + Burger ($29.70, 3 units) |
| Combo choice | Regular and VIP customers pick menu item *customer id mod 6*. The menu lists the three dishes, then the three combos, so customers whose id mod 6 is 3, 4 or 5 order a combo. The Critic still prefers Pasta |
| Happy Hour | Can be switched on once per game, at the start of a turn, and lasts that turn and the next; the turn screen shows how long is left. Orders taken while it is on get 20% off, after any VIP discount (a VIP's Burger: $15 × 0.9 × 0.8 = $10.80). The price is fixed when the order is taken, so earlier orders do not change |
| Happy Hour recovery | At the start of each Happy Hour turn, customers still waiting for a table or food recover 15 satisfaction (maximum 100). Customers who have left are not affected |

## Tests

There is no automated test class in this repository. Stage 2 was checked by compiling
with Java 17 and replaying two local input scripts, one victory and one defeat. Each
run's console output was compared byte for byte with a saved log. Those scripts and
logs are shared with the group separately. They are not part of this repository.

To repeat that check, compile as above, then run `Main` with a shared script as
standard input and compare the output with the shared log.

Stage 3 has two reproducible runs in [test/runs](test/runs). Both include a VIP and a
Critic.

| Script | Result | What it shows |
|---|---|---|
| `stage3-victory.txt` | VICTORY: 4 paid, $71.10, average satisfaction 79.0 | The Host seats each arrival. Happy Hour is switched on in turn 4, so the VIP's Burger costs $10.80 and waiting customers recover in turns 4 and 5. Customers 3 and 5 order combos |
| `stage3-defeat.txt` | DEFEAT: nobody is served, 2 unhappy departures | Every staff member waits and Happy Hour is never used |

```bash
java -cp out Main < test/runs/stage3-victory.txt
```

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
| `control` | `GameController`, `GameView` | `GameController` runs each turn, owns the Host, Waiter, Chef and Cashier, and retries a refused choice (`retryUntilValid`). `GameView` is the interface a user interface implements |
| `config` | `GameSettings` | One game's settings. `GameSettings.base()` is the required base setting, including arrivals and victory targets |
| `entities.customer` | `Customer` (abstract), `CustomerType`, `RegularCustomer`, `VIPCustomer`, `CriticCustomer`, `CustomerStatus`, `Order`, `OrderStatus` | Shared seat, order, eat and pay flow. `CustomerType` creates the Regular, VIP or Critic customer. Subclasses override the type name, satisfaction loss, food choice, discount and service bonus. `Order` stores the dish, bill and the turn it was placed |
| `entities.menu` | `MenuItem`, `ComboMeal` | A dish: name, price in cents, and preparation units. A `ComboMeal` is a `MenuItem` made of two `MenuItem`s |
| `entities.pricing` | `PricingPolicy`, `HappyHour` | `PricingPolicy` is a pricing rule applied when an order is taken, plus `percentOff()`, the one rounding rule every discount uses. `HappyHour` is the policy the manager can switch on |
| `entities.restaurant` | `Restaurant`, `Table`, `GameStats` | `Restaurant` manages the tables, customers, waiting queue, statistics and Happy Hour. `GameStats` stores the totals and checks victory |
| `entities.staff` | `Staff` (abstract), `FrontOfHouseStaff` (abstract), `Waiter`, `Host`, `Chef`, `Cashier` | `Staff.performTask` allows one successful task per turn and records it only after it succeeds. `FrontOfHouseStaff` holds the seating shared by the Waiter and the Host |
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
| 3 | Host, ComboMeal, Happy Hour | `stage-3` |

`stage-1` and `stage-2` are unchanged snapshots. The Stage 2 cleanup came after the
`stage-2` tag, and Stage 3 is built on top of it.

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
| `CustomerType.java` | New | Creates the Regular, VIP or Critic customer, so the controller does not choose the class |
| `GameController.java` | Changed | Asks `CustomerType` to create each arrival. The base setting uses a VIP on turn 4 and a Critic on turn 10 |
| `GameCLI.java` | Changed | Shows each customer's type from `getType()` and prints the bill, including a VIP discount |

The `stage-2` tag remains that earlier runnable snapshot. This branch later simplified
the same behaviour. The cleanup removed helper classes that were no longer needed
(`VictoryTargets`, `IdSequence`, `Money` and `WaiterAction`). `CustomerType` still creates each arrival. Staff
check that they can act, perform the task, and record it only when it succeeds.
Searching and the task descriptions use ordinary loops. Customer-specific behaviour
stays in the overridden methods. Shared validation stays in `Require`, and the
Boundary–Control–Entity split is unchanged.

### Stage 2 → Stage 3

Compare: https://github.com/aryanagra01/Resturant-rush/compare/stage-2...stage-3

| File | Change | Reason |
|---|---|---|
| `entities/staff/Staff.java` | Changed | `performTask()` runs a task and records it only if it succeeds, so the Host, Waiter, Chef and Cashier share the one-task-per-turn rule instead of each repeating it |
| `control/GameController.java` | Changed | `retryUntilValid()` asks again after a refused choice for the Host, Waiter and Chef. Offers Happy Hour after the game state is shown, applies its recovery, counts it down at the end of the turn, and adds the Host's step before the Waiter |
| `entities/pricing/PricingPolicy.java` | New | A pricing rule applied when an order is taken, after the customer's own discount, and `percentOff()`, the shared rounding for every discount |
| `entities/menu/ComboMeal.java` | New | A combo is a `MenuItem` made of two `MenuItem`s, so orders, the Chef and billing handle it like any dish |
| `config/GameSettings.java` | Changed | The base menu adds the three combos |
| `entities/pricing/HappyHour.java` | New | Happy Hour as a `PricingPolicy`: once per game, two turns, 20% off, 15 recovery |
| `entities/staff/FrontOfHouseStaff.java` | New | Seating moved out of `Waiter` so the Waiter and the Host share it, and the Chef and Cashier do not inherit it |
| `entities/staff/Host.java` | New | Seats one waiting customer per turn |
| `entities/staff/Waiter.java` | Changed | Extends `FrontOfHouseStaff`; applies its `PricingPolicy` after the customer's discount |
| `entities/customer/VIPCustomer.java` | Changed | Uses `PricingPolicy.percentOff()`, so every discount rounds the same way |
| `entities/customer/Customer.java` | Changed | `recover()` for Happy Hour |
| `entities/restaurant/Restaurant.java` | Changed | Holds the restaurant's Happy Hour |
| `control/GameView.java`, `boundary/GameCLI.java` | Changed | Host and Happy Hour prompts and messages. The Host and Chef options are numbered by position; `showSeated()` says who seated the customer |
| `test/runs/` | New | Reproducible Stage 3 victory and defeat scripts |

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
