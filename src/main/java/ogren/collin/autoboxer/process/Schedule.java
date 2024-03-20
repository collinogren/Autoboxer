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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Schedule {
    ArrayList<ScheduleElement> elements = new ArrayList<>();

    public Schedule(File file) {
        List<String> lines;
        try {
            lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            elements.add(new ScheduleElement(line));
        }
    }

    public ArrayList<ScheduleElement> getElements() {
        return elements;
    }
}
