package ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import models.AppState;
import models.Post;
import models.User;
import storage.Database;

import java.io.File;
import java.util.List;

/**
 * Profile screen — two modes:
 *   buildOwn()   — current user's own profile (shows Upload button)
 *   buildOther() — read-only view of another user (shows Follow / Unfollow)
 *
 * Displays: avatar, username, post / follower / following counts,
 * and a vertical list of the user's posts with thumbnail images.
 */
public class ProfileScreen {

    // ── Own profile ───────────────────────────────────────────────────────────

    public static BorderPane buildOwn() {
        return buildProfile(AppState.currentUser, null);
    }

    // ── Other user's profile ──────────────────────────────────────────────────

    public static BorderPane buildOther(String username, NavigationController.Tab returnTo) {
        // Direct lookup first, then case-insensitive fallback
        User target = AppState.userStore.get(username);
        if (target == null) {
            for (java.util.Map.Entry<String, User> e : AppState.userStore.entrySet()) {
                if (e.getKey().equalsIgnoreCase(username)) { target = e.getValue(); break; }
            }
        }
        if (target == null) {
            BorderPane err = new BorderPane();
            err.setCenter(new Label("User not found."));
            return err;
        }
        return buildProfile(target, returnTo);
    }

    // ── Shared builder ────────────────────────────────────────────────────────

    private static BorderPane buildProfile(User target, NavigationController.Tab returnTo) {
        boolean isOwn = target.getUsername().equals(AppState.currentUser.getUsername());

        BorderPane root = new BorderPane();
        root.setStyle(Styles.ROOT);

        // ── Top bar — BorderPane so logout is always pinned top-right ────────
        BorderPane topBar = new BorderPane();
        topBar.setStyle(Styles.TOP_BAR);

        Label usernameLbl = new Label(target.getUsername());
        usernameLbl.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#262626;");
        topBar.setLeft(usernameLbl);

        if (!isOwn) {
            Button backBtn = new Button("←");
            backBtn.setStyle(Styles.BTN_LINK + "-fx-font-size:18px;");
            backBtn.setOnAction(e -> NavigationController.backFromProfile(returnTo));
            topBar.setLeft(backBtn);
        }

        if (isOwn) {
            Button logoutBtn = new Button("Logout");
            logoutBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#ed4956;-fx-font-size:15px;-fx-font-weight:bold;-fx-cursor:hand;-fx-padding:0;");
            logoutBtn.setOnAction(e -> {
                AppState.currentUser = null;
                NavigationController.logout();
            });
            BorderPane.setAlignment(logoutBtn, Pos.CENTER_RIGHT);
            topBar.setRight(logoutBtn);
        }

        root.setTop(topBar);

        // ── Scrollable body ───────────────────────────────────────────────────
        VBox body = new VBox(0);
        body.setStyle(Styles.ROOT);

        // Avatar + stats row
        body.getChildren().add(buildHeaderRow(target, isOwn));

        // Separator
        Region sep = new Region();
        sep.setStyle("-fx-background-color: " + Styles.C_BORDER + ";");
        sep.setPrefHeight(1);
        body.getChildren().add(sep);

        // Posts grid / list
        body.getChildren().add(buildPostsList(target));

        ScrollPane scroll = new ScrollPane(body);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setCenter(scroll);

        return root;
    }

    // ── Profile header row (avatar, stats, follow/upload button) ─────────────

    private static VBox buildHeaderRow(User target, boolean isOwn) {
        // Large avatar circle
        Label avatar = new Label(target.getUsername().substring(0, 1).toUpperCase());
        avatar.setStyle(Styles.AVATAR_LARGE);
        avatar.setMinSize(80, 80);
        avatar.setMaxSize(80, 80);
        avatar.setPrefSize(80, 80);

        // Wrap in StackPane for camera button overlay
        javafx.scene.layout.StackPane avatarStack = new javafx.scene.layout.StackPane(avatar);
        avatarStack.setMinSize(80, 80);
        avatarStack.setMaxSize(80, 80);
        avatarStack.setPrefSize(80, 80);

        // Load profile picture if one exists — replaces the initials label
        DpLoader.load(target, avatarStack, 80);

        // Camera button — only on own profile
        if (isOwn) {
            javafx.scene.control.Button camBtn = new javafx.scene.control.Button("📷");
            camBtn.setStyle(
                "-fx-background-color:white;" +
                "-fx-border-color:#dbdbdb;" +
                "-fx-border-radius:50;" +
                "-fx-background-radius:50;" +
                "-fx-font-size:11px;" +
                "-fx-cursor:hand;" +
                "-fx-padding:3 4 3 4;"
            );
            camBtn.setOnAction(e -> {
                javafx.stage.FileChooser chooser = new javafx.stage.FileChooser();
                chooser.setTitle("Choose Profile Picture");
                chooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter(
                        "Images", "*.jpg","*.jpeg","*.png","*.gif","*.bmp"));
                java.io.File chosen = chooser.showOpenDialog(avatar.getScene().getWindow());
                if (chosen != null) {
                    try {
                        byte[] bytes = java.nio.file.Files.readAllBytes(chosen.toPath());
                        String b64   = java.util.Base64.getEncoder().encodeToString(bytes);
                        target.setDpPath(chosen.getAbsolutePath());
                        target.setDpData(b64);
                        storage.Database.saveUserDp(target.getUsername(), b64);
                    } catch (Exception ex) {
                        System.err.println("[DP] " + ex.getMessage());
                    }
                    NavigationController.showTab(NavigationController.Tab.PROFILE);
                }
            });
            javafx.scene.layout.StackPane.setAlignment(camBtn, javafx.geometry.Pos.BOTTOM_RIGHT);
            avatarStack.getChildren().add(camBtn);
        }

        // Stats — followers/following are clickable on own profile
        VBox postsBox     = statBox(String.valueOf(target.getPostCount()),      "Posts");
        VBox followersBox = statBox(String.valueOf(target.getFollowerCount()),  "Followers");
        VBox followingBox = statBox(String.valueOf(target.getFollowingCount()), "Following");

        if (isOwn) {
            followersBox.setStyle("-fx-cursor:hand;");
            followersBox.setOnMouseClicked(e -> NavigationController.showFollowList(
                    new java.util.ArrayList<>(target.getFollowers()), "Followers",
                    NavigationController.Tab.PROFILE));

            followingBox.setStyle("-fx-cursor:hand;");
            followingBox.setOnMouseClicked(e -> NavigationController.showFollowList(
                    new java.util.ArrayList<>(target.getFollowing()), "Following",
                    NavigationController.Tab.PROFILE));
        }

        HBox stats = new HBox(0, postsBox, followersBox, followingBox);
        stats.setAlignment(Pos.CENTER);
        HBox.setHgrow(stats, Priority.ALWAYS);

        HBox topRow = new HBox(20, avatarStack, stats);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.setPadding(new Insets(16, 16, 12, 16));

        // Action button row
        HBox actionRow = new HBox(10);
        actionRow.setPadding(new Insets(0, 16, 14, 16));

        if (isOwn) {
            Button uploadBtn = new Button("+ New Post");
            uploadBtn.setStyle(Styles.BTN_SECONDARY);
            uploadBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(uploadBtn, Priority.ALWAYS);
            uploadBtn.setOnAction(e -> NavigationController.showTab(
                    // We navigate by swapping center content through the shell.
                    // Upload is an additional screen — handled via UploadScreen tab trick.
                    NavigationController.Tab.PROFILE)); // placeholder; see below

            // Re-wire: actually open upload screen
            uploadBtn.setOnAction(e -> openUploadInShell());

            actionRow.getChildren().add(uploadBtn);
        } else {
            // Follow / Unfollow toggle
            boolean following = AppState.currentUser.getFollowing()
                    .contains(target.getUsername());
            Button followBtn = new Button(following ? "Unfollow" : "Follow");
            followBtn.setStyle(following ? Styles.BTN_UNFOLLOW : Styles.BTN_FOLLOW);
            followBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(followBtn, Priority.ALWAYS);

            followBtn.setOnAction(e -> {
                String from = AppState.currentUser.getUsername();
                String to   = target.getUsername();
                if (AppState.currentUser.getFollowing().contains(to)) {
                    // Unfollow
                    AppState.graph.unfollow(from, to);
                    AppState.currentUser.removeFollowing(to);
                    target.removeFollower(from);
                    Database.deleteFollow(from, to);
                    followBtn.setText("Follow");
                    followBtn.setStyle(Styles.BTN_FOLLOW);
                } else {
                    // Follow
                    AppState.graph.follow(from, to);
                    AppState.currentUser.addFollowing(to);
                    target.addFollower(from);
                    Database.saveFollow(from, to);
                    followBtn.setText("Unfollow");
                    followBtn.setStyle(Styles.BTN_UNFOLLOW);
                }
                // Refresh follower count label live
                followersBox.getChildren().setAll(
                        makeStatNum(String.valueOf(target.getFollowerCount())),
                        makeStatLbl("Followers"));
            });

            actionRow.getChildren().add(followBtn);
        }

        VBox header = new VBox(0, topRow, actionRow);
        header.setStyle("-fx-background-color: white;");
        return header;
    }

    // ── Posts grid (Instagram style — 3 per row) ─────────────────────────────

    private static VBox buildPostsList(User target) {
        VBox container = new VBox(0);
        container.setStyle(Styles.ROOT_BG);

        List<String> ids = target.getPostIds();
        if (ids.isEmpty()) {
            Label empty = new Label("No posts yet.");
            empty.setStyle(Styles.CAPTION + "-fx-font-size:14px;");
            VBox.setMargin(empty, new Insets(40, 0, 0, 0));
            container.setAlignment(Pos.TOP_CENTER);
            container.getChildren().add(empty);
            return container;
        }

        // 3 columns, each ~140px wide with 1px gaps
        double cellSize = 140;
        HBox currentRow = null;

        for (int i = ids.size() - 1; i >= 0; i--) {
            Post p = AppState.postStore.get(ids.get(i));
            if (p == null) continue;

            int col = (ids.size() - 1 - i) % 3;
            if (col == 0) {
                currentRow = new HBox(1);
                currentRow.setStyle("-fx-background-color:#dbdbdb;");
                container.getChildren().add(currentRow);
            }

            StackPane cell = buildGridCell(p, cellSize);
            currentRow.getChildren().add(cell);
        }

        return container;
    }

    private static StackPane buildGridCell(Post post, double size) {
        StackPane cell = new StackPane();
        cell.setPrefSize(size, size);
        cell.setMinSize(size, size);
        cell.setMaxSize(size, size);
        cell.setStyle("-fx-background-color:#efefef;-fx-cursor:hand;");

        Label placeholder = new Label("📷");
        placeholder.setStyle("-fx-font-size:22px;");
        cell.getChildren().add(placeholder);

        ImageView iv = new ImageView();
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setPreserveRatio(false);
        Rectangle clip = new Rectangle(size, size);
        iv.setClip(clip);

        ImageLoader.load(post, iv, cell, size, size);

        // Click to enlarge in a popup dialog
        cell.setOnMouseClicked(e -> showEnlargedPost(post));

        return cell;
    }

    private static void showEnlargedPost(Post post) {
        javafx.stage.Stage dialog = new javafx.stage.Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.setTitle("Post");
        dialog.setWidth(430);
        dialog.setResizable(false);

        VBox box = new VBox(0);
        box.setStyle("-fx-background-color:white;");

        // Author row
        Label avatar = new Label(post.getAuthorUsername().substring(0, 1).toUpperCase());
        avatar.setStyle(Styles.AVATAR);
        avatar.setMinSize(34, 34); avatar.setMaxSize(34, 34); avatar.setPrefSize(34, 34);
        Label authorLbl = new Label(post.getAuthorUsername());
        authorLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#262626;");
        HBox authorRow = new HBox(10, avatar, authorLbl);
        authorRow.setAlignment(Pos.CENTER_LEFT);
        authorRow.setPadding(new Insets(10, 14, 10, 14));

        // Full image
        StackPane imgPane = new StackPane();
        imgPane.setPrefHeight(380);
        imgPane.setStyle("-fx-background-color:#efefef;");
        Label loading = new Label("📷"); loading.setStyle("-fx-font-size:32px;");
        imgPane.getChildren().add(loading);

        ImageView iv = new ImageView();
        iv.setFitWidth(430); iv.setFitHeight(380);
        iv.setPreserveRatio(true);

        ImageLoader.load(post, iv, imgPane, 430, 380);

        // Likes row
        boolean liked = post.isLikedBy(AppState.currentUser.getUsername());
        Label likeLbl = new Label((liked ? "♥ " : "♡ ") + post.getLikes() + " likes");
        likeLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#262626;-fx-padding:8 14 8 14;");

        box.getChildren().addAll(authorRow, imgPane, likeLbl);
        dialog.setScene(new javafx.scene.Scene(box));
        dialog.show();
    }

    // ── Open upload screen inside the shell ───────────────────────────────────

    private static void openUploadInShell() {
        // We push UploadScreen into the shell center by reaching through the
        // NavigationController.  UploadScreen calls back to PROFILE on success.
        // The upload uses a custom "UPLOAD" pseudo-tab via the shell.
        // Because MainShell.refresh() handles only 3 tabs, we call it directly:
        NavigationController.showUpload();
    }

    // ── Helper widgets ────────────────────────────────────────────────────────

    private static VBox statBox(String value, String label) {
        VBox box = new VBox(2, makeStatNum(value), makeStatLbl(label));
        box.setAlignment(Pos.CENTER);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private static Label makeStatNum(String value) {
        Label l = new Label(value);
        l.setStyle(Styles.STAT_NUM);
        return l;
    }

    private static Label makeStatLbl(String label) {
        Label l = new Label(label);
        l.setStyle(Styles.STAT_LBL);
        return l;
    }

    /** Resolves both relative (images/x.jpg) and absolute paths to a File. */
    private static java.io.File resolveImage(String path) {
        java.io.File f = new java.io.File(path);
        if (f.exists()) return f;
        // Try relative to working directory
        java.io.File rel = new java.io.File(System.getProperty("user.dir"), path);
        if (rel.exists()) return rel;
        return f; // return original, will show placeholder
    }

}