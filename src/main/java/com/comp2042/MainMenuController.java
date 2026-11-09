package com.comp2042;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.fxml.FXML;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.util.Random;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainMenuController {

    private static final int X_VALUE = 20;
    private static final int Y_VALUE = 20;
    @FXML private Pane particles;
    @FXML private VBox mainContent;
    @FXML private VBox settingsContent;
    @FXML private ComboBox<String> difficultyChoice;
    @FXML private CheckBox powerUpsChoice;

    @FXML
    private void initialize() {
        difficultyChoice.getItems().setAll("Easy", "Normal", "Hard");
        difficultyChoice.setValue("Normal");
        Random random = new Random(42);
        for (int i = 0; i < 45; i++) {
            Circle speck = new Circle(random.nextDouble() * 760, random.nextDouble() * 480,
                    0.5 + random.nextDouble() * 0.7, Color.web("#ff9b84", 0.08 + random.nextDouble() * 0.12));
            particles.getChildren().add(speck);
        }
    }

    public void handleSettings() {
        mainContent.setVisible(false);
        settingsContent.setManaged(true);
        settingsContent.setVisible(true);
        difficultyChoice.requestFocus();
    }

    public void handleBack() {
        settingsContent.setVisible(false);
        settingsContent.setManaged(false);
        mainContent.setVisible(true);
    }

    public void handleQuit() {
        Platform.exit();
    }

    private Scene gameScene;

    private ViewManager viewManager = Main.getViewManager();

    public void handleStart() {
        viewManager.initGameStage();

        Stage gameStage = viewManager.getGameStage();
        launchSnake(gameStage);
        gameStage.setScene(gameScene);
        viewManager.getPrimaryStage().close();
        gameStage.show();
    }

    private void launchSnake(Stage gameStage) {
        char difficulty = difficultyChoice.getValue().charAt(0);
        Game game = new Game(X_VALUE, Y_VALUE, powerUpsChoice.isSelected(), difficulty);
        Obstacle obstacle = new Obstacle(game.getWidth(), game.getHeight());
        Snake snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        Coin coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);

        GameView gameView = new GameView(game);
        GameController controller = new GameController(snek, coin, game, gameView, obstacle, gameStage);
        gameStage.setOnHidden(event -> controller.close());
        gameStage.setOnCloseRequest(event -> {
            controller.close();
            viewManager.getPrimaryStage().show();
        });

        StackPane gamePane = new StackPane();
        BorderPane root = new BorderPane(gamePane);
        gameScene = new Scene(root, game.getWidth() * game.getMultiplier(), game.getHeight() * game.getMultiplier() + 240);

        gameScene.setOnKeyPressed(controller::handle);

        gameView.makeScene(gameScene, snek, coin);
        gamePane.setMinSize(0, 0);
        gamePane.widthProperty().addListener((observable, before, after) ->
                controller.resizeBoard(gamePane.getWidth(), gamePane.getHeight()));
        gamePane.heightProperty().addListener((observable, before, after) ->
                controller.resizeBoard(gamePane.getWidth(), gamePane.getHeight()));
        gameStage.fullScreenProperty().addListener((observable, before, after) -> controller.fullscreenChanged(after));
        gamePane.getChildren().addAll(gameView.gridCanvas, gameView.gameOverLabel, gameView.gameWonLabel, gameView.statusLabel);
        gamePane.setAlignment(gameView.gameOverLabel, Pos.TOP_CENTER);
        gamePane.setAlignment(gameView.gameWonLabel, Pos.TOP_CENTER);
        Button restartButton = new Button("Restart");
        restartButton.setFocusTraversable(false);
        restartButton.setTooltip(new Tooltip("Restart (R or Tab). Press an arrow key to begin again."));
        restartButton.setOnAction(event -> controller.restart());
        Button pauseButton = new Button("Pause (P)");
        pauseButton.setFocusTraversable(false);
        pauseButton.setOnAction(event -> controller.pause());
        HBox buttons = new HBox(12, gameView.scoreLabel, restartButton, pauseButton);
        buttons.setAlignment(Pos.CENTER);
        Label controlsHelp = new Label("Arrows: move / resume | P: pause | R / Tab: restart\n"
                + "Esc: menu | F11 or + / -: fullscreen");
        controlsHelp.setWrapText(true);
        Label coinHelp = new Label(game.isPowerUp()
                ? "Red: +5 | Gold: +15 | Other coins: +5\n"
                    + "Rainbow: faster movement | Blue: edge wrapping\n"
                    + "Rainbow and blue last until the next coin.\n"
                    + "Silver: adds obstacles that remain until restart."
                : "Power-ups are disabled. Red coins give +5 points.");
        coinHelp.setWrapText(true);
        VBox helpContent = new VBox(5, controlsHelp, coinHelp);
        helpContent.setStyle("-fx-font-size: 12px;");
        TitledPane help = new TitledPane("Controls & coin rules", helpContent);
        help.setFocusTraversable(false);
        help.setExpanded(true);
        VBox footer = new VBox(6, buttons, gameView.effectLabel, help);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(8));
        root.setBottom(footer);
    }

    public ViewManager getViewManager() {
        return viewManager;
    }

    public static int getxValue() {
        return X_VALUE;
    }

    public static int getyValue() {
        return Y_VALUE;
    }
}
