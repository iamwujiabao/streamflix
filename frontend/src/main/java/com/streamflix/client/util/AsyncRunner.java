package com.streamflix.client.util;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Runs blocking work on a background thread and pumps the
 * result back to the JavaFX Application Thread.
 */
public final class AsyncRunner {

    private static final ExecutorService POOL =
            Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "streamflix-async");
                t.setDaemon(true);
                return t;
            });

    private AsyncRunner() {}

    public static <T> void run(Supplier<T> work, Consumer<T> onSuccess) {
        run(work, onSuccess, AsyncRunner::showError);
    }

    public static <T> void run(Supplier<T> work,
                                Consumer<T> onSuccess,
                                Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override protected T call() { return work.get(); }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> onSuccess.accept(task.getValue())));
        task.setOnFailed(e -> Platform.runLater(() -> onError.accept(task.getException())));
        POOL.submit(task);
    }

    public static void showError(Throwable t) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(t.getMessage() != null ? t.getMessage() : t.toString());
        alert.showAndWait();
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("StreamFlix");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void shutdown() { POOL.shutdownNow(); }
}
