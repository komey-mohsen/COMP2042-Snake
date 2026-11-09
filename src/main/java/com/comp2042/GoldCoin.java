package com.comp2042;

public class GoldCoin extends Coin {

    public GoldCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    protected String imagePath() {
        return "resources/views/res/goldcoin.png";
    }

    public int getPoints() {
        int points = 15;
        return points;
    }
}
