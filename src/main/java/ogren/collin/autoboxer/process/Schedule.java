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

import ogren.collin.autoboxer.Logging;
import ogren.collin.autoboxer.control.MasterController;
import ogren.collin.autoboxer.utilities.Settings;
import ogren.collin.autoboxer.utilities.error_detection.BoxError;
import ogren.collin.autoboxer.utilities.error_detection.ErrorType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Schedule {

    private final ArrayList<String> rinks = new ArrayList<>();

    ArrayList<ScheduleElement> elements = new ArrayList<>();

    public Schedule(File file) {
        List<String> lines;
        try {
            lines = FileUtils.readLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            String message = "Failed to read schedule.txt.\nMake sure it exists.";
            Logging.logger.fatal((e));
            throw new RuntimeException(message);
        }

        // The day is the first line in the file
        String day = lines.getFirst();

        // Discard the day
        lines.removeFirst();

        ArrayList<RinkSchedule> rinks = new ArrayList<>();

        int index = -1;

        // Find all rinks in the schedule file and associate the raw text lines with each rink.
        for (String line : lines) {
            // Check for the rink flag (-r)
            if (line.toLowerCase().startsWith("-r")) {
                String rink = line.split(" ", 2)[1];
                rinks.add(new RinkSchedule(rink));
                // index determines which rink is being worked on.
                index += 1;
                // Make sure there is no duplicate rink.
                if (!this.rinks.contains(rink)) {
                    // Add the rink to the list
                    this.rinks.add(rink);
                }

                // Clearly there is no schedule element here so go to the next line.
                continue;
            }

            // If no rink flag (-r) was found then the line must be a schedule element.
            try {
                // index is used here to determine which rink to access and then a line is added to that rink.
                rinks.get(index).add(line);
            } catch (IndexOutOfBoundsException e) {
                String message = "No rink name provided in schedule.txt.\nUse the format \"-R rink name\" after stating the day.";
                Logging.logger.fatal("{}\n{}", Arrays.toString(e.getStackTrace()), message);
                throw new RuntimeException(message);
            }
        }

        // For every rink:
        for (int i = 0; i < rinks.size(); i++) {
            // For every line (a probable ScheduleElement) in that rink:
            for (String line : rinks.get(i)) {
                // if the line is empty or only contains tabs or spaces then go to the next iteration.
                if (line.isEmpty() || line.replace('\t', ' ').trim().isEmpty()) {
                    continue;
                }

                // Part of the remove leading zeros functionality.
                if (Settings.getRemoveLeadingZeros()) {
                    line = line.replaceFirst("^0+(?!$)", "");
                }

                // Split the line into an array by tabs.
                String[] split = line.split("\t");

                // if there are less than two strings separated by tabs then there must not be enough information and an
                // error should be thrown.
                if (split.length < 2) {
                    Logging.logger.fatal("Missing a start time which must exist.");
                    MasterController.errors.add(new BoxError(line, null, ErrorType.MISSING_START_TIME));
                    throw new RuntimeException("Missing a start time which must exist.");
                }

                // If this is the first rink then there is no need to consider the time.
                if (i == 0) {
                    // if there are less than three strings then the end time must be blank. Add a schedule element with
                    // the event number and starting time, but a blank end time.
                    if (split.length < 3) {
                        elements.add(new ScheduleElement(split[0], "", split[1], "", day, rinks.get(i).getRink()));
                    } else {
                        // Otherwise there must be all three entries so add with an event number, starting time, and end
                        // time.
                        elements.add(new ScheduleElement(split[0], "", split[1], split[2], day, rinks.get(i).getRink()));
                    }
                    // If this is a subsequent rink then time does need to be taken into account.
                    // This algorithm will sort first by time, then by schedule order as listed on the 104.
                } else {
                    // Handle the same way as the first rink except instead of adding the schedule element to the end of
                    // the array, use the getIndexToInsert() function to place the new schedule element in the right
                    // place based on chronology.
                    if (split.length < 3) {
                        elements.add(getIndexToInsert(elements, split[1]), new ScheduleElement(split[0], "", split[1], "", day, rinks.get(i).getRink()));
                    } else {
                        elements.add(getIndexToInsert(elements, split[1]), new ScheduleElement(split[0], "", split[1], split[2], day, rinks.get(i).getRink()));
                    }
                }
            }
        }

        // Handle duplicate events
        ArrayList<String> duplicateEvents = new ArrayList<>();
        boolean error = false;

        for (ScheduleElement scheduleElement : elements) {
            int multiplicity = 0;
            for (ScheduleElement scheduleElement1 : elements) {
                if (scheduleElement.getEventNumber().isEmpty()) {
                    continue;
                }

                if (scheduleElement.getEventNumber().equals(scheduleElement1.getEventNumber())) {
                    multiplicity++;
                }

                if (multiplicity > 1) {
                    boolean isErrorThrown = false;
                    for (String event : duplicateEvents) {
                        if (event.equals(scheduleElement.getEventNumber())) {
                            isErrorThrown = true;
                            break;
                        }
                    }
                    if (isErrorThrown) {
                        continue;
                    }

                    duplicateEvents.add(scheduleElement.getEventNumber());

                    MasterController.errors.add(new BoxError(scheduleElement.getEventNumber(), null, ErrorType.DUPLICATE_SCHEDULE_ENTRY));
                    error = true;
                }
            }
        }

        if (error) {
            throw new RuntimeException("Duplicate schedule entry.");
        }

        // This could probably be inlined with a previous part of this function.
        elements.removeIf(element -> element.getEventNumber().trim().isEmpty());
    }

    public ArrayList<String> getRinks() {
        return rinks;
    }

    public static String[] readScheduleFileToString(File directory) {
        File file = new File(directory.getPath() + "/schedule.txt");
        String contents = "";
        try {
            contents = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }

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
                if (!file.createNewFile()) {
                    throw new IOException("Failed to create schedule.txt file");
                }
            }
            FileUtils.write(file, builder.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Logging.logger.fatal((e));
            throw new RuntimeException(e);
        }
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
}
