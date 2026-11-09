package com.comp2042;

/** Testable mapping of the twelve random outcomes to existing coin types. */
public enum CoinType {
    NORMAL, GOLD, RAINBOW, BLUE, SILVER;

    /** Selects a coin, suppressing silver when obstacles already exist. */
    public static CoinType fromRoll(int roll, boolean powerUps, boolean hasObstacles) {
        if (roll < 0 || roll >= 12) throw new IllegalArgumentException("Coin roll must be between 0 and 11");
        if (!powerUps) return NORMAL;
        return switch (roll) {
            case 1 -> GOLD;
            case 2 -> RAINBOW;
            case 3 -> BLUE;
            case 4 -> hasObstacles ? NORMAL : SILVER;
            default -> NORMAL;
        };
    }
}
