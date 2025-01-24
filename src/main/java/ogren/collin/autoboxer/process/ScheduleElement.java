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

package ogren.collin.autoboxer.process;

import java.io.File;

public class ScheduleElement {
    private final String eventNumber;
    private String startTime;
    private String endTime;
    private final String day;
    private final String rink;
    private String eventName;
    private boolean processed = false;

    public ScheduleElement(String eventNumber, String eventName, String startTime, String endTime, String day, String rink) {
        if (eventNumber != null) {
            this.eventNumber = eventNumber.replaceAll(" ", "");
        } else {
            this.eventNumber = null;
        }
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

    public void setEventName(String name) {
        eventName = name;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
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

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
