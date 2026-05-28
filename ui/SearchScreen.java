package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import models.AppState;

import java.util.List;


public class SearchScreen {

    public static BorderPane build() {
        BorderPane root = new BorderPane();
        root.setStyle(Styles.ROOT);

        // ── Top bar with search input ─────────────────────────────────────────
        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-font-size:16px;");

        TextField searchField = new TextField();
        searchField.setPromptText("Search");
        searchField.setStyle(Styles.SEARCH_INPUT);
        searchField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        HBox topBar = new HBox(10, searchIcon, searchField);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle(Styles.TOP_BAR);
        root.setTop(topBar);

        // ── Results list ──────────────────────────────────────────────────────
        VBox resultsList = new VBox(0);
        resultsList.setStyle(Styles.ROOT);

        ScrollPane scroll = new ScrollPane(resultsList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setCenter(scroll);

        // Live search listener
        searchField.textProperty().addListener((obs, old, text) -> {
            resultsList.getChildren().clear();
            String prefix = text.trim();
            if (prefix.isEmpty()) return;

            List<String> results = AppState.trie.searchByPrefix(prefix);
            if (results.isEmpty()) {
                Label none = new Label("No users found.");
                none.setStyle(Styles.CAPTION + "-fx-font-size:13px;");
                VBox.setMargin(none, new Insets(24, 14, 0, 14));
                resultsList.getChildren().add(none);
                return;
            }

            for (String name : results) {
                resultsList.getChildren().add(buildResultRow(name));
            }
        });

        return root;
    }

    // ── Single search result row ──────────────────────────────────────────────

    private static HBox buildResultRow(String username) {
        Label avatar = new Label(username.substring(0, 1).toUpperCase());
        avatar.setStyle(Styles.AVATAR);
        avatar.setMinSize(40, 40);
        avatar.setMaxSize(40, 40);
        avatar.setPrefSize(40, 40);

        Label nameLbl = new Label(username);
        nameLbl.setStyle("-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#262626;");

        Label subLbl = new Label("@" + username);
        subLbl.setStyle(Styles.CAPTION);

        VBox textCol = new VBox(2, nameLbl, subLbl);
        textCol.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textCol, Priority.ALWAYS);

        HBox row = new HBox(12, avatar, textCol);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: white;" +
                "-fx-border-color: " + Styles.C_BORDER + ";" +
                "-fx-border-width: 0 0 1 0;" +
                "-fx-cursor: hand;");

        row.setOnMouseEntered(e -> row.setStyle(row.getStyle()
                .replace("white", "#f8f8f8")));
        row.setOnMouseExited(e  -> row.setStyle(row.getStyle()
                .replace("#f8f8f8", "white")));

        row.setOnMouseClicked(e -> {
            if (username.equals(AppState.currentUser.getUsername())) {
                NavigationController.showTab(NavigationController.Tab.PROFILE);
            } else {
                NavigationController.showOtherProfile(
                        username, NavigationController.Tab.SEARCH);
            }
        });

        return row;
    }
}
