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

package ogren.collin.autoboxer.process;

import ogren.collin.autoboxer.Logging;
import ogren.collin.autoboxer.control.MasterController;
import ogren.collin.autoboxer.pdf.EventSet;
import ogren.collin.autoboxer.pdf.OfficialSchedule;
import ogren.collin.autoboxer.utilities.Settings;
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
import java.util.Collections;
import java.util.Comparator;

public class Official {
    private final String name;
    private final ArrayList<EventSet> events = new ArrayList<>();
    private final ArrayList<OfficialScheduleBundle> scheduleElements = new ArrayList<>();

    private boolean hasPrinted = false;

    private static final String COMBINED_RINKS = "All Rinks";

    public Official(String name) {
        this.name = name;
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

    public static void saveAllByRink(ArrayList<Official> officials) {
        officials.sort(Comparator.comparing(Official::getNameLastFirst));
        for (String rink : MasterController.getSchedule().getRinks()) {
            try (PDDocument outputDocument = new PDDocument()) {
                PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
                for (Official official : officials) {
                    pdfMergerUtility.appendDocument(outputDocument, official.merge(rink));
                }

                checkOutputDirectory(rink);
                outputDocument.save(new File(MasterController.getBaseDir() + "/box/Officials/" + rink + "/All Officials" + " - " + rink + ".pdf"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void saveAllCombined(ArrayList<Official> officials) {
        officials.sort(Comparator.comparing(Official::getNameLastFirst));
        try (PDDocument outputDocument = new PDDocument()) {
            PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
            for (Official official : officials) {
                pdfMergerUtility.appendDocument(outputDocument, official.mergeCombined());
            }

            checkOutputDirectory(COMBINED_RINKS);
            outputDocument.save(new File(MasterController.getBaseDir() + "/box/Officials/" + COMBINED_RINKS + "/All Officials" + " - " + COMBINED_RINKS + ".pdf"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public String getNameLastFirst() {
        return StringUtils.toLastFirst(name);
    }

    public void addDocument(EventSet eventSet) {
        events.add(eventSet);
    }

    public PDDocument mergeByRink(String rink) {
        return merge(rink);
    }

    public PDDocument mergeCombined() {
        return merge(null);
    }

    private PDDocument merge(String rink) {
        PDDocument mergedDocument;
        if (Settings.getGenerateSchedule() && !hasPrinted) {
            mergedDocument = OfficialSchedule.generateSchedule(this);
        } else {
            mergedDocument = new PDDocument();
        }

        int initialPageCount = mergedDocument.getNumberOfPages();

        PDDocumentOutline outline = new PDDocumentOutline();
        mergedDocument.getDocumentCatalog().setDocumentOutline(outline);

        PDOutlineItem root = new PDOutlineItem();
        root.setTitle(getName());
        outline.addLast(root);

        //PDDocument mergedDocument = new PDDocument();
        for (EventSet event : events) {
            if (rink != null && !event.getRink().equals(rink)) {
                continue;
            }

            boolean firstPage = true;
            for (PDPage page : event.mergeDocuments().getPages()) {
                mergedDocument.addPage(page);
                if (firstPage) {
                    PDOutlineItem coversheet = new PDOutlineItem();
                    coversheet.setTitle(event.getEventNumber() + " - " + event.getRole());
                    coversheet.setDestination(mergedDocument.getPage(mergedDocument.getNumberOfPages() - 1));
                    root.addLast(coversheet);
                    firstPage = false;
                }
            }
        }

        // Make it so the schedule sheet doesn't get printed if there are no events on that rink for that official.
        if (mergedDocument.getNumberOfPages() == initialPageCount) {
            return new PDDocument();
        }

        hasPrinted = true;

        return mergedDocument;
    }

    public void saveCombined() {
        Logging.logger.info("Printing for " + getName());
        checkOutputDirectory(COMBINED_RINKS);
        try {
            PDDocument merged = mergeCombined();
            if (merged.getPages().getCount() <= 0) {
                System.out.println("Skipping PDF generation for " + name + " on rink " + COMBINED_RINKS + " because they have no assignments there.");
                merged.close();
                return;
            }

            merged.save(new File(MasterController.getBaseDir() + "/box/Officials/" + COMBINED_RINKS + "/" + StringUtils.toLastFirst(name) + " - " + COMBINED_RINKS + ".pdf"));
            merged.close();
        } catch (IOException e) {
            String message = "Failed to save papers for " + getName();
            // Primarily this is here to explain why you cannot use * in your file name. I guess I could strip the
            // * out of the file name, but fact is the program won't work correctly if you have "* Bob Johnson" in
            // Hal but then have "Bob Johnson" in ISUCalc, so I think this is a better way as it will teach the user
            // the intended lesson and reduce headache for the user in the end.
            MasterController.errors.add(new BoxError(null, name, ErrorType.FILE_SAVE_ERROR));
            Logging.logger.fatal("{}\n{}", Arrays.toString(e.getStackTrace()), message);
            throw new RuntimeException(message);
        }

        for (EventSet eventSet : events) {
            eventSet.close();
        }
    }

    public void saveByRink() {
        Logging.logger.info("Printing for " + getName());
        for (String rink : MasterController.getSchedule().getRinks()) {
            checkOutputDirectory(rink);
            try {
                PDDocument merged = merge(rink);
                if (merged.getPages().getCount() <= 0) {
                    System.out.println("Skipping PDF generation for " + name + " on rink " + rink + " because they have no assignments there.");
                    merged.close();
                    continue;
                }

                merged.save(new File(MasterController.getBaseDir() + "/box/Officials/" + rink + "/" + StringUtils.toLastFirst(name) + " - " + rink + ".pdf"));
                merged.close();
            } catch (IOException e) {
                String message = "Failed to save papers for " + getName();
                // Primarily this is here to explain why you cannot use * in your file name. I guess I could strip the
                // * out of the file name, but fact is the program won't work correctly if you have "* Bob Johnson" in
                // Hal but then have "Bob Johnson" in ISUCalc, so I think this is a better way as it will teach the user
                // the intended lesson and reduce headache for the user in the end.
                MasterController.errors.add(new BoxError(null, name, ErrorType.FILE_SAVE_ERROR));
                Logging.logger.fatal("{}\n{}", Arrays.toString(e.getStackTrace()), message);
                throw new RuntimeException(message);
            }
        }

        for (EventSet eventSet : events) {
            eventSet.close();
        }
    }

    public ArrayList<OfficialScheduleBundle> getScheduleElements() {
        return scheduleElements;
    }

    public void tryAddScheduleBundle(ScheduleElement scheduleElement, Role role) {
        for (OfficialScheduleBundle officialScheduleBundle : scheduleElements) {
            if (officialScheduleBundle.scheduleElement().equals(scheduleElement)) {
                officialScheduleBundle.role().add(role.toString());
                return;
            }
        }
        scheduleElements.add(new OfficialScheduleBundle(scheduleElement, new ArrayList<>(Collections.singletonList(role.name())))); // Consider using role.toString()
    }
}