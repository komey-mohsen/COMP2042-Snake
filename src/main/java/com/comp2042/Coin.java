package com.comp2042;

import javafx.scene.image.Image;

import java.awt.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;

public class Coin {
    private Point pos;
    private Image image;

    public Coin(int x, int y, Snake snek, Obstacle obstacle) {
        List<Point> occupied = new ArrayList<>(snek.getBody());
        occupied.add(snek.getHead());
        occupied.addAll(obstacle.getObstacles());
        List<Point> free = BoardCells.freeCells(x, y, occupied);
        if (free.isEmpty()) throw new IllegalStateException("Cannot spawn a coin on a full board");
        pos = free.get(new Random().nextInt(free.size()));
    }

    public Point getPos() {
        return new Point(pos);
    }

    public int getPoints() {
        int points = 5;
        return points;
    }

    public Image getImage() {
        if (image == null) image = new Image(imagePath());
        return image;
    }

    /** Supplies the sprite only when the view first requests it. */
    protected String imagePath() {
        return "resources/views/res/redcoin.png";
    }
}
