package com.comp2042;

import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.awt.*;

public class GameView {

    public Canvas gridCanvas;
    public GraphicsContext gc;

    public Label gameOverLabel;
    public Label gameWonLabel;
    public Label scoreLabel;
    public Label statusLabel;
    public Label effectLabel;
    private boolean gameOverVis = false;
    private boolean gameWonVis = false;

    private Game game;
    private final BoardLayout baseLayout;
    private BoardLayout boardLayout;
    private double viewportWidth;
    private double viewportHeight;
    private boolean fullsize = false;
    private int[][] turnArray;

    public GameView(Game game) {
        this.game = game;
        baseLayout = new BoardLayout(game.getWidth(), game.getHeight(), game.getMultiplier());
        boardLayout = baseLayout;
        resetTurns();
    }

    public void makeScene(Scene scene, Snake snek, Coin coin) {
        gridCanvas = new Canvas(boardLayout.width(), boardLayout.height());
        viewportWidth = boardLayout.width();
        viewportHeight = boardLayout.height();
        gc = gridCanvas.getGraphicsContext2D();
        drawGrid(coin, snek, null, gc, false, 0);

        gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setId("gameOverLabel");
        String css = "resources/views/styling.css";
        gameOverLabel.getStylesheets().add(css);
        gameOverLabel.setVisible(false);

        gameWonLabel = new Label("YOU WON!");
        gameWonLabel.setId("gameWonLabel");
        gameWonLabel.getStylesheets().add(css);
        gameWonLabel.setVisible(false);

        scoreLabel = new Label("Score: " + game.getScorePoints());
        scoreLabel.setId("scoreLabel");
        scoreLabel.getStylesheets().add(css);
        scoreLabel.setFont(new Font("Arial", 20));
        scoreLabel.setVisible(true);

        statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setStyle("-fx-background-color: rgba(254,255,228,0.94); -fx-text-fill: #243629; "
                + "-fx-font-size: 18px; -fx-padding: 12px; -fx-text-alignment: center;");
        statusLabel.setMouseTransparent(true);
        showSessionState(SessionControls.State.READY);
        effectLabel = new Label(game.isPowerUp() ? "No temporary power-up" : "Power-ups disabled");
        effectLabel.setWrapText(true);
        effectLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #38543d;");
    }

    public void setFullscreen(boolean fullScreen, Obstacle obstacle, Snake snek, Coin coin) {
        fullsize = fullScreen;
        resizeBoard(viewportWidth, viewportHeight, obstacle, snek, coin);
    }

    /** Recalculates from the original layout rather than repeatedly multiplying canvas dimensions. */
    public void resizeBoard(double width, double height, Obstacle obstacle, Snake snek, Coin coin) {
        if (width <= 0 || height <= 0) return;
        viewportWidth = width;
        viewportHeight = height;
        BoardLayout fitted = baseLayout.fitted(width, height);
        boardLayout = fullsize || fitted.cellSize() < baseLayout.cellSize() ? fitted : baseLayout;
        gridCanvas.setWidth(boardLayout.width());
        gridCanvas.setHeight(boardLayout.height());
        double fullScale = boardLayout.cellSize() / baseLayout.cellSize();
        gameOverLabel.setFont(new Font("Arial", 25 * fullScale));
        gameWonLabel.setFont(new Font("Arial", 25 * fullScale));
        drawGrid(coin, snek, obstacle, gc, false, 0);
    }

    public void drawGrid(Coin coin, Snake snek, Obstacle obstacle, GraphicsContext gc, boolean a, int b) {
        gc.setFill(game.getColor());
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        if (coin != null) {
            BoardLayout.Cell cell = boardLayout.bounds(coin.getPos());
            gc.drawImage(coin.getImage(), cell.x(), cell.y(), cell.size() * 0.85, cell.size() * 0.85);
        }

        int bodyIndex = 0;
        for (Point point : snek.getBody()) {
            drawBody(snek, point, bodyIndex);
            bodyIndex++;
        }

        drawHead(snek);

        if (obstacle != null) {
            gc.setFill(obstacle.getColor());
            for (Point point : obstacle.getObstacles()) {
                BoardLayout.Cell cell = boardLayout.bounds(point);
                gc.fillRect(cell.x(), cell.y(), cell.size(), cell.size());
            }
        }
    }

    private void drawHead(Snake snek) {
        snek.getImageViewHead().setRotate(snek.getSnakeDir());
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Image rotatedImage = snek.getImageViewHead().snapshot(params, null);
        drawCellImage(rotatedImage, snek.getHead());
    }

    private void drawBody(Snake snek, Point point, int index) {
        Image rotatedImage;
        if (snek.getBody().size() <= 1) {
            snek.getImageViewTail().setRotate(snek.getSnakeDir());
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = snek.getImageViewTail().snapshot(params, null);
        } else if (index == 0) {
            int rotateTail;
            if (snek.getBody().get(1).getX() == snek.getBody().get(0).getX() && snek.getBody().get(1).getY() == snek.getBody().get(0).getY() - 1) {
                rotateTail = 0;
            } else if (snek.getBody().get(1).getX() == snek.getBody().get(0).getX() && snek.getBody().get(1).getY() == snek.getBody().get(0).getY() + 1) {
                rotateTail = 180;
            } else if (snek.getBody().get(1).getX() == snek.getBody().get(0).getX() - 1 && snek.getBody().get(1).getY() == snek.getBody().get(0).getY()) {
                rotateTail = 270;
            } else {
                rotateTail = 90;
            }
            snek.getImageViewTail().setRotate(rotateTail);
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = snek.getImageViewTail().snapshot(params, null);
        } else if (index > 0 && (turnArray[(int) snek.getBody().get(index).getX()][(int) snek.getBody().get(index).getY()] != 0)) {
            snek.getImageViewTurn().setRotate(90 * turnArray[(int) snek.getBody().get(index).getX()][(int) snek.getBody().get(index).getY()]);
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = snek.getImageViewTurn().snapshot(params, null);
        } else {
            snek.getImageViewBody().setRotate(snek.getBodyDir(index));
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = snek.getImageViewBody().snapshot(params, null);
        }

        if (turnArray[(int) snek.getBody().get(0).getX()][(int) snek.getBody().get(0).getY()] != 0) {
            turnArray[(int) snek.getBody().get(0).getX()][(int) snek.getBody().get(0).getY()] = 0;
        }

        drawCellImage(rotatedImage, point);
    }

    private void drawCellImage(Image image, Point position) {
        BoardLayout.Cell cell = boardLayout.bounds(position);
        gc.drawImage(image, cell.x(), cell.y(), cell.size(), cell.size());
    }

    public void resetTurns() {
        turnArray = new int[game.getWidth()][game.getHeight()];
    }

    public void turnDir(Snake snek, Direction previous, Direction current) {
        if ((previous == Direction.LEFT && current == Direction.UP) || (previous == Direction.DOWN && current == Direction.RIGHT)) {
            turnArray[(int) snek.getHead().getX()][(int) snek.getHead().getY()] = 2;
        } else if ((previous == Direction.LEFT && current == Direction.DOWN) || (previous == Direction.UP && current == Direction.RIGHT)) {
            turnArray[(int) snek.getHead().getX()][(int) snek.getHead().getY()] = 3;
        } else if ((previous == Direction.RIGHT && current == Direction.UP) || (previous == Direction.DOWN && current == Direction.LEFT)) {
            turnArray[(int) snek.getHead().getX()][(int) snek.getHead().getY()] = 1;
        } else if ((previous == Direction.UP && current == Direction.LEFT) || (previous == Direction.RIGHT && current == Direction.DOWN)) {
            turnArray[(int) snek.getHead().getX()][(int) snek.getHead().getY()] = 4;
        } else turnArray[(int) snek.getHead().getX()][(int) snek.getHead().getY()] = 0;
    }

    public void gameOver(Snake snek) {
        if (snek.getDead() && !gameOverVis) {
            gameOverVis = true;
            gameOverLabel.setVisible(true);
        } else if (!snek.getDead() && gameOverVis) {
            gameOverLabel.setVisible(false);
            gameOverVis = false;
        }
    }

    public void gameWon() {
        if (game.gameWon() && !gameWonVis) {
            gameWonVis = true;
            gameWonLabel.setVisible(true);
        } else if (!game.gameWon() && gameWonVis) {
            gameWonVis = false;
            gameWonLabel.setVisible(false);
        }
    }

    public void updateScoreLabel() {
        scoreLabel.setText("Score: " + game.getScorePoints());
    }

    /** Displays the pause/ready state without covering a completed game's result. */
    public void showSessionState(SessionControls.State state) {
        if (statusLabel == null) return;
        statusLabel.setText(state == SessionControls.State.PAUSED
                ? "PAUSED\nPress an arrow key to resume" : "Press an arrow key to start");
        statusLabel.setVisible(state == SessionControls.State.PAUSED || state == SessionControls.State.READY);
    }

    /** Keeps temporary-effect lifetime and persistent obstacles visible during play. */
    public void updateEffects(Coin collected, Obstacle obstacle) {
        String effect;
        if (!game.isPowerUp()) effect = "Power-ups disabled";
        else if (collected instanceof RainbowCoin) effect = "Speed boost until the next coin";
        else if (collected instanceof BlueCoin) effect = "Edge wrapping until the next coin";
        else effect = "No temporary power-up";
        effectLabel.setText(effect + " | Obstacles: " + obstacle.getObstacles().size());
    }

}
