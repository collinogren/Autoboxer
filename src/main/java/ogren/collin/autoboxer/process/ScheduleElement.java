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

import ogren.collin.autoboxer.gui.GUIFXController;
import ogren.collin.autoboxer.pdf.PDFManipulator;

import java.io.File;
import java.util.ArrayList;

public class ScheduleElement {
    private final String eventNumber;
    private String eventName;
    private final String startTime;

    private final String endTime;

    private final String day;

    private final String rink;

    public ScheduleElement(String eventNumber, String eventName, String startTime, String endTime, String day, String rink) {
        this.eventNumber = eventNumber;
        this.eventName = eventName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.day = day;
        this.rink = rink;
    }

    public String getEventNumber() {
        return eventNumber;
    }

    public String getEventName() {
        return eventName;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEventName(String name) {
        eventName = name;
    }

    public String getDay() {
        return day;
    }

    public boolean matchFileNameToEventNumber(File file) {
        String number = file.getName().split(" ")[0];
        return number.equalsIgnoreCase(eventNumber);
    }

    public String getRink() {
        return rink;
    }
}
