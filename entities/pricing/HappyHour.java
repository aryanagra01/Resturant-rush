package entities.pricing;

import utility.Require;

/**
 * Happy Hour: the manager can switch it on once per game, at the start of a turn.
 * It lasts for that turn and the next. Orders taken while it is on get 20% off,
 * after any VIP discount.
 */
public class HappyHour implements PricingPolicy {
    public static final int DISCOUNT_PERCENT = 20;
    public static final int DURATION_TURNS = 2;

    private boolean used;
    private int turnsLeft;

    /** True until the manager has switched it on once. */
    public boolean isAvailable() {
        return !used;
    }

    public boolean isActive() {
        return turnsLeft > 0;
    }

    /** Active turns left, counting the current one. */
    public int getTurnsLeft() {
        return turnsLeft;
    }

    /**
     * Switches Happy Hour on for this turn and the next.
     *
     * @throws utility.GameRuleException if it has already been used this game
     */
    public void activate() {
        Require.rule(isAvailable(), "Happy Hour can only be used once per game.");
        used = true;
        turnsLeft = DURATION_TURNS;
    }

    /** Called at the end of every turn. Counts down an active Happy Hour. */
    public void endTurn() {
        if (turnsLeft > 0) {
            turnsLeft--;
        }
    }

    /** 20% off while Happy Hour is on. Otherwise the bill is unchanged. */
    @Override
    public int apply(int billCents) {
        return isActive() ? PricingPolicy.percentOff(billCents, DISCOUNT_PERCENT) : billCents;
    }

    @Override
    public String toString() {
        return "HappyHour[used=" + used + ", turnsLeft=" + turnsLeft + "]";
    }
}
