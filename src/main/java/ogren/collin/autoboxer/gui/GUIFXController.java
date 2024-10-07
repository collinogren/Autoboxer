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

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ogren.collin.autoboxer.Logging;
import ogren.collin.autoboxer.control.MasterController;
import ogren.collin.autoboxer.process.Schedule;
import ogren.collin.autoboxer.utilities.APIUtilities;
import ogren.collin.autoboxer.utilities.Settings;
import ogren.collin.autoboxer.utilities.remote_utilities.RemoteUtilities;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GUIFXController implements javafx.fxml.Initializable {

    private final ArrayList<String> rinkSchedules = new ArrayList<>();

    private File boxDirectory;

    @FXML
    private MenuItem openMenu, closeMenu, documentationMenu, copyrightMenu, versionMenu;

    @FXML
    private RadioMenuItem lightThemeRadioButtonMenu, darkThemeRadioButtonMenu;

    @FXML
    private Button browseButton, generateButton, six0Button, six0SubButton, six0SSButton, coversheetsButton, judgeButton, techButton;

    @FXML
    private CheckBox combinePaperworkButton, generateSSButton, generateSOButton, generateTAButton, removeZerosButton, buildByBoardButton;

    @FXML
    private TextField delimiterField, boxDirectoryField, dayField;

    @FXML
    private TabPane tabPane;

    private Tab addTab;

    public static void viewGithub() {
        RemoteUtilities.browseToURL("https://github.com/collinogren/Autoboxer");
    }

    // Do any initialization work such as adding listeners which cannot be added within the FXML file or setting up
    // additional GUI components.
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Settings.loadSettings();
        versionMenu.setText("Version " + APIUtilities.getAPIVersion());
        combinePaperworkButton.setSelected(Settings.getCombinePaperwork());
        generateSSButton.setSelected(Settings.getGenerateSchedule());
        generateSOButton.setSelected(Settings.getGenerateStartingOrders());
        generateTAButton.setSelected(Settings.getGenerateTASheets());
        removeZerosButton.setSelected(Settings.getRemoveLeadingZeros());
        buildByBoardButton.setSelected(Settings.getBuildByBoard());
        delimiterField.setText(Settings.getEventNameDelimiter());
        delimiterField.textProperty().addListener((observable, oldValue, newValue) -> Settings.setEventNameDelimiter(newValue));
        newTabButton();
        setDirDependentButtonsDisabled();
        boxDirectoryField.textProperty().addListener((observable, oldValue, newValue) -> {
            boxDirectory = new File(newValue);
            setDirDependentButtonsDisabled();
        });

        dayField.textProperty().addListener((observable, oldValue, newValue) -> {
            save();
        });

        ToggleGroup themeGroup = new ToggleGroup();
        darkThemeRadioButtonMenu.setToggleGroup(themeGroup);
        lightThemeRadioButtonMenu.setToggleGroup(themeGroup);

        if (Settings.getTheme().equals(Settings.DARK_THEME)) {
            darkThemeRadioButtonMenu.setSelected(true);
        } else if (Settings.getTheme().equals(Settings.LIGHT_THEME)) {
           lightThemeRadioButtonMenu.setSelected(true);
        }
    }

    // Setup key bindings.
    public void setup(Scene scene) {
        KeyCombination saveCombo = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
        scene.getAccelerators().put(saveCombo, this::save);
    }

    // Handle browsing for a box directory.
    @FXML
    void browse() {
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
        Settings.setEventNameDelimiter(delimiterField.getText());
        generateBox();
    }

    // Called by the combine paperwork checkbox
    @FXML
    private void combinePaperwork() {
        Settings.setCombinePaperwork(combinePaperworkButton.isSelected());
    }

    // Called by the generate schedule sheets checkbox.
    @FXML
    private void generateSS() {
        Settings.setGenerateSchedule(generateSSButton.isSelected());
    }

    // Called by the generate starting orders checkbox.
    @FXML
    private void generateSO() {
        Settings.setGenerateStartingOrders(generateSOButton.isSelected());
    }

    // Called by the generate TA sheets checkbox.
    @FXML
    private void generateTA() {
        Settings.setGenerateTASheets(generateTAButton.isSelected());
    }

    // Called by the remove leading zeros checkbox.
    @FXML
    private void removeLeadingZeros() {
        Settings.setRemoveLeadingZeros(removeZerosButton.isSelected());
    }

    @FXML
    private void buildByBoard() {
        generateSSButton.setDisable(buildByBoardButton.isSelected());
        Settings.setBuildByBoard(buildByBoardButton.isSelected());
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

    @FXML
    private void setClawPrintDirSix0Sub() {
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

    // Called by text property change listeners.
    private void save() {
        if (boxDirectory != null) {
            if (boxDirectory.exists()) {
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
    private void documentationMenuAction() {
        viewGithub();
    }

    @FXML
    private void copyrightMenuAction() {
        try {
            CopyrightFX.start(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void setDarkTheme() {
        Settings.setThemeDark();
        GUIFX.setTheme();
    }

    @FXML
    private void setLightTheme() {
        Settings.setThemeLight();
        GUIFX.setTheme();
    }

    // Creates a view for editing schedules per rink.
    private void createRinkView(String rinkName, String textContent) {
        Tab tab = new Tab();
        tab.setClosable(true);
        tab.setText(rinkName);
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        TextField textField = new TextField();
        textField.setText(rinkName);
        textField.setPromptText("Rink Name");

        TextArea textArea = new TextArea();
        textArea.setText(textContent);
        VBox.setVgrow(textArea, Priority.ALWAYS);

        rinkSchedules.add("-R " + rinkName + "\n" + textContent);
        int index = rinkSchedules.size() - 1;

        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            rinkSchedules.set(index, "-R " + tab.getText() + "\n" + newValue);
            save();
        });

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            tab.setText(newValue);
            rinkSchedules.set(index, "-R " + tab.getText() + "\n" + textArea.getText());
            save();
        });

        vbox.getChildren().add(textField);
        vbox.getChildren().add(textArea);

        // Add quick tools for filling the start and end times when some cells are left blank in the 104.
        HBox autofillTimesHBox = new HBox();
        Pane spacerPane = new Pane();
        HBox.setHgrow(spacerPane, Priority.ALWAYS);
        Button autofillTimes = new Button("Autofill Blank Times...");
        autofillTimes.setOnAction(e -> AutofillTimesFX.start(textArea));
        autofillTimesHBox.setAlignment(Pos.CENTER);
        autofillTimesHBox.getChildren().add(spacerPane);
        autofillTimesHBox.getChildren().add(autofillTimes);

        vbox.getChildren().add(autofillTimesHBox);

        if (boxDirectory != null) {
            if (boxDirectory.exists()) {
                generateButton.setDisable(false);
            }
        }

        tab.setOnClosed(event -> {
            rinkSchedules.set(index, "");
            if (tabPane.getTabs().size() < 3) {
                generateButton.setDisable(true);
            }
            System.out.println(index);
            save();
        });

        tab.setContent(vbox);

        tabPane.getTabs().add(tabPane.getTabs().size() - 1, tab);

        save();
    }

    // Creates a tab which functions as a button to create a new tab behind it.
    private void newTabButton() {
        addTab = new Tab("+");
        addTab.setClosable(false);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == addTab) {
                createRinkView("Rink " + (tabPane.getTabs().size() - 1), ""); // Adding new tab before the "button" tab
                tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2); // Selecting the tab before the button, which is the newly created one
            }
        });

        tabPane.getTabs().add(addTab);
    }

    // Function to set the output directory for ClawPDF by editing the Windows registry.
    private void setClawPDFRegVariable(String endDir) {
        String directory = boxDirectory.getPath().replace("/", "\\").replace("\\", "\\\\") + "\\\\" + endDir;
        try {
            String[] regCommand = {"REG", "ADD", "HKEY_CURRENT_USER\\Software\\clawSoft\\clawPDF\\Settings\\ConversionProfiles\\0\\AutoSave", "/v", "TargetDirectory", "/d", "\"" + directory + "\"", "/f"};
            Runtime.getRuntime().exec(regCommand);
        } catch (IOException e) {
            Logging.logger.error(e);
            throw new RuntimeException(e);
        }
    }

    /*
        This would only work if Autoboxer is run as administrator. I made the decision to suggest keeping direct
        printing turned on instead of doing this because of that.
        The purpose for this function was to disable spooling when printing from Hal which seems to break when printing
        using spooling, but only sometimes. I suspect that this is a Hal bug since I have had it happen printing to a
        Brother laser printer as well as to clawPDF. Regardless, disabling spooling seems to fix the problem and doesn't
        take too much longer.
    */
    @SuppressWarnings("unused")
    private void setClawPDFPrintSpoolingRegVariable(boolean b) {
        String spooling;
        if (b) {
            spooling = "PrintWhileSpooling";
        } else {
            spooling = "PrintDirect";
        }
        try {
            String[] regCommand = {"REG", "ADD", "HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Print\\Printers\\clawPDF\\DsSpooler", "/v", "printSpooling", "/d", "\"" + spooling + "\"", "/f"};
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
        addTab.setDisable(b);
        dayField.setDisable(b);
    }

    private void closeStage(Control control) {
        Stage stage = (Stage) control.getScene().getWindow();
        stage.close();
    }

    private void generateBox() {
        try {
            if (boxDirectory.exists()) {
                new ProgressGUIFX().start();
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
                            stage.setAlwaysOnTop(true);
                            stage.getIcons().add(GUIFX.autoboxerIcon);
                            alert.setTitle("Error");
                            Label text = new Label("Failed to generate the box.\n" + e.getMessage());
                            text.setMinWidth(400);
                            alert.getDialogPane().setContent(text);
                            if (!MasterController.errors.isEmpty()) {
                                ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Show Messages");
                            }
                            e.printStackTrace();
                            Logging.logger.fatal(e.getMessage());
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK && !MasterController.errors.isEmpty()) {
                                new ErrorsFX().start(MasterController.errors);
                            }
                            ProgressGUIFX.setDone(true);
                        });

                        error = true;
                    }

                    if (!error) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                            stage.setAlwaysOnTop(true);
                            stage.getIcons().add(GUIFX.autoboxerIcon);
                            alert.setTitle("Success");
                            alert.setHeaderText("Success");
                            if (MasterController.errors.isEmpty()) {
                                Label text = new Label("Successfully generated the box.\nRemember to select a physical printer when attempting to print.");
                                text.setMinWidth(400);
                                text.setWrapText(true);
                                alert.getDialogPane().setContent(text);
                            } else {
                                Label text = new Label("Successfully generated the box, but errors or warnings are present.\nRemember to select a physical printer when attempting to print.");
                                text.setMinWidth(400);
                                text.setWrapText(true);
                                alert.getDialogPane().setContent(text);
                                ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Show Messages");
                            }

                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK && !MasterController.errors.isEmpty()) {
                                new ErrorsFX().start(MasterController.errors);
                            }
                        });
                    }
                });
                executorService.shutdown();
                closeStage(generateButton);
            }
        } catch (NullPointerException ignored) {
        } catch (Exception e) {
            Logging.logger.fatal((e));
            throw new RuntimeException(e);
        }
    }
}
