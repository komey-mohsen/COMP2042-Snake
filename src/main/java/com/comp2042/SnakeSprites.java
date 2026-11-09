package com.comp2042;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/** Derives sprite orientation from one body snapshot, including neighbours across wrapped edges. */
public final class SnakeSprites {
    public enum Kind {
        HEAD("snakehead.png"), BODY("snakebody.png"), CORNER("snaketurnleft.png"), TAIL("snaketail.png");
        private final String file;
        Kind(String file) { this.file = file; }
        public String resourcePath() { return "/resources/views/res/" + file; }
    }

    /** Immutable drawing instruction; it does not expose mutable model points. */
    public record Part(Kind kind, int rotation, int x, int y) { }

    private SnakeSprites() { }

    /** Plans one linear pass over the tail-first body plus the head. */
    public static List<Part> layout(List<Point> body, Point head, int heading, int width, int height) {
        List<Integer> links = new ArrayList<>(body.size());
        for (int index = 0; index < body.size(); index++) {
            Point next = index + 1 < body.size() ? body.get(index + 1) : head;
            links.add(direction(body.get(index), next, width, height));
        }
        return layout(body, head, heading, links);
    }

    /** Uses recorded link directions on narrow boards where coordinates alone are ambiguous. */
    public static List<Part> layout(List<Point> body, Point head, int heading, List<Integer> links) {
        if (links.size() != body.size()) throw new IllegalArgumentException("Each body cell needs one direction");
        List<Part> parts = new ArrayList<>(body.size() + 1);
        for (int index = 0; index < body.size(); index++) {
            Point current = body.get(index);
            int towardHead = links.get(index);
            if (index == 0) {
                parts.add(new Part(Kind.TAIL, towardHead, current.x, current.y));
            } else {
                int towardTail = (links.get(index - 1) + 180) % 360;
                if (Math.floorMod(towardHead - towardTail, 360) == 180) {
                    parts.add(new Part(Kind.BODY, towardHead % 180, current.x, current.y));
                } else {
                    parts.add(new Part(Kind.CORNER, cornerRotation(towardHead, towardTail), current.x, current.y));
                }
            }
        }
        parts.add(new Part(Kind.HEAD, heading, head.x, head.y));
        return List.copyOf(parts);
    }

    private static int direction(Point from, Point to, int width, int height) {
        int dx = to.x - from.x;
        int dy = to.y - from.y;
        if (dy == 0) {
            if (dx == 1 || dx == -(width - 1)) return 90;
            if (dx == -1 || dx == width - 1) return 270;
        }
        if (dx == 0) {
            if (dy == -1 || dy == height - 1) return 0;
            if (dy == 1 || dy == -(height - 1)) return 180;
        }
        throw new IllegalArgumentException("Snake segments must occupy adjacent board cells");
    }

    private static int cornerRotation(int first, int second) {
        int low = Math.min(first, second);
        int high = Math.max(first, second);
        if (low == 180 && high == 270) return 0;
        if (low == 0 && high == 270) return 90;
        if (low == 0 && high == 90) return 180;
        if (low == 90 && high == 180) return 270;
        throw new IllegalArgumentException("A corner must join perpendicular cells");
    }
}
