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

public class Time {

    private static long hoursToMinutes(long hours) {
        return hours * 60;
    }

    public static long parseTimeMinutes(String timeString) {
        long time = 0;
        boolean pm = timeString.toLowerCase().contains("pm");
        if (pm) {
            time += hoursToMinutes(12);
        }

        String[] timeArray = timeString.split(":");
        time += hoursToMinutes(Long.parseLong(timeArray[0]));
        time += Long.parseLong(timeArray[1].split(" ")[0]);

        return time;
    }
}
