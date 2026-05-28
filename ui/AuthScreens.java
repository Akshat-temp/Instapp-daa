package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import models.AppState;
import models.User;
import storage.Database;


public class AuthScreens {

    
    public static BorderPane buildLogin() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");

        
        VBox center = new VBox();
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(60, 40, 20, 40));
        center.setSpacing(0);

        
        Label logo = new Label("Instapp");
        logo.setStyle(
            "-fx-font-size: 52px;" +
            "-fx-font-family: 'Segoe Script';" +
            "-fx-text-fill: #262626;"
        );
        logo.setPadding(new Insets(0, 0, 36, 0));

        // Username field
        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setStyle(inputStyle());
        userField.setMaxWidth(Double.MAX_VALUE);

        spacerNode(center, 0);

        // Password field row (with show/hide toggle)
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setStyle(inputStyle());
        passField.setMaxWidth(Double.MAX_VALUE);

        TextField passVisible = new TextField();
        passVisible.setPromptText("Password");
        passVisible.setStyle(inputStyle());
        passVisible.setMaxWidth(Double.MAX_VALUE);
        passVisible.setManaged(false);
        passVisible.setVisible(false);

        Button eyeBtn = new Button("👁");
        eyeBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 0 10 0 0;"
        );
        eyeBtn.setOnAction(e -> {
            boolean showing = passVisible.isVisible();
            if (showing) {
                passField.setText(passVisible.getText());
                passField.setVisible(true);   passField.setManaged(true);
                passVisible.setVisible(false); passVisible.setManaged(false);
            } else {
                passVisible.setText(new String(passField.getText()));
                passVisible.setVisible(true);  passVisible.setManaged(true);
                passField.setVisible(false);   passField.setManaged(false);
            }
        });

        StackPane passRow = new StackPane();
        passRow.setAlignment(Pos.CENTER_RIGHT);
        passRow.getChildren().addAll(passField, passVisible, eyeBtn);

        
        Label forgot = new Label("Forgot password?");
        forgot.setStyle(
            "-fx-text-fill: #3897f0;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;"
        );
        forgot.setMaxWidth(Double.MAX_VALUE);
        forgot.setAlignment(Pos.CENTER_RIGHT);

        
        Label msg = new Label();
        msg.setStyle("-fx-text-fill: #ed4956; -fx-font-size: 12px;");
        msg.setWrapText(true);
        msg.setTextAlignment(TextAlignment.CENTER);

        Button loginBtn = new Button("Log In");
        loginBtn.setStyle(
            "-fx-background-color: #4fb3f6;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 12 0 12 0;"
        );
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setDefaultButton(true);
        loginBtn.setOnAction(e -> {
            String u  = userField.getText().trim();
            String pw = passField.isVisible()
                    ? passField.getText()
                    : passVisible.getText();
            User found = AppState.userStore.get(u);
            if (found == null || !found.getPassword().equals(pw)) {
                msg.setText("Wrong username or password.");
                return;
            }
            AppState.currentUser = found;
            userField.clear(); passField.clear(); passVisible.clear();
            msg.setText("");
            NavigationController.enterApp();
        });

        // OR divider
        HBox orRow = buildOrDivider();

        // Sign Up link row
        HBox signupRow = new HBox(4);
        signupRow.setAlignment(Pos.CENTER);
        Label noAcc = new Label("Don't have an account?");
        noAcc.setStyle("-fx-text-fill: #8e8e8e; -fx-font-size: 13px;");
        Button signupLink = new Button("Sign Up.");
        signupLink.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #3897f0;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0;"
        );
        signupLink.setOnAction(e -> NavigationController.showSignup());
        signupRow.getChildren().addAll(noAcc, signupLink);

       
        center.getChildren().addAll(
            logo,
            userField,  gap(10),
            passRow,    gap(6),
            forgot,     gap(16),
            msg,
            loginBtn,   gap(20),
            orRow,      gap(20),
            signupRow
        );

        root.setCenter(center);

        
        Label fromGehu = new Label("from GEHU");
        fromGehu.setStyle(
            "-fx-text-fill: #c7c7c7;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );
        fromGehu.setMaxWidth(Double.MAX_VALUE);
        fromGehu.setAlignment(Pos.CENTER);
        fromGehu.setPadding(new Insets(0, 0, 20, 0));
        root.setBottom(fromGehu);

        return root;
    }

    
    public static BorderPane buildSignup() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");

        VBox center = new VBox();
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(60, 40, 20, 40));
        center.setSpacing(0);

        Label logo = new Label("Instapp");
        logo.setStyle(
            "-fx-font-size: 46px;" +
            "-fx-font-family: 'Segoe Script';" +
            "-fx-text-fill: #262626;"
        );
        logo.setPadding(new Insets(0, 0, 8, 0));

        Label sub = new Label("Sign up to see photos from your friends.");
        sub.setStyle("-fx-text-fill: #8e8e8e; -fx-font-size: 13px;");
        sub.setWrapText(true);
        sub.setTextAlignment(TextAlignment.CENTER);
        sub.setPadding(new Insets(0, 0, 24, 0));

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setStyle(inputStyle());
        userField.setMaxWidth(Double.MAX_VALUE);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setStyle(inputStyle());
        passField.setMaxWidth(Double.MAX_VALUE);

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");
        confirmField.setStyle(inputStyle());
        confirmField.setMaxWidth(Double.MAX_VALUE);

        Label msg = new Label();
        msg.setStyle("-fx-text-fill: #ed4956; -fx-font-size: 12px;");
        msg.setWrapText(true);
        msg.setTextAlignment(TextAlignment.CENTER);

        Button signupBtn = new Button("Sign Up");
        signupBtn.setStyle(
            "-fx-background-color: #4fb3f6;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 12 0 12 0;"
        );
        signupBtn.setMaxWidth(Double.MAX_VALUE);
        signupBtn.setOnAction(e -> {
            String u  = userField.getText().trim();
            String pw = passField.getText();
            String cp = confirmField.getText();

            if (u.isEmpty() || pw.isEmpty()) {
                err(msg, "Please fill in all fields."); return;
            }
            if (u.length() < 3) {
                err(msg, "Username must be at least 3 characters."); return;
            }
            if (!u.matches("[a-zA-Z0-9._]+")) {
                err(msg, "Only letters, numbers, dots and underscores."); return;
            }
            if (AppState.userStore.containsKey(u)) {
                err(msg, "Username already taken."); return;
            }
            if (pw.length() < 4) {
                err(msg, "Password must be at least 4 characters."); return;
            }
            if (!pw.equals(cp)) {
                err(msg, "Passwords do not match."); return;
            }

            User newUser = new User(u, pw);
            AppState.userStore.put(u, newUser);
            AppState.trie.insert(u);
            AppState.graph.addUser(u);
            Database.saveUser(newUser);

            userField.clear(); passField.clear(); confirmField.clear();
            
            new Thread(() -> {
                try { Thread.sleep(800); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> NavigationController.showLogin());
            }).start();
        });

      
        HBox orRow = buildOrDivider();

        HBox loginRow = new HBox(4);
        loginRow.setAlignment(Pos.CENTER);
        Label hasAcc = new Label("Already have an account?");
        hasAcc.setStyle("-fx-text-fill: #8e8e8e; -fx-font-size: 13px;");
        Button loginLink = new Button("Log In.");
        loginLink.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #3897f0;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0;"
        );
        loginLink.setOnAction(e -> NavigationController.showLogin());
        loginRow.getChildren().addAll(hasAcc, loginLink);

        center.getChildren().addAll(
            logo, sub,
            userField,  gap(10),
            passField,  gap(10),
            confirmField, gap(14),
            msg,        gap(4),
            signupBtn,  gap(20),
            orRow,      gap(20),
            loginRow
        );

        root.setCenter(center);

        Label fromGehu = new Label("from GEHU");
        fromGehu.setStyle(
            "-fx-text-fill: #c7c7c7;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;"
        );
        fromGehu.setMaxWidth(Double.MAX_VALUE);
        fromGehu.setAlignment(Pos.CENTER);
        fromGehu.setPadding(new Insets(0, 0, 20, 0));
        root.setBottom(fromGehu);

        return root;
    }

    

    private static String inputStyle() {
        return
            "-fx-background-color: #fafafa;" +
            "-fx-border-color: #dbdbdb;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 12 14 12 14;" +
            "-fx-text-fill: #262626;";
    }

    private static HBox buildOrDivider() {
        Region left  = new Region(); HBox.setHgrow(left,  Priority.ALWAYS);
        left.setStyle("-fx-background-color: #dbdbdb;"); left.setMaxHeight(1); left.setPrefHeight(1);
        Region right = new Region(); HBox.setHgrow(right, Priority.ALWAYS);
        right.setStyle("-fx-background-color: #dbdbdb;"); right.setMaxHeight(1); right.setPrefHeight(1);
        Label orLbl = new Label("  OR  ");
        orLbl.setStyle("-fx-text-fill: #8e8e8e; -fx-font-size: 12px; -fx-font-weight: bold;");
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER);
        row.getChildren().addAll(left, orLbl, right);
        return row;
    }

    private static Region gap(double h) {
        Region r = new Region(); r.setPrefHeight(h); return r;
    }

    private static void spacerNode(VBox vbox, double h) {
        Region r = new Region(); r.setPrefHeight(h); vbox.getChildren().add(r);
    }

    private static void err(Label lbl, String text) {
        lbl.setStyle("-fx-text-fill: #ed4956; -fx-font-size: 12px;");
        lbl.setText(text);
    }
}
