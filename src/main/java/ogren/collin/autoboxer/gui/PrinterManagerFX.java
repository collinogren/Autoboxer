package ogren.collin.autoboxer.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ogren.collin.autoboxer.PrinterUtils;

import javax.print.PrintService;
import java.util.HashMap;

// Work in progress.
public class PrinterManagerFX {

    private static final String UNUSED = "Unused";
    private static final String DEFAULT_PHYSICAL = "Default Physical";
    private static final String CLAWPDF = "clawPDF";

    public static void start(boolean startupScreen) throws Exception {
        Stage printerManagerStage = new Stage();
        printerManagerStage.setResizable(false);
        printerManagerStage.setTitle("Autoboxer Printer Manager");
        printerManagerStage.getIcons().add(GUIFX.autoboxerIcon);
        printerManagerStage.setAlwaysOnTop(true);
        BorderPane root = new BorderPane();
        VBox vbox = new VBox();
        HBox printerManagerHBox = new HBox();
        VBox printerManagerVBox = new VBox();
        printerManagerVBox.setMinHeight(108);
        Text printerManagerTitleText = new Text("");
        printerManagerVBox.getChildren().add(printerManagerTitleText);
        HBox.setHgrow(printerManagerVBox, Priority.ALWAYS);
        printerManagerHBox.getChildren().addAll(printerManagerVBox);
        vbox.getChildren().add(printerManagerHBox);

        ScrollPane scrollPane = new ScrollPane();

        VBox printersVBox = new VBox();
        for (PrintService printService : PrinterUtils.getAllPrinters()) {

        }

        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        // scrollPane.setContent(licenseText);
        Separator separator1 = new Separator();
        separator1.setPadding(Insets.EMPTY);
        separator1.setPrefHeight(1);
        Separator separator2 = new Separator();
        separator2.setPadding(Insets.EMPTY);
        vbox.getChildren().addAll(separator1, scrollPane, separator2);

        String buttonText = "Continue to Autoboxer";
        if (!startupScreen) {
            buttonText = "Close";
        }

        Button proceedButton = new Button(buttonText);
        proceedButton.setMinWidth(166.4);
        HBox proceedHBox = new HBox();
        proceedHBox.setSpacing(10);
        Pane buttonSpacer = new Pane();
        HBox.setHgrow(buttonSpacer, Priority.ALWAYS);
        Label noticeLabel = new Label("This notice will not appear again until you update Autoboxer");
        noticeLabel.setAlignment(Pos.CENTER_LEFT);
        proceedHBox.setAlignment(Pos.CENTER);
        if (startupScreen) {
            proceedHBox.getChildren().add(noticeLabel);
        }
        proceedHBox.getChildren().addAll(buttonSpacer, proceedButton);
        vbox.getChildren().add(proceedHBox);

        vbox.setPrefHeight(Double.MAX_VALUE);
        // VBox.setVgrow(licenseText, Priority.ALWAYS);
        vbox.setSpacing(10);
        root.setCenter(vbox);
        root.setPadding(new Insets(10, 10, 10, 10));

        Scene copyrightScene = new Scene(root, 615, 450);
        printerManagerStage.setScene(copyrightScene);
        printerManagerStage.show();

        proceedButton.setOnAction(e -> {
            printerManagerStage.close();
        });
    }

    /* private static HBox createPrinterControls(PrintService printService) {
        HBox printerControlsHBox = new HBox();

        ComboBox<String> selectPrinterStatus = new ComboBox<>();
        selectPrinterStatus.getItems().addAll(
                UNUSED,
                DEFAULT_PHYSICAL,
                CLAWPDF
        );

        selectPrinterStatus.setOnAction(event -> {
            int selectedIndex = selectPrinterStatus.getSelectionModel().getSelectedIndex();

            switch (selectedIndex) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                default:
                    break;
            }
        });

        Text printerName = new Text(printService.getName());
        String status = "";
    }*/
}

