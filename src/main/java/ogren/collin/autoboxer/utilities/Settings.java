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

package ogren.collin.autoboxer.utilities;

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
    private static String version;
    private static boolean newInstall = false;

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
                if (!settingsFile.createNewFile()) {
                    Logging.logger.warn("Failed to create Autoboxer settings file.");
                }
                newInstall = true;
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
        version = properties.getProperty("version", "Unknown Version");
        if (version.equals("Unknown Version") || !APIUtilities.getAPIVersion().equals(version)) {
            updateProperties();
        }
    }

    private static void updateProperties() {
        properties.put("combinePaperwork", Boolean.toString(combinePaperwork));
        properties.put("generateSchedule", Boolean.toString(generateSchedule));
        properties.put("generateStartingOrders", Boolean.toString(generateStartingOrders));
        properties.put("generateTASheets", Boolean.toString(generateTASheets));
        properties.put("removeLeadingZeros", Boolean.toString(removeLeadingZeros));
        properties.put("eventNameDelimiter", eventNameDelimiter);
        properties.put("version", APIUtilities.getAPIVersion());
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

    public static String getVersion() {
        return version;
    }

    public static boolean isNewInstall() {
        return newInstall;
    }
}
