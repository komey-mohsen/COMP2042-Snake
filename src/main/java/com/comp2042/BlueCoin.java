package com.comp2042;

public class BlueCoin extends Coin {

    public BlueCoin(int x, int y, Snake snek, Obstacle obstacle) {
        super(x, y, snek, obstacle);
    }

    public int getPoints() {
        int points = 5;
        return points;
    }

    protected String imagePath() {
        return "resources/views/res/bluecoin.png";
    }
}
