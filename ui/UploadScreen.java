package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import models.AppState;
import models.Post;
import storage.Database;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

/**
 * Upload screen.
 * Reads image bytes → encodes to Base64 → stores in Railway DB.
 * Images load on ALL devices since data is in the database.
 */
public class UploadScreen {

    public static BorderPane build(Stage ownerStage) {
        BorderPane root = new BorderPane();
        root.setStyle(Styles.ROOT);

        // ── Top bar ───────────────────────────────────────────────────────────
        Label title = new Label("New Post");
        title.setStyle("-fx-font-size:17px;-fx-font-weight:bold;-fx-text-fill:#262626;");
        HBox.setHgrow(title, Priority.ALWAYS);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(Styles.BTN_LINK);
        cancelBtn.setOnAction(e -> NavigationController.showTab(NavigationController.Tab.PROFILE));

        HBox topBar = new HBox(10, cancelBtn, title);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle(Styles.TOP_BAR);
        root.setTop(topBar);

        // ── Preview ───────────────────────────────────────────────────────────
        StackPane previewPane = new StackPane();
        previewPane.setStyle("-fx-background-color:#efefef;");
        previewPane.setPrefHeight(280);

        Label previewLbl = new Label("📷  Tap 'Choose Photo' to select an image");
        previewLbl.setStyle(Styles.CAPTION + "-fx-font-size:13px;");
        previewLbl.setWrapText(true);
        previewLbl.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        previewPane.getChildren().add(previewLbl);

        ImageView preview = new ImageView();
        preview.setFitWidth(430);
        preview.setFitHeight(280);
        preview.setPreserveRatio(true);
        Rectangle clip = new Rectangle(430, 280);
        preview.setClip(clip);

        // Store chosen file path and its Base64 data
        final File[]   chosenFile  = { null };
        final String[] base64Cache = { "" };

        Label selectedLbl = new Label("No file selected.");
        selectedLbl.setStyle(Styles.CAPTION);

        Label msgLbl = new Label();
        msgLbl.setStyle(Styles.ERROR_MSG);
        msgLbl.setWrapText(true);

        // ── Choose button ─────────────────────────────────────────────────────
        Button chooseBtn = new Button("Choose Photo");
        chooseBtn.setStyle(Styles.BTN_SECONDARY);
        chooseBtn.setMaxWidth(Double.MAX_VALUE);
        chooseBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select a Photo");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"));
            File chosen = chooser.showOpenDialog(ownerStage);
            if (chosen == null) return;

            try {
                // Read all bytes and encode to Base64
                byte[] bytes = Files.readAllBytes(chosen.toPath());
                String b64   = Base64.getEncoder().encodeToString(bytes);

                chosenFile[0]  = chosen;
                base64Cache[0] = b64;

                selectedLbl.setText(chosen.getName() + " (" + (bytes.length / 1024) + " KB)");
                selectedLbl.setStyle(Styles.SECTION_TITLE);

                // Show preview from original file
                Image img = new Image(chosen.toURI().toString(), 430, 280, true, true);
                preview.setImage(img);
                previewPane.getChildren().setAll(preview);
                msgLbl.setText("");

            } catch (Exception ex) {
                msgLbl.setStyle(Styles.ERROR_MSG);
                msgLbl.setText("Could not read file: " + ex.getMessage());
                System.err.println("[Upload] Read error: " + ex.getMessage());
            }
        });

        // ── Share button ──────────────────────────────────────────────────────
        Button shareBtn = new Button("Share ✓");
        shareBtn.setStyle(Styles.BTN_PRIMARY);
        shareBtn.setMaxWidth(Double.MAX_VALUE);
        shareBtn.setOnAction(e -> {
            if (chosenFile[0] == null || base64Cache[0].isEmpty()) {
                msgLbl.setStyle(Styles.ERROR_MSG);
                msgLbl.setText("Please choose a photo first.");
                return;
            }

            String postId    = "P" + AppState.postCounter++;
            String imagePath = chosenFile[0].getAbsolutePath(); // keep original path as reference

            Post newPost = new Post(postId, AppState.currentUser.getUsername(), imagePath);
            newPost.setImageData(base64Cache[0]); // store Base64 in the post

            AppState.postStore.put(postId, newPost);
            AppState.currentUser.addPostId(postId);
            Database.savePost(newPost); // saves both image_url and image_data to Railway

            // Reset form
            chosenFile[0]  = null;
            base64Cache[0] = "";
            selectedLbl.setText("No file selected.");
            selectedLbl.setStyle(Styles.CAPTION);
            previewPane.getChildren().setAll(previewLbl);
            preview.setImage(null);

            msgLbl.setStyle(Styles.SUCCESS_MSG + "-fx-font-size:13px;");
            msgLbl.setText("Posted! ✓");

            // Navigate back to profile after short delay
            new Thread(() -> {
                try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(
                        () -> NavigationController.showTab(NavigationController.Tab.PROFILE));
            }).start();
        });

        VBox body = new VBox(14, previewPane, selectedLbl, chooseBtn, shareBtn, msgLbl);
        body.setPadding(new Insets(20, 24, 20, 24));
        body.setAlignment(Pos.TOP_CENTER);
        root.setCenter(body);

        return root;
    }
}