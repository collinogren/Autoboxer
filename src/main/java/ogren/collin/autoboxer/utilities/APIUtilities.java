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

package ogren.collin.autoboxer.utilities;

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

        String path = "/ogren/collin/resources/version.properties";
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
