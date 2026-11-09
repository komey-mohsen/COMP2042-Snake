package com.comp2042;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/** Headless checks of existing configuration, scoring and restart behaviour. */
class GameTest {
    @ParameterizedTest
    @CsvSource({"E,8.5,true", "N,8.5,false", "H,13.0,false"})
    void difficultyControlsSpeedAndBorders(char difficulty, double speed, boolean wraps) {
        Game game = new Game(20, 20, true, difficulty);
        assertEquals(speed, game.getSpeed());
        assertEquals(wraps, game.isNoBorder());
    }

    @Test
    void fillsBoardOnlyAfterAllAvailableCellsHaveBeenCollected() {
        Game game = new Game(3, 2, false, 'N');
        for (int i = 0; i < 3; i++) {
            game.incScore();
            assertFalse(game.gameWon());
        }
        game.incScore();
        assertTrue(game.gameWon());
    }

    @Test
    void restartRestoresTargetAndClearsBothScores() {
        Game game = new Game(3, 2, false, 'N');
        game.decMaxScore(3);
        game.incScore();
        game.addPoints(15);
        game.resetScore();
        assertEquals(0, game.getScorePoints());
        assertFalse(game.gameWon());
        for (int i = 0; i < 3; i++) {
            game.incScore();
            assertFalse(game.gameWon());
        }
        game.incScore();
        assertTrue(game.gameWon());
    }

    @Test
    void pointsAndCollectedCellCountAreIndependent() {
        Game game = new Game(3, 2, false, 'N');
        game.addPoints(15);
        game.addPoints(5);
        assertEquals(20, game.getScorePoints());
        assertFalse(game.gameWon());
        assertFalse(game.isPowerUp());
    }

    @Test
    void obstacleTargetSubtractsExactlyTheNumberActuallyPlaced() {
        Game game = new Game(3, 2, true, 'N');
        game.decMaxScore(2);
        game.incScore();
        assertFalse(game.gameWon());
        game.incScore();
        assertTrue(game.gameWon());
        assertThrows(IllegalArgumentException.class, () -> game.decMaxScore(-1));
    }

    @Test
    void boardFilledByNewObstaclesIsAlreadyWon() {
        Game game = new Game(3, 2, true, 'N');
        game.incScore();
        game.decMaxScore(4);
        assertTrue(game.gameWon());
    }
}
