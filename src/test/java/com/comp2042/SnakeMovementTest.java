package com.comp2042;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class SnakeMovementTest {
    private Obstacle obstaclesAt(Point... points) {
        return new Obstacle(6, 6) {
            @Override public ArrayList<Point> getObstacles() {
                return new ArrayList<>(List.of(points));
            }
        };
    }

    @ParameterizedTest
    @CsvSource({"L,0,3,5,3", "R,5,3,0,3", "U,3,0,3,5", "D,3,5,3,0"})
    void wrappedDestinationStillCollidesWithObstacles(char direction, int edgeX, int edgeY, int targetX, int targetY) {
        for (boolean easyMode : new boolean[] {true, false}) {
            Snake snake = new Snake(6, 6, easyMode, obstaclesAt(new Point(targetX, targetY)));
            snake.ignoreBorders(!easyMode); // Also exercise blue-coin wrapping in normal mode.
            while (!snake.getHead().equals(new Point(edgeX, edgeY))) snake.move(direction, null);
            var before = snake.getBody();
            assertFalse(snake.move(direction, new Point(targetX, targetY)));
            assertTrue(snake.getDead());
            assertEquals(new Point(edgeX, edgeY), snake.getHead());
            assertEquals(before, snake.getBody());
        }
    }

    @ParameterizedTest
    @CsvSource({"L,0,3,5,3", "R,5,3,0,3", "U,3,0,3,5", "D,3,5,3,0"})
    void emptyWrappedDestinationsRemainPlayable(char direction, int edgeX, int edgeY, int targetX, int targetY) {
        Snake snake = new Snake(6, 6, true, obstaclesAt());
        while (!snake.getHead().equals(new Point(edgeX, edgeY))) snake.move(direction, null);
        assertTrue(snake.move(direction, new Point(targetX, targetY)));
        assertEquals(new Point(targetX, targetY), snake.getHead());
        assertFalse(snake.getDead());
        assertEquals(2, snake.getBody().size());
    }

    @Test
    void threeCellSnakeCannotEnterItsNeck() {
        Snake snake = new Snake(6, 6, false, obstaclesAt());
        assertTrue(snake.move('L', new Point(2, 3)));
        var head = snake.getHead();
        var body = snake.getBody();
        assertFalse(snake.move('R', null));
        assertTrue(snake.getDead());
        assertEquals(head, snake.getHead());
        assertEquals(body, snake.getBody());
    }

    private Snake loopedSnake() {
        Snake snake = new Snake(6, 6, false, obstaclesAt());
        snake.move('L', new Point(2, 3));
        snake.move('D', new Point(2, 4));
        snake.move('R', null);
        return snake; // Head (3,4), tail (3,3), total length four.
    }

    @Test
    void tailCellCanBeEnteredWhenItMovesAway() {
        Snake snake = loopedSnake();
        assertEquals(new Point(3, 3), snake.getBody().get(0));
        assertFalse(snake.move('U', null));
        assertFalse(snake.getDead());
        assertFalse(snake.hitSelf());
        assertEquals(new Point(3, 3), snake.getHead());
        assertEquals(3, snake.getBody().size());
    }

    @Test
    void tailCellIsStillOccupiedWhenGrowing() {
        Snake snake = loopedSnake();
        assertFalse(snake.move('U', new Point(3, 3)));
        assertTrue(snake.getDead());
        assertEquals(new Point(3, 4), snake.getHead());
    }

    @Test
    void wallCollisionDoesNotChangeTheSnakeAndDeadSnakeCannotMove() {
        Snake snake = new Snake(6, 6, false, obstaclesAt());
        for (int i = 0; i < 3; i++) snake.move('L', null);
        var body = snake.getBody();
        assertFalse(snake.move('L', null));
        assertTrue(snake.getDead());
        assertFalse(snake.move('U', null));
        assertEquals(new Point(0, 3), snake.getHead());
        assertEquals(body, snake.getBody());
    }

    @Test
    void expiringBlueEffectRestoresWallCollision() {
        Snake snake = new Snake(6, 6, false, obstaclesAt());
        snake.ignoreBorders(true);
        for (int i = 0; i < 4; i++) snake.move('L', null);
        assertEquals(new Point(5, 3), snake.getHead());
        snake.ignoreBorders(false);
        snake.move('R', null);
        assertTrue(snake.getDead());
    }

    @Test
    void invalidDirectionIsRejectedWithoutChangingState() {
        Snake snake = new Snake(6, 6, false, obstaclesAt());
        var head = snake.getHead();
        var body = snake.getBody();
        assertThrows(IllegalArgumentException.class, () -> snake.move('X', null));
        assertEquals(head, snake.getHead());
        assertEquals(body, snake.getBody());
        assertFalse(snake.getDead());
    }
}
