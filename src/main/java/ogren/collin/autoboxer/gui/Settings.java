package ogren.collin.autoboxer.gui;

import ogren.collin.autoboxer.Logging;

import java.io.*;
import java.util.Properties;

public class Settings {

    private static boolean generateSchedule = true;
    private static boolean generateStartingOrders = true;
    private static boolean generateTASheets = true;
    private static boolean combinePaperwork = false;
    private static boolean removeLeadingZeros = true;
    private static String eventNameDelimiter = " - ";
    private static final Properties properties = new Properties();
    private static File settingsFile;

    public static void loadSettings() {
        settingsFile = new File(System.getenv("APPDATA") + "/Autoboxer/Autoboxer.properties");
        System.out.println(settingsFile.getPath());
        if (!settingsFile.getParentFile().exists()) {
            if (!settingsFile.getParentFile().mkdirs()) {
                Logging.logger.warn("Failed to create Autoboxer settings directories.");
                return;
            }
        }
        if (!settingsFile.exists()) {
            try {
                if (settingsFile.createNewFile()) {
                    Logging.logger.warn("Failed to create Autoboxer settings file.");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            updateProperties();
        }

        try {
            properties.load(new FileInputStream(settingsFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        generateSchedule = Boolean.parseBoolean(properties.getProperty("generateSchedule", Boolean.toString(generateSchedule)));
        generateStartingOrders = Boolean.parseBoolean(properties.getProperty("generateStartingOrders", Boolean.toString(generateStartingOrders)));
        generateTASheets = Boolean.parseBoolean(properties.getProperty("generateTASheets", Boolean.toString(generateTASheets)));
        combinePaperwork = Boolean.parseBoolean(properties.getProperty("combinePaperwork", Boolean.toString(combinePaperwork)));
        removeLeadingZeros = Boolean.parseBoolean(properties.getProperty("removeLeadingZeros", Boolean.toString(removeLeadingZeros)));
        eventNameDelimiter = properties.getProperty("eventNameDelimiter", eventNameDelimiter);
    }

    private static void updateProperties() {
        properties.put("combinePaperwork", Boolean.toString(combinePaperwork));
        properties.put("generateSchedule", Boolean.toString(generateSchedule));
        properties.put("generateStartingOrders", Boolean.toString(generateStartingOrders));
        properties.put("generateTASheets", Boolean.toString(generateTASheets));
        properties.put("removeLeadingZeros", Boolean.toString(removeLeadingZeros));
        properties.put("eventNameDelimiter", eventNameDelimiter);
        save();
    }

    private static void save() {
        try {
            properties.store(new FileOutputStream(settingsFile), "");
        } catch (IOException e) {
            Logging.logger.warn("Failed to create Autoboxer settings file.");
        }
    }

    public static void setCombinePaperwork(boolean b) {
        combinePaperwork = b;
        updateProperties();
    }

    public static void setGenerateSchedule(boolean b) {
        generateSchedule = b;
        updateProperties();
    }

    public static boolean getGenerateSchedule() {
        return generateSchedule;
    }

    public static void setGenerateStartingOrders(boolean b) {
        generateStartingOrders = b;
        updateProperties();
    }

    public static boolean getGenerateStartingOrders() {
        return generateStartingOrders;
    }

    public static void setGenerateTASheets(boolean b) {
        generateTASheets = b;
        updateProperties();
    }

    public static boolean getCombinePaperwork() {
        return combinePaperwork;
    }

    public static boolean getGenerateTASheets() {
        return generateTASheets;
    }

    public static void setEventNameDelimiter(String delimiter) {
        eventNameDelimiter = delimiter;
        updateProperties();
    }

    public static String getEventNameDelimiter() {
        return eventNameDelimiter;
    }

    public static void setRemoveLeadingZeros(boolean b) {
        removeLeadingZeros = b;
        updateProperties();
    }

    public static boolean getRemoveLeadingZeros() {
        return removeLeadingZeros;
    }
}
