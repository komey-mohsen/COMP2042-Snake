package com.comp2042;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ComboScoringTest {
    private final AtomicLong time = new AtomicLong();
    private final ComboScoring combo = new ComboScoring(time::get);
    private void advance(long millis) { time.addAndGet(Duration.ofMillis(millis).toNanos()); }

    @Test
    void consecutiveCoinsIncreasePointsUpToTheCap() {
        combo.resume();
        assertEquals(5, combo.collect(5));
        for (int multiplier = 2; multiplier <= 5; multiplier++) {
            advance(1000);
            assertEquals(5 * multiplier, combo.collect(5));
        }
        assertEquals(75, combo.collect(15));
        assertEquals(5, combo.multiplier());
        assertEquals(5, combo.bestMultiplier());
    }

    @Test
    void expiryBoundaryResetsTheChainAndTheCountdown() {
        combo.resume(); combo.collect(5);
        advance(4999);
        assertEquals(10, combo.collect(5));
        advance(5000);
        assertEquals(Duration.ZERO, combo.remaining());
        assertEquals(1, combo.multiplier());
        assertEquals(5, combo.collect(5));
        assertEquals(Duration.ofSeconds(5), combo.remaining());
        assertEquals(2, combo.bestMultiplier());
    }

    @Test
    void pausesFreezeTheWindowAndRepeatedResumeDoesNotResetIt() {
        combo.resume(); combo.collect(5); advance(2000);
        combo.pause(); combo.pause(); advance(60000);
        assertEquals(Duration.ofSeconds(3), combo.remaining());
        combo.resume(); advance(1000); combo.resume(); advance(1000);
        assertEquals(Duration.ofSeconds(1), combo.remaining());
        assertEquals(10, combo.collect(5));
    }

    @Test
    void timeBeforeStartingOrAfterResetDoesNotAffectTheNextRound() {
        advance(100000); combo.resume(); combo.collect(5); combo.collect(5);
        combo.reset(); advance(100000);
        assertEquals(1, combo.multiplier());
        assertEquals(1, combo.bestMultiplier());
        assertEquals(Duration.ZERO, combo.remaining());
        combo.resume(); assertEquals(5, combo.collect(5));
    }

    @Test
    void invalidOrOverflowingAwardsDoNotAdvanceTheCombo() {
        combo.resume();
        assertThrows(IllegalArgumentException.class, () -> combo.collect(0));
        assertThrows(IllegalArgumentException.class, () -> combo.collect(-5));
        assertEquals(5, combo.collect(5));
        assertThrows(ArithmeticException.class, () -> combo.collect(Integer.MAX_VALUE));
        assertEquals(1, combo.multiplier());
        assertEquals(10, combo.collect(5));
    }

    @Test
    void gameAwardsComboPointsWithoutChangingTheWinTarget() {
        Game game = new Game(3, 2, false, 'N', combo);
        combo.resume();
        assertEquals(5, game.awardCoin(5)); game.incScore();
        assertEquals(30, game.awardCoin(15)); game.incScore();
        assertEquals(15, game.awardCoin(5)); game.incScore();
        assertFalse(game.gameWon());
        assertEquals(20, game.awardCoin(5)); game.incScore();
        assertTrue(game.gameWon());
        assertEquals(70, game.getScorePoints());
        game.resetScore();
        assertEquals(0, game.getScorePoints());
        assertEquals(1, game.getCombo().bestMultiplier());
    }
}
