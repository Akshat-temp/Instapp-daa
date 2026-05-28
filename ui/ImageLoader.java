package ui;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import models.Post;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;

/**
 * Loads post images from Base64 DB data or file path fallback.
 * Uses ImageIO (any format) + bicubic scaling for best quality.
 * Grid thumbnails stay small (3 per row). Feed shows full width.
 */
public class ImageLoader {

    public static void load(Post post, ImageView iv, StackPane container,
                            double fitWidth, double fitHeight) {
        Thread t = new Thread(() -> {
            try {
                BufferedImage raw = null;

                // 1. Try Base64 from DB first
                if (post.hasImageData()) {
                    byte[] bytes = Base64.getDecoder().decode(post.getImageData());
                    raw = ImageIO.read(new ByteArrayInputStream(bytes));
                }

                // 2. Fallback to file path
                if (raw == null) {
                    File f = new File(post.getImageUrl());
                    if (f.exists()) raw = ImageIO.read(f);
                }

                if (raw == null) return;

                // 3. Scale with bicubic for high quality
                BufferedImage scaled = scale(raw, (int) fitWidth, (int) fitHeight);

                // 4. Convert to JavaFX WritableImage
                WritableImage fxImg = toFX(scaled);

                Platform.runLater(() -> {
                    iv.setImage(fxImg);
                    iv.setFitWidth(fitWidth);
                    iv.setFitHeight(fitHeight);
                    iv.setPreserveRatio(true);
                    iv.setSmooth(true);
                    container.getChildren().setAll(iv);
                });

            } catch (Exception ex) {
                System.err.println("[ImageLoader] " + ex.getMessage());
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /** High-quality bicubic scaling — preserves ratio, never upscales. */
    private static BufferedImage scale(BufferedImage src, int maxW, int maxH) {
        double sx = (double) maxW / src.getWidth();
        double sy = (double) maxH / src.getHeight();
        double s  = Math.min(sx, sy);
        if (s >= 1.0) return src; // never upscale

        int w = (int)(src.getWidth()  * s);
        int h = (int)(src.getHeight() * s);

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

    /** Convert BufferedImage → JavaFX WritableImage pixel by pixel. */
    private static WritableImage toFX(BufferedImage bi) {
        // Ensure ARGB
        if (bi.getType() != BufferedImage.TYPE_INT_ARGB) {
            BufferedImage argb = new BufferedImage(
                    bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = argb.createGraphics();
            g.drawImage(bi, 0, 0, null);
            g.dispose();
            bi = argb;
        }
        WritableImage wi = new WritableImage(bi.getWidth(), bi.getHeight());
        PixelWriter    pw = wi.getPixelWriter();
        int[] pixels = bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(),
                                 null, 0, bi.getWidth());
        for (int y = 0; y < bi.getHeight(); y++)
            for (int x = 0; x < bi.getWidth(); x++)
                pw.setArgb(x, y, pixels[y * bi.getWidth() + x]);
        return wi;
    }
}