package com.comp2042;

public class RainbowCoin extends Coin {

    public RainbowCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    public int getPoints() {
        int points = 5;
        return points;
    }

    public double getSpeedBonus() {
        double speedBonus = 5;
        return speedBonus;
    }

    protected String imagePath() {
        return "resources/views/res/rainbowcoin.png";
    }
}
