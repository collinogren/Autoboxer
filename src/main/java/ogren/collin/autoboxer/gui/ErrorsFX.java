package ogren.collin.autoboxer.gui;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ogren.collin.autoboxer.utilities.errordetection.BoxError;

import java.util.ArrayList;

public class ErrorsFX {

    public void start(ArrayList<BoxError> errors) {
        Stage stage = new Stage();
        stage.setResizable(true);
        stage.setTitle("Autoboxer Box Errors");
        stage.getIcons().add(GUIFX.autoboxerIcon);
        stage.setAlwaysOnTop(true);
        AnchorPane pane = new AnchorPane();
        TextArea area = new TextArea();
        for (BoxError error : errors) {
            area.appendText(error.createErrorMessage() + "\n");
        }
        pane.getChildren().add(area);

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }
}
