package com.comp2042;

/** Separates scores for different difficulties, power-up rules and board sizes. */
public record ScoreMode(char difficulty, boolean powerUps, int width, int height) {
    public ScoreMode {
        BoardCells.validateGameSize(width, height);
        if (difficulty != 'E' && difficulty != 'N' && difficulty != 'H') {
            throw new IllegalArgumentException("Difficulty must be E, N or H");
        }
    }
}
