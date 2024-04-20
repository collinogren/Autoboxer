package ogren.collin.autoboxer.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ogren.collin.autoboxer.control.MasterController;
import ogren.collin.autoboxer.pdf.PDFManipulator;

import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GUIFXController implements javafx.fxml.Initializable {

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

    private static boolean generateSchedule = true;
    private static boolean generateStartingOrders = true;
    private static boolean generateTASheets = true;

    @FXML
    private Button cancelButton, generateButton;

    @FXML
    private CheckBox generateSSButton, generateSOButton, generateTAButton, removeZerosButton;

    @FXML
    private TextField delimiterField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    }

    @FXML
    private void cancel() {
        closeStage(cancelButton);
    }

    @FXML
    private void generate() {
        //generateButton.getScene().getWindow().hide();
        PDFManipulator.setEventNameDelimiter(delimiterField.getText());
        generateBox();
    }

    @FXML
    private void generateSS() {
        setGenerateSchedule(generateSSButton.isSelected());
    }

    @FXML
    private void generateSO() {
        setGenerateStartingOrders(generateSOButton.isSelected());
    }

    @FXML
    private void generateTA() {
        setGenerateTASheets(generateTAButton.isSelected());
    }

    @FXML
    private void removeLeadingZeros() {
        PDFManipulator.setRemoveLeadingZeros(removeZerosButton.isSelected());
    }

    public static void setGenerateSchedule(boolean b) {
        generateSchedule = b;
    }

    public static boolean getGenerateSchedule() {
        return generateSchedule;
    }

    public static void setGenerateStartingOrders(boolean b) {
        generateStartingOrders = b;
    }

    public static boolean getGenerateStartingOrders() {
        return generateStartingOrders;
    }

    public static void setGenerateTASheets(boolean b) {
        generateTASheets = b;
    }

    public static boolean getGenerateTASheets() {
        return generateTASheets;
    }

    private void closeStage(Control control) {
        Stage stage = (Stage) control.getScene().getWindow();
        stage.close();
    }

    private void generateBox() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Autoboxer");
        File boxDirectory = directoryChooser.showDialog(generateButton.getScene().getWindow());
        try {
            if (boxDirectory.exists()) {
                new ProgressGUIFX().start((Stage) generateButton.getScene().getWindow());
                ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);
                executorService.execute(() -> {
                    boolean error = false;
                    try {
                    MasterController mc = new MasterController(boxDirectory.getPath());
                    mc.begin();
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                            stage.getIcons().add(GUIFX.autoboxerIcon);
                            alert.setTitle("Error");
                            alert.setContentText("Failed to generate the box.\n"+e.getMessage());
                            alert.show();
                            isDone = true;
                        });

                        error = true;
                    }

                    if (!error) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                            stage.getIcons().add(GUIFX.autoboxerIcon);
                            alert.setTitle("Success");
                            alert.setHeaderText("Success");
                            alert.setContentText("Successfully generated the box.");
                            alert.show();
                        });
                    }
                });
                executorService.shutdown();
                closeStage(generateButton);
            }
        } catch (NullPointerException npe) {} catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class ProgressGUIFX extends Application {

        @Override
        public void start(Stage primaryStage) throws Exception {
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

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                while (!isDone) {
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Platform.runLater(() -> {
                        progressBar.setProgress(progress);
                        progressText.setText(Math.round(progress * 100.0)+"%");
                    });
                }

                Platform.runLater(progressStage::close);
            });

            executor.shutdown();
        }
    }
}
