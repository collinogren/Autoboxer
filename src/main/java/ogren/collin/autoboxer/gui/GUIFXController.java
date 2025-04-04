/*
    Autoboxer to make creating "boxes" for figure skating competitions easier.
    Copyright (C) 2025 Collin Ogren

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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import ogren.collin.autoboxer.Logging;
import ogren.collin.autoboxer.PrinterUtils;
import ogren.collin.autoboxer.control.MasterController;
import ogren.collin.autoboxer.process.Schedule;
import ogren.collin.autoboxer.utilities.APIUtilities;
import ogren.collin.autoboxer.utilities.Settings;
import ogren.collin.autoboxer.utilities.remote_utilities.RemoteUtilities;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.awt.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GUIFXController implements javafx.fxml.Initializable {

    private boolean isDelimiterValid = false;

    private final ArrayList<String> rinkSchedules = new ArrayList<>();

    private File boxDirectory;

    @FXML
    public static AnchorPane basePane;

    @FXML
    private MenuItem openMenu, reopenMenu, closeMenu, documentationMenu, copyrightMenu, versionMenu, viewManualMenu, clawPDFDefaultMenu, openPrintersScannersMenu, defaultPrinterMenu;

    @FXML
    private RadioMenuItem lightThemeRadioButtonMenu, darkThemeRadioButtonMenu;

    @FXML
    private Button browseButton, generateButton, six0Button, six0SubButton, six0SSButton, coversheetsButton, judgeButton, techButton, openFolderButton;

    @FXML
    private CheckBox combinePaperworkButton, generateSSButton, generateSOButton, generateTAButton, buildByBoardButton, combineRinksByTimeButton;

    @FXML
    private TextField delimiterField, boxDirectoryField, dayField;

    @FXML
    private TabPane tabPane;

    @FXML
    private Text instructionLabel;

    private Tab addTab;

    public static void viewGithub() {
        RemoteUtilities.browseToURL("https://github.com/collinogren/Autoboxer");
    }

    // Do any initialization work such as adding listeners which cannot be added within the FXML file or setting up
    // additional GUI components.
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Settings.loadSettings();

        isDelimiterValid = isDelimiterValid();

        if (Settings.getLastBox().isEmpty() || !new File(Settings.getLastBox()).exists()) {
            reopenMenu.setDisable(true);
        }
        versionMenu.setText("Version " + APIUtilities.getAPIVersion());
        combinePaperworkButton.setSelected(Settings.getCombinePaperwork());
        combineRinksByTimeButton.setSelected(Settings.getCombineRinksByTime());
        generateSSButton.setSelected(Settings.getGenerateSchedule());
        generateSOButton.setSelected(Settings.getGenerateStartingOrders());
        generateTAButton.setSelected(Settings.getGenerateTASheets());
        buildByBoardButton.setSelected(Settings.getBuildByBoard());
        delimiterField.setText(Settings.getEventNameDelimiter());
        delimiterField.textProperty().addListener((observable, oldValue, newValue) -> {
            Settings.setEventNameDelimiter(newValue);
            isDelimiterValid = !Settings.getEventNameDelimiter().isEmpty();
            for (char c : Settings.getEventNameDelimiter().toCharArray()) {
                if (Character.isDigit(c) || Character.isLetter(c)) {
                    isDelimiterValid = false;
                    break;
                }
            }
            setGenerateButtonDisabledDirectly(!isDelimiterValid || boxDirectory == null || !boxDirectory.exists());
            updateInstructionLabel();
        });

        newTabButton();
        setDirDependentButtonsDisabled();
        boxDirectoryField.textProperty().addListener((observable, oldValue, newValue) -> {
            File newDirectory = new File(boxDirectoryField.getText().trim());
            if (newDirectory.exists()) {
                Settings.setLastBox(newValue);
                reopenMenu.setDisable(false);
                openDirectory(newDirectory);
            } else {
                if (tabPane.getTabs().size() >= 2) {
                    tabPane.getTabs().removeIf(Tab::isClosable);
                }
                boxDirectory = null;
                dayField.clear();
            }

            setDirDependentButtonsDisabled();
            updateInstructionLabel();
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
    private void browse() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Autoboxer");
        File tempBoxDirectory = directoryChooser.showDialog(generateButton.getScene().getWindow());
        if (tempBoxDirectory != null && tempBoxDirectory.exists()) {
            boxDirectoryField.setText(tempBoxDirectory.getPath());
        }
    }

    private void openDirectory(File directory) {
        if (directory != null) {
            boxDirectory = directory;
        }
        if (boxDirectory != null) {
            if (boxDirectory.exists()) {
                rinkSchedules.clear();

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

    @FXML
    private void combineRinksByTime() {
        Settings.setCombineRinksByTime(combineRinksByTimeButton.isSelected());
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

    @FXML
    private void buildByBoard() {
        generateSSButton.setDisable(buildByBoardButton.isSelected());
        combineRinksByTimeButton.setDisable(buildByBoardButton.isSelected());
        Settings.setBuildByBoard(buildByBoardButton.isSelected());
    }

    // Each of the following functions are called by their respective buttons to change print directory.
    @FXML
    private void setClawPrintDirCoversheets() {
        PrinterUtils.setClawPDFRegVariable(MasterController.COVERSHEET_DIR, boxDirectory);
    }

    @FXML
    private void setClawPrintDirJudge() {
        PrinterUtils.setClawPDFRegVariable(MasterController.JUDGE_SHEETS_DIR, boxDirectory);
    }

    @FXML
    private void setClawPrintDirTech() {
        PrinterUtils.setClawPDFRegVariable(MasterController.TECH_PANEL_DIR, boxDirectory);
    }

    @FXML
    private void setClawPrintDirSix0() {
        PrinterUtils.setClawPDFRegVariable(MasterController.SIX0_PRIMARY_DIR, boxDirectory);
    }

    @FXML
    private void setClawPrintDirSix0Sub() {
        PrinterUtils.setClawPDFRegVariable(MasterController.SIX0_SUBSEQUENT_DIR, boxDirectory);
    }

    @FXML
    private void setClawPrintDirSix0SS() {
        PrinterUtils.setClawPDFRegVariable(MasterController.SIX0_STARTING_ORDERS_DIR, boxDirectory);
    }

    // Called by the browse menu item button.
    @FXML
    private void openMenuAction() {
        browse();
    }

    @FXML
    private void reopenMenuAction() {
        boxDirectoryField.setText(Settings.getLastBox());
    }

    @FXML
    private void printersMenuAction() {
        defaultPrinterMenu.setText("Default Printer: " + PrinterUtils.getDefaultPrinter());
    }

    @FXML
    private void clawPDFDefaultMenuAction() {
        PrinterUtils.setDefaultPrinterToClawPDF();
    }

    @FXML
    private void openPrintersScannersMenuAction() {
        PrinterUtils.openPrintersScannersUtility();
    }

    // Called by text property change listeners.
    private void save() {
        if (boxDirectory != null) {
            if (boxDirectory.exists()) {
                Schedule.saveSchedule(boxDirectory, dayField.getText(), rinkSchedules);
            }
        }

        updateInstructionLabel();
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
    private void viewManualMenuAction() {
        RemoteUtilities.browseToURL("https://docs.google.com/document/d/1ac6aEsoojl9tfn0mypAnh7wRJRw1rJGbG6PHm9zuZ-M/edit?usp=sharing");
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

    @FXML
    private void openFolder() {
        try {
            Desktop.getDesktop().open(boxDirectory);
        } catch (IOException e) {
            System.err.println("Could not open directory.");
        }
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
        addTab.setTooltip(new Tooltip("Click to add a new rink schedule."));
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == addTab) {
                createRinkView("Rink " + (tabPane.getTabs().size() - 1), ""); // Adding new tab before the "button" tab
                tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2); // Selecting the tab before the button, which is the newly created one
            }
        });

        tabPane.getTabs().add(addTab);
    }

    /*
        This would only work if Autoboxer is run as administrator. I made the decision to suggest keeping direct
        printing turned on instead of doing this because of that.
        The purpose for this function was to disable spooling when printing from Hal which seems to break when printing
        using spooling, but only sometimes. I suspect that this is a Hal bug since I have had it happen printing to a
        Brother laser printer as well as to clawPDF. Regardless, disabling spooling seems to fix the problem and doesn't
        take too much longer.
    */
    // 12/30/2024 Tested this with an elevated C++ program and this method alone doesn't seem to be enough.
    // Shelving this idea for now.
    @SuppressWarnings("unused")
    private void setClawPDFPrintSpoolingRegVariable(boolean b) {
        String spooling;
        if (b) {
            spooling = "PrintWhileSpooling";
        } else {
            spooling = "PrintDirect";
        }
        try {
            // Mutilated command array. Needs to be re-separated.
            String[] regCommand = {"REG ADD HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Print\\Printers\\clawPDF\\DsSpooler /v printSpooling /d \"" + spooling + "\" /f"};
            Runtime.getRuntime().exec(regCommand);
        } catch (IOException e) {
            Logging.logger.error(e);
            throw new RuntimeException(e);
        }
    }

    private void setDirDependentButtonsDisabled() {
        try {
            setDirDependentButtonsDisabledDirectly(!boxDirectory.exists());
            setGenerateButtonDisabledDirectly(!isDelimiterValid || !boxDirectory.exists());
        } catch (NullPointerException npe) {
            setDirDependentButtonsDisabledDirectly(true);
        }
    }

    private void setDirDependentButtonsDisabledDirectly(boolean b) {
        coversheetsButton.setDisable(b);
        judgeButton.setDisable(b);
        techButton.setDisable(b);
        six0Button.setDisable(b);
        six0SubButton.setDisable(b);
        six0SSButton.setDisable(b);
        addTab.setDisable(b);
        dayField.setDisable(b);
        openFolderButton.setDisable(b);
    }

    private void setGenerateButtonDisabledDirectly(boolean b) {
        generateButton.setDisable(b);
    }

    private void closeStage(Control control) {
        Stage stage = (Stage) control.getScene().getWindow();
        stage.close();
    }

    private void generateBox() {
        try {
            if (boxDirectory.exists()) {
                new ProgressGUIFX().start();
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                    boolean error = false;
                    MasterController masterController = new MasterController(boxDirectory.getPath());
                    try {
                        masterController.begin();
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
                            if (!masterController.getErrors().isEmpty()) {
                                ((Button) alert.getDialogPane().lookupButton(ButtonType.OK)).setText("Show Messages");
                            }
                            e.printStackTrace();
                            Logging.logger.fatal(e.getMessage());
                            Optional<ButtonType> result = alert.showAndWait();
                            if (result.isPresent() && result.get() == ButtonType.OK && !masterController.getErrors().isEmpty()) {
                                new ErrorsFX().start(masterController.getErrors());
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
                            if (masterController.getErrors().isEmpty()) {
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
                            if (result.isPresent() && result.get() == ButtonType.OK && !masterController.getErrors().isEmpty()) {
                                new ErrorsFX().start(masterController.getErrors());
                            }
                        });
                    }
                });
                executorService.shutdown();
                GUIFX.primaryStage.hide();
            }
        } catch (NullPointerException ignored) {
        } catch (Exception e) {
            Logging.logger.fatal((e));
            throw new RuntimeException(e);
        }
    }

    private boolean isDelimiterValid() {
        return !Settings.getEventNameDelimiter().isEmpty();
    }

    private void updateInstructionLabel() {
        instructionLabel.setFill(Color.RED);
        if (boxDirectory == null || !boxDirectory.exists()) {
            instructionLabel.setText("Select a box folder with the \"browse\" button.");
            return;
        }

        if (!isDelimiterValid) {
            instructionLabel.setText("Enter a valid event number delimiter. No letters or numbers.");
            return;
        }

        if (tabPane.getTabs().size() <= 2 || !new File(boxDirectory.getPath() + "/schedule.txt").exists()) {
            instructionLabel.setText("Create rink schedules using the \"+\" button above.");
            return;
        }

        try {
            Schedule schedule = new Schedule(new File(boxDirectory.getPath() + "/schedule.txt"));
            if (schedule.getElements().isEmpty()) {
                instructionLabel.setText("No events listed on the rink schedule(s).");
                return;
            }
        } catch (Exception e) {
            instructionLabel.setText("Bad formatting in a rink schedule.");
            return;
        }

        instructionLabel.setFill(Color.GREEN);
        instructionLabel.setText("Ready to generate the box if all paperwork PDFs are in place.");
    }
}
