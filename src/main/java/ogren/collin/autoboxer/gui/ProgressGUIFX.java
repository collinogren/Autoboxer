package ogren.collin.autoboxer.gui;


import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ogren.collin.autoboxer.Logging;

import javax.swing.*;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProgressGUIFX {

    private static double progress = 0.0;
    private static boolean isDone = false;

    public static void setProgress(double d) {
        progress = d;
    }

    public static void addProgress(double d) {
        progress += d;
    }

    public static void setDone(boolean b) {
        isDone = b;
    }

    public void start() {
        Stage progressStage = new Stage();
        progressStage.setTitle("Box Progress");
        progressStage.getIcons().add(GUIFX.autoboxerIcon);
        AnchorPane anchorPane = new AnchorPane();
        ProgressBar progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setMaxHeight(Double.MAX_VALUE);
        Text progressText = new Text(progress+"%");
        StackPane stackPane = new StackPane();
        stackPane.getChildren().setAll(progressBar, progressText);
        AnchorPane.setTopAnchor(stackPane, 0.0);
        AnchorPane.setBottomAnchor(stackPane, 0.0);
        AnchorPane.setLeftAnchor(stackPane, 0.0);
        AnchorPane.setRightAnchor(stackPane, 0.0);
        anchorPane.getChildren().add(stackPane);
        Scene progressScene = new Scene(anchorPane, 275, 125);
        progressStage.setScene(progressScene);
        progressStage.show();

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            executor.execute(() -> {
                int period = 1000 / 60;
                Timer timer = new Timer();
                timer.schedule(new BoxTask(progressBar, progressText, timer, executor, progressStage), 0, period);
            });
        }
    }

    private static class BoxTask extends TimerTask {

        private final ProgressBar progressBar;
        private final Text progressText;
        private final Timer timer;
        private final ExecutorService executorService;
        private final Stage progressStage;

        private BoxTask(ProgressBar progressBar, Text progressText, Timer timer, ExecutorService executorService, Stage progressStage) {
            this.progressBar = progressBar;
            this.progressText = progressText;
            this.timer = timer;
            this.executorService = executorService;
            this.progressStage = progressStage;
        }

        @Override
        public void run() {
            try {
                // This should be replaced with something other than busy waiting in the future but it is not
                // a huge concern.
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Logging.logger.fatal(Arrays.toString(e.getStackTrace()));
                throw new RuntimeException(e);
            }
            Platform.runLater(() -> {
                progressBar.setProgress(progress);
                progressText.setText(Math.round(progress * 100.0)+"%");
            });

            if (isDone) {
                timer.cancel();
                executorService.shutdown();
                Platform.runLater(progressStage::close);
            }
        }
    }
}
