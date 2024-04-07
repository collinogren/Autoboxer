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

package ogren.collin.autoboxer.pdf;

import ogren.collin.autoboxer.process.IdentityBundle;
import ogren.collin.autoboxer.process.Role;
import ogren.collin.autoboxer.process.ScheduleElement;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.text.PDFTextStripper;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ogren.collin.autoboxer.pdf.FileType.*;

public class PDFManipulator {
    public static final String WRONG_FILE_TYPE = "wrongfiletype";
    private static String eventNameDelimiter = " - ";

    private static boolean removeLeadingZeros = true;
    private final PDDocument document;
    private final FileType fileType;
    private final File file;
    private final String contents;
    private boolean isRenamed = false;

    public static void setEventNameDelimiter(String delimiter) {
        eventNameDelimiter = delimiter;
    }

    public static void setRemoveLeadingZeros(boolean b) {
        removeLeadingZeros = b;
    }

    public PDFManipulator(File file, FileType fileType) {
        this.file = file;
        try {
            document = Loader.loadPDF(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.fileType = fileType;
        contents = readContents();
    }

    public static PDDocument boxOfficial(String name, PDDocument document, int occurrenceToBox) {
        try {
            TextLocator stripper = new TextLocator(name, occurrenceToBox);
            Writer writer = new OutputStreamWriter(new ByteArrayOutputStream());
            stripper.writeText(document, writer);
            StringLocationBundle locationBundle = stripper.getLocationBundle();
            if (locationBundle == null) {
                System.err.println("Failed to find judge " + name);
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

    public static String getEventNameDelimiter() {
        return eventNameDelimiter;
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
        String contents;
        try {
            boolean sortByPosition = fileType == SIX0_PRIMARY_JUDGE_SHEET || fileType == SIX0_PRIMARY_WORKSHEET || fileType == SIX0_STARTING_ORDERS;
            contents = parseToString(sortByPosition);
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
        String eventNumberSection = eventName.split(eventNameDelimiter)[0];
        ArrayList<String> eventNumbers = new ArrayList<>();
        StringBuilder eventNumber = new StringBuilder();
        for (char c : eventNumberSection.toCharArray()) {
            if (removeLeadingZeros && c == '0' && eventNumber.isEmpty()) {
                continue;
            }
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
        String offset = "renamed/";
        int i = 1;
        String destination = getDestination(eventNumber, i, offset);
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
            FileUtils.copyFile(file, new File(destination));
            setRenamed(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getDestination(String eventNumber, int i, String offset) {
        String judgeSheetType = "";
        String multiplicity = "";
        if (fileType == IJS_JUDGE_SHEET) {
            if (contents.contains("Ref. ")) {
                judgeSheetType = " referee";
            } else {
                judgeSheetType = " judge";
            }
            multiplicity += " " + i;
        }

        String officialName = "";
        if (fileType != IJS_COVERSHEET && fileType != SIX0_PRIMARY_JUDGE_SHEET && fileType != SIX0_PRIMARY_WORKSHEET && fileType != SIX0_SECONDARY) {
            officialName = " " + getOfficialName().trim().replace(' ', '_');
        }

        return file.getPath().split(file.getName())[0] + offset + eventNumber + " " + fileType.name() + officialName + judgeSheetType + multiplicity + ".pdf";
    }

    private String parseEventName() {
        String eventName = "Could not find event name!";
        switch (fileType) {
            case IJS_COVERSHEET, IJS_JUDGE_SHEET, IJS_REFEREE_SHEET, IJS_TC_SHEET, IJS_TS2_SHEET ->
                    eventName = parseEventNameIJS();
            case SIX0_PRIMARY_JUDGE_SHEET, SIX0_PRIMARY_WORKSHEET, SIX0_SECONDARY, SIX0_STARTING_ORDERS -> eventName = parseEventName60();
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
            case SIX0_STARTING_ORDERS -> text = "";
        }

        return text;
    }

    private String parseEventNameIJS() {
        String[] lines = contents.split("\n");
        int line;

        if (fileType == IJS_JUDGE_SHEET) {
            int notesValueOccurred = 0;
            for (line = 0; line < lines.length; line++) {
                if (lines[line].toLowerCase().contains("notes value")) {
                    notesValueOccurred++;
                    if (notesValueOccurred >= 3) {
                        line += 2;
                        break;
                    }
                }
            }
        } else {
            line = 1;
        }

        return extractName(lines[line]);
    }

    private String parseEventName60() {
        String[] lines = contents.split("\n");
        int line = switch (fileType) {
            case SIX0_PRIMARY_WORKSHEET -> 2;
            case SIX0_PRIMARY_JUDGE_SHEET -> 3;
            case SIX0_STARTING_ORDERS -> 1;
            default -> 0;
        };

        String name = lines[line].toUpperCase();

        // Do this better when it's not 1:00 AM.
        if (name.contains(" REF. ")) {
            name = name.split(" REF. ")[0];
        }
        if (name.contains(" J1 ")) {
            name = name.split(" J1 ")[0];
        }
        if (name.contains(" J2 ")) {
            name = name.split(" J2 ")[0];
        }
        if (name.contains(" J3 ")) {
            name = name.split(" J3 ")[0];
        }
        if (name.contains(" J4 ")) {
            name = name.split(" J4 ")[0];
        }
        if (name.contains(" J5 ")) {
            name = name.split(" J5 ")[0];
        }
        if (name.contains(" J6 ")) {
            name = name.split(" J6 ")[0];
        }
        if (name.contains(" J7 ")) {
            name = name.split(" J7 ")[0];
        }
        if (name.contains(" J8 ")) {
            name = name.split(" J8 ")[0];
        }
        if (name.contains(" J9 ")) {
            name = name.split(" J9 ")[0];
        }

        return name;
    }

    private String extractName(String line) {
        String[] split = line.split(getUniqueTextByType());
        String correctedName = "Error";
        try {
            correctedName = split[0] + " " + split[1];
        } catch (Exception e) {
            System.err.println(line);
            System.err.println(getUniqueTextByType());
            System.err.println(fileType.name());
        }
        correctedName = correctedName.toUpperCase();
        return correctedName;
    }

    public String parseToString(boolean sortByPosition) throws IOException {
        PDFTextStripper pdfTextStripper = new PDFTextStripper();
        pdfTextStripper.setSortByPosition(sortByPosition);
        return pdfTextStripper.getText(document);
    }

    public String getEventName() {
        return parseEventName();
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
        PDDocument temp = new PDDocument();

        for (PDPage page : document.getPages()) {
            try {
                temp.importPage(page);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return temp;
    }

    private String getOfficialName() {
        String[] lines = contents.split("\n");
        if (fileType == IJS_JUDGE_SHEET) {
            Pattern pattern = Pattern.compile("J\\d. {2}");
            for (String s : lines) {
                if (s.contains("Ref.  ")) {
                    return s.split(". {2}")[1].trim();
                }
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    return s.split(". {2}")[1].trim();
                }
            }
        }

        if (fileType == IJS_REFEREE_SHEET) {
            for (int line = 0; line < lines.length; line++) {
                if (lines[line].contains(" - REFEREE SHEET")) {
                    return lines[line + 1].trim();
                }
            }
        }

        if (fileType == IJS_TC_SHEET) {
            for (String s : lines) {
                if (s.startsWith("Technical Controller:   ")) {
                    return s.split("Technical Controller: {3}")[1].trim();
                }
            }
        }

        if (fileType == IJS_TS2_SHEET) {
            for (String s : lines) {
                if (s.startsWith("Technical Specialist 2:   ")) {
                    return s.split("Technical Specialist 2: {3}")[1].trim();
                }
            }
        }

        return "";
    }

    public ArrayList<IdentityBundle> getCoversheetsOfficialNames() {
        ArrayList<IdentityBundle> officialNames = new ArrayList<>();
        boolean refSecond = contents.contains("Judge 5 "); // This is horrible.
        String[] lines = contents.split("\n");
        if (fileType == IJS_COVERSHEET) {
            for (String s : lines) {
                if (!refSecond) {
                    if (s.contains("Referee ")) {
                        String name = s.split("Referee ")[1].split(",")[0].trim();
                        officialNames.add(new IdentityBundle(name, Role.REFEREE, countOfficialOccurrences(officialNames, name)));
                    }
                }
                Pattern judgePattern = Pattern.compile("Judge \\d ");
                Matcher judgeMatcher = judgePattern.matcher(s);
                if (judgeMatcher.find()) {
                    String delimiter = judgeMatcher.group();
                    String name = s.split(delimiter)[1].split(",")[0].trim();
                    officialNames.add(new IdentityBundle(name, Role.JUDGE, countOfficialOccurrences(officialNames, name)));
                }
                if (refSecond) {
                    if (s.contains("Referee ")) {
                        String name = s.split("Referee ")[1].split(",")[0].trim();
                        officialNames.add(new IdentityBundle(name, Role.REFEREE, countOfficialOccurrences(officialNames, name)));
                    }
                }
                if (s.contains("TC ")) {
                    String name = s.split("TC ")[1].split(",")[0].trim();
                    officialNames.add(new IdentityBundle(name, Role.TC, countOfficialOccurrences(officialNames, name)));
                }
                if (s.contains("TS1 ")) {
                    String name = s.split("TS1 ")[1].split(",")[0].trim();
                    officialNames.add(new IdentityBundle(name, Role.TS1, countOfficialOccurrences(officialNames, name)));
                }
                if (s.contains("TS2 ")) {
                    String name = s.split("TS2 ")[1].split(",")[0].trim();
                    officialNames.add(new IdentityBundle(name, Role.TS2, countOfficialOccurrences(officialNames, name)));
                }
                if (s.contains("DEO ")) {
                    String name = s.split("DEO ")[1].split(",")[0].trim();
                    officialNames.add(new IdentityBundle(name, Role.DEO, countOfficialOccurrences(officialNames, name)));
                }
            }
        } else if (fileType == SIX0_PRIMARY_JUDGE_SHEET || fileType == SIX0_PRIMARY_WORKSHEET) {
            Pattern judgePattern = Pattern.compile("J\\d ");
            for (String s : lines) {
                Matcher judgeMatcher = judgePattern.matcher(s);
                if (judgeMatcher.find()) {
                    String delimiter = judgeMatcher.group();
                    String name = s.split(delimiter)[1].trim();
                    officialNames.add(new IdentityBundle(name, Role.JUDGE, countOfficialOccurrences(officialNames, name)));
                }

                if (s.contains("Ref. ")) {
                    String name = s.split("Ref. ")[1].trim();
                    officialNames.add(new IdentityBundle(name, Role.REFEREE, countOfficialOccurrences(officialNames, name)));
                }
            }
        }

        return officialNames;
    }

    private int countOfficialOccurrences(ArrayList<IdentityBundle> officialNames, String name) {
        int count = 1;
        for (IdentityBundle officialName : officialNames) {
            if (officialName.name().contains(name)) {
                count++;
            }
        }

        return count;
    }
}
