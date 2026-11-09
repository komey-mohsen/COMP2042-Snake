package com.comp2042;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewManager {

    private Stage primaryStage = new Stage();
    private AnchorPane root;

    private Stage gameStage;

    private Image snakeLogo = new Image("resources/views/res/SnakeLogoWhite.png");

    public ViewManager() {
    }

    public void initalizeMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/resources/views/MainMenu.fxml"));
            root = loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load the Snake main menu", exception);
        }

        Scene mainScene = new Scene(root);

        primaryStage.setScene(mainScene);
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(snakeLogo);
        primaryStage.setTitle("Snake Game");
        primaryStage.show();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void initGameStage() {
        gameStage = new Stage();
        gameStage.setTitle("Snake Game");
        gameStage.getIcons().add(snakeLogo);
        gameStage.setResizable(false);
    }

    public Stage getGameStage() {
        return gameStage;
    }
}
