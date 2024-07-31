package ogren.collin.autoboxer.gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ogren.collin.autoboxer.utilities.errordetection.BoxError;

import java.util.ArrayList;

public class ErrorsFX {

    public void start(ArrayList<BoxError> errors) {
        Stage stage = new Stage();
        stage.setResizable(true);
        stage.setWidth(425);
        stage.setHeight(300);
        stage.setTitle("Autoboxer Box Errors");
        stage.getIcons().add(GUIFX.autoboxerIcon);
        VBox contents = new VBox();
        contents.setSpacing(5);
        contents.setPadding(new Insets(5, 5, 5, 5));
        TextArea textArea = new TextArea();
        textArea.setEditable(false);
        for (BoxError error : errors) {
            textArea.appendText(error.createErrorMessage() + "\n");
        }
        VBox.setVgrow(textArea, Priority.ALWAYS);
        contents.getChildren().add(textArea);
        HBox closeBox = new HBox();
        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeButton = new Button("Close");
        closeButton.setMinWidth(150);
        closeButton.setOnAction(e -> stage.close());
        closeBox.getChildren().addAll(spacer, closeButton);
        contents.getChildren().add(closeBox);

        Scene scene = new Scene(contents);
        stage.setScene(scene);
        stage.show();
    }
}
