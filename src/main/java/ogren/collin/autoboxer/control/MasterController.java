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

package ogren.collin.autoboxer.control;

import ogren.collin.autoboxer.Main;
import ogren.collin.autoboxer.UI;
import ogren.collin.autoboxer.pdf.EventSet;
import ogren.collin.autoboxer.pdf.FileType;
import ogren.collin.autoboxer.pdf.PDFManipulator;
import ogren.collin.autoboxer.process.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MasterController {

    private static final String COVERSHEET_DIR = "coversheets";
    private static final String JUDGE_SHEETS_DIR = "judges";
    private static final String TECH_PANEL_DIR = "tech";
    private static final String SIX0_PRIMARY_DIR = "60";
    private static final String SIX0_SUBSEQUENT_DIR = "60_sub";

    private final ArrayList<Official> officials = new ArrayList<>();

    private static String baseDir;

    private final Schedule schedule;

    private ArrayList<File> coversheets = new ArrayList<>();
    private ArrayList<File> judgeSheets = new ArrayList<>();
    private ArrayList<File> technicalSheets = new ArrayList<>();
    private ArrayList<File> six0Sheets = new ArrayList<>();
    private ArrayList<File> six0SecondarySheets = new ArrayList<>();

    public static String getBaseDir() {
        return baseDir;
    }

    public MasterController(String baseDir) {
        MasterController.baseDir = baseDir;
        try {
            FileUtils.deleteDirectory(new File(baseDir+"/"+COVERSHEET_DIR+"/"+"renamed"));
            FileUtils.deleteDirectory(new File(baseDir+"/"+JUDGE_SHEETS_DIR+"/"+"renamed"));
            FileUtils.deleteDirectory(new File(baseDir+"/"+TECH_PANEL_DIR+"/"+"renamed"));
            FileUtils.deleteDirectory(new File(baseDir+"/"+SIX0_PRIMARY_DIR+"/"+"renamed"));
            FileUtils.deleteDirectory(new File(baseDir+"/"+SIX0_SUBSEQUENT_DIR+"/"+"renamed"));
            FileUtils.deleteDirectory(new File(baseDir+"/box"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        schedule = new Schedule(new File(baseDir + "/schedule.txt"));
    }

    public void begin() {
        renameFiles();
        doTheBox();
        for (Official official : officials) {
            official.save();
        }
        UI.setProgress(100);
        UI.setDone(true);
    }

    private void renameFiles() {
        coversheets = getAllFiles(COVERSHEET_DIR);
        judgeSheets = getAllFiles(JUDGE_SHEETS_DIR);
        technicalSheets = getAllFiles(TECH_PANEL_DIR);
        six0Sheets = getAllFiles(SIX0_PRIMARY_DIR);
        six0SecondarySheets = getAllFiles(SIX0_SUBSEQUENT_DIR);
        try {
            rename(coversheets, FileType.IJS_COVERSHEET);
            rename(judgeSheets, FileType.IJS_JUDGE_SHEET);
            rename(technicalSheets, FileType.IJS_REFEREE_SHEET);
            rename(technicalSheets, FileType.IJS_TC_SHEET);
            rename(technicalSheets, FileType.IJS_TS2_SHEET);
            rename(six0Sheets, FileType.SIX0_PRIMARY_JUDGE_SHEET);
            rename(six0Sheets, FileType.SIX0_PRIMARY_WORKSHEET);
            rename(six0SecondarySheets, FileType.SIX0_SECONDARY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        coversheets = getAllFiles(COVERSHEET_DIR+"/renamed/");
        judgeSheets = getAllFiles(JUDGE_SHEETS_DIR+"/renamed/");
        technicalSheets = getAllFiles(TECH_PANEL_DIR+"/renamed/");
        six0Sheets = getAllFiles(SIX0_PRIMARY_DIR+"/renamed/");
        six0SecondarySheets = getAllFiles(SIX0_SUBSEQUENT_DIR+"/renamed/");
    }

    private void rename(ArrayList<File> files, FileType fileType) throws IOException {
        int numberOfEvents = schedule.getElements().size();
        ArrayList<PDFManipulator> pdfManipulators = new ArrayList<>();
        for (File file : files) {
            PDFManipulator pdfManipulator = new PDFManipulator(file, fileType);
            if (pdfManipulator.getEventName().equals(PDFManipulator.WRONG_FILE_TYPE)) { // Handle the fact that all technical panel sheets are printed into the same directory.
                continue;
            }
            pdfManipulators.add(pdfManipulator);
        }
        for (ScheduleElement se : schedule.getElements()) {
            for (PDFManipulator pdfManipulator : pdfManipulators) {
                String eventName = pdfManipulator.retrieveEventName();
                if (pdfManipulator.matchNameToSchedule(se, eventName) && !pdfManipulator.isRenamed()) {
                    if (se.getEventName().isEmpty()) {
                        se.setEventName(eventName.trim());
                    }
                    pdfManipulator.close();
                    pdfManipulator.rename(se.getEventNumber());
                }
            }

            UI.addProgress(((1.0 / numberOfEvents) / 8.0) / 2.0);
        }

        for (PDFManipulator pdfManipulator : pdfManipulators) {
            pdfManipulator.close();
        }
    }

    private ArrayList<File> getAllFiles(String relativeDir) {
        return (ArrayList<File>) FileUtils.listFiles(new File(baseDir + "/" + relativeDir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    }

    //Read schedule, one by one, look for event number from file names. Look in coversheets first, then look in 60. These are the two locations to get coversheets.
    // Second, once a coversheet has been selected, go through each official and make a copy, circle the judge, and collect all relevant pdfs and place them into the official's set of papers.
    private void doTheBox() {
        int numberOfEvents = schedule.getElements().size();
        for (ScheduleElement se : schedule.getElements()) {
            for (File file : coversheets) {
                if (matchFileNameToEventNumber(se, file)) {
                    String eventNumber = se.getEventNumber();
                    PDFManipulator pdfManipulator = new PDFManipulator(file, FileType.IJS_COVERSHEET);
                    ArrayList<IdentityBundle> identityBundles = pdfManipulator.getCoversheetsOfficialNames();
                    processEvent(eventNumber, identityBundles, pdfManipulator, se, true);
                }
            }
            for (File file : six0Sheets) {
                if (matchFileNameToEventNumber(se, file)) {
                    String eventNumber = se.getEventNumber();
                    PDFManipulator pdfManipulator = new PDFManipulator(file, FileType.SIX0_PRIMARY_JUDGE_SHEET);
                    ArrayList<IdentityBundle> identityBundles = pdfManipulator.getCoversheetsOfficialNames();
                    processEvent(eventNumber, identityBundles, pdfManipulator, se, false);
                }
            }

            UI.addProgress(((1.0 / numberOfEvents)) / 2.0);
        }
    }

    private void processEvent(String eventNumber, ArrayList<IdentityBundle> identityBundles, PDFManipulator pdfManipulator, ScheduleElement scheduleElement, boolean ijs) {
        for (IdentityBundle identity : identityBundles) {
            int officialIndex = getOfficialIndex(identity.name());
            PDDocument coversheet = pdfManipulator.reloadDocument();
            PDDocument circledCoversheet = PDFManipulator.boxOfficial(officials.get(officialIndex).getName(), coversheet, identity.occurrenceToBox());
            EventSet eventSet = new EventSet();
            eventSet.push(circledCoversheet);
            if (ijs) {
                switch (identity.role()) {
                    case REFEREE -> {
                        retrieveSheets(eventNumber, identity, eventSet, technicalSheets, FileType.IJS_REFEREE_SHEET);
                        retrieveSheets(eventNumber, identity, eventSet, judgeSheets, FileType.IJS_JUDGE_SHEET);
                    }
                    case JUDGE -> retrieveSheets(eventNumber, identity, eventSet, judgeSheets, FileType.IJS_JUDGE_SHEET);
                    case TC -> retrieveSheets(eventNumber, identity, eventSet, technicalSheets, FileType.IJS_TC_SHEET);
                    case TS2 -> retrieveSheets(eventNumber, identity, eventSet, technicalSheets, FileType.IJS_TS2_SHEET);
                }
            } else {
                retrieveSix0SecondarySheets(eventNumber, eventSet);
            }

            officials.get(officialIndex).addDocument(eventSet);
            officials.get(officialIndex).tryAddScheduleBundle(scheduleElement, identity.role());
        }
    }

    private void retrieveSix0SecondarySheets(String eventNumber, EventSet eventSet) {
        for (File file : six0SecondarySheets) {
            String[] split = file.getName().split(" ");
            if (split[0].equals(eventNumber)) {
                try {
                    eventSet.push(PDDocument.load(file));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void retrieveSheets(String eventNumber, IdentityBundle identity, EventSet eventSet, ArrayList<File> sheets, FileType fileType) {
        for (File file : sheets) {
            String[] split = file.getName().split(" ");
            boolean incorrectFile = true;
            for (FileType ft : matchRoleToFileTypeIJS(identity)) {
                if (fileType == ft) {
                    incorrectFile = false;
                    break;
                }
            }

            if (incorrectFile) {
                continue;
            }

            try {
                if (fileType != FileType.IJS_JUDGE_SHEET) {
                    split[2] = split[2].split(".pdf")[0];
                } else {
                    if ((split[3].equals("judge") && identity.role() == Role.REFEREE) || (split[3].equals("referee") && identity.role() == Role.JUDGE)) {
                        continue;
                    }
                }

                if (split[0].equals(eventNumber) && split[1].equals(fileType.name()) && split[2].replace('_', ' ').equals(identity.name())) {
                    try {
                        eventSet.push(PDDocument.load(file));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                System.out.println("Could not find match for "+file.getName()+". Maybe it is an extra file?");
            }
        }
    }

    private ArrayList<FileType> matchRoleToFileTypeIJS(IdentityBundle identityBundle) {
        ArrayList<FileType> types = new ArrayList<>();
        types.add(FileType.IJS_COVERSHEET);
        switch(identityBundle.role()) {
            case REFEREE -> {
                types.add(FileType.IJS_REFEREE_SHEET);
                types.add(FileType.IJS_JUDGE_SHEET);
                return types;
            }
            case JUDGE -> {
                types.add(FileType.IJS_JUDGE_SHEET);
                return types;
            }
            case TC -> {
                types.add(FileType.IJS_TC_SHEET);
                return types;
            }
            case TS2 -> {
                types.add(FileType.IJS_TS2_SHEET);
                return types;
            }
            case TS1, VIDEO, DEO -> {
                return types;
            }
        }

        return types;
    }

    private int getOfficialIndex(String name) {
        for (int i = 0; i < officials.size(); i++) {
            if (officials.get(i).getName().equals(name)) {
                return i;
            }
        }

        officials.add(new Official(name));
        return officials.size() - 1;
    }

    private boolean matchFileNameToEventNumber(ScheduleElement scheduleElement, File file) {
        String eventNumber = file.getName().split(" ")[0];
        return eventNumber.equalsIgnoreCase(scheduleElement.getEventNumber());
    }
}
