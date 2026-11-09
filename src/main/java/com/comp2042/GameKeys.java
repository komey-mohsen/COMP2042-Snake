package com.comp2042;

import javafx.scene.input.KeyCode;

/** Maps keyboard variants to gameplay directions without parsing enum names. */
public final class GameKeys {
    private GameKeys() { }

    public static Direction directionFor(KeyCode key) {
        if (key == null) return null;
        return switch (key) {
            case UP, KP_UP -> Direction.UP;
            case DOWN, KP_DOWN -> Direction.DOWN;
            case LEFT, KP_LEFT -> Direction.LEFT;
            case RIGHT, KP_RIGHT -> Direction.RIGHT;
            default -> null;
        };
    }
}
