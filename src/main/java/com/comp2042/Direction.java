package com.comp2042;

/** Cardinal movement directions and the rule preventing an immediate reversal. */
public enum Direction {
    UP, DOWN, LEFT, RIGHT;

    /** Returns whether the requested direction keeps moving or turns through 90 degrees. */
    public boolean canTurnTo(Direction requested) {
        if (requested == null) return false;
        return switch (this) {
            case UP -> requested != DOWN;
            case DOWN -> requested != UP;
            case LEFT -> requested != RIGHT;
            case RIGHT -> requested != LEFT;
        };
    }
}
