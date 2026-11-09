package com.comp2042;

import javafx.scene.paint.Color;

public class Game {
    private Color color = Color.web("feffe4");
    private int width;
    private int height;
    private int score;
    private int maxScore;
    private double multiplier;
    private int scorePoints;
    private double speed;
    private boolean powerUp;
    private boolean noBorder;
    private final char difficulty;
    private final ComboScoring combo;

    public Game(int x, int y, boolean powerUp, char difficulty) {
        this(x, y, powerUp, difficulty, new ComboScoring());
    }

    Game(int x, int y, boolean powerUp, char difficulty, ComboScoring combo) {
        int cells = BoardCells.validateGameSize(x, y);
        this.difficulty = difficulty;
        this.combo = java.util.Objects.requireNonNull(combo);
        if (difficulty == 'E') {
            speed = 8.5;
            noBorder = true;
        } else if (difficulty == 'N') {
            speed = 8.5;
            noBorder = false;
        } else if (difficulty == 'H') {
            speed = 13;
            noBorder = false;
        } else {
            throw new IllegalArgumentException("Difficulty must be E, N or H");
        }
        this.powerUp = powerUp;
        width = x;
        height = y;
        maxScore = cells - 2;
        if (height > 50) {
            multiplier = 10;
        } else {
            multiplier = 20;
        }
    }

    public boolean gameWon() {
        return score >= maxScore;
    }

    public void incScore() {
        score++;
    }

    public void decMaxScore(int num) {
        if (num < 0) throw new IllegalArgumentException("Obstacle count cannot be negative");
        this.maxScore = Math.max(0, maxScore - num);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void resetScore() {
        score = 0;
        scorePoints = 0;
        maxScore = width * height - 2;
        combo.reset();
    }

    public Color getColor() {
        return color;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void addPoints(int n) {
        scorePoints += n;
    }

    public int getScorePoints() {
        return scorePoints;
    }

    /** Awards multiplied points without changing the number of occupied board cells. */
    public int awardCoin(int basePoints) {
        int earned = combo.collect(basePoints);
        scorePoints = Math.addExact(scorePoints, earned);
        return earned;
    }

    public ComboScoring getCombo() { return combo; }
    public ScoreMode getScoreMode() { return new ScoreMode(difficulty, powerUp, width, height); }

    public boolean isPowerUp() {
        return powerUp;
    }

    public double getSpeed() {
        return speed;
    }

    public boolean isNoBorder() {
        return noBorder;
    }
}
