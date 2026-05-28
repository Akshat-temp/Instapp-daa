@echo off
REM ═══════════════════════════════════════════════════════════════════
REM  Instapp — Windows build + run script
REM ═══════════════════════════════════════════════════════════════════
REM
REM  Before running, edit the two paths below:
REM    JAVAFX_PATH  → folder containing javafx-sdk-21/lib
REM    PG_JAR       → full path to postgresql-42.x.x.jar
REM
REM  Also set your Railway DATABASE_URL (or leave blank for in-memory mode):
REM    set DATABASE_URL=postgresql://user:pass@host.railway.app:5432/dbname
REM
REM ═══════════════════════════════════════════════════════════════════

set JAVAFX_PATH=%~dp0lib
set PG_JAR=C:\postgresql-42.7.3.jar

REM ── Optional: set your Railway DATABASE_URL here ──────────────────
REM set DATABASE_URL=postgresql://postgres:OrvCfacYtBUwfQgPjuttoSjPvRTUPERB@zephyr.proxy.rlwy.net:45601/railway

set MODS=javafx.controls,javafx.graphics,javafx.base
set MP=--module-path "%JAVAFX_PATH%" --add-modules %MODS%
set CP=.;%PG_JAR%

echo [1/2] Compiling...
javac %MP% -cp %CP% ^
    models\User.java models\Post.java ^
    algorithm\UserTrie.java algorithm\SocialGraph.java algorithm\FeedRanker.java ^
    storage\Database.java ^
    ui\Styles.java ui\NavigationController.java ui\MainShell.java ^
    ui\AuthScreens.java ^
    ui\FeedScreen.java ui\SearchScreen.java ui\ProfileScreen.java ui\UploadScreen.java ^
    main\Main.java

if errorlevel 1 (
    echo [ERROR] Compilation failed. Check paths above.
    pause
    exit /b 1
)

echo [2/2] Running Instapp...
java %MP% -cp %CP% main.Main

pause
