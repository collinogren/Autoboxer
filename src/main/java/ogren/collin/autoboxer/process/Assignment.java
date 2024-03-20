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

package ogren.collin.autoboxer.process;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.ArrayList;

public class Assignment {
    String eventNumber; // String because event number could be 1 or 1A, for example.
    long time; // Time in seconds.

    private PDDocument coverSheet; // This includes the 6.0 judge's sheet.
    private ArrayList subsequentSheets = new ArrayList(); // This also includes the 6.0 worksheets.

    public Assignment(String eventName, String time) {
        eventNumber = parseEventNumber(eventName);
        this.time = Time.parseTimeMinutes(time);
    }

    private String parseEventNumber(String eventName) {
        String eventNumber = "";
        try {
            eventNumber = eventName.split(" ")[0];
        } catch (Exception e) {
            System.err.println("Failed to parse the event number from name from event with name: " + eventName + ". Make sure that the event name begins with the event number followed by a space.");
            e.printStackTrace();
        }

        return eventNumber;
    }
}
