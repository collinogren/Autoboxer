package ogren.collin.autoboxer.utilities.remote_utilities;

import javafx.application.Platform;
import ogren.collin.autoboxer.Logging;
import ogren.collin.autoboxer.gui.UpdateNotificationFX;
import ogren.collin.autoboxer.utilities.Settings;
import ogren.collin.autoboxer.utilities.remote_utilities.auto_update.RemoteAutoUpdateTextBundle;
import ogren.collin.autoboxer.utilities.remote_utilities.auto_update.RemoteTextParser;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class RemoteUtilities {

    public static void browseToURL(String url) {
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new URI(url));
        } catch (IOException | URISyntaxException e) {
            Logging.logger.error(e);
        }
    }

    public static void checkForUpdate() {
        Thread thread = new Thread(() -> {
            RemoteAutoUpdateTextBundle autoUpdateTextBundle = RemoteTextParser.getRemoteText();
            if (Settings.AUTOBOXER_NUMERIC_VERSION < autoUpdateTextBundle.numericVersion()) {
                Platform.runLater(() -> {
                    UpdateNotificationFX.start(autoUpdateTextBundle);
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
