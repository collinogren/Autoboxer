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

package ogren.collin.autoboxer.control;

import ogren.collin.autoboxer.Logging;
import ogren.collin.autoboxer.gui.ProgressGUIFX;
import ogren.collin.autoboxer.pdf.EventSet;
import ogren.collin.autoboxer.pdf.FileType;
import ogren.collin.autoboxer.pdf.PDFManipulator;
import ogren.collin.autoboxer.process.*;
import ogren.collin.autoboxer.utilities.Settings;
import ogren.collin.autoboxer.utilities.error_detection.BoxError;
import ogren.collin.autoboxer.utilities.error_detection.ErrorType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MasterController {

    // Constants for directories used to store the sheets from ISUCalcFS and Hal.
    public static final String COVERSHEET_DIR = "coversheets";
    public static final String JUDGE_SHEETS_DIR = "judges";
    public static final String TECH_PANEL_DIR = "tech";
    public static final String SIX0_PRIMARY_DIR = "60";
    public static final String SIX0_SUBSEQUENT_DIR = "60_sub";
    public static final String SIX0_STARTING_ORDERS_DIR = "60_starting_orders";

    // Constants for output directories for Autoboxer
    private static final String BOX_DIR = "box";
    private static final String TA_DIR = "box/TA";
    private static final String STARTING_ORDER_DIR = "box/Starting Orders";

    // Array to hold all BoxErrors that are emitted during box generation.
    public static ArrayList<BoxError> errors;

    // Base directory that was selected by the user.
    private static String baseDir;

    private ArrayList<Official> officials;
    private static Schedule schedule;

    // Arrays to hold all the different sheet types.
    private ArrayList<File> coversheets;
    private ArrayList<File> judgeSheets;
    private ArrayList<File> technicalSheets;
    private ArrayList<File> six0Sheets;
    private ArrayList<File> six0SecondarySheets;
    private ArrayList<File> six0StartingOrders;

    private void initArrays() {
        errors = new ArrayList<>();
        officials = new ArrayList<>();
        coversheets = new ArrayList<>();
        judgeSheets = new ArrayList<>();
        technicalSheets = new ArrayList<>();
        six0Sheets = new ArrayList<>();
        six0SecondarySheets = new ArrayList<>();
        six0StartingOrders = new ArrayList<>();
    }

    public MasterController(String baseDir) {
        MasterController.baseDir = baseDir;
        BuildByBoard.clearAll();
        initArrays();

       // Delete temporary directories.
        try {
            FileUtils.deleteDirectory(new File(baseDir + "/" + COVERSHEET_DIR + "/" + "renamed"));
            FileUtils.deleteDirectory(new File(baseDir + "/" + JUDGE_SHEETS_DIR + "/" + "renamed"));
            FileUtils.deleteDirectory(new File(baseDir + "/" + TECH_PANEL_DIR + "/" + "renamed"));
            FileUtils.deleteDirectory(new File(baseDir + "/" + SIX0_PRIMARY_DIR + "/" + "renamed"));
            FileUtils.deleteDirectory(new File(baseDir + "/" + SIX0_SUBSEQUENT_DIR + "/" + "renamed"));
            FileUtils.deleteDirectory(new File(baseDir + "/" + SIX0_STARTING_ORDERS_DIR + "/" + "renamed"));
            FileUtils.deleteDirectory(new File(baseDir + "/" + TA_DIR));
            FileUtils.deleteDirectory(new File(baseDir + "/" + STARTING_ORDER_DIR));
            FileUtils.deleteDirectory(new File(baseDir + "/" + BOX_DIR));
        } catch (IOException e) {
            String message = "Could not delete temp directories.\nMake sure none are open in another application.";
            Logging.logger.fatal("{}\n{}", Arrays.toString(e.getStackTrace()), message);
            throw new RuntimeException(message);
        }

        // Read the schedule data from the generated schedule.txt file.
        schedule = new Schedule(new File(baseDir + "/schedule.txt"));
    }

    public static String getBaseDir() {
        return baseDir;
    }

    public static Schedule getSchedule() {
        return schedule;
    }

    // Sub function to get the right PDFManipulators for the rename function.
    private ArrayList<PDFManipulator> getPDFManipulatorsToRename(ArrayList<File> files, FileType fileType) {
        ArrayList<PDFManipulator> pdfManipulators = new ArrayList<>();

        // For every file, make a PDFManipulator to extract the event name and ensure that the right file type is chosen.
        for (File file : files) {
            PDFManipulator pdfManipulator = new PDFManipulator(file, fileType);

            // Handle the fact that all technical panel sheets are printed into the same directory.
            if (pdfManipulator.getEventName().equals(PDFManipulator.WRONG_FILE_TYPE)) {
                continue;
            }

            try {
                if (fileType == FileType.IJS_TS1_SHEET && !pdfManipulator.parseToString(false).contains("Technical Specialist 1:   ")) {
                    continue;
                }

                if (fileType == FileType.IJS_TS2_SHEET && !pdfManipulator.parseToString(false).contains("Technical Specialist 2:   ")) {
                    continue;
                }

                pdfManipulators.add(pdfManipulator);
            } catch (IOException e) {
                Logging.logger.error("Failed to read a TS1 or TS2 sheet.\n{}", Arrays.toString(e.getStackTrace()));
                errors.add(new BoxError(null, null, ErrorType.TS_FILE_READ_ERROR));
            }
        }
        return pdfManipulators;
    }

    /*
         Function to begin building the box.
         First, rename the files.
         File name format is #event SHEET_TYPE for most but for IJS judges' sheets they are # SHEET_TYPE First_Last judge/referee #multiplcity.
         Files are renamed to make it quicker and easier to sort.
         Next, sort the paperwork, then print.
         Finally, ensure the progress bar shows 100 percent when it is done.
     */
    public void begin() {
        renameFiles();
        doTheBox();
        save();
        ProgressGUIFX.setProgress(100);
        ProgressGUIFX.setDone(true);
    }

    private void save() {
        if (!Settings.getBuildByBoard()) {
            if (Settings.getCombinePaperwork()) {
                if (Settings.getCombineRinksByTime()) {
                    Official.saveAllCombined(officials);
                } else {
                    Official.saveAllByRink(officials);
                }
            } else {
                for (Official official : officials) {
                    if (Settings.getCombineRinksByTime()) {
                        official.saveCombined();
                    } else {
                        official.saveByRink();
                    }
                }
            }
        } else {
            if (Settings.getCombinePaperwork()) {
                BuildByBoard.saveAll();
            } else {
                BuildByBoard.save();
            }
        }
    }

    /*
         For every sheet type, get the files from the directory they reside in, rename and copy to the renamed directory,
         then re-retrieve all files from the renamed directory.
         This function can easily be multithreaded, so it is.
     */
    private void renameFiles() {
        // Initially get technical panel sheets because this will be accessed across threads.
        technicalSheets = getAllFiles(TECH_PANEL_DIR);

        // Create executor service to handle multithreading.
        try (ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2)) {

            // Rename coversheets.
            executor.execute(() -> {
                try {
                    coversheets = getAllFiles(COVERSHEET_DIR);
                    rename(coversheets, FileType.IJS_COVERSHEET);
                    coversheets = getAllFiles(COVERSHEET_DIR + "/renamed/");
                } catch (IOException e) {
                    String message = "Failed to parse coversheets.";
                    Logging.logger.fatal("{}\n{}", Arrays.toString(e.getStackTrace()), message);
                    throw new RuntimeException(message);
                }
            });

            // Rename IJS judges' sheets.
            executor.execute(() -> {
                try {
                    judgeSheets = getAllFiles(JUDGE_SHEETS_DIR);
                    rename(judgeSheets, FileType.IJS_JUDGE_SHEET);
                    judgeSheets = getAllRenamedJudgesSheetsInOrder();
                } catch (IOException e) {
                    String message = "Failed to parse IJS judges' sheets.";
                    Logging.logger.fatal("{}\n{}", Arrays.toString(e.getStackTrace()), message);
                    throw new RuntimeException(message);
                }
            });

            // Rename IJS referee sheets.
            executor.execute(() -> {
                try {
                    rename(technicalSheets, FileType.IJS_REFEREE_SHEET);
                } catch (IOException e) {
                    String message = "Failed to parse IJS referee sheets.";
                    Logging.logger.fatal("{}\n{}", Arrays.toString(e.getStackTrace()), message);
                    throw new RuntimeException(message);
                }
            });

            // Rename technical controller sheets.
            executor.execute(() -> {
                try {
                    rename(technicalSheets, FileType.IJS_TC_SHEET);
                } catch (IOException e) {
                    String message = "Failed to parse technical controller sheets.";
                    Logging.logger.fatal("{}\n{}", Arrays.toString(e.getStackTrace()), message);
                    throw new RuntimeException(message);
                }
            });

            // Rename technical specialist 1 sheets.
            executor.execute(() -> {
                try {
                    rename(technicalSheets, FileType.IJS_TS1_SHEET);
                } catch (IOException e) {
                    String message = "Failed to parse technical specialist 1 sheets.";
                    Logging.logger.fatal("{}\n{}", Arrays.toString(e.getStackTrace()), message);
                    throw new RuntimeException(message);
                }
            });

            // Rename technical specialist 2 sheets.
            executor.execute(() -> {
                try {
                    rename(technicalSheets, FileType.IJS_TS2_SHEET);
                } catch (IOException e) {
                    String message = "Failed to parse technical specialist 2 sheets.";
                    Logging.logger.fatal("{}\n{}", Arrays.toString(e.getStackTrace()), message);
                    throw new RuntimeException(message);
                }
            });

            // rename 6.0 primary and secondary sheets.
            executor.execute(() -> {
                try {
                    six0Sheets = getAllFiles(SIX0_PRIMARY_DIR);
                    rename(six0Sheets, FileType.SIX0_PRIMARY_JUDGE_SHEET);
                    rename(six0Sheets, FileType.SIX0_PRIMARY_WORKSHEET);
                    six0Sheets = getAllFiles(SIX0_PRIMARY_DIR + "/renamed/");
                    six0SecondarySheets = getAllFiles(SIX0_SUBSEQUENT_DIR);
                    rename(six0SecondarySheets, FileType.SIX0_SECONDARY);
                    six0SecondarySheets = getAllFiles(SIX0_SUBSEQUENT_DIR + "/renamed/");
                } catch (IOException e) {
                    String message = "Failed to parse 6.0 judges' sheets / worksheets / subsequent worksheets.";
                    Logging.logger.fatal("{}\n{}", Arrays.toString(e.getStackTrace()), message);
                    throw new RuntimeException(message);
                }
            });

            // Rename 6.0 starting orders.
            executor.execute(() -> {
                try {
                    six0StartingOrders = getAllFiles(SIX0_STARTING_ORDERS_DIR);
                    rename(six0StartingOrders, FileType.SIX0_STARTING_ORDERS);
                    six0StartingOrders = getAllFiles(SIX0_STARTING_ORDERS_DIR + "/renamed");
                } catch (IOException e) {
                    String message = "Failed to parse 6.0 starting orders.";
                    Logging.logger.fatal("{}\n{}", Arrays.toString(e.getStackTrace()), message);
                    throw new RuntimeException(message);
                }
            });

            // Wait for processes to finish and destroy the executor.
            executor.shutdown();

            try {
                boolean ignored = executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                Logging.logger.fatal((e));
                throw new RuntimeException(e);
            }
        }

        /*
            Once again since technical panel sheets are processed by multiple threads but are dumped into the same
            folder, retrieval of the renamed files must wait until execution of the multithreaded code is finished.
         */
        technicalSheets = getAllFiles(TECH_PANEL_DIR + "/renamed/");
    }

    // The sub function to actually rename the files within each directory.
    private void rename(ArrayList<File> files, FileType fileType) throws IOException {
        int numberOfEvents = schedule.getElements().size();
        ArrayList<PDFManipulator> pdfManipulators = getPDFManipulatorsToRename(files, fileType);
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

            se.setProcessed(true);
            ProgressGUIFX.addProgress(((1.0 / numberOfEvents) / 10.0) / 2.0);
        }

        for (PDFManipulator pdfManipulator : pdfManipulators) {
            pdfManipulator.close();
        }
    }

    private ArrayList<File> getAllFiles(String relativeDir) {
        return (ArrayList<File>) FileUtils.listFiles(new File(baseDir + "/" + relativeDir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    }

    private ArrayList<File> getAllRenamedJudgesSheetsInOrder() {
        ArrayList<File> judgesSheets = getAllFiles("judges/renamed/");
        judgesSheets.sort(Comparator.comparing((File f) -> Integer.parseInt((f.getName()).split(" ")[4].split(".pdf")[0])));
        return judgesSheets;
    }

    // Read schedule, one by one, look for event number from file names. Look in coversheets first, then look in 60. These are the two locations to get coversheets.
    // Second, once a coversheet has been selected, go through each official and make a copy, circle the judge, and collect all relevant pdfs and place them into the official's set of papers.
    private void doTheBox() {
        HashMap<String, PDDocument> taSheets = new HashMap<>();
        HashMap<String, PDDocument> startingOrders = new HashMap<>();
        int numberOfEvents = schedule.getElements().size();
        for (ScheduleElement se : schedule.getElements()) {
            boolean[] sheetsExistPtr = {false};

            sortIJS(se, startingOrders, taSheets, sheetsExistPtr);

            sort60StartingOrders(se, startingOrders, sheetsExistPtr);

            sort60Primary(se, sheetsExistPtr);

            if (!sheetsExistPtr[0]) {
                errors.add(new BoxError(se.getEventNumber(), null, ErrorType.MISSING_PAPERS_FOR_SCHEDULED_EVENT));
            }

            ProgressGUIFX.addProgress(((1.0 / numberOfEvents)) / 2.0);
        }

        generateStartingOrders(startingOrders);
        generateTASheets(taSheets);
    }

    private void sortIJS(ScheduleElement se, HashMap<String, PDDocument> startingOrders, HashMap<String, PDDocument> taSheets, boolean[] sheetsExistPtr) {
        for (File file : coversheets) {
            if (se.matchFileNameToEventNumber(file)) {
                String eventNumber = se.getEventNumber();
                PDFManipulator pdfManipulator = new PDFManipulator(file, FileType.IJS_COVERSHEET);
                sortIJSTAAndStartingOrders(Settings.getGenerateStartingOrders(), startingOrders, se, pdfManipulator);

                sortIJSTAAndStartingOrders(Settings.getGenerateTASheets(), taSheets, se, pdfManipulator);
                ArrayList<IdentityBundle> identityBundles = pdfManipulator.getCoversheetsOfficialNames();
                processEvent(eventNumber, identityBundles, pdfManipulator, se, true);
                sheetsExistPtr[0] = true;
            }
        }
    }

    // Sort the top sheets for 6.0.
    private void sort60Primary(ScheduleElement se, boolean[] sheetsExistPtr) {
        for (File file : six0Sheets) {
            if (se.matchFileNameToEventNumber(file)) {
                String eventNumber = se.getEventNumber();
                PDFManipulator pdfManipulator = new PDFManipulator(file, FileType.SIX0_PRIMARY_JUDGE_SHEET);
                ArrayList<IdentityBundle> identityBundles = pdfManipulator.getCoversheetsOfficialNames();
                processEvent(eventNumber, identityBundles, pdfManipulator, se, false);
                sheetsExistPtr[0] = true;
            }
        }
    }

    // Sort the starting orders for 6.0
    private void sort60StartingOrders(ScheduleElement se, HashMap<String, PDDocument> startingOrders, boolean[] sheetsExistPtr) {
        for (File file : six0StartingOrders) {
            if (se.matchFileNameToEventNumber(file)) {
                if (Settings.getGenerateStartingOrders()) {
                    PDFManipulator pdfManipulator = new PDFManipulator(file, FileType.SIX0_STARTING_ORDERS);
                    if (!startingOrders.containsKey(se.getRink())) {
                        startingOrders.put(se.getRink(), new PDDocument());
                    }

                    pdfManipulator.addToPDF(startingOrders.get(se.getRink()));
                }

                sheetsExistPtr[0] = true;
            }
        }
    }

    // Sort IJSCompanion Judges' sheets for starting orders and TA sheets.
    private void sortIJSTAAndStartingOrders(boolean generate, HashMap<String, PDDocument> sheets, ScheduleElement se, PDFManipulator pdfManipulator) {
        if (generate) {
            if (!sheets.containsKey(se.getRink())) {
                sheets.put(se.getRink(), new PDDocument());
            }

            pdfManipulator.addToPDF(sheets.get(se.getRink()));
        }
    }

    // Put the IJS and 6.0 starting orders together to create the start order set.
    private void generateStartingOrders(HashMap<String, PDDocument> startingOrders) {
        if (Settings.getGenerateStartingOrders()) {
            for (String rink : schedule.getRinks()) {
                if (startingOrders.containsKey(rink)) {
                    try {
                        File file = new File(baseDir + "/" + STARTING_ORDER_DIR + "/Starting Orders - " + rink + ".pdf");
                        if (!file.getParentFile().exists()) {
                            if (!file.getParentFile().mkdirs()) {
                                Logging.logger.fatal("Failed to create directories necessary for printing starting orders.");
                                throw new RuntimeException("Failed to create directories necessary for printing starting orders.");
                            }
                        }
                        PDDocument document = startingOrders.get(rink);
                        document.save(file);
                    } catch (IOException e) {
                        Logging.logger.fatal((e));
                        throw new RuntimeException("Failed to save starting orders.");
                    }
                }
            }
        }
    }

    // Place all IJSCompanion judges' sheets in 104 order to create the TA sheets set.
    private void generateTASheets(HashMap<String, PDDocument> taSheets) {
        if (Settings.getGenerateTASheets()) {
            for (String rink : schedule.getRinks()) {
                if (taSheets.containsKey(rink)) {
                    try {
                        File file = new File(baseDir + "/" + TA_DIR + "/TA Sheets - " + rink + ".pdf");
                        if (!file.getParentFile().exists()) {
                            if (!file.getParentFile().mkdirs()) {
                                Logging.logger.fatal("Failed to create directories necessary for printing TA sheets.");
                                throw new RuntimeException("Failed to create directories necessary for printing TA sheets.");
                            }
                        }
                        PDDocument document = taSheets.get(rink);
                        document.save(file);
                    } catch (IOException e) {
                        Logging.logger.fatal((e));
                        throw new RuntimeException("Failed to save TA sheets.");
                    }
                }
            }
        }
    }

    private void processEvent(String eventNumber, ArrayList<IdentityBundle> identityBundles, PDFManipulator pdfManipulator, ScheduleElement scheduleElement, boolean ijs) {
        for (IdentityBundle identity : identityBundles) {
            int officialIndex = getOfficialIndex(identity.name());
            PDDocument coversheet = pdfManipulator.reloadDocument(); // Try with resources breaks this for some reason.
            PDDocument circledCoversheet = PDFManipulator.boxOfficial(officials.get(officialIndex).getName(), coversheet, identity.occurrenceToBox());
            EventSet eventSet = new EventSet(scheduleElement.getEventNumber(), identity.role(), scheduleElement.getRink(), identity.name());
            eventSet.push(circledCoversheet);
            if (ijs) {
                switch (identity.role()) {
                    case REFEREE -> {
                        retrieveSheets(eventNumber, identity, eventSet, technicalSheets, FileType.IJS_REFEREE_SHEET);
                        retrieveSheets(eventNumber, identity, eventSet, judgeSheets, FileType.IJS_JUDGE_SHEET);
                        if (eventSet.getSize() < 3) {
                            errors.add(new BoxError(eventNumber, identity.name(), ErrorType.MISSING_REFEREE_PAPERS));
                        }
                    }
                    case JUDGE -> {
                        retrieveSheets(eventNumber, identity, eventSet, judgeSheets, FileType.IJS_JUDGE_SHEET);
                        if (identity.role() == Role.JUDGE) {
                            if (eventSet.getSize() < 2) {
                                errors.add(new BoxError(eventNumber, identity.name(), ErrorType.MISSING_JUDGE_PAPERS));
                            }
                        }
                    }
                    case TC -> {
                        retrieveSheets(eventNumber, identity, eventSet, technicalSheets, FileType.IJS_TC_SHEET);
                        if (eventSet.getSize() < 2) {
                            errors.add(new BoxError(eventNumber, identity.name(), ErrorType.MISSING_TECHNICAL_PANEL_PAPERS));
                        }
                    }
                    case TS2 -> {
                        retrieveSheets(eventNumber, identity, eventSet, technicalSheets, FileType.IJS_TS2_SHEET);
                        retrieveSheets(eventNumber, identity, eventSet, judgeSheets, FileType.IJS_JUDGE_SHEET);
                        if (eventSet.getSize() < 2) {
                            errors.add(new BoxError(eventNumber, identity.name(), ErrorType.MISSING_TECHNICAL_PANEL_PAPERS));
                        }
                    }
                    case TS1 -> {
                        retrieveSheets(eventNumber, identity, eventSet, technicalSheets, FileType.IJS_TS1_SHEET);
                        retrieveSheets(eventNumber, identity, eventSet, judgeSheets, FileType.IJS_JUDGE_SHEET);
                        if (eventSet.getSize() < 1) {
                            errors.add(new BoxError(eventNumber, identity.name(), ErrorType.MISSING_TECHNICAL_PANEL_PAPERS));
                        }
                    }
                    case VIDEO -> {
                        retrieveSheets(eventNumber, identity, eventSet, judgeSheets, FileType.IJS_JUDGE_SHEET);
                        if (eventSet.getSize() < 1) {
                            errors.add(new BoxError(eventNumber, identity.name(), ErrorType.MISSING_TECHNICAL_PANEL_PAPERS));
                        }
                    }
                }
            } else {
                retrieveSix0SecondarySheets(eventNumber, eventSet);
            }

            if (!Settings.getBuildByBoard()) {
                officials.get(officialIndex).addDocument(eventSet);
                officials.get(officialIndex).tryAddScheduleBundle(scheduleElement, identity.role());
            } else {
                buildByBoardAddToList(identity, eventSet);
            }
        }
    }

    private void buildByBoardAddToList(IdentityBundle identity, EventSet eventSet) {
        switch(identity.role()) {
            case REFEREE -> BuildByBoard.referee.add(eventSet);
            case AR -> BuildByBoard.assistant_referee.add(eventSet);
            case JUDGE -> {
                int i = identity.getJudgeNumber() - 1;
                if (i < 0 || i > 8) {
                    Logging.logger.fatal("Bad judge number. {}, {}, {}", identity.getJudgeNumber(), identity.name(), identity.role());
                    throw new RuntimeException("Bad judge number");
                }
                BuildByBoard.judges.get(i).add(eventSet);
            }
            case TC -> BuildByBoard.tc.add(eventSet);
            case TS1 -> BuildByBoard.ts1.add(eventSet);
            case TS2 -> BuildByBoard.ts2.add(eventSet);
            case DEO -> BuildByBoard.deo.add(eventSet);
            case VIDEO -> BuildByBoard.video.add(eventSet);
        }
    }

    private void retrieveSix0SecondarySheets(String eventNumber, EventSet eventSet) {
        for (File file : six0SecondarySheets) {
            String[] split = file.getName().split(" ");
            if (split[0].trim().equals(eventNumber.trim())) {
                try {
                    eventSet.push(Loader.loadPDF(file));
                } catch (IOException e) {
                    Logging.logger.fatal((e));
                    throw new RuntimeException("Failed to retrieve 6.0 subsequent worksheets.");
                }
            }
        }
    }

    private void retrieveSheets(String eventNumber, IdentityBundle identity, EventSet eventSet, ArrayList<File> sheets, FileType fileType) {
        for (File file : sheets) {
            String[] split = file.getName().split(" ");
            boolean incorrectFile = true;
            for (FileType ft : identity.matchRoleToFileTypeIJS()) {
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
                    if (
                        (split[3].equals("judge") && identity.role() == Role.REFEREE) ||
                        (split[3].equals("referee") && identity.role() == Role.JUDGE) ||
                        (split[3].equals("video") && identity.role() != Role.VIDEO) ||
                        (split[3].equals("dance_ts") && (identity.role() != Role.TS1 && identity.role() != Role.TS2))
                    ) {
                        continue;
                    }
                }

                if (split[0].equals(eventNumber) && split[1].equals(fileType.name()) && (split[2].equals("generic")) && (identity.role() == Role.TS1 || identity.role() == Role.TS2)) {
                    try {
                        eventSet.push(Loader.loadPDF(file));
                    } catch (IOException e) {
                        Logging.logger.fatal((e));
                        throw new RuntimeException("Failed to load a PDF at " + file.getPath());
                    }
                }

                if (split[0].equals(eventNumber) && split[1].equals(fileType.name()) && (split[2].replace('_', ' ').equals(identity.name()))) {
                    try {
                        eventSet.push(Loader.loadPDF(file));
                    } catch (IOException e) {
                        Logging.logger.fatal((e));
                        throw new RuntimeException("Failed to load a PDF at " + file.getPath());
                    }
                }
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                Logging.logger.info("Could not find match for {}. Maybe it is an extra file?", file.getName());
            }
        }
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

    public ArrayList<BoxError> getErrors() {
        return errors;
    }
}
