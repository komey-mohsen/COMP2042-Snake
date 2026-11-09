package com.comp2042;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.awt.*;
import java.util.ArrayList;

public class Snake {
    private Color headColor = Color.web("2eb872");
    private Color bodyColor = Color.web("a3de83");
    private boolean dead = false;
    private final Point head = new Point();
    private final ArrayList<Point> body = new ArrayList<>();
    private final int gx;
    private final int gy;
    private boolean ignoreBorders = false;
    private boolean noBorder;
    private Obstacle obstacle;
    private ImageView imageHead;
    private ImageView imageBody;
    private ImageView imageTurn;
    private ImageView imageTail;
    private int snakeDir = 270;

    public Snake(int x, int y, boolean noBorder, Obstacle obstacle) {
        this.obstacle = obstacle;
        this.noBorder = noBorder;
        this.head.setLocation(x / 2, y / 2);
        this.body.add(new Point(x / 2 + 1, y / 2));
        this.gx = x;
        this.gy = y;
    }

    public void move(char dir) {
        body.add(new Point(head));
        switch (dir) {
            case 'U':
                snakeDir = 0;
                head.translate(0, -1);
                if (borderControl() && (!ignoreBorders && !noBorder) || obstacle.getObstacles().contains(head)) {
                    head.translate(0, 1);
                    dead = true;
                } else if (borderControl() && (ignoreBorders || noBorder)) {
                    head.setLocation(head.getX(), gy - 1);
                }
                break;

            case 'D':
                snakeDir = 180;
                head.translate(0, 1);
                if (borderControl() && (!ignoreBorders && !noBorder) || obstacle.getObstacles().contains(head)) {
                    head.translate(0, -1);
                    dead = true;
                } else if (borderControl() && (ignoreBorders || noBorder)) {
                    head.setLocation(head.getX(), 0);
                }
                break;

            case 'L':
                snakeDir = 270;
                head.translate(-1, 0);
                if (borderControl() && (!ignoreBorders && !noBorder) || obstacle.getObstacles().contains(head)) {
                    head.translate(1, 0);
                    dead = true;
                } else if (borderControl() && (ignoreBorders || noBorder)) {
                    head.setLocation(gx - 1, head.getY());
                }
                break;

            case 'R':
                snakeDir = 90;
                head.translate(1, 0);
                if (borderControl() && (!ignoreBorders && !noBorder) || obstacle.getObstacles().contains(head)) {
                    head.translate(-1, 0);
                    dead = true;
                } else if (borderControl() && (ignoreBorders || noBorder)) {
                    head.setLocation(0, head.getY());
                }
                break;
        }

        if (body.size() > 4 && hitSelf() && !body.get(0).equals(head)) {
            dead = true;
        }
    }

    public int getBodyDir(int bodyCount) {
        if (bodyCount == 0) {
            return 0;
        } else if (body.get(bodyCount).getY() == body.get(bodyCount - 1).getY()) {
            return 270;
        } else {
            return 0;
        }
    }

    public boolean ateCoin(Coin coin) {
        if (!head.equals(coin.getPos())) {
            body.remove(0);
        }
        return head.equals(coin.getPos());
    }

    private boolean borderControl() {
        return head.getX() < 0 || head.getY() < 0 || head.getX() >= gx || head.getY() >= gy;
    }

    public boolean hitSelf() {
        return body.contains(head);
    }

    public ArrayList<Point> getBody() {
        return new ArrayList<>(body);
    }

    public Point getHead() {
        return new Point(head);
    }

    public Color getHeadColor() {
        return headColor;
    }

    public Color getBodyColor() {
        return bodyColor;
    }

    public boolean getDead() {
        return dead;
    }

    public void ignoreBorders(boolean ign) {
        ignoreBorders = ign;
    }

    public ImageView getImageViewHead() {
        if (imageHead == null) imageHead = new ImageView(new Image("resources/views/res/snakehead.png"));
        return imageHead;
    }

    public ImageView getImageViewBody() {
        if (imageBody == null) imageBody = new ImageView(new Image("resources/views/res/snakebody.png"));
        return imageBody;
    }

    public ImageView getImageViewTurn() {
        if (imageTurn == null) imageTurn = new ImageView(new Image("resources/views/res/snaketurnleft.png"));
        return imageTurn;
    }

    public int getSnakeDir() {
        return snakeDir;
    }

    public ImageView getImageViewTail() {
        if (imageTail == null) imageTail = new ImageView(new Image("resources/views/res/snaketail.png"));
        return imageTail;
    }
}
