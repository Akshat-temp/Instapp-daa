package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import main.AppState;
import models.User;
import storage.Database;

/**
 * Sign-up / registration screen.
 */
public class SignupScreen {

    public static VBox build() {
        Label title = new Label("Create Account");
        title.setStyle(Styles.LOGO + "-fx-font-size:26px;");

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setStyle(Styles.INPUT);
        userField.setMaxWidth(Double.MAX_VALUE);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setStyle(Styles.INPUT);
        passField.setMaxWidth(Double.MAX_VALUE);

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");
        confirmField.setStyle(Styles.INPUT);
        confirmField.setMaxWidth(Double.MAX_VALUE);

        Label msg = new Label();
        msg.setStyle(Styles.ERROR_MSG);
        msg.setWrapText(true);

        Button signupBtn = new Button("Sign Up");
        signupBtn.setStyle(Styles.BTN_PRIMARY);
        signupBtn.setMaxWidth(Double.MAX_VALUE);
        signupBtn.setOnAction(e -> {
            String u  = userField.getText().trim();
            String pw = passField.getText();
            String cp = confirmField.getText();

            if (u.isEmpty() || pw.isEmpty()) {
                err(msg, "Please fill in all fields.");
                return;
            }
            if (u.length() < 3) {
                err(msg, "Username must be at least 3 characters.");
                return;
            }
            if (!u.matches("[a-zA-Z0-9._]+")) {
                err(msg, "Username can only contain letters, numbers, dots, underscores.");
                return;
            }
            if (AppState.userStore.containsKey(u)) {
                err(msg, "Username already taken.");
                return;
            }
            if (pw.length() < 4) {
                err(msg, "Password must be at least 4 characters.");
                return;
            }
            if (!pw.equals(cp)) {
                err(msg, "Passwords do not match.");
                return;
            }

            // Create account
            User newUser = new User(u, pw);
            AppState.userStore.put(u, newUser);
            AppState.trie.insert(u);
            AppState.graph.addUser(u);
            Database.saveUser(newUser);

            msg.setStyle(Styles.SUCCESS_MSG);
            msg.setText("Account created! Please log in.");
            userField.clear();
            passField.clear();
            confirmField.clear();
        });

        Button backBtn = new Button("← Back");
        backBtn.setStyle(Styles.BTN_LINK);
        

        VBox root = new VBox(14, title, spacer(10),
                userField, passField, confirmField,
                spacer(4), signupBtn, msg, spacer(4), backBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(70, 48, 70, 48));
        root.setStyle(Styles.ROOT);
        return root;
    }

    private static void err(Label lbl, String text) {
        lbl.setStyle(Styles.ERROR_MSG);
        lbl.setText(text);
    }

    private static javafx.scene.layout.Region spacer(double h) {
        javafx.scene.layout.Region r = new javafx.scene.layout.Region();
        r.setPrefHeight(h);
        return r;
    }
}
