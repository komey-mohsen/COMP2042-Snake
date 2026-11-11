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
        if (image == null) image = transparentSprite(new Image(imagePath()));
        return image;
    }

    /** Removes only the white background connected to the sprite edges, preserving highlights. */
    private static Image transparentSprite(Image source) {
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        var pixels = source.getPixelReader();
        var result = new javafx.scene.image.WritableImage(pixels, width, height);
        boolean[] visited = new boolean[width * height];
        var pending = new java.util.ArrayDeque<Integer>();
        for (int x = 0; x < width; x++) {
            pending.add(x);
            pending.add((height - 1) * width + x);
        }
        for (int y = 0; y < height; y++) {
            pending.add(y * width);
            pending.add(y * width + width - 1);
        }
        while (!pending.isEmpty()) {
            int index = pending.removeFirst();
            if (visited[index]) continue;
            visited[index] = true;
            int x = index % width;
            int y = index / width;
            int argb = pixels.getArgb(x, y);
            boolean background = (argb >>> 24) == 0
                    || (((argb >> 16) & 255) >= 240 && ((argb >> 8) & 255) >= 240 && (argb & 255) >= 240);
            if (!background) continue;
            result.getPixelWriter().setArgb(x, y, 0);
            if (x > 0) pending.add(index - 1);
            if (x + 1 < width) pending.add(index + 1);
            if (y > 0) pending.add(index - width);
            if (y + 1 < height) pending.add(index + width);
        }
        return result;
    }
    /** Supplies the sprite only when the view first requests it. */
    protected String imagePath() {
        return "resources/views/res/redcoin.png";
    }
}
