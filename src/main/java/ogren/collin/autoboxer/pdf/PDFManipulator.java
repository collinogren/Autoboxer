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
import ogren.collin.autoboxer.process.ScheduleElement;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static ogren.collin.autoboxer.control.MasterController.TEST_MODE;
import static ogren.collin.autoboxer.pdf.FileType.IJS_JUDGE_SHEET;

public class PDFManipulator {

    private static final String judgeSheet60NameKey = "REFEREE AND JUDGES PERSONAL RECORD SHEET";
    private PDDocument document;
    private FileType fileType;

    private File file;

    private String contents;

    private boolean isRenamed = false;

    private static final String EVENT_NAME_DELIMITER = " - ";

    public static final String WRONG_FILE_TYPE = "wrongfiletype";

    public PDFManipulator(File file, FileType fileType) {
        this.file = file;
        try {
            document = PDDocument.load(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.fileType = fileType;
        contents = readContents();
    }

    public String retrieveEventName() {
        String eventName;
        if (!isReallyFileType()) {
            eventName = WRONG_FILE_TYPE;
        } else {
            eventName = parseEventName();
        }

        return eventName;
    }

    private String readContents() {
        String contents = "Could not find event name!";
        try {
            contents = parseToString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return contents;
    }

    private boolean isReallyFileType() {
        return contents.contains(getRegex());
    }

    public void close() {
        try {
            document.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean matchNameToSchedule(ScheduleElement scheduleElement, String eventName) {
        System.out.println(eventName);
        for (String e : scrutinizeName(eventName)) {
            if (e.equalsIgnoreCase(scheduleElement.getEventNumber())) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<String> scrutinizeName(String eventName) {
        String eventNumberSection = eventName.split(EVENT_NAME_DELIMITER)[0];
        ArrayList<String> eventNumbers = new ArrayList<>();
        StringBuilder eventNumber = new StringBuilder();
        for (char c : eventNumberSection.toCharArray()) {
            if (isNumberOrLetter(c)) {
                eventNumber.append(c);
            } else {
                String eventNumberString = eventNumber.toString();
                System.out.println(eventNumberString);
                if (!eventNumberString.isEmpty()) {
                    System.out.println(eventNumber);
                    eventNumbers.add(eventNumberString);
                    eventNumber = new StringBuilder();
                }
            }
        }

        eventNumbers.add(eventNumber.toString());

        return eventNumbers;
    }

    private boolean isNumberOrLetter(char element) {
        for (char c = 'a'; c <= 'z'; c++) {
            if (element == c) {
                return true;
            }
        }

        for (char c = 'A'; c <= 'Z'; c++) {
            if (element == c) {
                return true;
            }
        }

        for (char c = '0'; c <= '9'; c++) {
            if (element == c) {
                return true;
            }
        }

        return false;
    }

    public void rename(String eventNumber) {
        String offset = "";
        if (TEST_MODE) {
            offset = "/renamed/";
        }
        int i = 1;
        String multiplicity = "";
        if (fileType == IJS_JUDGE_SHEET) {
            multiplicity += " " + i;
        }

        String destination = file.getPath().split(file.getName())[0] + offset + eventNumber + " " + fileType.name() + multiplicity + ".pdf";
        boolean exists = new File(destination).exists();

        while (exists && fileType == IJS_JUDGE_SHEET) {
            i++;
            String[] split;
            split = destination.split(" " + (i - 1) + ".pdf");
            split[0] += " " + i;
            destination = split[0] + ".pdf";
            exists = new File(destination).exists();
        }
        try {
            if (TEST_MODE) {
                FileUtils.copyFile(file, new File(destination));
            } else {
                FileUtils.moveFile(file, new File(destination));
            }
            setRenamed(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String parseEventName() {
        String eventName = "Could not find event name!";
        switch (fileType) {
            case IJS_COVERSHEET, IJS_JUDGE_SHEET, IJS_REFEREE_SHEET, IJS_TC_SHEET, IJS_TS2_SHEET -> eventName = parseEventNameIJS();
            case SIX0_JUDGE_SHEET, SIX0_WORKSHEET -> eventName = parseEventName60();
        }

        return eventName;
    }

    private String getRegex() {
        String regex = null;
        switch (fileType) {
            case IJS_COVERSHEET, IJS_JUDGE_SHEET -> regex = " / ";
            case IJS_REFEREE_SHEET -> regex = " - REFEREE SHEET";
            case IJS_TC_SHEET -> regex = " - TECHNICAL CONTROLLER SHEET";
            case IJS_TS2_SHEET -> regex = " - TECHNICAL SPECIALIST SHEET";
            case SIX0_JUDGE_SHEET, SIX0_WORKSHEET -> regex = "";
        }

        return regex;
    }

    private String parseEventNameIJS() {
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

    private String parseEventName60() {
        String[] lines = contents.split("\n");
        int line;
        for (line = 0; line < lines.length; line++) {
            if (lines[line].toLowerCase().contains(judgeSheet60NameKey.toLowerCase())) {
                line--;
                break;
            }
        }

        String correctedName = lines[line].toUpperCase();

        return correctedName;
    }

    private String extractName(String line) {
        String eventName = line;

        String[] split = eventName.split(getRegex());
        String correctedName = "Error";
        try {
            correctedName = split[0] + " " + split[1];
        } catch (Exception e) {
            System.err.println(eventName);
            System.err.println(getRegex());
            System.err.println(fileType.name());
        }
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

    public boolean isRenamed() {
        return isRenamed;
    }

    public void setRenamed(boolean b) {
        isRenamed = b;
    }
}
