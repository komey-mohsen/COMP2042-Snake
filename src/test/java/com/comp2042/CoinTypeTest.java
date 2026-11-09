package com.comp2042;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CoinTypeTest {
    @Test
    void everyExistingCoinTypeIsReachable() {
        EnumSet<CoinType> reached = EnumSet.noneOf(CoinType.class);
        for (int roll = 0; roll < 12; roll++) reached.add(CoinType.fromRoll(roll, true, false));
        assertEquals(EnumSet.allOf(CoinType.class), reached);
        assertEquals(CoinType.GOLD, CoinType.fromRoll(1, true, false));
        assertEquals(CoinType.RAINBOW, CoinType.fromRoll(2, true, false));
    }

    @Test
    void disabledPowerUpsAlwaysProduceNormalCoins() {
        for (int roll = 0; roll < 12; roll++) assertEquals(CoinType.NORMAL, CoinType.fromRoll(roll, false, false));
    }

    @Test
    void silverCannotSpawnWhenObstaclesExist() {
        assertEquals(CoinType.NORMAL, CoinType.fromRoll(4, true, true));
        assertEquals(CoinType.SILVER, CoinType.fromRoll(4, true, false));
    }

    @Test
    void rejectsInvalidRandomOutcomes() {
        assertThrows(IllegalArgumentException.class, () -> CoinType.fromRoll(-1, true, false));
        assertThrows(IllegalArgumentException.class, () -> CoinType.fromRoll(12, true, false));
    }
}
