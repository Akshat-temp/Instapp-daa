package ui;


public final class Styles {

    private Styles() {}

    // ── Colour palette (Instagram-inspired) ──────────────────────────────────
    public static final String C_WHITE      = "#ffffff";
    public static final String C_BG         = "#fafafa";
    public static final String C_BORDER     = "#dbdbdb";
    public static final String C_TEXT       = "#262626";
    public static final String C_SUBTLE     = "#8e8e8e";
    public static final String C_PURPLE     = "#833ab4";
    public static final String C_PINK       = "#c13584";
    public static final String C_RED        = "#e1306c";
    public static final String C_ERROR      = "#ed4956";
    public static final String C_SUCCESS    = "#3897f0";

    // ── Gradient (used on primary buttons and logo) ───────────────────────────
    public static final String GRADIENT =
            "linear-gradient(to right, #833ab4, #c13584, #e1306c)";

    // ── Reusable style strings ────────────────────────────────────────────────

    /** Full-screen white root */
    public static final String ROOT =
            "-fx-background-color: " + C_WHITE + ";";

    /** Full-screen off-white root */
    public static final String ROOT_BG =
            "-fx-background-color: " + C_BG + ";";

    /** Primary gradient button */
    public static final String BTN_PRIMARY =
            "-fx-background-color: " + GRADIENT + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 0 10 0;";

    /** Outline / secondary button */
    public static final String BTN_SECONDARY =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + C_PURPLE + ";" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: " + C_PURPLE + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 9 0 9 0;";

    /** Small follow button (purple fill) */
    public static final String BTN_FOLLOW =
            "-fx-background-color: " + C_PURPLE + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 5 12 5 12;";

    /** Small unfollow button (grey outline) */
    public static final String BTN_UNFOLLOW =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + C_TEXT + ";" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 4 12 4 12;";

    /** Like button — not liked */
    public static final String BTN_LIKE =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + C_TEXT + ";" +
            "-fx-font-size: 20px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0;";

    /** Like button — liked (red heart) */
    public static final String BTN_LIKED =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + C_RED + ";" +
            "-fx-font-size: 20px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0;";

    /** Styled text input */
    public static final String INPUT =
            "-fx-background-color: " + C_BG + ";" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 10 12 10 12;" +
            "-fx-text-fill: " + C_TEXT + ";";

    /** Nav bar container */
    public static final String NAV_BAR =
            "-fx-background-color: " + C_WHITE + ";" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-width: 1 0 0 0;" +
            "-fx-padding: 0;";

    /** Active nav tab button */
    public static final String NAV_ACTIVE =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + C_TEXT + ";" +
            "-fx-font-size: 22px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 0 10 0;";

    /** Inactive nav tab button */
    public static final String NAV_INACTIVE =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + C_SUBTLE + ";" +
            "-fx-font-size: 22px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 0 10 0;";

    /** Top bar (header) */
    public static final String TOP_BAR =
            "-fx-background-color: " + C_WHITE + ";" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-width: 0 0 1 0;" +
            "-fx-padding: 10 16 10 16;";

    /** Feed post card */
    public static final String CARD =
            "-fx-background-color: " + C_WHITE + ";" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-width: 0 0 1 0;";

    /** Small avatar circle label */
    public static final String AVATAR =
            "-fx-background-color: " + GRADIENT + ";" +
            "-fx-background-radius: 50;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-alignment: center;";

    /** Large avatar for profile */
    public static final String AVATAR_LARGE =
            "-fx-background-color: " + GRADIENT + ";" +
            "-fx-background-radius: 50;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;" +
            "-fx-alignment: center;";

    /** Logo text style */
    public static final String LOGO =
            "-fx-font-size: 28px;" +
            "-fx-font-style: italic;" +
            "-fx-text-fill: " + C_PURPLE + ";";

    /** Section header text */
    public static final String SECTION_TITLE =
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + C_TEXT + ";";

    /** Subtle caption / secondary text */
    public static final String CAPTION =
            "-fx-font-size: 12px;" +
            "-fx-text-fill: " + C_SUBTLE + ";";

    /** Bold stat number */
    public static final String STAT_NUM =
            "-fx-font-size: 17px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: " + C_TEXT + ";";

    /** Stat label below number */
    public static final String STAT_LBL =
            "-fx-font-size: 12px;" +
            "-fx-text-fill: " + C_TEXT + ";";

    /** Error message label */
    public static final String ERROR_MSG =
            "-fx-font-size: 12px;" +
            "-fx-text-fill: " + C_ERROR + ";";

    /** Success message label */
    public static final String SUCCESS_MSG =
            "-fx-font-size: 12px;" +
            "-fx-text-fill: " + C_SUCCESS + ";";

    /** Back / link button */
    public static final String BTN_LINK =
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + C_PURPLE + ";" +
            "-fx-font-size: 14px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0;";

    /** Search bar input */
    public static final String SEARCH_INPUT =
            "-fx-background-color: #efefef;" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 8 12 8 12;" +
            "-fx-text-fill: " + C_TEXT + ";";

    /** Recommend strip card */
    public static final String RECOMMEND_CARD =
            "-fx-background-color: " + C_WHITE + ";" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 12 10 12;";
}
