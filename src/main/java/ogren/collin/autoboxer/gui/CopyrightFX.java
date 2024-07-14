package ogren.collin.autoboxer.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class CopyrightFX {
    public static void start(boolean startupScreen) throws Exception {
        Stage copyrightStage = new Stage();
        copyrightStage.setResizable(false);
        copyrightStage.setTitle("Autoboxer Copyright and License Information");
        copyrightStage.getIcons().add(GUIFX.autoboxerIcon);
        BorderPane root = new BorderPane();
        VBox vbox = new VBox();
        HBox copyrightHBox = new HBox();
        VBox copyrightVBox = new VBox();
        copyrightVBox.setMinHeight(108);
        Text copyrightText = new Text("""
                Autoboxer Copyright (C) 2024 Collin Ogren
                This program comes with ABSOLUTELY NO WARRANTY.
                This is free software, and you are welcome to redistribute it
                under certain conditions. Details in the license text below.""");
        copyrightVBox.getChildren().add(copyrightText);
        Hyperlink hyperlink = new Hyperlink("Access the source code here");
        hyperlink.setOnAction(event -> GUIFXController.viewGithub());
        Hyperlink openSourceCredits = new Hyperlink("Open source library credits");
        openSourceCredits.setOnAction(event -> {
            try {
                ReferencesFX.start();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        copyrightVBox.getChildren().addAll(hyperlink, openSourceCredits);
        ImageView gnuGPLV3Logo = new ImageView(new Image(Objects.requireNonNull(CopyrightFX.class.getResourceAsStream("/gplv3.png"))));
        gnuGPLV3Logo.setSmooth(true);
        gnuGPLV3Logo.setPreserveRatio(true);
        gnuGPLV3Logo.setFitHeight(108);

        HBox.setHgrow(copyrightVBox, Priority.ALWAYS);
        copyrightHBox.getChildren().addAll(copyrightVBox, gnuGPLV3Logo);
        vbox.getChildren().add(copyrightHBox);

        ScrollPane scrollPane = new ScrollPane();
        Text licenseText = new Text(IOUtils.toString(Objects.requireNonNull(CopyrightFX.class.getResourceAsStream("/COPYING.txt")), StandardCharsets.UTF_8));
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(licenseText);
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
        VBox.setVgrow(licenseText, Priority.ALWAYS);
        vbox.setSpacing(10);
        root.setCenter(vbox);
        root.setPadding(new Insets(10, 10, 10, 10));

        Scene progressScene = new Scene(root, 615, 450);
        copyrightStage.setScene(progressScene);
        copyrightStage.show();

        proceedButton.setOnAction(e -> {
            copyrightStage.close();
        });
    }
}
