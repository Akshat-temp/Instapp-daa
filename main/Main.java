package main;

import models.AppState;
import javafx.application.Application;
import javafx.stage.Stage;
import ui.NavigationController;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        
        AppState.init();

        
        NavigationController.init(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
