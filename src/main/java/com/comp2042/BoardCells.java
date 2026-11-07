package com.comp2042;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Enumerates free board cells so spawning always terminates, including on a full board. */
final class BoardCells {
    private BoardCells() { }

    static List<Point> freeCells(int width, int height, Collection<Point> occupied) {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("Board dimensions must be positive");
        Set<Point> blocked = new HashSet<>(occupied);
        List<Point> free = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Point point = new Point(x, y);
                if (!blocked.contains(point)) free.add(point);
            }
        }
        return free;
    }
}
