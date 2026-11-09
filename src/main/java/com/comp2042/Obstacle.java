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
    private final Random random;

    public Obstacle(int x, int y) {
       this(x, y, new Random());
    }

    Obstacle(int x, int y, Random random) {
       this.x = x;
       this.y = y;
       this.random = random;
    }

    /** Places distinct in-bounds obstacles and returns the number actually added. */
    public int newObstacles(Snake snek, Coin coin, int amount) {
        if (amount < 0) throw new IllegalArgumentException("Obstacle amount cannot be negative");
        if (amount == 0 || obstacles.size() >= 50) return 0;
        List<Point> occupied = new ArrayList<>(snek.getBody());
        occupied.add(snek.getHead());
        occupied.add(coin.getPos());
        occupied.addAll(obstacles);
        List<Point> free = BoardCells.freeCells(x, y, occupied);
        Collections.shuffle(free, random);
        int added = 0;
        Point head = snek.getHead();
        for (Point candidate : free) {
            if (added >= amount || obstacles.size() >= 50) break;
            obstacles.add(candidate);
            if (BoardCells.allFreeCellsConnected(x, y, obstacles, head)) added++;
            else obstacles.remove(obstacles.size() - 1);
        }
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
