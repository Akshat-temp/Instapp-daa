@echo off
REM ═══════════════════════════════════════════════════════════════════
REM  Instapp — Windows build + run script
REM ═══════════════════════════════════════════════════════════════════

REM Change to the folder where run.bat lives — fixes relative image paths
cd /d "%~dp0"

set JAVAFX_PATH=%~dp0lib
set PG_JAR=%~dp0lib\postgresql-42.7.3.jar
set DATABASE_URL=postgresql://postgres:OrvCfacYtBUwfQgPjuttoSjPvRTUPERB@zephyr.proxy.rlwy.net:45601/railway

set MODS=javafx.controls,javafx.graphics,javafx.base
set MP=--module-path "%JAVAFX_PATH%" --add-modules %MODS%
set CP=.;%PG_JAR%

echo [1/2] Compiling...
javac %MP% -cp %CP% ^
    models\User.java models\Post.java models\AppState.java ^
    algorithm\UserTrie.java algorithm\SocialGraph.java algorithm\FeedRanker.java algorithm\TrendingTracker.java ^
    storage\Database.java ^
    ui\Styles.java ui\NavigationController.java ui\MainShell.java ^
    ui\AuthScreens.java ui\ImageLoader.java ui\DpLoader.java ^
    ui\FeedScreen.java ui\SearchScreen.java ui\ProfileScreen.java ui\UploadScreen.java ui\FollowListScreen.java ^
    main\Main.java

if errorlevel 1 (
    echo [ERROR] Compilation failed. Check paths above.
    pause
    exit /b 1
)

echo [2/2] Running Instapp...
java %MP% -cp %CP% --enable-native-access=javafx.graphics,javafx.media -Duser.timezone=UTC -Dprism.order=sw -Djavafx.verbose=false -Djava.util.logging.config.file=NUL main.Main
pause