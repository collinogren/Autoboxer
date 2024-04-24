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

import atlantafx.base.theme.CupertinoLight;
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
        Application.setUserAgentStylesheet(new CupertinoLight().getUserAgentStylesheet());
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/gui.fxml")));

        mainScene = new Scene(root, 550, 350);

        autoboxerIcon = new Image("/Autoboxer.png");

        stage.getIcons().add(autoboxerIcon);
        stage.setResizable(false);
        stage.setTitle("Autoboxer");
        stage.setScene(mainScene);
        stage.show();
    }
}
