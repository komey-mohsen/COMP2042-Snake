package com.comp2042;

import java.awt.Point;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpawningTest {
    @Test
    void freeCellEnumerationIncludesFirstAndLastColumns() {
        assertEquals(List.of(new Point(0, 0), new Point(2, 0)),
                BoardCells.freeCells(3, 1, List.of(new Point(1, 0))));
        assertThrows(IllegalArgumentException.class, () -> BoardCells.freeCells(0, 1, List.of()));
    }

    @Test
    void obstaclesAreBoundedDistinctAndAvoidSnakeAndCoin() {
        Obstacle obstacles = new Obstacle(20, 20);
        Snake snake = new Snake(20, 20, false, obstacles);
        Coin coin = new Coin(20, 20, snake, obstacles);
        assertEquals(50, obstacles.newObstacles(snake, coin, 1000));
        assertEquals(0, obstacles.newObstacles(snake, coin, 5));
        var cells = obstacles.getObstacles();
        assertEquals(cells.size(), new HashSet<>(cells).size());
        for (Point point : cells) {
            assertTrue(point.x >= 0 && point.x < 20 && point.y >= 0 && point.y < 20);
            assertNotEquals(snake.getHead(), point);
            assertFalse(snake.getBody().contains(point));
            assertNotEquals(coin.getPos(), point);
        }
    }

    @Test
    void crowdedBoardTerminatesAndCoinUsesOnlyRemainingCell() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Obstacle obstacles = new Obstacle(3, 2);
            Snake snake = new Snake(3, 2, false, obstacles);
            Coin coin = new Coin(3, 2, snake, obstacles) {
                @Override public Point getPos() { return new Point(0, 1); }
            };
            assertEquals(3, obstacles.newObstacles(snake, coin, 1000));
            for (int i = 0; i < 20; i++) assertEquals(coin.getPos(), new Coin(3, 2, snake, obstacles).getPos());
            assertEquals(0, obstacles.newObstacles(snake, coin, 1000));
            var occupied = obstacles.getObstacles();
            occupied.add(snake.getHead());
            occupied.addAll(snake.getBody());
            occupied.add(coin.getPos());
            assertTrue(BoardCells.freeCells(3, 2, occupied).isEmpty());
        });
    }

    @Test
    void obstacleSnapshotsCannotModifyTheBoard() {
        Obstacle obstacles = new Obstacle(3, 2);
        Snake snake = new Snake(3, 2, false, obstacles);
        Coin coin = new Coin(3, 2, snake, obstacles);
        obstacles.newObstacles(snake, coin, 1);
        Point original = new Point(obstacles.getObstacles().get(0));
        var snapshot = obstacles.getObstacles();
        snapshot.get(0).translate(100, 100);
        snapshot.clear();
        assertEquals(List.of(original), obstacles.getObstacles());
        assertThrows(IllegalArgumentException.class, () -> obstacles.newObstacles(snake, coin, -1));
    }

    @Test
    void fullBoardCoinRequestFailsPromptlyInsteadOfLoopingForever() {
        assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Obstacle obstacles = new Obstacle(3, 1);
            Snake snake = new Snake(3, 1, false, obstacles);
            Coin lastCoin = new Coin(3, 1, snake, obstacles);
            assertEquals(new Point(0, 0), lastCoin.getPos());
            assertTrue(snake.move('L', lastCoin.getPos()));
            assertThrows(IllegalStateException.class, () -> new Coin(3, 1, snake, obstacles));
        });
    }

    @Test
    void reproducedEdgePocketIsDetectedAsUnreachable() {
        var pocket = List.of(new Point(19, 16), new Point(18, 17), new Point(19, 18));
        assertFalse(BoardCells.allFreeCellsConnected(20, 20, pocket, new Point(10, 10)));
        assertTrue(BoardCells.allFreeCellsConnected(20, 20, pocket.subList(0, 2), new Point(10, 10)));
    }

    @Test
    void silverObstaclesLeaveEveryFreeCellReachableAcrossSeededLayouts() {
        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            for (int seed = 0; seed < 100; seed++) {
                Obstacle obstacles = new Obstacle(20, 20, new Random(seed));
                Snake snake = new Snake(20, 20, false, obstacles);
                Coin coin = new Coin(20, 20, snake, obstacles) {
                    @Override public Point getPos() { return new Point(0, 0); }
                };
                assertEquals(22, obstacles.newObstacles(snake, coin, 22));
                assertTrue(BoardCells.allFreeCellsConnected(20, 20, obstacles.getObstacles(), snake.getHead()), "Seed " + seed);
            }
        });
    }

    @Test
    void crowdedBoardMayPlaceFewerObstaclesToKeepAnAccessPath() {
        Obstacle obstacles = new Obstacle(3, 2, new Random(7));
        Snake snake = new Snake(3, 2, false, obstacles);
        Coin coin = new Coin(3, 2, snake, obstacles) {
            @Override public Point getPos() { return new Point(0, 0); }
        };
        int added = obstacles.newObstacles(snake, coin, 100);
        assertEquals(2, added);
        assertTrue(BoardCells.allFreeCellsConnected(3, 2, obstacles.getObstacles(), snake.getHead()));
        Game game = new Game(3, 2, true, 'N');
        game.decMaxScore(added);
        game.incScore();
        assertFalse(game.gameWon());
        game.incScore();
        assertTrue(game.gameWon());
    }
}
