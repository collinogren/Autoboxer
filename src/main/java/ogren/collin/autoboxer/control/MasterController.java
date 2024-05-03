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

import ogren.collin.autoboxer.Logging;
import ogren.collin.autoboxer.gui.GUIFXController;
import ogren.collin.autoboxer.pdf.EventSet;
import ogren.collin.autoboxer.pdf.FileType;
import ogren.collin.autoboxer.pdf.PDFManipulator;
import ogren.collin.autoboxer.process.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MasterController {

    public static final String COVERSHEET_DIR = "coversheets";
    public static final String JUDGE_SHEETS_DIR = "judges";
    public static final String TECH_PANEL_DIR = "tech";
    public static final String SIX0_PRIMARY_DIR = "60";
    public static final String SIX0_SUBSEQUENT_DIR = "60_sub";
    public static final String SIX0_STARTING_ORDERS_DIR = "60_starting_orders";

    private static final String BOX_DIR = "box";
    private static final String TA_DIR = "box/TA";
    private static final String STARTING_ORDER_DIR = "box/Starting Orders";
    private static String baseDir;
    private final ArrayList<Official> officials = new ArrayList<>();
    private final Schedule schedule;

    private ArrayList<File> coversheets = new ArrayList<>();
    private ArrayList<File> judgeSheets = new ArrayList<>();
    private ArrayList<File> technicalSheets = new ArrayList<>();
    private ArrayList<File> six0Sheets = new ArrayList<>();
    private ArrayList<File> six0SecondarySheets = new ArrayList<>();
    private ArrayList<File> six0StartingOrders = new ArrayList<>();

    public MasterController(String baseDir) {
        MasterController.baseDir = baseDir;
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
        schedule = new Schedule(new File(baseDir + "/schedule.txt"));
    }

    public static String getBaseDir() {
        return baseDir;
    }

    // Sub function to get the right PDFManipulators for the rename function.
    private static ArrayList<PDFManipulator> getPDFManipulatorsToRename(ArrayList<File> files, FileType fileType) {
        ArrayList<PDFManipulator> pdfManipulators = new ArrayList<>();

        // For every file, make a PDFManipulator to extract the event name and ensure that the right file type is chosen.
        for (File file : files) {
            PDFManipulator pdfManipulator = new PDFManipulator(file, fileType);

            // Handle the fact that all technical panel sheets are printed into the same directory.
            if (pdfManipulator.getEventName().equals(PDFManipulator.WRONG_FILE_TYPE)) {
                continue;
            }
            pdfManipulators.add(pdfManipulator);
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
        for (Official official : officials) {
            official.save();
        }
        GUIFXController.setProgress(100);
        GUIFXController.setDone(true);
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
                    judgeSheets = getAllFiles(JUDGE_SHEETS_DIR + "/renamed/");
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

            // Wait for processes to finish, and destroy the executor.
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

            GUIFXController.addProgress(((1.0 / numberOfEvents) / 9.0) / 2.0);
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
        HashMap<String, PDDocument> taSheets = new HashMap<>();
        HashMap<String, PDDocument> startingOrders = new HashMap<>();
        int numberOfEvents = schedule.getElements().size();
        for (ScheduleElement se : schedule.getElements()) {
            for (File file : coversheets) {
                if (se.matchFileNameToEventNumber(file)) {
                    String eventNumber = se.getEventNumber();
                    PDFManipulator pdfManipulator = new PDFManipulator(file, FileType.IJS_COVERSHEET);
                    sortIJSTAAndStartingOrders(GUIFXController.getGenerateStartingOrders(), startingOrders, se, pdfManipulator);

                    sortIJSTAAndStartingOrders(GUIFXController.getGenerateTASheets(), taSheets, se, pdfManipulator);
                    ArrayList<IdentityBundle> identityBundles = pdfManipulator.getCoversheetsOfficialNames();
                    processEvent(eventNumber, identityBundles, pdfManipulator, se, true);
                }
            }

            sort60StartingOrders(se, startingOrders);

            sort60Primary(se);

            GUIFXController.addProgress(((1.0 / numberOfEvents)) / 2.0);
        }

        generateStartingOrders(startingOrders);
        generateTASheets(taSheets);
    }

    private void sort60Primary(ScheduleElement se) {
        for (File file : six0Sheets) {
            if (se.matchFileNameToEventNumber(file)) {
                String eventNumber = se.getEventNumber();
                PDFManipulator pdfManipulator = new PDFManipulator(file, FileType.SIX0_PRIMARY_JUDGE_SHEET);
                ArrayList<IdentityBundle> identityBundles = pdfManipulator.getCoversheetsOfficialNames();
                processEvent(eventNumber, identityBundles, pdfManipulator, se, false);
            }
        }
    }

    private void sort60StartingOrders(ScheduleElement se, HashMap<String, PDDocument> startingOrders) {
        for (File file : six0StartingOrders) {
            if (se.matchFileNameToEventNumber(file)) {
                if (GUIFXController.getGenerateStartingOrders()) {
                    PDFManipulator pdfManipulator = new PDFManipulator(file, FileType.SIX0_STARTING_ORDERS);
                    if (!startingOrders.containsKey(se.getRink())) {
                        startingOrders.put(se.getRink(), new PDDocument());
                    }

                    pdfManipulator.addToPDF(startingOrders.get(se.getRink()));
                }
            }
        }
    }

    private void sortIJSTAAndStartingOrders(boolean generate, HashMap<String, PDDocument> sheets, ScheduleElement se, PDFManipulator pdfManipulator) {
        if (generate) {
            if (!sheets.containsKey(se.getRink())) {
                sheets.put(se.getRink(), new PDDocument());
            }

            pdfManipulator.addToPDF(sheets.get(se.getRink()));
        }
    }

    private void generateStartingOrders(HashMap<String, PDDocument> startingOrders) {
        if (GUIFXController.getGenerateStartingOrders()) {
            for (String rink : Schedule.getRinks()) {
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

    private void generateTASheets(HashMap<String, PDDocument> taSheets) {
        if (GUIFXController.getGenerateTASheets()) {
            for (String rink : Schedule.getRinks()) {
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
            PDDocument coversheet = pdfManipulator.reloadDocument();
            PDDocument circledCoversheet = PDFManipulator.boxOfficial(officials.get(officialIndex).getName(), coversheet, identity.occurrenceToBox());
            EventSet eventSet = new EventSet(scheduleElement.getEventNumber(), identity.role(), scheduleElement.getRink());
            eventSet.push(circledCoversheet);
            if (ijs) {
                switch (identity.role()) {
                    case REFEREE -> {
                        retrieveSheets(eventNumber, identity, eventSet, technicalSheets, FileType.IJS_REFEREE_SHEET);
                        retrieveSheets(eventNumber, identity, eventSet, judgeSheets, FileType.IJS_JUDGE_SHEET);
                    }
                    case JUDGE ->
                            retrieveSheets(eventNumber, identity, eventSet, judgeSheets, FileType.IJS_JUDGE_SHEET);
                    case TC -> retrieveSheets(eventNumber, identity, eventSet, technicalSheets, FileType.IJS_TC_SHEET);
                    case TS2 ->
                            retrieveSheets(eventNumber, identity, eventSet, technicalSheets, FileType.IJS_TS2_SHEET);
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
                    if ((split[3].equals("judge") && identity.role() == Role.REFEREE) || (split[3].equals("referee") && identity.role() == Role.JUDGE)) {
                        continue;
                    }
                }

                if (split[0].equals(eventNumber) && split[1].equals(fileType.name()) && split[2].replace('_', ' ').equals(identity.name())) {
                    try {
                        eventSet.push(Loader.loadPDF(file));
                    } catch (IOException e) {
                        Logging.logger.fatal((e));
                        throw new RuntimeException("Failed to load a PDF at " + file.getPath());
                    }
                }
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                Logging.logger.info("Could not find match for " + file.getName() + ". Maybe it is an extra file?");
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
}
