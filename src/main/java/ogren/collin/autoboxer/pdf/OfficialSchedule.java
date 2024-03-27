package ogren.collin.autoboxer.pdf;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.HorizontalAlignment;
import be.quodlibet.boxable.Row;
import ogren.collin.autoboxer.process.Official;
import ogren.collin.autoboxer.process.OfficialScheduleBundle;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;
public class OfficialSchedule {

    private static final int ROLE_WIDTH = 16;
    private static final int EVENT_NUMBER_WIDTH = 5;
    private static final int EVENT_NAME_WIDTH = 55;
    private static final int TIME_WIDTH = 12;

    public static PDDocument generateSchedule(Official official) {
        PDPage page = new PDPage(PDRectangle.LETTER);
        PDDocument document = new PDDocument();

        try {
            float margin = 10;
            float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
            float tableWidth = page.getMediaBox().getWidth() - (2 * margin);

            boolean drawContent = true;
            float bottomMargin = 0;
            float yPosition = 0;

            BaseTable table = new BaseTable(yPosition, yStartNewPage, bottomMargin, tableWidth, margin, document, page, true, drawContent);
            Row<PDPage> name = table.createRow(14);
            Cell<PDPage> cell = name.createCell(100, official.getName());
            cell.setFontSize(14);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            table.addHeaderRow(name);
            Row<PDPage> header = table.createRow(14);
            header.createCell(TIME_WIDTH, "START TIME").setFont(PDType1Font.HELVETICA_BOLD);
            header.createCell(TIME_WIDTH, "END TIME").setFont(PDType1Font.HELVETICA_BOLD);
            Cell<PDPage> c = header.createCell(EVENT_NUMBER_WIDTH, "#");
            c.setFont(PDType1Font.HELVETICA_BOLD);
            c.setAlign(HorizontalAlignment.CENTER);
            header.createCell(EVENT_NAME_WIDTH, "EVENT NAME").setFont(PDType1Font.HELVETICA_BOLD);
            header.createCell(ROLE_WIDTH, "ROLE").setFont(PDType1Font.HELVETICA_BOLD);
            table.addHeaderRow(header);
            for (OfficialScheduleBundle scheduleElement : official.getScheduleElements()) {
                StringBuilder roles = new StringBuilder();
                scheduleElement.role().sort(String::compareToIgnoreCase);
                for (int i = 0; i < scheduleElement.role().size(); i++) {
                    roles.append(scheduleElement.role().get(i));
                    if (i < scheduleElement.role().size() - 1) {
                        roles.append(", ");
                    }
                }

                Row<PDPage> row = table.createRow(10);
                row.createCell(TIME_WIDTH, scheduleElement.scheduleElement().getStartTime()).setFont(PDType1Font.HELVETICA);
                row.createCell(TIME_WIDTH, scheduleElement.scheduleElement().getEndTime()).setFont(PDType1Font.HELVETICA);
                Cell<PDPage> c2 = row.createCell(EVENT_NUMBER_WIDTH, scheduleElement.scheduleElement().getEventNumber());
                c2.setFont(PDType1Font.HELVETICA);
                c2.setAlign(HorizontalAlignment.CENTER);
                row.createCell(EVENT_NAME_WIDTH, scheduleElement.scheduleElement().getEventName().split(" - ")[1]).setFont(PDType1Font.HELVETICA);
                row.createCell(ROLE_WIDTH, roles.toString()).setFont(PDType1Font.HELVETICA);
            }
            table.draw();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return document;
    }
}
