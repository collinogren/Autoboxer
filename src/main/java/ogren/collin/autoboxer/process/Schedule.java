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

    private static final ArrayList<String> rinks = new ArrayList<>();

    ArrayList<ScheduleElement> elements = new ArrayList<>();

    public Schedule(File file) {
        List<String> lines;
        try {
            lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read schedule.txt.\nMake sure it exists.");
        }

        String day = lines.getFirst();
        lines.removeFirst();

        ArrayList<Rink> rinks = new ArrayList<>();

        int index = -1;

        for (String line : lines) {
            if (line.toLowerCase().startsWith("-r")) {
                String rink = line.split(" ", 2)[1];
                rinks.add(new Rink(rink));
                index += 1;
                if (!Schedule.rinks.contains(rink)) {
                    Schedule.rinks.add(rink);
                }
                continue;
            }

            try {
                rinks.get(index).add(line);
            } catch(IndexOutOfBoundsException e) {
                e.printStackTrace();
                throw new RuntimeException("No rink name provided in schedule.txt.\nUse the format \"-R rink name\" after stating the day.");
            }
        }

        for (int i = 0; i < rinks.size(); i++) {
            for (String line : rinks.get(i)) {
                if (line.isEmpty()) {
                    continue;
                }

                String[] split = line.split("\t");

                if (split.length < 2) {
                    throw new RuntimeException("Missing a start time which must exist.");
                }

                if (i == 0) {
                    if (split.length < 3) {
                        elements.add(new ScheduleElement(line, "", split[1], "", day, rinks.get(i).getRink()));
                    } else {
                        elements.add(new ScheduleElement(split[0], "", split[1], split[2], day, rinks.get(i).getRink()));
                    }
                } else {
                    if (split.length < 3) {
                        elements.add(getIndexToInsert(elements, split[1]), new ScheduleElement(line, "", split[1], "", day, rinks.get(i).getRink()));
                    } else {
                        elements.add(getIndexToInsert(elements, split[1]), new ScheduleElement(split[0], "", split[1], split[2], day, rinks.get(i).getRink()));
                    }
                }
            }
        }
    }

    public static ArrayList<String> getRinks() {
        return rinks;
    }

    private int getIndexToInsert(ArrayList<ScheduleElement> scheduleElements, String time) {
        long startTime = Time.parseTimeMinutes(time);
        for (int i = 0; i < scheduleElements.size(); i++) {
            if (startTime < Time.parseTimeMinutes(scheduleElements.get(i).getStartTime())) {
                if (i > 0) {
                    return i;
                } else {
                    return i;
                }
            }
        }

        return scheduleElements.size();
    }

    public ArrayList<ScheduleElement> getElements() {
        return elements;
    }

    public static String[] readScheduleFileToString(File directory) {
        File file = new File(directory.getPath() + "/schedule.txt");
        String contents = "";
        try {
            contents = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException ignored) {}

        return contents.split("-[rR] ");
    }

    public static void saveSchedule(File directory, String day, ArrayList<String> rinks) {
        StringBuilder builder = new StringBuilder();
        builder.append(day);
        builder.append("\n");

        for (String rink : rinks) {
            if (rink.isEmpty()) {
                continue;
            }
            builder.append(rink);
            builder.append("\n");
        }

        try {
            File file = new File(directory.getPath() + "/schedule.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileUtils.write(file, builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
