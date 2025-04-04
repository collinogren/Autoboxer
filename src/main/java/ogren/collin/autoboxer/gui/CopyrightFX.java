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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import ogren.collin.autoboxer.utilities.APIUtilities;
import org.apache.commons.io.IOUtils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class CopyrightFX {

    public static void start(boolean startupScreen) throws Exception {
        Stage copyrightStage = new Stage();
        copyrightStage.setResizable(false);
        copyrightStage.setTitle(copyrightInfoTitle());
        copyrightStage.getIcons().add(GUIFX.autoboxerIcon);
        copyrightStage.setAlwaysOnTop(true);
        BorderPane root = new BorderPane();
        VBox vbox = new VBox();
        HBox copyrightHBox = new HBox();
        VBox copyrightVBox = new VBox();
        copyrightVBox.setMinHeight(108);
        Text copyrightText = new Text("""
                Autoboxer Copyright (C) 2025 Collin Ogren
                This program comes with ABSOLUTELY NO WARRANTY.
                This is free software, and you are welcome to redistribute it
                under certain conditions. Details in the license text below.""");
        copyrightVBox.getChildren().add(copyrightText);
        Hyperlink hyperlink = new Hyperlink("Access the source code here");
        hyperlink.setOnAction(event -> GUIFXController.viewGithub());
        Hyperlink openSourceCredits = new Hyperlink("Open source library credits");
        copyrightVBox.getChildren().addAll(hyperlink, openSourceCredits);
        ImageView gnuGPLV3Logo = new ImageView(new Image(Objects.requireNonNull(CopyrightFX.class.getResourceAsStream("/ogren/collin/resources/gplv3.png"))));
        gnuGPLV3Logo.setSmooth(true);
        gnuGPLV3Logo.setPreserveRatio(true);
        gnuGPLV3Logo.setFitHeight(108);

        HBox.setHgrow(copyrightVBox, Priority.ALWAYS);
        copyrightHBox.getChildren().addAll(copyrightVBox, gnuGPLV3Logo);
        vbox.getChildren().add(copyrightHBox);

        ScrollPane scrollPane = new ScrollPane();
        Text licenseText = new Text(IOUtils.toString(Objects.requireNonNull(CopyrightFX.class.getResourceAsStream("/ogren/collin/resources/COPYING.txt")), StandardCharsets.UTF_8));
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

        Scene copyrightScene = new Scene(root, GUIFX.WINDOW_WIDTH, GUIFX.WINDOW_HEIGHT);
        copyrightStage.setScene(copyrightScene);
        copyrightStage.show();

        openSourceCredits.setOnAction(event -> {
            try {
                copyrightStage.setScene(ReferencesFX.start(copyrightStage, copyrightScene));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        proceedButton.setOnAction(e -> {
            copyrightStage.close();
        });
    }

    protected static String copyrightInfoTitle() {
        return "Autoboxer v" + APIUtilities.getAPIVersion() + " Copyright and License Information";
    }
}
