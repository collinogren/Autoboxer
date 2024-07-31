package ogren.collin.autoboxer.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ReferencesFX {
    public static Scene start(Stage stage, Scene copyrightScene) throws Exception {
        stage.setTitle("Autoboxer Open Source Credits");
        BorderPane root = new BorderPane();
        VBox vbox = new VBox();
        Label openSourceText = new Label("Open Source Library Credits");
        vbox.getChildren().add(openSourceText);

        ScrollPane scrollPane = new ScrollPane();
        VBox scrollBox = new VBox();
        Text creditsText = new Text(IOUtils.toString(Objects.requireNonNull(ReferencesFX.class.getResourceAsStream("/OpenSourceCredits.txt")), StandardCharsets.UTF_8));
        scrollBox.getChildren().addAll(creditsText, createSeparator());
        Text apacheLicenseText = new Text(IOUtils.toString(Objects.requireNonNull(ReferencesFX.class.getResourceAsStream("/apache/LICENSE.txt")), StandardCharsets.UTF_8));
        scrollBox.getChildren().addAll(apacheLicenseText, createSeparator());
        Text commonsIONotice = new Text(IOUtils.toString(Objects.requireNonNull(ReferencesFX.class.getResourceAsStream("/apache/commons-io/NOTICE.txt")), StandardCharsets.UTF_8));
        scrollBox.getChildren().addAll(commonsIONotice, createSeparator());
        Text log4j2Notice = new Text(IOUtils.toString(Objects.requireNonNull(ReferencesFX.class.getResourceAsStream("/apache/log4j2/NOTICE.txt")), StandardCharsets.UTF_8));
        scrollBox.getChildren().addAll(log4j2Notice, createSeparator());
        Text pdfboxNotice = new Text(IOUtils.toString(Objects.requireNonNull(ReferencesFX.class.getResourceAsStream("/apache/pdfbox/NOTICE.txt")), StandardCharsets.UTF_8));
        scrollBox.getChildren().add(pdfboxNotice);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(scrollBox);
        vbox.getChildren().addAll(createSeparator(), scrollPane, createSeparator());

        Button proceedButton = new Button("Back");
        proceedButton.setMinWidth(166.4);
        HBox proceedHBox = new HBox();
        Pane buttonSpacer = new Pane();
        HBox.setHgrow(buttonSpacer, Priority.ALWAYS);
        proceedHBox.setAlignment(Pos.CENTER);
        proceedHBox.getChildren().addAll(buttonSpacer, proceedButton);
        vbox.getChildren().add(proceedHBox);

        vbox.setPrefHeight(Double.MAX_VALUE);
        VBox.setVgrow(creditsText, Priority.ALWAYS);
        vbox.setSpacing(10);
        root.setCenter(vbox);
        root.setPadding(new Insets(10, 10, 10, 10));

        Scene openSourceCreditsScene = new Scene(root, 615, 450);

        proceedButton.setOnAction(e -> {
            stage.setTitle("Autoboxer Copyright and License Information");
            stage.setScene(copyrightScene);
        });

        return openSourceCreditsScene;
    }

    private static Separator createSeparator() {
        Separator separator = new Separator();
        separator.setPadding(Insets.EMPTY);
        separator.setPrefHeight(1);
        return separator;
    }
}
