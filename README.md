# Instapp — Phase 3

A JavaFX social media application implementing Instagram/Meta algorithms.
Built for B.Tech CSE DAA Project (Graphic Era Hill University).

---

## What's new in Phase 3

| Feature | Status |
|---|---|
| Swing → JavaFX UI (separate `ui/` package) | ✅ Done |
| Bottom navigation bar (Feed / Search / Profile) | ✅ Done |
| "People You May Know" strip on Feed (BFS graph) | ✅ Done |
| Like toggle per user (no double-liking) | ✅ Fixed |
| PostgreSQL on Railway (persistent storage) | ✅ Done |
| Graceful in-memory fallback (no DATABASE_URL) | ✅ Done |
| Real-time Trie prefix search | ✅ Done |
| Max-Heap ranked feed | ✅ Done |
| Follow / Unfollow from profile and recommend strip | ✅ Done |
| Image preview on Upload screen | ✅ Done |
| DM system | ✖ Removed by design |

---

## Project structure

```
Instapp/
├── main/
│   ├── Main.java          ← JavaFX Application entry point
│   └── AppState.java      ← Shared in-memory state + DB loader
│
├── models/
│   ├── User.java          ← User data model
│   └── Post.java          ← Post model with per-user like set
│
├── algorithm/
│   ├── UserTrie.java      ← O(L) prefix search
│   ├── SocialGraph.java   ← Directed graph + BFS recommendations
│   └── FeedRanker.java    ← Max-Heap feed ranking
│
├── storage/
│   └── Database.java      ← PostgreSQL (Railway) JDBC layer
│
├── ui/
│   ├── Styles.java        ← Shared design tokens / CSS strings
│   ├── NavigationController.java  ← Scene & screen routing
│   ├── MainShell.java     ← Persistent shell + bottom nav bar
│   ├── WelcomeScreen.java
│   ├── SignupScreen.java
│   ├── LoginScreen.java
│   ├── FeedScreen.java    ← Ranked feed + People You May Know strip
│   ├── SearchScreen.java  ← Real-time Trie search
│   ├── ProfileScreen.java ← Own profile + other user read-only
│   └── UploadScreen.java  ← Image picker + post creation
│
├── run.bat   ← Windows compile + run
├── run.sh    ← Linux/macOS compile + run
└── README.md
```

---

## Prerequisites

| Requirement | Download |
|---|---|
| JDK 17 or above | https://adoptium.net |
| JavaFX SDK 21 | https://gluonhq.com/products/javafx/ |
| PostgreSQL JDBC 42.x | https://jdbc.postgresql.org/ |

---

## Setup steps

### 1. Download JavaFX SDK
Go to https://gluonhq.com/products/javafx/ → choose **JavaFX 21 SDK** for your OS.
Extract it. Note the full path to the `lib/` folder inside it.

### 2. Download the PostgreSQL JDBC driver
Go to https://jdbc.postgresql.org/download/ → download `postgresql-42.7.3.jar` (or latest).

### 3. (Optional) Set up Railway PostgreSQL
1. Go to https://railway.app and create a project.
2. Add a **PostgreSQL** service.
3. Copy the **DATABASE_URL** from the service's "Connect" tab.
   It looks like: `postgresql://postgres:password@host.railway.app:5432/railway`
4. Set it as an environment variable before running:
   - **Windows**: `set DATABASE_URL=postgresql://...`
   - **Linux/Mac**: `export DATABASE_URL="postgresql://..."`

   If you skip this step, the app works fine in-memory (data resets on exit).

### 4. Edit the run script
Open `run.bat` (Windows) or `run.sh` (Linux/Mac) and set:
```
JAVAFX_PATH = path to your javafx-sdk-21/lib folder
PG_JAR      = path to your postgresql-42.x.x.jar file
```

### 5. Run
```bash
# Windows
run.bat

# Linux / macOS
chmod +x run.sh
./run.sh
```

---

## Algorithms implemented

### Trie — Real-time search
- Class: `algorithm/UserTrie.java`
- Every username is inserted on signup.
- `searchByPrefix(prefix)` walks the trie in O(P) then collects up to 7 results in O(R).
- Used by: SearchScreen (live text listener).

### Graph + BFS — "People You May Know"
- Class: `algorithm/SocialGraph.java`
- Directed adjacency list: `Map<String, Set<String>>`.
- `recommendUsers(u)` runs BFS 2 levels deep from u, returning users followed by u's friends that u does not already follow.
- Capped at 10 results.
- Used by: FeedScreen recommendation strip.

### Max-Heap — Feed ranking
- Class: `algorithm/FeedRanker.java`
- All posts are inserted into a `PriorityQueue` ordered by likes (desc), then timestamp (desc) on ties.
- O(n log n) sort. Result is always the most-liked post first.
- Used by: FeedScreen post list.

---

## Like system (fixed)

Each `Post` stores a `Set<String> likedBy` (usernames).
- Liking = `toggleLike(username)` → adds username to set.
- Unliking = `toggleLike(username)` again → removes username from set.
- Like count = `likedBy.size()` — structurally impossible to double-count.
- Persisted in the `likes` table with `(username, post_id)` as primary key.

---

## Compiling manually (without the run scripts)

```bash
JAVAFX=/path/to/javafx-sdk-21/lib
PG=/path/to/postgresql-42.7.3.jar

# Compile
javac --module-path $JAVAFX --add-modules javafx.controls,javafx.graphics,javafx.base \
  -cp ".:$PG" \
  models/User.java models/Post.java \
  algorithm/UserTrie.java algorithm/SocialGraph.java algorithm/FeedRanker.java \
  storage/Database.java \
  ui/Styles.java ui/NavigationController.java ui/MainShell.java \
  ui/WelcomeScreen.java ui/SignupScreen.java ui/LoginScreen.java \
  ui/FeedScreen.java ui/SearchScreen.java ui/ProfileScreen.java ui/UploadScreen.java \
  main/AppState.java main/Main.java

# Run
java --module-path $JAVAFX --add-modules javafx.controls,javafx.graphics,javafx.base \
  -cp ".:$PG" main.Main
```
