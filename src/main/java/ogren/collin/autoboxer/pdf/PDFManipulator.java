/*
    Autoboxer to make creating "boxes" for Figure Skating competitions easier.
    Copyright (C) 2024 Collin Ogren

    This program is free software -> you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https ->//www.gnu.org/licenses/>.
 */

package ogren.collin.autoboxer.pdf;

import ogren.collin.autoboxer.Main;
import ogren.collin.autoboxer.process.Schedule;
import ogren.collin.autoboxer.process.ScheduleElement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PDFManipulator {
    private PDDocument document;
    private FileType fileType;

    private ScheduleElement scheduleElement;

    public PDFManipulator(File file, FileType fileType, Schedule schedule) throws IOException {
        document = PDDocument.load(file);
        this.fileType = fileType;
        String eventName = parseEventName();
        matchToSchedule(eventName, schedule);
    }

    private void matchToSchedule(String eventName, Schedule schedule) {
        for (ScheduleElement se : schedule.getElements()) {
            if (se.isProcessed()) {
                continue;
            }

            if (eventName.contains(se.getEventNumber())) {

            }
        }
    }

    private String parseEventName() {
        String eventName = "Could not find event name!";
        switch (fileType) {
            case FileType.IJS_COVERSHEET, FileType.IJS_JUDGE_SHEET, FileType.IJS_REFEREE_SHEET, FileType.IJS_TC_SHEET, FileType.IJS_TS2_SHEET -> eventName = parseEventNameIJS();
            case FileType.SIX0_JUDGE_SHEET, SIX0_WORKSHEET -> System.err.println("Not implemented");
        }

        return eventName;
    }

    private String parseEventNameIJS() {
        String contents = "Could not find event name!";
        try {
            contents = parseToString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] lines = contents.split("\n");
        int line;
        for (line = 0; line < lines.length; line++) {
            if (lines[line].toLowerCase().contains(Main.getCompetitionName().toLowerCase())) {
                line++;
                break;
            }
        }

        String correctedName = extractName(lines[line]);

        return correctedName;
    }

    private String extractName(String line) {
        String eventName = line;
        String regex = null;
        switch (fileType) {
            case IJS_COVERSHEET, IJS_JUDGE_SHEET -> regex = " / ";
            case IJS_REFEREE_SHEET -> regex = " - REFEREE SHEET";
            case IJS_TC_SHEET -> regex = " - TECHNICAL CONTROLLER SHEET";
            case IJS_TS2_SHEET -> regex = " - TECHNICAL SPECIALIST SHEET";
        }

        String[] split = eventName.split(regex);
        String correctedName = split[0] + " " + split[1];
        correctedName = correctedName.toUpperCase();
        return correctedName;
    }

    public String parseToString() throws IOException {
        return new PDFTextStripper().getText(document);
    }

    public String getEventName() {
        String name = parseEventName();
        return name;
    }
}
