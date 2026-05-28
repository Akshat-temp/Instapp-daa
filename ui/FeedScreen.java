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
import java.util.ArrayList;
import java.util.List;

/**
 * Feed screen layout (matches Instagram structure):
 *
 *  ┌─────────────────────────────────┐  ← fixed TOP
 *  │  Instapp logo        Logout  │
 *  ├─────────────────────────────────┤
 *  │  People You May Know (h-scroll) │  ← fixed, does NOT scroll with feed
 *  ├─────────────────────────────────┤
 *  │                                 │
 *  │   Post card 1  (scrollable)     │  ← CENTER: only this part scrolls
 *  │   Post card 2                   │
 *  │   Post card 3  ...              │
 *  │                                 │
 *  ├─────────────────────────────────┤
 *  │  🏠          🔍          👤     │  ← fixed BOTTOM (MainShell nav bar)
 *  └─────────────────────────────────┘
 */
public class FeedScreen {

    public static BorderPane build() {
        BorderPane root = new BorderPane();
        root.setStyle(Styles.ROOT_BG);

        // ── Fixed TOP: logo bar + People You May Know strip ──────────────────
        VBox topFixed = new VBox(0);

        // Logo bar
        HBox topBar = new HBox();
        topBar.setStyle("-fx-background-color:white;-fx-padding:10 16 10 16;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("Instapp");
        logo.setStyle("-fx-font-size:32px;-fx-font-family:'Segoe Script';-fx-text-fill:#262626;");
        topBar.getChildren().add(logo);

        topFixed.getChildren().add(topBar);

        // People You May Know strip (fixed, below logo bar)
        HBox strip = buildRecommendStrip();
        if (strip.isVisible()) {
            topFixed.getChildren().add(strip);
        }

        root.setTop(topFixed);

        // ── CENTER: scrollable feed posts only ────────────────────────────────
        VBox postsList = new VBox(0);
        postsList.setStyle(Styles.ROOT_BG);

        // ── Smart feed logic ──────────────────────────────────────────────────
        // Split all posts into two buckets:
        //   1. Posts from people the current user follows  → ranked by FeedRanker (Max-Heap)
        //   2. Posts from everyone else                    → ranked by TrendingTracker (Min-Heap top-K)
        // New user (no following) → bucket 1 is empty → sees only bucket 2 (trending)
        // As user follows people  → bucket 1 grows, their posts appear first seamlessly

        java.util.Set<String> following = new java.util.HashSet<>(AppState.currentUser.getFollowing());
        String me = AppState.currentUser.getUsername();

        List<Post> followingPosts = new ArrayList<>();
        List<Post> otherPosts     = new ArrayList<>();

        for (Post p : AppState.postStore.values()) {
            if (p.getAuthorUsername().equals(me)) continue; // skip own posts in feed
            if (following.contains(p.getAuthorUsername())) {
                followingPosts.add(p);
            } else {
                otherPosts.add(p);
            }
        }

        // Rank bucket 1 by likes (Max-Heap — FeedRanker)
        List<Post> rankedFollowing = AppState.ranker.getRankedFeed(followingPosts);

        // Rank bucket 2 by trending (Min-Heap top-K — TrendingTracker, K = all)
        List<Post> rankedTrending  = AppState.trending.getTopK(otherPosts, otherPosts.size());

        // Combine seamlessly — no divider
        List<Post> feed = new ArrayList<>();
        feed.addAll(rankedFollowing);
        feed.addAll(rankedTrending);

        if (feed.isEmpty()) {
            Label empty = new Label("No posts on the platform yet.");
            empty.setStyle(Styles.CAPTION + "-fx-font-size:14px;");
            empty.setWrapText(true);
            empty.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            VBox.setMargin(empty, new Insets(80, 0, 0, 0));
            postsList.setAlignment(Pos.TOP_CENTER);
            postsList.getChildren().add(empty);
        } else {
            for (Post p : feed) {
                postsList.getChildren().add(buildPostCard(p));
            }
        }

        ScrollPane scroll = new ScrollPane(postsList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setCenter(scroll);

        return root;
    }

    // ── People You May Know — fixed horizontal strip ──────────────────────────

    private static HBox buildRecommendStrip() {
        List<String> suggestions = AppState.graph.recommendUsers(
                AppState.currentUser.getUsername());

        // If BFS returns nothing (new user) → show top-K most followed users
        // Uses a Min-Heap of size K to find the most followed people efficiently
        if (suggestions.isEmpty()) {
            String me = AppState.currentUser.getUsername();

            // Collect all candidates first (exclude self and already-following)
            java.util.List<models.User> candidates = new java.util.ArrayList<>();
            for (models.User u : AppState.userStore.values()) {
                if (u.getUsername().equals(me)) continue;
                if (AppState.currentUser.getFollowing().contains(u.getUsername())) continue;
                candidates.add(u);
            }

            // Always show top 6 — most followed first
            int K = Math.min(candidates.size(), 6);

            // Min-Heap of size K — evicts least followed when full
            java.util.PriorityQueue<models.User> minHeap = new java.util.PriorityQueue<>(
                Math.max(1, K),
                (a, b) -> a.getFollowerCount() - b.getFollowerCount());

            for (models.User u : candidates) {
                if (minHeap.size() < K) {
                    minHeap.offer(u);         // heap not full yet — always add
                } else if (u.getFollowerCount() >= minHeap.peek().getFollowerCount()) {
                    minHeap.poll();           // evict least followed
                    minHeap.offer(u);
                }
            }

            // Drain heap → descending order (most followed first)
            suggestions = new java.util.ArrayList<>();
            while (!minHeap.isEmpty()) suggestions.add(0, minHeap.poll().getUsername());
        }

        // Still nobody on the platform — hide strip
        if (suggestions.isEmpty()) {
            HBox empty = new HBox();
            empty.setVisible(false);
            empty.setManaged(false);
            return empty;
        }

        // Header label
        Label hdr = new Label("Suggested for you");
        hdr.setStyle(Styles.SECTION_TITLE + "-fx-font-size:12px;-fx-padding:0 0 6 0;");

        // Horizontal scrollable cards
        HBox cards = new HBox(10);
        cards.setPadding(new Insets(4, 4, 4, 4));
        for (String name : suggestions) {
            cards.getChildren().add(buildSuggestionCard(name));
        }

        ScrollPane hScroll = new ScrollPane(cards);
        hScroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        hScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        hScroll.setFitToHeight(true);
        hScroll.setPrefHeight(118);
        HBox.setHgrow(hScroll, Priority.ALWAYS);

        VBox stripContent = new VBox(2, hdr, hScroll);
        stripContent.setPadding(new Insets(10, 14, 8, 14));
        stripContent.setStyle("-fx-background-color:white;");
        HBox.setHgrow(stripContent, Priority.ALWAYS);

        HBox strip = new HBox(stripContent);
        strip.setStyle(
            "-fx-background-color:white;" +
            "-fx-border-color:" + Styles.C_BORDER + ";" +
            "-fx-border-width:0 0 1 0;"
        );
        return strip;
    }

    private static VBox buildSuggestionCard(String name) {
        // Avatar — show DP if user has one, else gradient circle with letter
        javafx.scene.layout.StackPane avatar = new javafx.scene.layout.StackPane();
        avatar.setMinSize(46, 46); avatar.setMaxSize(46, 46); avatar.setPrefSize(46, 46);
        avatar.setStyle("-fx-cursor:hand;");
        avatar.setOnMouseClicked(e -> NavigationController.showOtherProfile(
                name, NavigationController.Tab.FEED));

        Label initials = new Label(name.substring(0, 1).toUpperCase());
        initials.setStyle(Styles.AVATAR);
        initials.setMinSize(46, 46); initials.setMaxSize(46, 46); initials.setPrefSize(46, 46);
        avatar.getChildren().add(initials);

        models.User sugUser = AppState.userStore.get(name);
        

        Label nameLbl = new Label(name);
        nameLbl.setStyle(Styles.CAPTION + "-fx-font-size:11px;-fx-text-fill:#262626;-fx-cursor:hand;");
        nameLbl.setMaxWidth(82);
        nameLbl.setOnMouseClicked(e -> NavigationController.showOtherProfile(
                name, NavigationController.Tab.FEED));

        boolean alreadyFollowing = AppState.currentUser.getFollowing().contains(name);
        Button followBtn = new Button(alreadyFollowing ? "Following" : "Follow");
        followBtn.setStyle(alreadyFollowing ? Styles.BTN_UNFOLLOW : Styles.BTN_FOLLOW);
        followBtn.setOnAction(e -> {
            User toUser = AppState.userStore.get(name);
            if (toUser == null) return;
            String from = AppState.currentUser.getUsername();
            if (AppState.currentUser.getFollowing().contains(name)) {
                AppState.graph.unfollow(from, name);
                AppState.currentUser.removeFollowing(name);
                toUser.removeFollower(from);
                Database.deleteFollow(from, name);
                followBtn.setText("Follow");
                followBtn.setStyle(Styles.BTN_FOLLOW);
            } else {
                AppState.graph.follow(from, name);
                AppState.currentUser.addFollowing(name);
                toUser.addFollower(from);
                Database.saveFollow(from, name);
                followBtn.setText("Following");
                followBtn.setStyle(Styles.BTN_UNFOLLOW);
            }
        });

        VBox card = new VBox(6, avatar, nameLbl, followBtn);
        card.setAlignment(Pos.CENTER);
        card.setStyle(Styles.RECOMMEND_CARD);
        card.setPrefWidth(90);
        return card;
    }

    // ── Individual post card ──────────────────────────────────────────────────

    private static VBox buildPostCard(Post post) {
        VBox card = new VBox(0);
        card.setStyle(Styles.CARD);

        // Author row
        Label avatarLbl = new Label(post.getAuthorUsername().substring(0, 1).toUpperCase());
        avatarLbl.setStyle(Styles.AVATAR);
        avatarLbl.setMinSize(34, 34);
        avatarLbl.setMaxSize(34, 34);
        avatarLbl.setPrefSize(34, 34);
        StackPane avatar = new StackPane(avatarLbl);
        avatar.setMinSize(34, 34); avatar.setMaxSize(34, 34); avatar.setPrefSize(34, 34);
      

        Label authorLbl = new Label(post.getAuthorUsername());
        authorLbl.setStyle(
            "-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#262626;-fx-cursor:hand;");
        authorLbl.setOnMouseClicked(e -> {
            if (!post.getAuthorUsername().equals(AppState.currentUser.getUsername())) {
                NavigationController.showOtherProfile(
                        post.getAuthorUsername(), NavigationController.Tab.FEED);
            } else {
                NavigationController.showTab(NavigationController.Tab.PROFILE);
            }
        });

        HBox authorRow = new HBox(10, avatar, authorLbl);
        authorRow.setAlignment(Pos.CENTER_LEFT);
        authorRow.setPadding(new Insets(10, 14, 10, 14));
        authorRow.setStyle("-fx-background-color:white;");
        card.getChildren().add(authorRow);

        // Post image
        StackPane imgContainer = new StackPane();
        imgContainer.setStyle("-fx-background-color:#efefef;");
        imgContainer.setPrefHeight(380);

        Label loadingLbl = new Label("📷");
        loadingLbl.setStyle("-fx-font-size:32px;");
        imgContainer.getChildren().add(loadingLbl);

        ImageView iv = new ImageView();
        iv.setFitWidth(430);
        iv.setFitHeight(380);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        Rectangle clip = new Rectangle(430, 380);
        iv.setClip(clip);

        ImageLoader.load(post, iv, imgContainer, 430, 380);
        card.getChildren().add(imgContainer);

        // Like row
        boolean liked = post.isLikedBy(AppState.currentUser.getUsername());
        Button likeBtn = new Button(liked ? "♥" : "♡");
        likeBtn.setStyle(liked ? Styles.BTN_LIKED : Styles.BTN_LIKE);

        Label likeCount = new Label(post.getLikes() + " likes");
        likeCount.setStyle(
            "-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#262626;");

        likeBtn.setOnAction(e -> {
            String me = AppState.currentUser.getUsername();
            boolean nowLiked = post.toggleLike(me);
            if (nowLiked) {
                Database.saveLike(me, post.getPostId());
                AppState.currentUser.addLikedPost(post.getPostId());
                likeBtn.setStyle(Styles.BTN_LIKED + "-fx-font-size:28px;");
                likeBtn.setText("♥");
            } else {
                Database.deleteLike(me, post.getPostId());
                AppState.currentUser.removeLikedPost(post.getPostId());
                likeBtn.setStyle(Styles.BTN_LIKE + "-fx-font-size:28px;");
                likeBtn.setText("♡");
            }
            likeCount.setText(post.getLikes() + " likes");
        });

        HBox likeRow = new HBox(8, likeBtn, likeCount);
        likeRow.setAlignment(Pos.CENTER_LEFT);
        likeRow.setPadding(new Insets(6, 14, 12, 10));
        likeRow.setStyle("-fx-background-color:white;");
        card.getChildren().add(likeRow);

        return card;
    }



}