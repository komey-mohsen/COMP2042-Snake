package com.comp2042;

public class SilverCoin extends Coin {

    public SilverCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    protected String imagePath() {
        return "resources/views/res/silvercoin.png";
    }

    public int getPoints() {
        int points = 5;
        return points;
    }
}
