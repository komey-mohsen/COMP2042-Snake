package com.comp2042;

import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Snake {
    private Color headColor = Color.web("2eb872");
    private Color bodyColor = Color.web("a3de83");
    private boolean dead = false;
    private final Point head = new Point();
    private final ArrayList<Point> body = new ArrayList<>();
    private final ArrayList<Integer> bodyDirections = new ArrayList<>();
    private final int gx;
    private final int gy;
    private boolean ignoreBorders = false;
    private boolean noBorder;
    private Obstacle obstacle;
    private int snakeDir = 270;

    public Snake(int x, int y, boolean noBorder, Obstacle obstacle) {
        BoardCells.validateGameSize(x, y);
        this.obstacle = obstacle;
        this.noBorder = noBorder;
        this.head.setLocation(x / 2, y / 2);
        this.body.add(new Point(x / 2 + 1, y / 2));
        this.bodyDirections.add(270);
        this.gx = x;
        this.gy = y;
    }

    /**
     * Advances one tick, checking the final wrapped destination before changing the body.
     * @param dir U, D, L or R
     * @param food position of the current coin, or null when no coin is present
     * @return true only when this move safely collects the coin and grows the snake
     */
    public boolean move(char dir, Point food) {
        if (dead) return false;
        Point destination = new Point(head);
        int rotation = switch (dir) {
            case 'U' -> { destination.translate(0, -1); yield 0; }
            case 'D' -> { destination.translate(0, 1); yield 180; }
            case 'L' -> { destination.translate(-1, 0); yield 270; }
            case 'R' -> { destination.translate(1, 0); yield 90; }
            default -> throw new IllegalArgumentException("Direction must be U, D, L or R");
        };
        if (ignoreBorders || noBorder) {
            destination.setLocation(Math.floorMod(destination.x, gx), Math.floorMod(destination.y, gy));
        } else if (destination.x < 0 || destination.y < 0 || destination.x >= gx || destination.y >= gy) {
            dead = true;
            return false;
        }
        // Wrapping along a one-cell axis does not advance into another cell.
        if (destination.equals(head)) return false;

        boolean grows = destination.equals(food);
        // A tail cell is free only if it moves away on this non-growing tick.
        int firstOccupied = grows ? 0 : 1;
        boolean hitsBody = body.subList(firstOccupied, body.size()).contains(destination);
        if (obstacle.getObstacles().contains(destination) || hitsBody) {
            dead = true;
            return false;
        }
        if (!grows) {
            body.remove(0);
            bodyDirections.remove(0);
        }
        body.add(new Point(head));
        bodyDirections.add(rotation);
        head.setLocation(destination);
        snakeDir = rotation;
        return grows;
    }

    public boolean hitSelf() {
        return body.contains(head);
    }

    public ArrayList<Point> getBody() {
        ArrayList<Point> snapshot = new ArrayList<>(body.size());
        for (Point point : body) snapshot.add(new Point(point));
        return snapshot;
    }

    public Point getHead() {
        return new Point(head);
    }

    /** Keeps actual link direction when two wrapped routes have the same coordinates. */
    public List<Integer> getBodyDirections() {
        return List.copyOf(bodyDirections);
    }

    public Color getHeadColor() {
        return headColor;
    }

    public Color getBodyColor() {
        return bodyColor;
    }

    public boolean getDead() {
        return dead;
    }

    public void ignoreBorders(boolean ign) {
        ignoreBorders = ign;
    }

    public int getSnakeDir() {
        return snakeDir;
    }

}
