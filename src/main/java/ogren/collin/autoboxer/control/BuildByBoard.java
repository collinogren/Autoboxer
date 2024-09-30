package ogren.collin.autoboxer.control;

import jdk.jfr.Event;
import ogren.collin.autoboxer.Logging;
import ogren.collin.autoboxer.pdf.EventSet;
import ogren.collin.autoboxer.process.Schedule;
import ogren.collin.autoboxer.process.StringUtils;
import ogren.collin.autoboxer.utilities.errordetection.BoxError;
import ogren.collin.autoboxer.utilities.errordetection.ErrorType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class BuildByBoard {
    public static ArrayList<EventSet> referee = new ArrayList<>();
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

    private static void saveIndividual(ArrayList<EventSet> events, String position) {
        if (events.isEmpty()) {
            return;
        }
        for (String rink : Schedule.getRinks()) {
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
        saveIndividual(referee, "Referee");
        for (int i = 0; i < 9; i++) {
            saveIndividual(judges.get(i), "Judge " + (i + 1));
        }
        saveIndividual(tc, "Technical Controller");
        saveIndividual(ts1, "Technical Specialist 1");
        saveIndividual(ts2, "Technical Specialist 2");
        saveIndividual(deo, "Data Entry Operator");
        saveIndividual(video, "Video Replay Operator");
    }

    public static PDDocument merge(ArrayList<EventSet> events, String position, String rink) {
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
