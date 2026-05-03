package com.streamflix.client.ui;

import com.streamflix.client.AppContext;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

/**
 * Main application shell with a left sidebar and a content area.
 */
public class MainView {

    private final Stage     stage;
    private final BorderPane root = new BorderPane();
    private final StackPane  contentArea = new StackPane();

    public MainView(Stage stage) { this.stage = stage; }

    public void show() {
        root.setLeft(buildSidebar());
        root.setTop(buildTopBar());
        root.setCenter(contentArea);
        showHome();

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/styles/streamflix.css").toExternalForm());
        stage.setTitle("StreamFlix — " + AppContext.get().getCurrentUser().username);
        stage.setScene(scene);
    }

    // ------------------------ Sidebar -----------------------------------

    private VBox buildSidebar() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(20, 12, 20, 12));
        box.setMinWidth(220);
        box.getStyleClass().add("sidebar");

        Label brand = new Label("🎬 StreamFlix");
        brand.getStyleClass().add("sidebar-brand");

        Button homeBtn        = sideButton("🏠  Home",          this::showHome);
        Button trendingBtn    = sideButton("🔥  Trending",      this::showTrending);
        Button forYouBtn      = sideButton("✨  For You",       this::showForYou);
        Button searchBtn      = sideButton("🔍  Search/Filter", this::showSearch);
        Button channelsBtn    = sideButton("📺  Channels",      this::showChannels);
        Button playlistsBtn   = sideButton("📋  Playlists",     this::showPlaylists);
        Button historyBtn     = sideButton("⏱️  History",       this::showHistory);
        Button notificationsBtn = sideButton("🔔  Notifications", this::showNotifications);
        Button plansBtn       = sideButton("💎  Subscriptions", this::showPlans);

        box.getChildren().addAll(brand, sep(), homeBtn, trendingBtn, forYouBtn,
                searchBtn, channelsBtn, playlistsBtn, historyBtn, notificationsBtn, plansBtn);
        return box;
    }

    private Button sideButton(String label, Runnable action) {
        Button b = new Button(label);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.getStyleClass().add("side-btn");
        b.setOnAction(e -> action.run());
        return b;
    }

    private Region sep() {
        Region r = new Region();
        r.setPrefHeight(1);
        r.setMaxWidth(Double.MAX_VALUE);
        r.getStyleClass().add("sep");
        VBox.setMargin(r, new Insets(8, 6, 8, 6));
        return r;
    }

    // ------------------------ Top bar -----------------------------------

    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.getStyleClass().add("topbar");

        Label hello = new Label("Hi, " + AppContext.get().getCurrentUser().username);
        hello.getStyleClass().add("hello");

        Button logout = new Button("Sign out");
        logout.getStyleClass().add("link");
        logout.setOnAction(e -> {
            AppContext.get().logout();
            new LoginView(stage).show();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().addAll(spacer, hello, logout);
        return bar;
    }

    // ------------------------ Navigation --------------------------------

    private void replaceContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    private void showHome()          { replaceContent(new HomeView()); }
    private void showTrending()      { replaceContent(new TrendingView()); }
    private void showForYou()        { replaceContent(new RecommendedView()); }
    private void showSearch()        { replaceContent(new SearchView()); }
    private void showChannels()      { replaceContent(new ChannelsView()); }
    private void showPlaylists()     { replaceContent(new PlaylistsView()); }
    private void showHistory()       { replaceContent(new HistoryView()); }
    private void showNotifications() { replaceContent(new NotificationsView()); }
    private void showPlans()         { replaceContent(new PlansView()); }
}
