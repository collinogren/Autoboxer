/*
    Autoboxer to make creating "boxes" for figure skating competitions easier.
    Copyright (C) 2024 Collin Ogren

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ogren.collin.autoboxer.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ogren.collin.autoboxer.Logging;
import ogren.collin.autoboxer.control.MasterController;
import ogren.collin.autoboxer.pdf.PDFManipulator;
import ogren.collin.autoboxer.process.Schedule;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GUIFXController implements javafx.fxml.Initializable {

    private static double progress = 0.0;
    private static boolean isDone = false;

    private final ArrayList<String> rinkSchedules = new ArrayList<>();

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

    private File boxDirectory;

    @FXML
    private MenuItem openMenu, saveMenu, closeMenu, aboutMenu;

    @FXML
    private Button browseButton, generateButton, six0Button, six0SubButton, six0SSButton, coversheetsButton, judgeButton, techButton;

    @FXML
    private CheckBox generateSSButton, generateSOButton, generateTAButton, removeZerosButton;

    @FXML
    private TextField delimiterField, boxDirectoryField, dayField;

    @FXML
    TabPane tabPane;

    // Do any initialization work such as adding listeners which cannot be added within the FXML file or setting up
    // additional GUI components.
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        newTabButton();
        boxDirectoryField.textProperty().addListener((observable, oldValue, newValue) -> {
            boxDirectory = new File(newValue);
            setDirDependentButtonsDisabled();
        });
    }

    // Setup key bindings.
    public void setup(Scene scene) {
        KeyCombination saveCombo = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(saveCombo, this::saveMenuAction);
    }

    // Handle browsing for a box directory.
    @FXML void browse() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Autoboxer");
        File tempBoxDirectory = directoryChooser.showDialog(generateButton.getScene().getWindow());
        if (tempBoxDirectory != null) {
            boxDirectory = tempBoxDirectory;
        }
        if (boxDirectory != null) {
            if (boxDirectory.exists()) {
                rinkSchedules.clear();
                boxDirectoryField.setText(boxDirectory.getPath());

                if (tabPane.getTabs().size() >= 2) {
                    tabPane.getTabs().removeIf(Tab::isClosable);
                }

                String[] contentsByRink = Schedule.readScheduleFileToString(boxDirectory);
                dayField.setText(contentsByRink[0].trim());

                for (int j = 1; j < contentsByRink.length; j++) {
                    String[] lines = contentsByRink[j].split("\n");
                    String rinkName = "";
                    StringBuilder scheduleText = new StringBuilder();
                    for (int i = 0; i < lines.length; i++) {
                        if (i == 0) {
                            rinkName = lines[i].trim();
                            continue;
                        }

                        scheduleText.append(lines[i]);
                        if (i != lines.length - 1) {
                            scheduleText.append("\n");
                        }
                    }

                    createRinkView(rinkName, scheduleText.toString());
                }
            }
        }
        setDirDependentButtonsDisabled();
    }

    // Called by the generate button.
    @FXML
    private void generate() {
        //generateButton.getScene().getWindow().hide();
        PDFManipulator.setEventNameDelimiter(delimiterField.getText());
        generateBox();
    }

    // Called by the generate schedule sheets checkbox.
    @FXML
    private void generateSS() {
        setGenerateSchedule(generateSSButton.isSelected());
    }

    // Called by the generate starting orders checkbox.
    @FXML
    private void generateSO() {
        setGenerateStartingOrders(generateSOButton.isSelected());
    }

    // Called by the generate TA sheets checkbox.
    @FXML
    private void generateTA() {
        setGenerateTASheets(generateTAButton.isSelected());
    }

    // Called by the remove leading zeros checkbox.
    @FXML
    private void removeLeadingZeros() {
        PDFManipulator.setRemoveLeadingZeros(removeZerosButton.isSelected());
    }

    // Each of the following functions are called by their respective buttons to change print directory.
    @FXML
    private void setClawPrintDirCoversheets() {
        setClawPDFRegVariable(MasterController.COVERSHEET_DIR);
    }

    @FXML
    private void setClawPrintDirJudge() {
        setClawPDFRegVariable(MasterController.JUDGE_SHEETS_DIR);
    }

    @FXML
    private void setClawPrintDirTech() {
        setClawPDFRegVariable(MasterController.TECH_PANEL_DIR);
    }

    @FXML
    private void setClawPrintDirSix0() {
        setClawPDFRegVariable(MasterController.SIX0_PRIMARY_DIR);
    }

    @FXML private void setClawPrintDirSix0Sub() {
        setClawPDFRegVariable(MasterController.SIX0_SUBSEQUENT_DIR);
    }

    @FXML
    private void setClawPrintDirSix0SS() {
        setClawPDFRegVariable(MasterController.SIX0_STARTING_ORDERS_DIR);
    }

    // Called by the browse menu item button.
    @FXML
    private void openMenuAction() {
        browse();
    }

    // Called by the save menu item button.
    @FXML
    private void saveMenuAction() {
        if (boxDirectory != null) {
            if (boxDirectory.exists() && tabPane.getTabs().size() > 2) {
                Schedule.saveSchedule(boxDirectory, dayField.getText(), rinkSchedules);
            }
        }
    }

    // Called by the close menu item button.
    @FXML
    private void closeMenuAction() {
        closeStage(generateButton);
    }

    // Called by the about menu item button.
    @FXML
    private void aboutMenuAction() {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new URI("https://github.com/collinogren/Autoboxer"));
        } catch (IOException | URISyntaxException e) {
            Logging.logger.error(e);
        }
    }

    // Creates a view for editing schedules per rink.
    private void createRinkView(String rinkName, String textContent) {
        Tab tab = new Tab();
        tab.setClosable(true);
        tab.setText(rinkName);
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(5, 5, 5, 5));
        TextField textField = new TextField();
        textField.setText(rinkName);
        textField.setPromptText("Rink Name");

        TextArea textArea = new TextArea();
        textArea.setText(textContent);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        rinkSchedules.add("-R " + rinkName+"\n"+ textContent);
        int index = rinkSchedules.size() - 1;

        textArea.textProperty().addListener((observable, oldValue, newValue) -> rinkSchedules.set(index, "-R "+tab.getText()+"\n"+newValue));

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            tab.setText(newValue);
            rinkSchedules.set(index, "-R "+tab.getText()+"\n"+textArea.getText());
        });

        vbox.getChildren().add(textField);
        vbox.getChildren().add(textArea);

        if (boxDirectory != null) {
            if (boxDirectory.exists()) {
                generateButton.setDisable(false);
                saveMenu.setDisable(false);
            }
        }

        tab.setOnClosed(event -> {
            rinkSchedules.set(index, "");
            if (tabPane.getTabs().size() < 3) {
                saveMenu.setDisable(true);
                generateButton.setDisable(true);
            }
        });

        tab.setContent(vbox);

        tabPane.getTabs().add(tabPane.getTabs().size() - 1, tab);
    }

    // Creates a tab which functions as a button to create a new tab behind it.
    private void newTabButton() {
        Tab addTab = new Tab("+"); // You can replace the text with an icon
        addTab.setClosable(false);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if(newTab == addTab) {
                createRinkView("Rink "+(tabPane.getTabs().size() - 1), ""); // Adding new tab before the "button" tab
                tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2); // Selecting the tab before the button, which is the newly created one
            }
        });

        tabPane.getTabs().add(addTab);
    }

    // Function to set the output directory for ClawPDF by editing the Windows registry.
    private void setClawPDFRegVariable(String endDir) {
        String directory = boxDirectory.getPath().replace("/", "\\").replace("\\", "\\\\") + "\\\\" + endDir;
        try {
            String[] regCommand = {"REG", "ADD", "HKEY_CURRENT_USER\\Software\\clawSoft\\clawPDF\\Settings\\ConversionProfiles\\0\\AutoSave", "/v", "TargetDirectory", "/d", "\""+directory+"\"", "/f"};
            Runtime.getRuntime().exec(regCommand);
        } catch (IOException e) {
            Logging.logger.error(e);
            throw new RuntimeException(e);
        }
    }

    private void setDirDependentButtonsDisabled() {
        try {
            setDirDependentButtonsDisabledDirectly(!boxDirectory.exists());
        } catch (NullPointerException npe) {
            setDirDependentButtonsDisabledDirectly(true);
        }
    }

    private void setDirDependentButtonsDisabledDirectly(boolean b) {
        generateButton.setDisable(b);
        coversheetsButton.setDisable(b);
        judgeButton.setDisable(b);
        techButton.setDisable(b);
        six0Button.setDisable(b);
        six0SubButton.setDisable(b);
        six0SSButton.setDisable(b);
        saveMenu.setDisable(b);
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
                            e.printStackTrace();
                            Logging.logger.fatal(e.getMessage());
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
        } catch (NullPointerException ignored) {} catch (Exception e) {
            Logging.logger.fatal((e));
            throw new RuntimeException(e);
        }
    }

    private static class ProgressGUIFX extends Application {

        @Override
        public void start(Stage primaryStage) {
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

            // Try with resources breaks the progress bar for some reason. \_(-_-)_/ Too bad.
            ExecutorService executor = Executors.newFixedThreadPool(1);
            executor.execute(() -> {
                while (!isDone) {
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
                }

                Platform.runLater(progressStage::close);
            });

            executor.shutdown();
        }
    }
}
