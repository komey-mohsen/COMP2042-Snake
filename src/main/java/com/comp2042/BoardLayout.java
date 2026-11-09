package com.comp2042;

import java.awt.Point;

/** A single cell scale shared by snake sprites, coins, obstacles and board coordinates. */
public record BoardLayout(int columns, int rows, double cellSize) {
    public BoardLayout {
        if (columns <= 0 || rows <= 0 || !Double.isFinite(cellSize) || cellSize <= 0) {
            throw new IllegalArgumentException("Board dimensions and cell size must be positive and finite");
        }
    }

    /** Fits the board into the actual play area, which excludes the controls below it. */
    public BoardLayout fitted(double availableWidth, double availableHeight) {
        if (!Double.isFinite(availableWidth) || !Double.isFinite(availableHeight)
                || availableWidth <= 0 || availableHeight <= 0) {
            throw new IllegalArgumentException("Available play area must be positive and finite");
        }
        return new BoardLayout(columns, rows, Math.min(availableWidth / columns, availableHeight / rows));
    }

    public double width() { return columns * cellSize; }
    public double height() { return rows * cellSize; }

    /** Returns the same cell bounds for all kinds of board object. */
    public Cell bounds(Point position) {
        return new Cell(position.x * cellSize, position.y * cellSize, cellSize);
    }

    public record Cell(double x, double y, double size) { }
}
