package ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import static ui.NavigationController.Tab.*;


public class MainShell {

    private final BorderPane root      = new BorderPane();
    private final Stage      stage;

    private final Button btnFeed    = navBtn("🏠");
    private final Button btnSearch  = navBtn("🔍");
    private final Button btnProfile = navBtn("👤");

    private NavigationController.Tab activeTab = FEED;

    MainShell(Stage stage) {
        this.stage = stage;
        root.setBottom(buildNavBar());
        root.setStyle(Styles.ROOT);
    }

    public BorderPane getRoot() { return root; }


    public void refresh(NavigationController.Tab tab) {
        activeTab = tab;
        updateHighlight();
        Node content = switch (tab) {
            case FEED    -> FeedScreen.build();
            case SEARCH  -> SearchScreen.build();
            case PROFILE -> ProfileScreen.buildOwn();
        };
        root.setCenter(content);
    }

    /** Show another user's read-only profile over whatever tab is active. */
    public void showOtherProfile(String username, NavigationController.Tab returnTo) {
        updateHighlight();
        root.setCenter(ProfileScreen.buildOther(username, returnTo));
    }

    /** Show the upload screen (no nav highlight change). */
    public void showUpload() {
        updateHighlight();
        root.setCenter(UploadScreen.build(stage));
    }

    /** Show followers or following list. */
    public void showFollowList(java.util.List<String> users, String title,
                               NavigationController.Tab returnTo) {
        updateHighlight();
        root.setCenter(FollowListScreen.build(users, title, returnTo));
    }

   

    private HBox buildNavBar() {
        btnFeed.setOnAction(e    -> NavigationController.showTab(FEED));
        btnSearch.setOnAction(e  -> NavigationController.showTab(SEARCH));
        btnProfile.setOnAction(e -> NavigationController.showTab(PROFILE));

        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER);
        bar.setStyle(Styles.NAV_BAR);
        bar.setPrefHeight(56);

        for (Button b : new Button[]{btnFeed, btnSearch, btnProfile}) {
            HBox.setHgrow(b, Priority.ALWAYS);
            b.setMaxWidth(Double.MAX_VALUE);
            bar.getChildren().add(b);
        }

        updateHighlight();
        return bar;
    }

    private void updateHighlight() {
        btnFeed.setStyle   (activeTab == FEED    ? Styles.NAV_ACTIVE : Styles.NAV_INACTIVE);
        btnSearch.setStyle (activeTab == SEARCH  ? Styles.NAV_ACTIVE : Styles.NAV_INACTIVE);
        btnProfile.setStyle(activeTab == PROFILE ? Styles.NAV_ACTIVE : Styles.NAV_INACTIVE);
    }

    private static Button navBtn(String icon) {
        Button b = new Button(icon);
        b.setStyle(Styles.NAV_INACTIVE);
        return b;
    }
}
