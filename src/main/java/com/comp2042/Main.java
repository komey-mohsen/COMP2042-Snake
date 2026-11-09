package com.comp2042;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private static ViewManager viewManager;
    private final HighScoreStore highScores;

    public Main() { this(HighScoreStore.local()); }

    Main(HighScoreStore highScores) { this.highScores = highScores; }

    @Override
    public void start(Stage primaryStage) {
        viewManager = new ViewManager(highScores);
        viewManager.initalizeMainMenu();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static ViewManager getViewManager() {
        return viewManager;
    }
}
