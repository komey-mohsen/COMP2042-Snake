package com.comp2042;

import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.Collections;
import java.util.List;

public class Obstacle {
    private Color color = Color.BLACK;
    private ArrayList<Point> obstacles = new ArrayList<>();
    private int x;
    private int y;

    public Obstacle(int x, int y) {
       this.x = x;
       this.y = y;
    }

    /** Places distinct in-bounds obstacles and returns the number actually added. */
    public int newObstacles(Snake snek, Coin coin, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Obstacle amount cannot be negative");
        List<Point> occupied = new ArrayList<>(snek.getBody());
        occupied.add(snek.getHead());
        occupied.add(coin.getPos());
        occupied.addAll(obstacles);
        List<Point> free = BoardCells.freeCells(x, y, occupied);
        Collections.shuffle(free, new Random());
        int added = Math.min(amount, Math.min(free.size(), Math.max(0, 50 - obstacles.size())));
        obstacles.addAll(free.subList(0, added));
        return added;
    }

    public Color getColor() {
        return color;
    }

    public ArrayList<Point> getObstacles() {
        ArrayList<Point> copy = new ArrayList<>();
        for (Point point : obstacles) copy.add(new Point(point));
        return copy;
    }

    public void reset() {
        obstacles.clear();
    }
}
