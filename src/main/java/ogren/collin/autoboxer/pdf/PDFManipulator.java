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

package ogren.collin.autoboxer.pdf;

import ogren.collin.autoboxer.Logging;
import ogren.collin.autoboxer.control.MasterController;
import ogren.collin.autoboxer.process.IdentityBundle;
import ogren.collin.autoboxer.process.Role;
import ogren.collin.autoboxer.process.ScheduleElement;
import ogren.collin.autoboxer.utilities.Settings;
import ogren.collin.autoboxer.utilities.error_detection.BoxError;
import ogren.collin.autoboxer.utilities.error_detection.ErrorType;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ogren.collin.autoboxer.pdf.FileType.*;

public class PDFManipulator {
    public static final String WRONG_FILE_TYPE = "wrongfiletype";

    private final PDDocument document;
    private final FileType fileType;
    private final File file;
    private final String contents;
    private boolean isRenamed = false;

    public PDFManipulator(File file, FileType fileType) {
        this.file = file;

        // Load PDF
        try {
            document = Loader.loadPDF(file);
        } catch (IOException e) {
            String errorMessage = "Failed to load PDF at " + file.getPath();
            Logging.logger.fatal("{}\n{}", e, errorMessage);
            throw new RuntimeException(errorMessage);
        }
        this.fileType = fileType;
        contents = readContents();
    }

    // Place a box around a given official's name after a certain number (usually 0) of occurrences. 0 is only not used
    // when an official works two positions at once, i.e. referee and judge.
    public static PDDocument boxOfficial(String name, PDDocument document, int occurrenceToBox) {
        try {
            // Create a rectangle (LocationBundle) which represents the box that will go around the official's name.
            TextLocator textStripper = new TextLocator(name, occurrenceToBox);
            Writer writer = new OutputStreamWriter(new ByteArrayOutputStream());
            textStripper.writeText(document, writer);
            StringLocationBundle locationBundle = textStripper.getLocationBundle();
            if (locationBundle == null) {
                Logging.logger.warn("Failed to find judge {}", name);
                return document;
            }
            PDPage page = document.getPage(0);
            PDPageContentStream stream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, false);
            stream.setStrokingColor(Color.BLACK);
            stream.addRect((float) locationBundle.x() - 4f, PDRectangle.LETTER.getHeight() - (float) locationBundle.y() - 4f, (float) locationBundle.width() + 8f, (float) locationBundle.height() + 10f);
            stream.stroke();
            stream.close();
        } catch (IOException e) {
            String message = "Failed to add an officials' rectangle to a file.";
            Logging.logger.fatal("{} {}", e, message);
            throw new RuntimeException(message);
        }

        return document;
    }

    // If the right file type was selected, return the event name. Otherwise, return the WRONG_FILE_TYPE constant.
    public String retrieveEventName() {
        String eventName;
        if (!isReallyFileType()) {
            eventName = WRONG_FILE_TYPE;
        } else {
            eventName = parseEventName();
        }

        return eventName;
    }

    // Parse contents to a string. Certain file types need to be sorted by position while others do not.
    private String readContents() {
        String contents;
        try {
            boolean sortByPosition = fileType == SIX0_PRIMARY_JUDGE_SHEET || fileType == SIX0_PRIMARY_WORKSHEET || fileType == SIX0_STARTING_ORDERS;
            contents = parseToString(sortByPosition);
        } catch (IOException e) {
            String message = "Failed to read a PDF at " + file.getPath();
            Logging.logger.fatal("{}\n{}", Arrays.toString(e.getStackTrace()), message);
            throw new RuntimeException(message);
        }

        return contents;
    }

    // Verify that a file really is what it is supposed to be by checking for a string that is unique to the sheet type.
    private boolean isReallyFileType() {
        return contents.contains(getUniqueTextByType());
    }

    public void close() {
        try {
            document.close();
        } catch (IOException e) {
            Logging.logger.fatal((e));
            throw new RuntimeException(e);
        }
    }

    // Figure out if the event number in the schedule element equals the event number(s) on the sheet.
    public boolean matchNameToSchedule(ScheduleElement scheduleElement, String eventName) {
        /*
            The required index if greater than zero and less than the number of event numbers in a category will force
            the use of the first event number if the event is a pattern dance first segment, the second event number if
            the event is a pattern dance second segment, and will force the third event number if the event is a free
            dance segment.
         */
        int required_index = -1;
        if (eventName.contains(" P1") || eventName.contains(" PD1") || eventName.contains(" RHYTHM DANCE") || eventName.toUpperCase().contains(" SHORT PROGRAM")) {
            required_index = 0;
        } else if (eventName.contains(" P2") || eventName.contains(" PD2") || eventName.toUpperCase().contains(" FREE SKATE")) {
            required_index = 1;
        } else if (eventName.contains(" FD") || eventName.toUpperCase().contains(" FREE DANCE")) {
            required_index = 2;
        }

        // Get event numbers from name
        ArrayList<String> scrutinizedEventNumbers = scrutinizeName(eventName);

        // Check if the program should force the event number to a specific index.
        if (required_index >= 0) {
            if (required_index > scrutinizedEventNumbers.size() - 1) {
                required_index = scrutinizedEventNumbers.size() - 1;
            }
            return scrutinizedEventNumbers.get(required_index).equalsIgnoreCase(scheduleElement.getEventNumber());
        }

        // Otherwise operate as usual by finding the first match.
        for (String e : scrutinizeName(eventName)) {
            if (e.equalsIgnoreCase(scheduleElement.getEventNumber())) {
                return true;
            }
        }
        return false;
    }

    // This function takes a close look at the event name and figures out what event number(s) are in the name.
    // It returns an array of strings which represent all event numbers in a name.
    private ArrayList<String> scrutinizeName(String eventName) {
        String eventNumberSection = eventName.split(Settings.getEventNameDelimiter())[0];
        ArrayList<String> eventNumbers = new ArrayList<>();
        StringBuilder eventNumber = new StringBuilder();
        for (char c : eventNumberSection.toCharArray()) {
            // Handle how some accountants use a number format of 001 and similar.
            if (Settings.getRemoveLeadingZeros() && c == '0' && eventNumber.isEmpty()) {
                continue;
            }

            // If the character being looked at is either a number or letter then it is a part of an event number.
            if (isNumberOrLetter(c)) {
                eventNumber.append(c);
            } else {
                // If it is not a number or letter, then it must be indicating another segment in the same category.
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

    // Check if a character is a number or letter.
    private boolean isNumberOrLetter(char element) {
        return (element >= 'a' && element <= 'z') || (element >= 'A' && element <= 'Z') || (element >= '0' && element <= '9');
    }

    // Check if a character is a number or letter. Old function, probably should delete.
    @Deprecated
    private boolean isNumberOrLetterOld(char element) {
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

    // Copy PDFs to the renamed/ directory and rename them in such a manner as to store important sorting information in the name.
    public void rename(String eventNumber) {
        String offset = "renamed/";
        int i = 1;
        String destination = getDestination(eventNumber, offset);
        boolean exists = new File(destination).exists();

        // If the file is an IJS judge's sheet, then handle it accordingly.
        while (exists && fileType == IJS_JUDGE_SHEET) {
            i++;
            String[] split;
            split = destination.split(" " + (i - 1) + ".pdf");
            split[0] += " " + i;
            destination = split[0] + ".pdf";
            exists = new File(destination).exists();
        }

        // Try to copy.
        try {
            FileUtils.copyFile(file, new File(destination));
            setRenamed(true);
        } catch (IOException e) {
            Logging.logger.fatal((e));
            throw new RuntimeException(e);
        }
    }

    // Get destination of copied file.
    private String getDestination(String eventNumber, String offset) {
        // Either " referee", " judge", " dance_ts"
        String judgeSheetType = "";
        // Gives a unique name (1, 2, 3...n) to judges' sheets which officials often have more than one sheet of.
        String multiplicity = "";

        // If the file type is an IJS judges' sheet, then determine whether it is for a referee, judge, or
        // technical specialist for dance.
        if (fileType == IJS_JUDGE_SHEET) {

            // Referee condition.
            if (contents.contains("Ref. ")) {
                judgeSheetType = " referee";

                // Video condition
            } else if (contents.contains("Vid. ")) {
                judgeSheetType = " video";
                // Judge condition
            } else if (contents.contains("J1. ") ||
                    contents.contains("J2. ") ||
                    contents.contains("J3. ") ||
                    contents.contains("J4. ") ||
                    contents.contains("J5. ") ||
                    contents.contains("J6. ") ||
                    contents.contains("J7. ") ||
                    contents.contains("J8. ") ||
                    contents.contains("J9. ")) {
                judgeSheetType = " judge";

                //Technical specialist condition
            } else {
                judgeSheetType = " dance_ts";
            }

            // Assign multiplicity to a space followed by the index of multiplicity for the file.
            multiplicity = " " + parseMultiplicityFromFile();
        }

        // For any IJS file that is not a coversheet,
        // assign officialName to the official's name with '_' instead of spaces.
        String officialName = "";
        if (fileType != IJS_COVERSHEET && fileType != SIX0_PRIMARY_JUDGE_SHEET && fileType != SIX0_PRIMARY_WORKSHEET && fileType != SIX0_SECONDARY) {
            officialName = " " + getOfficialName().trim().replace(' ', '_');
        }

        // Return a fully constructed output file path containing relevant information for sorting.
        // Such information should be stored in the file name for fast and easy retrieval. This prevents unnecessary
        // rereading of file contents to determine the purpose of a file.
        return file.getPath().split(file.getName())[0] + offset + eventNumber + " " + fileType.name() + officialName + judgeSheetType + multiplicity + ".pdf";
    }

    private String parseMultiplicityFromFile() {
        if (fileType == IJS_JUDGE_SHEET) {
            String[] lines = contents.split("\n");
            int skaterNumber = 1;
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                String[] split = line.split(" / ");
                if (split.length < 2) {
                    continue;
                }
                if ((split[0] + " " + split[1]).equalsIgnoreCase(getEventName())) {
                    skaterNumber = Integer.parseInt(lines[i + 1].split(". ")[0]);
                    break;
                }
            }
            return "" + ((skaterNumber / 3) + 1);
        } else {
            return "";
        }
    }

    // Wrapper function to determine whether to parse an IJS file or a 6.0 file.
    private String parseEventName() {
        // Default case that assumes the event name is non-existent.
        String eventName = "Could not find event name!";

        // Match file type to parser function.
        switch (fileType) {
            // Check if it is IJS
            case IJS_COVERSHEET, IJS_JUDGE_SHEET, IJS_REFEREE_SHEET, IJS_TC_SHEET, IJS_TS1_SHEET, IJS_TS2_SHEET ->
                // And if it is, run the IJS event name parser.
                    eventName = parseEventNameIJS();
            // Check if it is 6.0
            case SIX0_PRIMARY_JUDGE_SHEET, SIX0_PRIMARY_WORKSHEET, SIX0_SECONDARY, SIX0_STARTING_ORDERS ->
                // And if it is, run the 6.0 event name parser.
                    eventName = parseEventName60();
        }

        return eventName;
    }

    // A function which returns a sequence of text that, if the file contents contains it, strongly indicates that
    // the file really is of the file type expected. Moreover, if it does not have it, then it must not be the file type
    // expected.
    private String getUniqueTextByType() {
        String text = null;
        switch (fileType) {
            case IJS_COVERSHEET, IJS_JUDGE_SHEET -> text = " / ";
            case IJS_REFEREE_SHEET -> text = " - REFEREE SHEET";
            case IJS_TC_SHEET -> text = " - TECHNICAL CONTROLLER SHEET";
            case IJS_TS1_SHEET, IJS_TS2_SHEET -> text = " - TECHNICAL SPECIALIST SHEET";
            case SIX0_PRIMARY_JUDGE_SHEET -> text = "REFEREE AND JUDGES PERSONAL RECORD SHEET";
            case SIX0_PRIMARY_WORKSHEET -> text = "Signature US Figure Skating # End";
            case SIX0_SECONDARY -> text = ""; // This file is so plain, it's basically a saltine cracker.
            case SIX0_STARTING_ORDERS -> text = "";
        }

        return text;
    }

    // Parse an IJS event name out of the loaded file.
    private String parseEventNameIJS() {
        // Split the work into lines by new line character.
        String[] lines = contents.split("\n");
        int line;

        // Handle

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

        String name;
        try {
            name = extractName(lines[line]);
        } catch (ArrayIndexOutOfBoundsException e) {
            MasterController.errors.add(new BoxError(file.getPath(), null, ErrorType.WRONG_FILE_POSITION));
            name = "";
        }

        return name;
    }

    // Parse the event name from a 6.0 file.
    private String parseEventName60() {
        // Split the contents into a string for each line.
        String[] lines = contents.split("\n");
        // Map which line contains the name based on the file type.
        int line = switch (fileType) {
            case SIX0_PRIMARY_WORKSHEET -> 2;
            case SIX0_PRIMARY_JUDGE_SHEET -> 3;
            case SIX0_STARTING_ORDERS -> 1;
            default -> 0;
        };

        // get the name from the array of lines and make it uppercase.
        String name = lines[line].toUpperCase();

        /*
        By default, the program assumes a generic free skating worksheet for secondary worksheets, but since others
        exist and can be used, even if at a lower frequency, they should still be supported. This will likely be an
        ongoing development pattern for other HAL worksheets.
         */
        if (fileType == SIX0_SECONDARY) {
        /*
        Sorcery to make free skating - all levels worksheets work.
        Basically "Spins T.E. P.C. Place" on the first line can be used to infer that this is a
        free skating - all levels worksheet and that the event number is on the next line after
        "Free Skating - All Levels"
         */
            if (name.contains("Spins T.E. P.C. Place".toUpperCase())) {
                try {
                    name = lines[line + 1].toUpperCase().split("Free Skating - All Levels".toUpperCase())[1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }

        /*
        Same as above but the first line is "Failures: 0.1 - 0.4" and the name is 7 lines down.
         */
            if (name.contains("Failures: 0.1 - 0.4".toUpperCase())) {
                name = lines[line + 7].toUpperCase();
            }

            /* might fix solo dance worksheets.
            if (name.contains("Edge Elements".toUpperCase())) {
                name = lines[line + 1].toUpperCase();
            }
             */
        }

        // Do this better when it's not 1:00 AM.
        if (name.contains(" AREF. ")) {
            name = name.split(" AREF. ")[0];
        }
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
        //noinspection UnreachableCode
        try {
            correctedName = split[0] + " " + split[1];
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }
        correctedName = correctedName.toUpperCase();
        return correctedName;
    }

    // Function to parse the loaded file to a string, takes a boolean for whether it should sort text by position.
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

    // Reload the document. Useful when a PDF needs to be copied.
    // PDFs often need to be copied because most simple ways of copying a PDDocument results in a shallow copy.
    // This function basically performs a deep copy of the PDDocument.
    public PDDocument reloadDocument() {
        PDDocument temp = new PDDocument();

        for (PDPage page : document.getPages()) {
            try {
                temp.importPage(page);
            } catch (IOException e) {
                Logging.logger.fatal((e));
                throw new RuntimeException(e);
            }
        }

        return temp;
    }

    // Get the official's name out of the paperwork.
    private String getOfficialName() {
        String[] lines = contents.split("\n");
        if (fileType == IJS_JUDGE_SHEET) {
            Pattern pattern = Pattern.compile("J\\d. {2}");
            for (String s : lines) {
                if (s.contains("Ref.  ")) {
                    return s.split(". {2}")[1].trim();
                }
                if (s.contains("Vid.  ")) {
                    return s.split(". {2}")[1].trim();
                }
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    return s.split(". {2}")[1].trim();
                }
            }

            return "generic";
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

        if (fileType == IJS_TS1_SHEET) {
            for (String s : lines) {
                if (s.startsWith("Technical Specialist 1:   ")) {
                    return s.split("Technical Specialist 1: {3}")[1].trim();
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

    private String findBackupDelimiter(String substring) {
        if (substring.contains("Referee ")) {
            return "Referee ";
        } else if (substring.contains("Judge 1 ")) {
            return "Judge 1 ";
        } else if (substring.contains("Judge 2 ")) {
            return "Judge 2 ";
        } else if (substring.contains("Judge 3")) {
            return "Judge 3 ";
        } else if (substring.contains("Judge 4")) {
            return "Judge 4 ";
        } else if (substring.contains("Judge 5 ")) {
            return "Judge 5 ";
        } else if (substring.contains("Judge 6 ")) {
            return "Judge 6 ";
        } else if (substring.contains("Judge 7 ")) {
            return "Judge 7 ";
        } else if (substring.contains("Judge 8 ")) {
            return "Judge 8 ";
        } else if (substring.contains("Judge 9 ")) {
            return "Judge 9 ";
        } else if (substring.contains("TC ")) {
            return "TC ";
        } else if (substring.contains("TS1 ")) {
            return "TS1 ";
        } else if (substring.contains("TS2 ")) {
            return "TS2 ";
        } else if (substring.contains("DEO ")) {
            return "DEO ";
        } else if (substring.contains("Video ")) {
            return "Video";
        } else if (substring.contains("Ice Ref ")) {
            return "Ice Ref ";
        } else if (substring.contains("Assist Ref ")) {
            return "Assist Ref ";
        }

        return "";
    }

    private String processCoverSheetNameLine(String s, String roleText) {
        String name = s.split(roleText)[1].split(",")[0].trim();
        String backupDelimiter = findBackupDelimiter(name);
        if (!backupDelimiter.isEmpty()) {
            name = name.split(backupDelimiter)[0];
        }

        return name.trim();
    }

    public ArrayList<IdentityBundle> getCoversheetsOfficialNames() {
        ArrayList<IdentityBundle> officialNames = new ArrayList<>();
        // While I said the next line is horrible, I actually think it might be the best possible solution given the
        // parameters I have to work with. I still do not really like it though, feels hacky. -Two months on.
        boolean refSecond = contents.contains("Judge 5 "); // This is horrible.
        ArrayList<String> lines = new ArrayList<>(Arrays.asList(contents.split("\n")));

        /*
            TODO: Handle the issue where an official lacking an affiliation will cause a terrible mess in the paperwork.
            This is a moderately rare occurrence and can be spotted easily by proofing the box prior to printing, but
            it would be nice to be able to either handle it without issue or to throw an error message.
            In either case the program would need to check if the name has one of the official roles in their name.
            As an example, if judge 1 does not have an affiliation, the name often ends up being
            "<judge's name> Referee <referee's name>" so the program could check if the string " Referee " is in the
            official's name in order to determine what the name should be. Alternatively, people could just not hide
            their hometown since it's standard practice to have an affiliation. Silly me for assuming I guess.
         */

        if (fileType == IJS_COVERSHEET) {
            lines.removeLast();
            for (String s : lines) {
                if (!refSecond) {
                    if (s.contains("Referee ")) {
                        String name = processCoverSheetNameLine(s, "Referee ");
                        officialNames.add(new IdentityBundle(name, Role.REFEREE, countOfficialOccurrences(officialNames, name)));
                    }
                }
                Pattern judgePattern = Pattern.compile("Judge \\d ");
                Matcher judgeMatcher = judgePattern.matcher(s);
                if (judgeMatcher.find()) {
                    String delimiter = judgeMatcher.group();
                    int judgeNumber = 0;
                    try {
                        judgeNumber = Integer.parseInt(delimiter.split("Judge ")[1].trim());
                    } catch (NumberFormatException ignored) {
                        Logging.logger.warn("Failed to read judge number on line {}", s);
                    }
                    String name = processCoverSheetNameLine(s, delimiter);
                    officialNames.add(new IdentityBundle(name, Role.JUDGE, judgeNumber, countOfficialOccurrences(officialNames, name)));
                }
                if (refSecond) {
                    if (s.contains("Referee ")) {
                        String name = processCoverSheetNameLine(s, "Referee ");
                        officialNames.add(new IdentityBundle(name, Role.REFEREE, countOfficialOccurrences(officialNames, name)));
                    }
                }
                if (s.contains("Ice Ref ")) {
                    String name = processCoverSheetNameLine(s, "Ice Ref ");
                    officialNames.add(new IdentityBundle(name, Role.AR, countOfficialOccurrences(officialNames, name)));
                }
                if (s.contains("Assist Ref ")) {
                    String name = processCoverSheetNameLine(s, "Assist Ref ");
                    officialNames.add(new IdentityBundle(name, Role.AR, countOfficialOccurrences(officialNames, name)));
                }
                if (s.contains("TC ")) {
                    String name = processCoverSheetNameLine(s, "TC ");
                    officialNames.add(new IdentityBundle(name, Role.TC, countOfficialOccurrences(officialNames, name)));
                }
                if (s.contains("TS1 ")) {
                    String name = processCoverSheetNameLine(s, "TS1 ");
                    officialNames.add(new IdentityBundle(name, Role.TS1, countOfficialOccurrences(officialNames, name)));
                }
                if (s.contains("TS2 ")) {
                    String name = processCoverSheetNameLine(s, "TS2 ");
                    officialNames.add(new IdentityBundle(name, Role.TS2, countOfficialOccurrences(officialNames, name)));
                }
                if (s.contains("DEO ")) {
                    String name = processCoverSheetNameLine(s, "DEO ");
                    officialNames.add(new IdentityBundle(name, Role.DEO, countOfficialOccurrences(officialNames, name)));
                }
                if (s.contains("Video ")) {
                    String name = processCoverSheetNameLine(s, "Video ");
                    officialNames.add(new IdentityBundle(name, Role.VIDEO, countOfficialOccurrences(officialNames, name)));
                }
            }
        } else if (fileType == SIX0_PRIMARY_JUDGE_SHEET || fileType == SIX0_PRIMARY_WORKSHEET) {
            Pattern judgePattern = Pattern.compile("J\\d ");
            for (String s : lines) {
                Matcher judgeMatcher = judgePattern.matcher(s);
                if (judgeMatcher.find()) {
                    String delimiter = judgeMatcher.group();
                    int judgeNumber = 0;
                    try {
                        judgeNumber = Integer.parseInt(delimiter.split("J")[1].trim());
                    } catch (NumberFormatException ignored) {
                        Logging.logger.warn("Failed to read judge number on line {}", s);
                    }
                    String name = s.split(delimiter)[1].trim();
                    officialNames.add(new IdentityBundle(name, Role.JUDGE, judgeNumber, countOfficialOccurrences(officialNames, name)));
                }

                if (s.contains("Ice AR ")) {
                    String name = s.split("Ice AR ")[1].trim();
                    officialNames.add(new IdentityBundle(name, Role.AR, countOfficialOccurrences(officialNames, name)));
                }

                if (s.contains("ARef. ")) {
                    String name = s.split("ARef. ")[1].trim();
                    officialNames.add(new IdentityBundle(name, Role.AR, countOfficialOccurrences(officialNames, name)));
                }

                if (s.contains("Ref. ") && !s.contains("ARef. ")) {
                    String name = s.split("Ref. ")[1].trim();
                    officialNames.add(new IdentityBundle(name, Role.REFEREE, countOfficialOccurrences(officialNames, name)));
                }
            }
        }

        return officialNames;
    }

    // Count how many times an official occurs in a document. Useful for when an official has more than one role.
    private int countOfficialOccurrences(ArrayList<IdentityBundle> officialNames, String name) {
        int count = 1;
        for (IdentityBundle officialName : officialNames) {
            if (officialName.name().contains(name)) {
                count++;
            }
        }

        return count;
    }

    // Add this instance's PDDocument to a destination document.
    public void addToPDF(PDDocument dest) {
        try {
            // Copy pages from this document and import into dest.
            for (PDPage page : getDocument().getPages()) {
                dest.importPage(page);
            }
        } catch (IOException ioe) {
            Logging.logger.fatal(ioe);
        }
    }
}
