package ogren.collin.autoboxer.gui;

import com.sun.source.tree.DoWhileLoopTree;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import ogren.collin.autoboxer.utilities.remote_utilities.RemoteUtilities;
import ogren.collin.autoboxer.utilities.remote_utilities.auto_update.RemoteAutoUpdateTextBundle;

public class UpdateNotificationFX {

    public static void start(RemoteAutoUpdateTextBundle autoUpdateTextBundle) {
        Stage updateNotificationStage = new Stage();
        updateNotificationStage.setWidth(450);
        updateNotificationStage.setHeight(150);
        updateNotificationStage.setResizable(false);
        updateNotificationStage.setTitle("Autoboxer Update Notification");
        updateNotificationStage.getIcons().add(GUIFX.autoboxerIcon);
        updateNotificationStage.setAlwaysOnTop(true);
        VBox contents = new VBox();
        contents.setAlignment(Pos.CENTER);
        contents.setSpacing(5);
        contents.setPadding(new Insets(5, 5, 5, 5));
        Label updateAvailable = new Label("A new Autoboxer update is available.\nWould you like to download version " + autoUpdateTextBundle.version() + "?");
        updateAvailable.setTextAlignment(TextAlignment.CENTER);
        VBox.setVgrow(updateAvailable, Priority.ALWAYS);
        updateAvailable.setWrapText(true);
        contents.getChildren().add(updateAvailable);
        HBox buttons = new HBox();
        buttons.setSpacing(5);
        Button downloadButton = new Button("Download");
        downloadButton.setPrefWidth(Integer.MAX_VALUE);
        downloadButton.setDefaultButton(true);
        Button noButton = new Button("No");
        noButton.setPrefWidth(Integer.MAX_VALUE);
        downloadButton.setOnAction(e -> {
            RemoteUtilities.browseToURL(autoUpdateTextBundle.url());
            buttons.getChildren().clear();
            updateAvailable.setText("Please close Autoboxer and run the downloaded installer.");
            Button closeButton = new Button("Close");
            closeButton.setPrefWidth(Integer.MAX_VALUE);
            closeButton.setOnAction(e1 -> {
                Platform.exit();
            });
            buttons.getChildren().add(closeButton);
            closeButton.requestFocus();
        });

        noButton.setOnAction(e -> {
            updateNotificationStage.close();
        });

        HBox.setHgrow(noButton, Priority.ALWAYS);
        HBox.setHgrow(downloadButton, Priority.ALWAYS);

        buttons.getChildren().addAll(noButton, downloadButton);
        Pane spacer = new Pane();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        contents.getChildren().addAll(spacer, buttons);

        Scene scene = new Scene(contents);
        updateNotificationStage.setScene(scene);
        updateNotificationStage.show();

        downloadButton.requestFocus();
    }
}
