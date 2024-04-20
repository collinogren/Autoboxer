package ogren.collin.autoboxer.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;


public class GUIFX extends Application {

    public static Scene mainScene;

    public static void main(String[] args) {
        GUIFX.launch(args);
    }

    public static Image autoboxerIcon;

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/gui.fxml")));

        mainScene = new Scene(root, 300, 200);

        autoboxerIcon = new Image("/Autoboxer.png");

        stage.getIcons().add(autoboxerIcon);
        stage.setTitle("Autoboxer");
        stage.setScene(mainScene);
        stage.show();
    }
}
