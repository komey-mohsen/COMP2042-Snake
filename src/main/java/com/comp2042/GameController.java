package com.comp2042;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Random;

public class GameController {
    private Snake snek;
    private Coin coin;
    private GameView gameView;
    private Game game;
    private Stage stage;
    private String coinSoundPath = "resources/views/res/coinSound.wav";
    private Obstacle obstacle;

    private final SessionControls controls;

    private int legacyScoreMultiplier = 2;

    private void calculateBonusScore() {
        int bonus = game.getScorePoints() * legacyScoreMultiplier;
        System.out.println("Bonus score: " + bonus);
    }

    public GameController(Snake snek, Coin coin, Game game, GameView gameView, Obstacle obstacle, Stage stage) {
        this.snek = snek;
        this.coin = coin;
        this.gameView = gameView;
        this.game = game;
        this.stage = stage;

        this.obstacle = obstacle;
        controls = new SessionControls(animation::play, animation::stop);
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRate(game.getSpeed());
    }

    private final Timeline animation = new Timeline(new KeyFrame(Duration.seconds(1), event -> tick()));

    private void tick() {
        if (controls.getState() != SessionControls.State.RUNNING) return;
        if (snek.getDead() || game.gameWon()) {
            controls.finish();
            return;
        } else {

            boolean collected = snek.move(controls.getDirection().name().charAt(0), coin.getPos());
            if (collected) {
                powerCoin();
                game.incScore();
                if (game.gameWon()) {
                    controls.finish();
                    coin = null;
                    gameView.gameWon();
                    gameView.updateScoreLabel();
                    gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
                    return;
                }
                newCoin();
            }

            controls.tickCompleted();
            if (snek.getDead()) controls.finish();

            if (!snek.getDead() || snek.hitSelf()) {
                gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
            }

            gameView.gameWon();
            gameView.gameOver(snek);
            gameView.updateScoreLabel();
        }
    }

    public void handle(KeyEvent event) {
        if (controls.getState() == SessionControls.State.CLOSED) return;
        boolean finished = snek.getDead() || game.gameWon();
        if (!finished && event.getCode().isArrowKey()) {
            if (controls.requestDirection(Direction.valueOf(event.getCode().name()))) {
                gameView.turnDir(snek, controls.getPreviousDirection(), controls.getDirection());
            }
        }

        if (event.getCode().equals(KeyCode.P)) {
            pause();
        }

        if (event.getCode().equals(KeyCode.TAB) || event.getCode().equals(KeyCode.R)) {
            restart();
            event.consume();
        }

        if (event.getCode().equals(KeyCode.PLUS)) {
            gameView.scaleToFullscreen(true, stage, obstacle, snek, coin);
            gameView.gameOverLabel.setStyle("-fx-font-size: 50;");
            gameView.gameWonLabel.setStyle("-fx-font-size: 50;");
            gameView.scoreLabel.setStyle("-fx-font-size: 35;");
        }

        if (event.getCode().equals(KeyCode.MINUS)) {
            gameView.scaleToFullscreen(false, stage, obstacle, snek, coin);
            gameView.gameOverLabel.setStyle("-fx-font-size: 15;");
            gameView.gameWonLabel.setStyle("-fx-font-size: 15;");
            gameView.scoreLabel.setStyle("-fx-font-size: 15;");
        }

        if (event.getCode().equals(KeyCode.ESCAPE)) {
            close();
            Main.getViewManager().getPrimaryStage().show();
            stage.close();
        }
    }

    public void restart() {
        if (controls.getState() == SessionControls.State.CLOSED) return;
        controls.reset();
        animation.jumpTo(Duration.ZERO);

        obstacle.reset();
        game.resetScore();
        snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);

        animation.setRate(game.getSpeed());
        gameView.resetTurns();

        gameView.gameWon();
        gameView.gameOver(snek);
        gameView.updateScoreLabel();

        gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
    }

    /** Pauses only this window's round. */
    public void pause() { controls.pause(); }

    /** Stops a session when its window is closed or hidden. */
    public void close() { controls.close(); }

    private void powerCoin() {
        animation.setRate(game.getSpeed());
        snek.ignoreBorders(false);
        if (coin.getClass().equals(RainbowCoin.class)) {
            RainbowCoin tempCoin = (RainbowCoin) coin;
            animation.setRate(game.getSpeed() + tempCoin.getSpeedBonus());
        } else if (coin.getClass().equals(SilverCoin.class)){
            Random rnd = new Random();
            int added = obstacle.newObstacles(snek, coin, rnd.nextInt(game.getWidth() * game.getHeight()/ 20) +3);
            game.decMaxScore(added);
        } else if (coin.getClass().equals(BlueCoin.class)) {
            snek.ignoreBorders(true);
        }
        game.addPoints(coin.getPoints());
    }

    private void newCoin() {
        CoinType type = CoinType.fromRoll(new Random().nextInt(12), game.isPowerUp(), !obstacle.getObstacles().isEmpty());
        coin = switch (type) {
            case GOLD -> new GoldCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            case RAINBOW -> new RainbowCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            case BLUE -> new BlueCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            case SILVER -> new SilverCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            case NORMAL -> new Coin(game.getWidth(), game.getHeight(), snek, obstacle);
        };
    }
}
