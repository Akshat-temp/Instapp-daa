package main;

import models.AppState;
import javafx.application.Application;
import javafx.stage.Stage;
import ui.NavigationController;

/**
 * Instapp — JavaFX entry point.
 *
 * Initialises the database connection, hydrates in-memory state,
 * then hands control to the NavigationController which manages all scenes.
 *
 * ─────────────────────────────────────────────────────────────────────────
 * HOW TO RUN (see README.md for full details)
 *
 *   1. Install JDK 17+
 *   2. Download JavaFX SDK 21  →  https://gluonhq.com/products/javafx/
 *   3. Download PostgreSQL JDBC driver  →  https://jdbc.postgresql.org/
 *   4. Set DATABASE_URL env var to your Railway PostgreSQL URL (optional —
 *      app works in-memory without it for local testing)
 *   5. Compile + run with the provided run.bat / run.sh scripts.
 * ─────────────────────────────────────────────────────────────────────────
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Initialise DB + load in-memory state
        AppState.init();

        // Hand off to navigation controller
        NavigationController.init(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
