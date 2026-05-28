package storage;

import models.Post;
import models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Railway PostgreSQL persistence layer.
 *
 * TABLES:
 *   users   (username PK, password)
 *   posts   (post_id PK, author FK, image_url, image_data TEXT, timestamp)
 *   follows (follower FK, following FK, PK composite)
 *   likes   (username FK, post_id FK, PK composite)
 *
 * image_data stores the full Base64-encoded image so posts are visible
 * on ALL devices without sharing files.
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
        }
    }

    public static boolean isAvailable() { return available; }

    private static void ensureConnected() {
        if (!available) return;
        try {
            if (conn == null || conn.isClosed() || !conn.isValid(3)) {
                init();
            }
        } catch (SQLException ignored) {}
    }

    private static String toJdbcUrl(String raw) {
        String s    = raw.replace("postgresql://", "").replace("postgres://", "");
        int    at   = s.indexOf('@');
        String ui   = s.substring(0, at);
        String hp   = s.substring(at + 1);
        String user = ui.split(":", 2)[0];
        String pass = ui.contains(":") ? ui.split(":", 2)[1] : "";
        return "jdbc:postgresql://" + hp
                + "?user="           + user
                + "&password="       + pass
                + "&sslmode=require"
                + "&connectTimeout=10"
                + "&socketTimeout=30"
                + "&TimeZone=UTC";
    }

    // ── Schema ────────────────────────────────────────────────────────────────

    private static void createSchema() throws SQLException {
        try (Statement st = conn.createStatement()) {

            st.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "    username  TEXT NOT NULL," +
                "    password  TEXT NOT NULL," +
                "    dp_data   TEXT," +
                "    CONSTRAINT pk_users PRIMARY KEY (username)" +
                ")"
            );

            st.execute(
                "CREATE TABLE IF NOT EXISTS posts (" +
                "    post_id    TEXT   NOT NULL," +
                "    author     TEXT   NOT NULL," +
                "    image_url  TEXT   NOT NULL," +
                "    image_data TEXT," +
                "    timestamp  BIGINT NOT NULL," +
                "    CONSTRAINT pk_posts       PRIMARY KEY (post_id)," +
                "    CONSTRAINT fk_post_author FOREIGN KEY (author)" +
                "        REFERENCES users(username) ON DELETE CASCADE" +
                ")"
            );

            // Add image_data column for databases created before this version
            try {
                st.execute("ALTER TABLE posts ADD COLUMN IF NOT EXISTS image_data TEXT");
            } catch (SQLException ignored) {}
            // Add dp_data column for existing databases
            try {
                st.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS dp_data TEXT");
            } catch (SQLException ignored) {}

            st.execute(
                "CREATE TABLE IF NOT EXISTS follows (" +
                "    follower  TEXT NOT NULL," +
                "    following TEXT NOT NULL," +
                "    CONSTRAINT pk_follows     PRIMARY KEY (follower, following)," +
                "    CONSTRAINT fk_follow_from FOREIGN KEY (follower)" +
                "        REFERENCES users(username) ON DELETE CASCADE," +
                "    CONSTRAINT fk_follow_to   FOREIGN KEY (following)" +
                "        REFERENCES users(username) ON DELETE CASCADE," +
                "    CONSTRAINT chk_no_self    CHECK (follower <> following)" +
                ")"
            );

            st.execute(
                "CREATE TABLE IF NOT EXISTS likes (" +
                "    username TEXT NOT NULL," +
                "    post_id  TEXT NOT NULL," +
                "    CONSTRAINT pk_likes     PRIMARY KEY (username, post_id)," +
                "    CONSTRAINT fk_like_user FOREIGN KEY (username)" +
                "        REFERENCES users(username) ON DELETE CASCADE," +
                "    CONSTRAINT fk_like_post FOREIGN KEY (post_id)" +
                "        REFERENCES posts(post_id) ON DELETE CASCADE" +
                ")"
            );

            st.execute("CREATE INDEX IF NOT EXISTS idx_posts_author      ON posts(author)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_posts_timestamp   ON posts(timestamp DESC)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_follows_follower  ON follows(follower)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_follows_following ON follows(following)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_likes_post_id     ON likes(post_id)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_likes_username    ON likes(username)");
        }
        System.out.println("[DB] Schema ready — 4 tables, 6 indexes.");
    }

    // ── USERS ─────────────────────────────────────────────────────────────────

    public static void saveUser(User u) {
        if (!available) return;
        ensureConnected();
        String sql = "INSERT INTO users (username, password) VALUES (?, ?) " +
                     "ON CONFLICT (username) DO NOTHING";
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
        String sql = "SELECT username, password, dp_data FROM users";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User(rs.getString("username"), rs.getString("password"));
                String dpData = rs.getString("dp_data");
                if (dpData != null && !dpData.isBlank()) u.setDpData(dpData);
                list.add(u);
            }
        } catch (SQLException e) {
            System.err.println("[DB] getAllUsers: " + e.getMessage());
        }
        return list;
    }

    /** Save or update a user's DP Base64 data. */
    public static void saveUserDp(String username, String dpData) {
        if (!available) return;
        ensureConnected();
        String sql = "UPDATE users SET dp_data = ? WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dpData);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] saveUserDp: " + e.getMessage());
        }
    }

    // ── POSTS ─────────────────────────────────────────────────────────────────

    public static void savePost(Post p) {
        if (!available) return;
        ensureConnected();
        String sql = "INSERT INTO posts (post_id, author, image_url, image_data, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?) ON CONFLICT (post_id) DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getPostId());
            ps.setString(2, p.getAuthorUsername());
            ps.setString(3, p.getImageUrl());
            ps.setString(4, p.getImageData());   // null is fine for TEXT column
            ps.setLong(5,   p.getTimestamp());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] savePost: " + e.getMessage());
        }
    }

    public static List<Post> getAllPosts() {
        List<Post> list = new ArrayList<>();
        if (!available) return list;
        ensureConnected();
        String sql = "SELECT post_id, author, image_url, image_data, timestamp " +
                     "FROM posts ORDER BY timestamp DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Post p = new Post(
                    rs.getString("post_id"),
                    rs.getString("author"),
                    rs.getString("image_url"),
                    rs.getLong("timestamp")
                );
                p.setImageData(rs.getString("image_data")); // may be null — fine
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("[DB] getAllPosts: " + e.getMessage());
        }
        return list;
    }

    // ── FOLLOWS ───────────────────────────────────────────────────────────────

    public static void saveFollow(String follower, String following) {
        if (!available) return;
        ensureConnected();
        String sql = "INSERT INTO follows (follower, following) VALUES (?, ?) " +
                     "ON CONFLICT DO NOTHING";
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
        String sql = "DELETE FROM follows WHERE follower = ? AND following = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
        String sql = "SELECT follower, following FROM follows";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{ rs.getString("follower"), rs.getString("following") });
            }
        } catch (SQLException e) {
            System.err.println("[DB] getAllFollows: " + e.getMessage());
        }
        return list;
    }

    // ── LIKES ─────────────────────────────────────────────────────────────────

    public static void saveLike(String username, String postId) {
        if (!available) return;
        ensureConnected();
        String sql = "INSERT INTO likes (username, post_id) VALUES (?, ?) " +
                     "ON CONFLICT DO NOTHING";
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
        String sql = "DELETE FROM likes WHERE username = ? AND post_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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
        String sql = "SELECT username, post_id FROM likes";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{ rs.getString("username"), rs.getString("post_id") });
            }
        } catch (SQLException e) {
            System.err.println("[DB] getAllLikes: " + e.getMessage());
        }
        return list;
    }
}