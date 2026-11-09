package com.comp2042;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayDeque;

/** Enumerates free board cells so spawning always terminates, including on a full board. */
final class BoardCells {
    private BoardCells() { }

    /** Validates the horizontal two-cell start and leaves space for the first coin. */
    static int validateGameSize(int width, int height) {
        if (width < 3 || height < 1) throw new IllegalArgumentException("Game boards need at least 3 columns and 1 row");
        try {
            return Math.multiplyExact(width, height);
        } catch (ArithmeticException overflow) {
            throw new IllegalArgumentException("Board cell count is too large", overflow);
        }
    }

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

    /** Ignores the moving body, but ensures fixed obstacles cannot isolate any playable cell. */
    static boolean allFreeCellsConnected(int width, int height, Collection<Point> obstacles, Point start) {
        Set<Point> blocked = new HashSet<>(obstacles);
        if (start.x < 0 || start.y < 0 || start.x >= width || start.y >= height || blocked.contains(start)) return false;
        Set<Point> visited = new HashSet<>();
        ArrayDeque<Point> queue = new ArrayDeque<>();
        queue.add(new Point(start));
        visited.add(start);
        int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            Point point = queue.remove();
            for (int[] step : steps) {
                Point next = new Point(point.x + step[0], point.y + step[1]);
                if (next.x >= 0 && next.y >= 0 && next.x < width && next.y < height
                        && !blocked.contains(next) && visited.add(next)) queue.add(next);
            }
        }
        return visited.size() == width * height - blocked.size();
    }
}
