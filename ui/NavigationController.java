package ui;

import javafx.scene.Scene;
import javafx.stage.Stage;


public class NavigationController {

    private static Stage     stage;
    private static Scene     authScene;
    private static Scene     appScene;
    private static MainShell shell;

    /** Called once in Main.start(). App opens directly on the Login screen. */
    public static void init(Stage primaryStage) {
        stage = primaryStage;
        stage.setTitle("Instapp");
        stage.setWidth(430);
        stage.setHeight(760);
        stage.setResizable(false);

        // App opens directly on login screen (no separate welcome page)
        authScene = new Scene(AuthScreens.buildLogin(), 430, 760);

        shell    = new MainShell(primaryStage);
        appScene = new Scene(shell.getRoot(), 430, 760);

        stage.setScene(authScene);
        stage.show();
    }

    // ── Auth screens ──────────────────────────────────────────────────────────

    public static void showLogin()  { authScene.setRoot(AuthScreens.buildLogin()); }
    public static void showSignup() { authScene.setRoot(AuthScreens.buildSignup()); }

    // ── App screens ───────────────────────────────────────────────────────────

    /** Called immediately after a successful login — lands on Feed. */
    public static void enterApp() {
        shell.refresh(Tab.FEED);
        stage.setScene(appScene);
    }

    /** Switch to a named tab. */
    public static void showTab(Tab tab) { shell.refresh(tab); }

    /** Show followers or following list. */
    public static void showFollowList(java.util.List<String> users, String title, Tab returnTo) {
        shell.showFollowList(users, title, returnTo);
    }

    /** Open the upload screen. */
    public static void showUpload() { shell.showUpload(); }

    /** Show another user's read-only profile. */
    public static void showOtherProfile(String username, Tab returnTo) {
        shell.showOtherProfile(username, returnTo);
    }

    /** Return from other-user profile to a given tab. */
    public static void backFromProfile(Tab returnTo) { shell.refresh(returnTo); }

    /** Logout — return to login screen. */
    public static void logout() {
        stage.setScene(authScene);
        authScene.setRoot(AuthScreens.buildLogin());
    }

    public enum Tab { FEED, SEARCH, PROFILE }
}
