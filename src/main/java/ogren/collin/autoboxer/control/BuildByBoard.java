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
import ogren.collin.autoboxer.pdf.EventSet;
import ogren.collin.autoboxer.process.Schedule;
import ogren.collin.autoboxer.utilities.error_detection.BoxError;
import ogren.collin.autoboxer.utilities.error_detection.ErrorType;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

// California mode. Sort by panel position, not official's name.
public class BuildByBoard {
    public static ArrayList<EventSet> referee = new ArrayList<>();
    public static ArrayList<EventSet> assistant_referee = new ArrayList<>();
    public static ArrayList<ArrayList<EventSet>> judges = new ArrayList<>() {{
        add(new ArrayList<>());
        add(new ArrayList<>());
        add(new ArrayList<>());
        add(new ArrayList<>());
        add(new ArrayList<>());
        add(new ArrayList<>());
        add(new ArrayList<>());
        add(new ArrayList<>());
        add(new ArrayList<>());
    }};
    public static ArrayList<EventSet> tc = new ArrayList<>();
    public static ArrayList<EventSet> ts1 = new ArrayList<>();
    public static ArrayList<EventSet> ts2 = new ArrayList<>();
    public static ArrayList<EventSet> deo = new ArrayList<>();
    public static ArrayList<EventSet> video = new ArrayList<>();

    private static final String REFEREE = "Referee";
    private static final String ASSISTANT_REFEREE = "Assistant Referee";
    private static final String JUDGE = "Judge ";
    private static final String TC = "Technical Controller";
    private static final String TS1 = "Technical Specialist 1";
    private static final String TS2 = "Technical Specialist 2";
    private static final String DEO = "Data Entry Operator";
    private static final String VIDEO = "Video Replay Operator";

    public static void clearAll() {
        referee.clear();
        assistant_referee.clear();
        for (ArrayList<EventSet> judge: judges) {
            judge.clear();
        }
        tc.clear();
        ts1.clear();
        ts2.clear();
        deo.clear();
        video.clear();
    }

    private static void saveIndividual(ArrayList<EventSet> events, String position) {
        if (events.isEmpty()) {
            return;
        }
        for (String rink : MasterController.getSchedule().getRinks()) {
            checkOutputDirectory(rink);
            try {
                PDDocument merged = merge(events, position, rink);
                if (merged.getPages().getCount() <= 0) {
                    System.out.println("Skipping PDF generation for " + position + " on rink " + rink + " because they have no assignments there.");
                    return;
                }
                merged.save(new File(MasterController.getBaseDir() + "/box/Officials/" + rink + "/" + position + " - " + rink + ".pdf"));
                merged.close();
            } catch (IOException e) {
                String message = "Failed to save papers for " + position;
                // Primarily this is here to explain why you cannot use * in your file name. I guess I could strip the
                // * out of the file name, but fact is the program won't work correctly if you have "* Bob Johnson" in
                // Hal but then have "Bob Johnson" in ISUCalc, so I think this is a better way as it will teach the user
                // the intended lesson and reduce headache for the user in the end.
                MasterController.errors.add(new BoxError(null, position, ErrorType.FILE_SAVE_ERROR));
                Logging.logger.fatal("{}\n{}", Arrays.toString(e.getStackTrace()), message);
                throw new RuntimeException(message);
            }
        }

        for (EventSet eventSet : events) {
            eventSet.close();
        }
    }

    public static void save() {
        saveIndividual(referee, REFEREE);
        saveIndividual(assistant_referee, ASSISTANT_REFEREE);
        for (int i = 0; i < 9; i++) {
            saveIndividual(judges.get(i), JUDGE + (i + 1));
        }
        saveIndividual(tc, TC);
        saveIndividual(ts1, TS1);
        saveIndividual(ts2, TS2);
        saveIndividual(deo, DEO);
        saveIndividual(video, VIDEO);
    }

    public static void saveAll() {
        for (String rink : MasterController.getSchedule().getRinks()) {
            try (PDDocument outputDocument = new PDDocument()) {
                PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
                pdfMergerUtility.appendDocument(outputDocument, merge(referee, REFEREE, rink));
                pdfMergerUtility.appendDocument(outputDocument, merge(assistant_referee, ASSISTANT_REFEREE, rink));
                for (int i = 0; i < 9; i++) {
                    pdfMergerUtility.appendDocument(outputDocument, merge(judges.get(i), JUDGE + (i + 1), rink));
                }
                pdfMergerUtility.appendDocument(outputDocument, merge(tc, TC, rink));
                pdfMergerUtility.appendDocument(outputDocument, merge(ts1, TS1, rink));
                pdfMergerUtility.appendDocument(outputDocument, merge(ts2, TS2, rink));
                pdfMergerUtility.appendDocument(outputDocument, merge(deo, DEO, rink));
                pdfMergerUtility.appendDocument(outputDocument, merge(video, VIDEO, rink));

                checkOutputDirectory(rink);
                outputDocument.save(new File(MasterController.getBaseDir() + "/box/Officials/" + rink + "/All Officials" + " - " + rink + ".pdf"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // It bothers me that this is very similar to the merge function in Official.java, but here's the thing,
    // There are almost certainly not going to be any more classes like this that deal with outputting papers
    // in a different way so it might be a better use of time to not abstract this away. However, cleaner code is nice
    // too. Trade-offs, I suppose.
    public static PDDocument merge(ArrayList<EventSet> events, String position, String rink) {
        if (events.isEmpty()) {
            return new PDDocument();
        }
        PDDocument document = new PDDocument();
        PDDocumentOutline outline = new PDDocumentOutline();
        document.getDocumentCatalog().setDocumentOutline(outline);

        PDOutlineItem root = new PDOutlineItem();
        root.setTitle(position);
        outline.addLast(root);

        //PDDocument mergedDocument = new PDDocument();
        for (EventSet event : events) {
            if (!event.getRink().equals(rink)) {
                continue;
            }
            boolean firstPage = true;
            for (PDPage page : event.mergeDocuments().getPages()) {
                document.addPage(page);
                if (firstPage) {
                    PDOutlineItem coversheet = new PDOutlineItem();
                    coversheet.setTitle(event.getEventNumber() + " - " + event.getRole() + " " + event.getOfficialName());
                    coversheet.setDestination(document.getPage(document.getNumberOfPages() - 1));
                    root.addLast(coversheet);
                    firstPage = false;
                }
            }
        }

        return document;
    }

    private static void checkOutputDirectory(String rink) {
        if (!new File(MasterController.getBaseDir() + "/box/Officials/" + rink).exists()) {
            boolean success = new File(MasterController.getBaseDir() + "/box/Officials/" + rink).mkdirs();
            if (!success) {
                Logging.logger.fatal("Failed to create directory /box/Officials/{}", rink);
                throw new RuntimeException("Failed to create directory /box/Officials/" + rink);
            }
        }
    }
}
