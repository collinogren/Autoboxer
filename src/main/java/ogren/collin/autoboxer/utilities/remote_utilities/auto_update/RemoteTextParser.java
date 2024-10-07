package ogren.collin.autoboxer.utilities.remote_utilities.auto_update;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Scanner;

public class RemoteTextParser {

    private static final String AUTO_UPDATE_FILE_URL = "https://pastebin.com/raw/hPimjxBc";

    public static RemoteAutoUpdateTextBundle getRemoteText() {
        String version = "Remote version unavailable";
        int numericVersion = -1;
        String downloadURL = "Download URL unavailable";
        try {
            URL url = URI.create(AUTO_UPDATE_FILE_URL).toURL();
            Scanner scanner = new Scanner(url.openStream());
            version = scanner.nextLine();
            numericVersion = Integer.parseInt(scanner.nextLine());
            downloadURL = scanner.nextLine();
        } catch (IOException ioe) {
            System.err.println("Failed to check for updates.");
        }

        return new RemoteAutoUpdateTextBundle(version, numericVersion, downloadURL);
    }
}
