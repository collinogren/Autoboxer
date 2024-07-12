package ogren.collin.autoboxer.utilities;

import ogren.collin.autoboxer.Logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class APIUtilities {

    private static String version = null;

    public static String getAPIVersion() {
        if (version != null) {
            return version;
        }

        String path = "/version.properties";
        InputStream stream = Objects.requireNonNull(APIUtilities.class.getResourceAsStream(path));

        Properties properties = new Properties();
        try {
            properties.load(stream);
            stream.close();
            version = (String) properties.get("version");
            return version;
        } catch (IOException ioe) {
            version = "Unknown Version";
            return version;
        }
    }
}
