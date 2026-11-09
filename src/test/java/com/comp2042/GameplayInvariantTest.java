package com.comp2042;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GameplayInvariantTest {
    @Test
    void wrappingOnASingleRowNeverOverlapsTheHeadAndBody() {
        Snake snake = new Snake(6, 1, true, new Obstacle(6, 1));
        Point before = snake.getHead();
        var body = snake.getBody();
        assertFalse(snake.move('U', new Point(0, 0)));
        assertFalse(snake.hitSelf());
        assertEquals(before, snake.getHead());
        assertEquals(body, snake.getBody());
    }

    @Test
    void seededMovementKeepsLiveSnakesValidAndRenderable() {
        for (int[] size : new int[][] {{3, 1}, {3, 2}, {3, 3}, {4, 2}, {5, 5}, {20, 20}}) {
            for (boolean wraps : new boolean[] {false, true}) {
                for (int seed = 0; seed < 50; seed++) {
                    Random random = new Random(seed);
                    Snake snake = new Snake(size[0], size[1], wraps, new Obstacle(size[0], size[1]));
                    Direction direction = Direction.LEFT;
                    for (int step = 0; step < 300 && !snake.getDead(); step++) {
                        Direction requested = Direction.values()[random.nextInt(4)];
                        if (direction.canTurnTo(requested)) direction = requested;
                        List<Point> occupied = new ArrayList<>(snake.getBody()); occupied.add(snake.getHead());
                        var free = BoardCells.freeCells(size[0], size[1], occupied);
                        if (free.isEmpty()) break;
                        Point food = free.get(random.nextInt(free.size()));
                        int length = occupied.size();
                        boolean grew = snake.move(direction.name().charAt(0), food);
                        if (!snake.getDead()) {
                            var cells = snake.getBody(); cells.add(snake.getHead());
                            String scenario = size[0] + "x" + size[1] + " wrap=" + wraps + " seed=" + seed + " step=" + step;
                            assertEquals(length + (grew ? 1 : 0), cells.size(), scenario);
                            assertEquals(cells.size(), new HashSet<>(cells).size(), scenario);
                            for (Point point : cells) assertTrue(point.x >= 0 && point.y >= 0 && point.x < size[0] && point.y < size[1], scenario);
                            assertEquals(cells.size(), SnakeSprites.layout(snake.getBody(), snake.getHead(),
                                    snake.getSnakeDir(), snake.getBodyDirections()).size(), scenario);
                        }
                    }
                }
            }
        }
    }
}
