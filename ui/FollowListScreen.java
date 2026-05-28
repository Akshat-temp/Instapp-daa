package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import models.AppState;

import java.util.List;

/**
 * Reusable screen that shows a list of users (followers OR following).
 * Clicking any row opens that user's profile — same behaviour as Search.
 */
public class FollowListScreen {

    /**
     * @param usernames list of usernames to display
     * @param title     "Followers" or "Following"
     * @param returnTo  which tab to go back to after closing this screen
     */
    public static BorderPane build(List<String> usernames, String title,
                                   NavigationController.Tab returnTo) {
        BorderPane root = new BorderPane();
        root.setStyle(Styles.ROOT);

        // ── Top bar ───────────────────────────────────────────────────────────
        javafx.scene.control.Button backBtn = new javafx.scene.control.Button("←");
        backBtn.setStyle(Styles.BTN_LINK + "-fx-font-size:18px;");
        backBtn.setOnAction(e -> NavigationController.showTab(returnTo));

        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#262626;");
        HBox.setHgrow(titleLbl, Priority.ALWAYS);

        HBox topBar = new HBox(10, backBtn, titleLbl);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle(Styles.TOP_BAR);
        root.setTop(topBar);

        // ── List ──────────────────────────────────────────────────────────────
        VBox list = new VBox(0);
        list.setStyle(Styles.ROOT);

        if (usernames.isEmpty()) {
            Label empty = new Label("No " + title.toLowerCase() + " yet.");
            empty.setStyle(Styles.CAPTION + "-fx-font-size:14px;");
            VBox.setMargin(empty, new Insets(40, 0, 0, 0));
            list.setAlignment(Pos.TOP_CENTER);
            list.getChildren().add(empty);
        } else {
            for (String username : usernames) {
                list.getChildren().add(buildRow(username, returnTo));
            }
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setCenter(scroll);

        return root;
    }

    // ── Single user row (same style as SearchScreen) ──────────────────────────

    private static HBox buildRow(String username, NavigationController.Tab returnTo) {
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
        row.setStyle(
            "-fx-background-color:white;" +
            "-fx-border-color:" + Styles.C_BORDER + ";" +
            "-fx-border-width:0 0 1 0;" +
            "-fx-cursor:hand;"
        );

        row.setOnMouseEntered(e -> row.setStyle(row.getStyle().replace("white", "#f8f8f8")));
        row.setOnMouseExited (e -> row.setStyle(row.getStyle().replace("#f8f8f8", "white")));

        row.setOnMouseClicked(e -> {
            if (username.equals(AppState.currentUser.getUsername())) {
                NavigationController.showTab(NavigationController.Tab.PROFILE);
            } else {
                // Open their profile; back button returns to PROFILE tab
                NavigationController.showOtherProfile(username, NavigationController.Tab.PROFILE);
            }
        });

        return row;
    }
}
