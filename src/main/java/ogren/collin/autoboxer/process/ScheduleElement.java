/*
    Autoboxer to make creating "boxes" for Figure Skating competitions easier.
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

public class ScheduleElement {

    private String eventNumber;
    private long time;
    private boolean processed;

    public ScheduleElement(String eventNumber, long time, boolean processed) {
        this.eventNumber = eventNumber;
        this.time = time;
        this.processed = processed;
    }

    public void setProcessed(boolean b) {
        processed = b;
    }

    public String getEventNumber() {
        return eventNumber;
    }

    public long getTime() {
        return time;
    }

    public boolean isProcessed() {
        return processed;
    }
}
