package ui;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import models.User;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;

/**
 * Loads a User's profile picture (DP) asynchronously into a circular
 * avatar StackPane, replacing the initials label.
 *
 * Usage:
 *   DpLoader.load(user, avatarStack, 80);
 *
 * If the user has no DP (no Base64 data, no file path), nothing happens
 * and the initials label stays visible.
 */
public class DpLoader {

    /**
     * @param user         The user whose DP to load.
     * @param container    The StackPane that currently holds the initials Label.
     * @param diameter     The circle diameter in pixels (e.g. 80 for large avatar).
     */
    public static void load(User user, StackPane container, double diameter) {
        Thread t = new Thread(() -> {
            try {
                BufferedImage raw = null;

                // 1. Prefer Base64 data stored in memory (loaded from DB)
                if (user.hasDpData()) {
                    byte[] bytes = Base64.getDecoder().decode(user.getDpData());
                    raw = ImageIO.read(new ByteArrayInputStream(bytes));
                }

                // 2. Fallback to file path (e.g. just-picked before reload)
                if (raw == null && user.hasDp()) {
                    File f = new File(user.getDpPath());
                    if (f.exists()) raw = ImageIO.read(f);
                }

                if (raw == null) return; // no DP available — keep initials

                // 3. Crop to square from centre, then scale to circle diameter
                BufferedImage cropped = cropToSquare(raw);
                int size = (int) diameter;
                BufferedImage scaled  = scale(cropped, size, size);

                // 4. Convert to JavaFX WritableImage
                WritableImage fxImg = toFX(scaled);

                Platform.runLater(() -> {
                    ImageView iv = new ImageView(fxImg);
                    iv.setFitWidth(diameter);
                    iv.setFitHeight(diameter);
                    iv.setPreserveRatio(false);
                    iv.setSmooth(true);

                    // Circular clip so the image fits the round avatar
                    Circle clip = new Circle(diameter / 2, diameter / 2, diameter / 2);
                    iv.setClip(clip);

                    // Replace all children (the initials Label) with the image
                    container.getChildren().setAll(iv);
                });

            } catch (Exception ex) {
                System.err.println("[DpLoader] " + ex.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── Image helpers ─────────────────────────────────────────────────────────

    /** Centre-crops to a square so the circle doesn't squash portrait/landscape images. */
    private static BufferedImage cropToSquare(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w == h) return src;
        int side = Math.min(w, h);
        int x = (w - side) / 2;
        int y = (h - side) / 2;
        return src.getSubimage(x, y, side, side);
    }

    /** Bicubic scaling to exact size. */
    private static BufferedImage scale(BufferedImage src, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                           RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,
                           RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    /** Converts a BufferedImage to a JavaFX WritableImage. */
    private static WritableImage toFX(BufferedImage bi) {
        if (bi.getType() != BufferedImage.TYPE_INT_ARGB) {
            BufferedImage argb = new BufferedImage(
                    bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = argb.createGraphics();
            g.drawImage(bi, 0, 0, null);
            g.dispose();
            bi = argb;
        }
        WritableImage wi = new WritableImage(bi.getWidth(), bi.getHeight());
        PixelWriter pw   = wi.getPixelWriter();
        int[] pixels = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(),
                                 null, 0, bi.getWidth());
        for (int y = 0; y < bi.getHeight(); y++)
            for (int x = 0; x < bi.getWidth(); x++)
                pw.setArgb(x, y, pixels[y * bi.getWidth() + x]);
        return wi;
    }
}
