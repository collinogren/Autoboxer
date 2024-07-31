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

import ogren.collin.autoboxer.Logging;
import ogren.collin.autoboxer.control.MasterController;
import ogren.collin.autoboxer.utilities.Settings;
import ogren.collin.autoboxer.utilities.errordetection.BoxError;
import ogren.collin.autoboxer.utilities.errordetection.ErrorType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Schedule {

    private static final ArrayList<String> rinks = new ArrayList<>();

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
            } catch (IndexOutOfBoundsException e) {
                String message = "No rink name provided in schedule.txt.\nUse the format \"-R rink name\" after stating the day.";
                Logging.logger.fatal("{}\n{}", Arrays.toString(e.getStackTrace()), message);
                throw new RuntimeException(message);
            }
        }

        for (int i = 0; i < rinks.size(); i++) {
            for (String line : rinks.get(i)) {
                if (line.isEmpty() || line.replace('\t', ' ').trim().isEmpty()) {
                    continue;
                }

                if (Settings.getRemoveLeadingZeros()) {
                    line = line.replaceFirst("^0+(?!$)", "");
                }

                String[] split = line.split("\t");

                if (split.length < 2) {
                    Logging.logger.fatal("Missing a start time which must exist.");
                    MasterController.errors.add(new BoxError(line, null, ErrorType.MISSING_START_TIME));
                    throw new RuntimeException("Missing a start time which must exist.");
                }

                // Okay moron (past me), comment your code because this is literal dark magic over here.
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

        /*
         I only did it like this because the previous code is so unreadable that I couldn't figure out how to do this
         inline with inserting new schedule elements. Maybe fix this sometime hmm?
         */
        elements.removeIf(element -> element.getEventNumber().trim().isEmpty());
    }

    public static ArrayList<String> getRinks() {
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
