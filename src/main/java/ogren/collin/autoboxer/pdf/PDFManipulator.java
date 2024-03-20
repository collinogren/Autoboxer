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
import ogren.collin.autoboxer.process.IdentityBundle;
import ogren.collin.autoboxer.process.Role;
import ogren.collin.autoboxer.process.ScheduleElement;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ogren.collin.autoboxer.control.MasterController.TEST_MODE;
import static ogren.collin.autoboxer.pdf.FileType.*;

public class PDFManipulator {
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
        return contents.contains(getUniqueTextByType());
    }

    public void close() {
        try {
            document.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean matchNameToSchedule(ScheduleElement scheduleElement, String eventName) {
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
                if (!eventNumberString.isEmpty()) {
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
            offset = "renamed/";
        }
        int i = 1;
        String multiplicity = "";
        if (fileType == IJS_JUDGE_SHEET) {
            multiplicity += " " + i;
        }

        String officialName = "";
        if (fileType != IJS_COVERSHEET && fileType != SIX0_PRIMARY_JUDGE_SHEET && fileType != SIX0_PRIMARY_WORKSHEET && fileType != SIX0_SECONDARY) {
            officialName = " " + getOfficialName().trim().replace(' ', '_');
        }

        System.out.println(fileType);
        System.out.println("Official's name: " + officialName);

        String destination = file.getPath().split(file.getName())[0] + offset + eventNumber + " " + fileType.name() + officialName + multiplicity + ".pdf";
        System.out.println(destination);
        boolean exists = new File(destination).exists();

        while (exists && fileType == IJS_JUDGE_SHEET) {
            i++;
            String[] split;
            split = destination.split(" " + (i - 1) + ".pdf");
            split[0] += " " + i;
            destination = split[0] + ".pdf";
            exists = new File(destination).exists();
        }
        System.out.println(destination);
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
            case SIX0_PRIMARY_JUDGE_SHEET, SIX0_PRIMARY_WORKSHEET, SIX0_SECONDARY -> eventName = parseEventName60();
        }

        return eventName;
    }

    private String getUniqueTextByType() {
        String text = null;
        switch (fileType) {
            case IJS_COVERSHEET, IJS_JUDGE_SHEET -> text = " / ";
            case IJS_REFEREE_SHEET -> text = " - REFEREE SHEET";
            case IJS_TC_SHEET -> text = " - TECHNICAL CONTROLLER SHEET";
            case IJS_TS2_SHEET -> text = " - TECHNICAL SPECIALIST SHEET";
            case SIX0_PRIMARY_JUDGE_SHEET -> text = "REFEREE AND JUDGES PERSONAL RECORD SHEET";
            case SIX0_PRIMARY_WORKSHEET -> text = "Signature US Figure Skating # End";
            case SIX0_SECONDARY -> text = ""; // This file is so plain, it's basically a saltine cracker.
        }

        return text;
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
        loop:
        for (line = 0; line < lines.length; line++) {
            switch (fileType) {
                case SIX0_PRIMARY_JUDGE_SHEET -> {
                    if (lines[line].toLowerCase().contains(getUniqueTextByType().toLowerCase())) {
                        line--;
                        break loop;
                    }
                }
                case SIX0_PRIMARY_WORKSHEET -> {
                    if (lines[line].toLowerCase().contains(Main.getCompetitionName().toLowerCase())) {
                        line++;
                        break loop;
                    }
                }
                case SIX0_SECONDARY -> {
                    break loop; // Should be 0.
                }
                default -> System.err.println("Something's gone horribly wrong");
            }

        }

        String correctedName = lines[ line].toUpperCase();

        return correctedName;
    }

    private String extractName(String line) {
        String eventName = line;

        String[] split = eventName.split(getUniqueTextByType());
        String correctedName = "Error";
        try {
            correctedName = split[0] + " " + split[1];
        } catch (Exception e) {
            System.err.println(eventName);
            System.err.println(getUniqueTextByType());
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

    public PDDocument getDocument() {
        return document;
    }

    public PDDocument reloadDocument() {
        try {
            //document.close();
            document = PDDocument.load(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return document;
    }

    private String getOfficialName() {
        String[] lines = contents.split("\n");
        if (fileType == IJS_JUDGE_SHEET) {
            Pattern pattern = Pattern.compile("J\\d. {2}");
            for (int line = 0; line < lines.length; line++) {
                if (lines[line].contains("Ref.  ")) {
                    return lines[line].split(". {2}")[1];
                }
                Matcher matcher = pattern.matcher(lines[line]);
                if (matcher.find()) {
                    return lines[line].split(". {2}")[1];
                }
            }
        }

        if (fileType == IJS_REFEREE_SHEET) {
            for (int line = 0; line < lines.length; line++) {
                if (lines[line].contains(" - REFEREE SHEET")) {
                    return lines[line + 1];
                }
            }
        }

        if (fileType == IJS_TC_SHEET) {
            for (int line = 0; line < lines.length; line++) {
                if (lines[line].startsWith("Technical Controller:   ")) {
                    return lines[line].split("Technical Controller: {3}")[1];
                }
            }
        }

        if (fileType == IJS_TS2_SHEET) {
            for (int line = 0; line < lines.length; line++) {
                if (lines[line].startsWith("Technical Specialist 2:   ")) {
                    return lines[line].split("Technical Specialist 2: {3}")[1];
                }
            }
        }

        return "";
    }

    public ArrayList<IdentityBundle> getCoversheetsOfficialNames() {
        ArrayList<IdentityBundle> officialNames = new ArrayList<>();
        String[] lines = contents.split("\n");
        if (fileType == IJS_COVERSHEET) {
            for (int line = 0; line < lines.length; line++) {
                if (lines[line].contains("Referee ")) {
                    String name = lines[line].split("Referee ")[1].split(",")[0];
                    officialNames.add(new IdentityBundle(name, Role.REFEREE));
                }
                if (lines[line].contains("TC ")) {
                    String name = lines[line].split("TC ")[1].split(",")[0];
                    officialNames.add(new IdentityBundle(name, Role.TC));
                }
                if (lines[line].contains("TS1 ")) {
                    String name = lines[line].split("TS1 ")[1].split(",")[0];
                    officialNames.add(new IdentityBundle(name, Role.TS1));
                }
                if (lines[line].contains("TS2 ")) {
                    String name = lines[line].split("TS2 ")[1].split(",")[0];
                    officialNames.add(new IdentityBundle(name, Role.TS2));
                }
                if (lines[line].contains("DEO ")) {
                    String name = lines[line].split("DEO ")[1].split(",")[0];
                    officialNames.add(new IdentityBundle(name, Role.DEO));
                }
                Pattern judgePattern = Pattern.compile("Judge \\d ");
                Matcher judgeMatcher = judgePattern.matcher(lines[line]);
                if (judgeMatcher.find()) {
                    String delimiter = judgeMatcher.group();
                    String name = lines[line].split(delimiter)[1].split(",")[0];
                    officialNames.add(new IdentityBundle(name, Role.JUDGE));
                }
            }
        } else if (fileType == SIX0_PRIMARY_JUDGE_SHEET || fileType == SIX0_PRIMARY_WORKSHEET) {
            Pattern judgePattern = Pattern.compile("J \\d ");
            for (int line = 0; line < lines.length; line++) {
                Matcher judgeMatcher = judgePattern.matcher(lines[line]);
                if (judgeMatcher.find()) {
                    String delimiter = judgeMatcher.group();
                    String name = lines[line].split(delimiter)[1];
                    officialNames.add(new IdentityBundle(name, Role.JUDGE));
                }

                if (lines[line].startsWith("Ref. ")) {
                    String name = lines[line].split("Ref. ")[1];
                    officialNames.add(new IdentityBundle(name, Role.REFEREE));
                }
            }
        }

        return officialNames;
    }

    public static PDDocument boxOfficial(String name, PDDocument document, int occurrenceToBox) {
        try {
            TextLocator stripper = new TextLocator(name, occurrenceToBox);
            Writer writer = new OutputStreamWriter(new ByteArrayOutputStream());
            stripper.writeText(document, writer);
            StringLocationBundle locationBundle = stripper.getLocationBundle();
            if (locationBundle == null) {
                System.err.println("Failed to find judge "+name);
                return document;
            }
            PDPage page = document.getPage(0);
            PDPageContentStream stream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
            stream.setStrokingColor(Color.BLACK);
            stream.addRect((float) locationBundle.x() - 4f, PDRectangle.LETTER.getHeight() - (float) locationBundle.y() - 4f, (float) locationBundle.width() + 8f, (float) locationBundle.height() + 10f);
            stream.stroke();
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return document;
    }
}
