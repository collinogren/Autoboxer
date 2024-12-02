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

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ogren.collin.autoboxer.utilities.APIUtilities;
import ogren.collin.autoboxer.utilities.Settings;
import ogren.collin.autoboxer.utilities.remote_utilities.RemoteUtilities;
import java.util.Objects;

public class GUIFX extends Application {

    public static Scene mainScene;
    public static Image autoboxerIcon;

    public static void main(String[] args) {
        GUIFX.launch(args);
    }

    public static void setTheme() {
        if (Settings.getTheme().equals(Settings.DARK_THEME)) {
            Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
        } else if (Settings.getTheme().equals(Settings.LIGHT_THEME)) {
            Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
        }
    }

    public static Stage primaryStage;

    // Initialize JavaFX application.
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        RemoteUtilities.checkForUpdate();
        FXMLLoader fxmlLoader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/ogren/collin/resources/gui.fxml")));
        Parent root = fxmlLoader.load();

        mainScene = new Scene(root, 600, 400);

        GUIFXController controller = fxmlLoader.getController();
        controller.setup(mainScene);

        setTheme();

        autoboxerIcon = new Image("/ogren/collin/resources/Autoboxer.png");

        primaryStage.getIcons().add(autoboxerIcon);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(mainScene.getWidth() + 20);
        primaryStage.setMinHeight(mainScene.getHeight() + 40);
        primaryStage.setTitle("Autoboxer");
        primaryStage.setScene(mainScene);

        primaryStage.show();

        if (Settings.isNewInstall() || !Settings.getVersion().equals(APIUtilities.getAPIVersion())) {
            CopyrightFX.start(true);
        }
    }
}
