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
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    @FXML private VBox scoresContent;
    @FXML private ComboBox<String> scoreDifficultyChoice;
    @FXML private CheckBox scorePowerUpsChoice;
    @FXML private TableView<HighScoreEntry> highScoresTable;
    @FXML private Label scoresStatus;

    @FXML
    private void initialize() {
        difficultyChoice.getItems().setAll("Easy", "Normal", "Hard");
        difficultyChoice.setValue("Normal");
        configureHighScores();
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
        scoresContent.setVisible(false);
        scoresContent.setManaged(false);
        mainContent.setVisible(true);
    }

    public void handleQuit() {
        viewManager.getHighScores().flush();
        Platform.exit();
    }

    private void configureHighScores() {
        TableColumn<HighScoreEntry, Number> rank = new TableColumn<>("Rank");
        rank.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(highScoresTable.getItems().indexOf(cell.getValue()) + 1));
        rank.setPrefWidth(60);
        TableColumn<HighScoreEntry, Number> points = new TableColumn<>("Score");
        points.setCellValueFactory(cell -> new ReadOnlyIntegerWrapper(cell.getValue().points()));
        points.setPrefWidth(110);
        TableColumn<HighScoreEntry, String> combo = new TableColumn<>("Best combo");
        combo.setCellValueFactory(cell -> new ReadOnlyStringWrapper("×" + cell.getValue().bestCombo()));
        combo.setPrefWidth(110);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd MMM uuuu HH:mm").withZone(ZoneId.systemDefault());
        TableColumn<HighScoreEntry, String> date = new TableColumn<>("Date");
        date.setCellValueFactory(cell -> new ReadOnlyStringWrapper(format.format(cell.getValue().recordedAt())));
        date.setPrefWidth(290);
        for (TableColumn<?, ?> column : new TableColumn<?, ?>[] {rank, points, combo, date}) column.setSortable(false);
        highScoresTable.getColumns().setAll(java.util.List.of(rank, points, combo, date));
        highScoresTable.setPlaceholder(new Label("No scores yet. Play a round to set a record."));
        scoreDifficultyChoice.getItems().setAll("Easy", "Normal", "Hard");
        scoreDifficultyChoice.setValue("Normal");
    }

    public void handleHighScores() {
        mainContent.setVisible(false);
        settingsContent.setVisible(false);
        settingsContent.setManaged(false);
        scoresContent.setManaged(true);
        scoresContent.setVisible(true);
        scoreDifficultyChoice.setValue(difficultyChoice.getValue());
        scorePowerUpsChoice.setSelected(powerUpsChoice.isSelected());
        viewManager.getHighScores().flush();
        refreshHighScores();
    }

    public void refreshHighScores() {
        if (scoreDifficultyChoice.getValue() == null) return;
        ScoreMode mode = new ScoreMode(scoreDifficultyChoice.getValue().charAt(0), scorePowerUpsChoice.isSelected(), X_VALUE, Y_VALUE);
        var snapshot = viewManager.getHighScores().snapshot(mode);
        highScoresTable.getItems().setAll(snapshot.entries());
        scoresStatus.setText(!snapshot.storageAvailable() || snapshot.pending()
                ? "Local saving is unavailable. Session scores are retained; reopen this page to retry."
                : "Top 10 · Classic 20×20 · Saved only on this computer");
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
        GameController controller = new GameController(snek, coin, game, gameView, obstacle, gameStage, viewManager.getHighScores());
        gameStage.setOnHidden(event -> controller.close());
        gameStage.setOnCloseRequest(event -> {
            controller.close();
            viewManager.getPrimaryStage().show();
        });

        StackPane gamePane = new StackPane();
        BorderPane root = new BorderPane(gamePane);
        gameScene = new Scene(root, game.getWidth() * game.getMultiplier(), game.getHeight() * game.getMultiplier() + 300);

        gameScene.setOnKeyPressed(controller::handle);

        gameView.makeScene(snek, coin);
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
        Label comboHelp = new Label("Coin values are base points. Collect within 5s of active play\n"
                + "to increase ×1 → ×5. A timeout resets the combo; pause freezes it.");
        comboHelp.setWrapText(true);
        VBox helpContent = new VBox(5, controlsHelp, coinHelp, comboHelp);
        helpContent.setStyle("-fx-font-size: 12px;");
        TitledPane help = new TitledPane("Controls & coin rules", helpContent);
        help.setFocusTraversable(false);
        help.setExpanded(true);
        VBox footer = new VBox(6, buttons, gameView.bestScoreLabel, gameView.comboLabel,
                gameView.effectLabel, gameView.scoreSaveLabel, help);
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
