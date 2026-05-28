package storage;

import models.Post;
import models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════════════════════
 *  Instapp — Railway PostgreSQL Database Layer
 * ═══════════════════════════════════════════════════════════════════
 *
 *  TABLES (4 total — fully relational with FK constraints + indexes)
 *
 *  ┌─────────────────────────────────────────────────────────────┐
 *  │  TABLE: users                                               │
 *  │  ─────────────────────────────────────────────────────────  │
 *  │  username    TEXT  PRIMARY KEY                              │
 *  │  password    TEXT  NOT NULL                                 │
 *  └─────────────────────────────────────────────────────────────┘
 *
 *  ┌─────────────────────────────────────────────────────────────┐
 *  │  TABLE: posts                                               │
 *  │  ─────────────────────────────────────────────────────────  │
 *  │  post_id     TEXT    PRIMARY KEY                            │
 *  │  author      TEXT    NOT NULL  → FK → users(username)       │
 *  │  image_url   TEXT    NOT NULL                               │
 *  │  timestamp   BIGINT  NOT NULL  (epoch ms, for feed ranking) │
 *  └─────────────────────────────────────────────────────────────┘
 *
 *  ┌─────────────────────────────────────────────────────────────┐
 *  │  TABLE: follows                                             │
 *  │  ─────────────────────────────────────────────────────────  │
 *  │  follower    TEXT  NOT NULL  → FK → users(username)         │
 *  │  following   TEXT  NOT NULL  → FK → users(username)         │
 *  │  PRIMARY KEY (follower, following) ← no duplicate follows   │
 *  └─────────────────────────────────────────────────────────────┘
 *
 *  ┌─────────────────────────────────────────────────────────────┐
 *  │  TABLE: likes                                               │
 *  │  ─────────────────────────────────────────────────────────  │
 *  │  username    TEXT  NOT NULL  → FK → users(username)         │
 *  │  post_id     TEXT  NOT NULL  → FK → posts(post_id)          │
 *  │  PRIMARY KEY (username, post_id) ← no double-likes          │
 *  └─────────────────────────────────────────────────────────────┘
 *
 *  INDEXES (for fast queries on large data)
 *  ─────────────────────────────────────────────────────────────
 *  idx_posts_author       → posts(author)        fast profile load
 *  idx_posts_timestamp    → posts(timestamp)     fast feed ranking
 *  idx_follows_follower   → follows(follower)    fast following list
 *  idx_follows_following  → follows(following)   fast followers list
 *  idx_likes_post_id      → likes(post_id)       fast like count
 *  idx_likes_username     → likes(username)      fast liked-by-user
 *
 *  CONNECTION
 *  ─────────────────────────────────────────────────────────────
 *  Set env var DATABASE_URL to your Railway PostgreSQL URL:
 *    postgresql://user:pass@host.railway.app:5432/railway
 *
 *  If not set → app runs in in-memory mode (data resets on exit).
 * ═══════════════════════════════════════════════════════════════════
 */
public class Database {

    private static Connection conn      = null;
    private static boolean    available = false;

    // ── Connect ───────────────────────────────────────────────────────────────

    public static void init() {
        String rawUrl = System.getenv("DATABASE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            System.out.println("[DB] No DATABASE_URL — running in-memory only.");
            return;
        }
        try {
            Class.forName("org.postgresql.Driver");
            conn      = DriverManager.getConnection(toJdbcUrl(rawUrl));
            available = true;
            System.out.println("[DB] Connected to Railway PostgreSQL.");
            createSchema();
        } catch (Exception e) {
            System.err.println("[DB] Connection failed: " + e.getMessage());
            System.out.println("[DB] Falling back to in-memory mode.");
        }
    }

    public static boolean isAvailable() { return available; }

    /** Reconnects if the connection dropped (e.g. Railway sleep). */
    private static void ensureConnected() {
        if (!available) return;
        try {
            if (conn == null || conn.isClosed() || !conn.isValid(3)) {
                System.out.println("[DB] Reconnecting...");
                init();
            }
        } catch (SQLException ignored) {}
    }

    /** Converts Railway DATABASE_URL → JDBC URL. */
    private static String toJdbcUrl(String raw) {
        String stripped = raw.replace("postgresql://", "").replace("postgres://", "");
        int    at       = stripped.indexOf('@');
        String userInfo = stripped.substring(0, at);
        String hostPath = stripped.substring(at + 1);
        String user     = userInfo.split(":", 2)[0];
        String pass     = userInfo.contains(":") ? userInfo.split(":", 2)[1] : "";
        return "jdbc:postgresql://" + hostPath
                + "?user="           + user
                + "&password="       + pass
                + "&sslmode=require"
                + "&connectTimeout=10"
                + "&socketTimeout=30";
    }

    // ── Schema ────────────────────────────────────────────────────────────────

    private static void createSchema() throws SQLException {
        try (Statement s = conn.createStatement()) {

            // TABLE: users
            s.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    username  TEXT NOT NULL,
                    password  TEXT NOT NULL,
                    CONSTRAINT pk_users PRIMARY KEY (username)
                )
            """);

            // TABLE: posts
            s.execute("""
                CREATE TABLE IF NOT EXISTS posts (
                    post_id   TEXT   NOT NULL,
                    author    TEXT   NOT NULL,
                    image_url TEXT   NOT NULL,
                    timestamp BIGINT NOT NULL,
                    CONSTRAINT pk_posts       PRIMARY KEY (post_id),
                    CONSTRAINT fk_post_author FOREIGN KEY (author)
                        REFERENCES users(username) ON DELETE CASCADE
                )
            """);

            // TABLE: follows
            s.execute("""
                CREATE TABLE IF NOT EXISTS follows (
                    follower  TEXT NOT NULL,
                    following TEXT NOT NULL,
                    CONSTRAINT pk_follows      PRIMARY KEY (follower, following),
                    CONSTRAINT fk_follow_from  FOREIGN KEY (follower)
                        REFERENCES users(username) ON DELETE CASCADE,
                    CONSTRAINT fk_follow_to    FOREIGN KEY (following)
                        REFERENCES users(username) ON DELETE CASCADE,
                    CONSTRAINT chk_no_self     CHECK (follower <> following)
                )
            """);

            // TABLE: likes
            s.execute("""
                CREATE TABLE IF NOT EXISTS likes (
                    username TEXT NOT NULL,
                    post_id  TEXT NOT NULL,
                    CONSTRAINT pk_likes      PRIMARY KEY (username, post_id),
                    CONSTRAINT fk_like_user  FOREIGN KEY (username)
                        REFERENCES users(username) ON DELETE CASCADE,
                    CONSTRAINT fk_like_post  FOREIGN KEY (post_id)
                        REFERENCES posts(post_id)  ON DELETE CASCADE
                )
            """);

            // INDEXES
            s.execute("CREATE INDEX IF NOT EXISTS idx_posts_author      ON posts(author)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_posts_timestamp   ON posts(timestamp DESC)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_follows_follower  ON follows(follower)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_follows_following ON follows(following)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_likes_post_id     ON likes(post_id)");
            s.execute("CREATE INDEX IF NOT EXISTS idx_likes_username    ON likes(username)");
        }

        System.out.println("[DB] Schema ready — 4 tables, 6 indexes.");
    }

    // ── USERS ─────────────────────────────────────────────────────────────────

    public static void saveUser(User u) {
        if (!available) return;
        ensureConnected();
        String sql = "INSERT INTO users (username, password) VALUES (?, ?) ON CONFLICT (username) DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] saveUser: " + e.getMessage());
        }
    }

    public static List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        if (!available) return list;
        ensureConnected();
        try (Statement s  = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT username, password FROM users")) {
            while (rs.next())
                list.add(new User(rs.getString("username"), rs.getString("password")));
        } catch (SQLException e) {
            System.err.println("[DB] getAllUsers: " + e.getMessage());
        }
        return list;
    }

    // ── POSTS ─────────────────────────────────────────────────────────────────

    public static void savePost(Post p) {
        if (!available) return;
        ensureConnected();
        String sql = "INSERT INTO posts (post_id, author, image_url, timestamp) VALUES (?, ?, ?, ?) ON CONFLICT (post_id) DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getPostId());
            ps.setString(2, p.getAuthorUsername());
            ps.setString(3, p.getImageUrl());
            ps.setLong  (4, p.getTimestamp());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] savePost: " + e.getMessage());
        }
    }

    public static List<Post> getAllPosts() {
        List<Post> list = new ArrayList<>();
        if (!available) return list;
        ensureConnected();
        try (Statement s  = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT post_id, author, image_url, timestamp FROM posts ORDER BY timestamp DESC")) {
            while (rs.next())
                list.add(new Post(
                        rs.getString("post_id"),
                        rs.getString("author"),
                        rs.getString("image_url"),
                        rs.getLong("timestamp")));
        } catch (SQLException e) {
            System.err.println("[DB] getAllPosts: " + e.getMessage());
        }
        return list;
    }

    // ── FOLLOWS ───────────────────────────────────────────────────────────────

    public static void saveFollow(String follower, String following) {
        if (!available) return;
        ensureConnected();
        String sql = "INSERT INTO follows (follower, following) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, follower);
            ps.setString(2, following);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] saveFollow: " + e.getMessage());
        }
    }

    public static void deleteFollow(String follower, String following) {
        if (!available) return;
        ensureConnected();
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM follows WHERE follower = ? AND following = ?")) {
            ps.setString(1, follower);
            ps.setString(2, following);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] deleteFollow: " + e.getMessage());
        }
    }

    public static List<String[]> getAllFollows() {
        List<String[]> list = new ArrayList<>();
        if (!available) return list;
        ensureConnected();
        try (Statement s  = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT follower, following FROM follows")) {
            while (rs.next())
                list.add(new String[]{ rs.getString("follower"), rs.getString("following") });
        } catch (SQLException e) {
            System.err.println("[DB] getAllFollows: " + e.getMessage());
        }
        return list;
    }

    // ── LIKES ─────────────────────────────────────────────────────────────────

    public static void saveLike(String username, String postId) {
        if (!available) return;
        ensureConnected();
        String sql = "INSERT INTO likes (username, post_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, postId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] saveLike: " + e.getMessage());
        }
    }

    public static void deleteLike(String username, String postId) {
        if (!available) return;
        ensureConnected();
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM likes WHERE username = ? AND post_id = ?")) {
            ps.setString(1, username);
            ps.setString(2, postId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] deleteLike: " + e.getMessage());
        }
    }

    public static List<String[]> getAllLikes() {
        List<String[]> list = new ArrayList<>();
        if (!available) return list;
        ensureConnected();
        try (Statement s  = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT username, post_id FROM likes")) {
            while (rs.next())
                list.add(new String[]{ rs.getString("username"), rs.getString("post_id") });
        } catch (SQLException e) {
            System.err.println("[DB] getAllLikes: " + e.getMessage());
        }
        return list;
    }
}
