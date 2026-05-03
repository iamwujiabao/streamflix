package com.streamflix.client.ui;

import com.streamflix.client.AppContext;
import com.streamflix.client.model.AuthResponse;
import com.streamflix.client.util.AsyncRunner;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Standalone login / register window, displayed before the main shell.
 */
public class LoginView {

    private final Stage stage;

    public LoginView(Stage stage) { this.stage = stage; }

    public void show() {
        TabPane tabs = new TabPane();
        tabs.getTabs().add(buildLoginTab());
        tabs.getTabs().add(buildRegisterTab());
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Label title = new Label("🎬 StreamFlix");
        title.getStyleClass().add("title");

        VBox root = new VBox(20, title, tabs);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("login-root");

        Scene scene = new Scene(root, 460, 540);
        scene.getStylesheets().add(getClass().getResource("/styles/streamflix.css").toExternalForm());

        stage.setTitle("StreamFlix — Sign In");
        stage.setScene(scene);
        stage.show();
    }

    private Tab buildLoginTab() {
        TextField username = new TextField();
        username.setPromptText("username");
        PasswordField password = new PasswordField();
        password.setPromptText("password");

        Button submit = new Button("Sign In");
        submit.setDefaultButton(true);
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.getStyleClass().add("primary");

        Label feedback = new Label();
        feedback.getStyleClass().add("feedback");

        submit.setOnAction(e -> {
            String u = username.getText().trim();
            String p = password.getText();
            if (u.isEmpty() || p.isEmpty()) {
                feedback.setText("Please fill in both fields.");
                return;
            }
            submit.setDisable(true);
            feedback.setText("Signing in...");
            AsyncRunner.run(
                    () -> AppContext.get().api().login(u, p),
                    (AuthResponse r) -> {
                        AppContext.get().setCurrentUser(r.user);
                        new MainView(stage).show();
                    },
                    err -> {
                        submit.setDisable(false);
                        feedback.setText(err.getMessage());
                    });
        });

        VBox box = new VBox(12,
                fieldLabel("Username"), username,
                fieldLabel("Password"), password,
                submit, feedback);
        box.setPadding(new Insets(20));

        Tab t = new Tab("Sign In", box);
        return t;
    }

    private Tab buildRegisterTab() {
        TextField username = new TextField(); username.setPromptText("username (3+ chars)");
        TextField email    = new TextField(); email.setPromptText("email");
        PasswordField password = new PasswordField(); password.setPromptText("password (6+ chars)");
        TextField fullName = new TextField(); fullName.setPromptText("full name (optional)");
        TextField country  = new TextField(); country.setPromptText("country (optional)");

        Button submit = new Button("Create Account");
        submit.setMaxWidth(Double.MAX_VALUE);
        submit.getStyleClass().add("primary");

        Label feedback = new Label();
        feedback.getStyleClass().add("feedback");

        submit.setOnAction(e -> {
            if (username.getText().trim().length() < 3
                    || email.getText().trim().isEmpty()
                    || password.getText().length() < 6) {
                feedback.setText("Username 3+, valid email, password 6+ chars.");
                return;
            }
            submit.setDisable(true);
            feedback.setText("Creating account...");
            AsyncRunner.run(
                    () -> AppContext.get().api().register(
                            username.getText().trim(), email.getText().trim(),
                            password.getText(),
                            fullName.getText().trim(), country.getText().trim()),
                    (AuthResponse r) -> {
                        AppContext.get().setCurrentUser(r.user);
                        new MainView(stage).show();
                    },
                    err -> {
                        submit.setDisable(false);
                        feedback.setText(err.getMessage());
                    });
        });

        VBox box = new VBox(10,
                fieldLabel("Username"),  username,
                fieldLabel("Email"),     email,
                fieldLabel("Password"),  password,
                fieldLabel("Full name"), fullName,
                fieldLabel("Country"),   country,
                submit, feedback);
        box.setPadding(new Insets(20));
        return new Tab("Register", box);
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("field-label");
        return l;
    }
}
