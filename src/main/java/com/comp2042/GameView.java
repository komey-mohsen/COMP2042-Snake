package com.comp2042;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

import java.awt.Point;
import java.util.EnumMap;
import java.util.Map;

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
    private final Map<SnakeSprites.Kind, Image> spriteImages = new EnumMap<>(SnakeSprites.Kind.class);

    public GameView(Game game) {
        this.game = game;
        baseLayout = new BoardLayout(game.getWidth(), game.getHeight(), game.getMultiplier());
        boardLayout = baseLayout;
    }

    public void makeScene(Snake snek, Coin coin) {
        gridCanvas = new Canvas(boardLayout.width(), boardLayout.height());
        viewportWidth = boardLayout.width();
        viewportHeight = boardLayout.height();
        gc = gridCanvas.getGraphicsContext2D();
        drawGrid(coin, snek, null);

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
        drawGrid(coin, snek, obstacle);
    }

    public void drawGrid(Coin coin, Snake snek, Obstacle obstacle) {
        gc.setFill(game.getColor());
        gc.fillRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());

        if (coin != null) {
            BoardLayout.Cell cell = boardLayout.bounds(coin.getPos());
            gc.drawImage(coin.getImage(), cell.x(), cell.y(), cell.size() * 0.85, cell.size() * 0.85);
        }

        for (SnakeSprites.Part part : SnakeSprites.layout(snek.getBody(), snek.getHead(),
                snek.getSnakeDir(), game.getWidth(), game.getHeight())) {
            drawPart(part);
        }

        if (obstacle != null) {
            gc.setFill(obstacle.getColor());
            for (Point point : obstacle.getObstacles()) {
                BoardLayout.Cell cell = boardLayout.bounds(point);
                gc.fillRect(cell.x(), cell.y(), cell.size(), cell.size());
            }
        }
    }

    private void drawPart(SnakeSprites.Part part) {
        Image image = spriteImages.computeIfAbsent(part.kind(), kind -> {
            var resource = Main.class.getResource(kind.resourcePath());
            if (resource == null) throw new IllegalStateException("Missing snake sprite: " + kind.resourcePath());
            return new Image(resource.toExternalForm());
        });
        BoardLayout.Cell cell = boardLayout.bounds(new Point(part.x(), part.y()));
        // Rotate the drawing context; no per-segment ImageView or snapshot is allocated.
        gc.save();
        try {
            gc.translate(cell.x() + cell.size() / 2, cell.y() + cell.size() / 2);
            gc.rotate(part.rotation());
            gc.drawImage(image, -cell.size() / 2, -cell.size() / 2, cell.size(), cell.size());
        } finally {
            gc.restore();
        }
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
