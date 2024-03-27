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
        String day = official.getScheduleElements().getFirst().scheduleElement().getDay();

        try {
            float margin = 10;
            float yStartNewPage = page.getMediaBox().getHeight() - (2 * margin);
            float tableWidth = page.getMediaBox().getWidth() - (2 * margin);

            boolean drawContent = true;
            float bottomMargin = 0;
            float yPosition = 0;

            BaseTable table = new BaseTable(yPosition, yStartNewPage, bottomMargin, tableWidth, margin, document, page, true, drawContent);
            Row<PDPage> name = table.createRow(14);
            Cell<PDPage> cell = name.createCell(100, official.getName() + "—" + day);
            cell.setFontSize(14);
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setBottomPadding(2);
            cell.setTopPadding(4);
            table.addHeaderRow(name);
            Row<PDPage> header = table.createRow(10);
            cell = header.createCell(TIME_WIDTH, "START TIME");
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setBottomPadding(1);
            cell.setTopPadding(3);
            cell.setAlign(HorizontalAlignment.CENTER);
            cell = header.createCell(TIME_WIDTH, "END TIME");
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setBottomPadding(1);
            cell.setTopPadding(3);
            cell.setAlign(HorizontalAlignment.CENTER);
            cell = header.createCell(EVENT_NUMBER_WIDTH, "#");
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setBottomPadding(1);
            cell.setTopPadding(3);
            cell.setAlign(HorizontalAlignment.CENTER);
            cell = header.createCell(EVENT_NAME_WIDTH, "EVENT NAME");
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setBottomPadding(1);
            cell.setTopPadding(3);
            cell = header.createCell(ROLE_WIDTH, "ROLE");
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setBottomPadding(1);
            cell.setTopPadding(3);
            cell.setAlign(HorizontalAlignment.CENTER);
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
                cell = row.createCell(TIME_WIDTH, scheduleElement.scheduleElement().getStartTime());
                cell.setAlign(HorizontalAlignment.CENTER);
                cell.setFont(PDType1Font.HELVETICA);
                cell.setBottomPadding(1);
                cell.setTopPadding(3);
                cell = row.createCell(TIME_WIDTH, scheduleElement.scheduleElement().getEndTime());
                cell.setAlign(HorizontalAlignment.CENTER);
                cell.setFont(PDType1Font.HELVETICA);
                cell.setBottomPadding(1);
                cell.setTopPadding(3);
                cell = row.createCell(EVENT_NUMBER_WIDTH, scheduleElement.scheduleElement().getEventNumber());
                cell.setFont(PDType1Font.HELVETICA);
                cell.setBottomPadding(1);
                cell.setTopPadding(3);
                cell.setAlign(HorizontalAlignment.CENTER);
                cell = row.createCell(EVENT_NAME_WIDTH, scheduleElement.scheduleElement().getEventName().split(" - ")[1]);
                cell.setFont(PDType1Font.HELVETICA);
                cell.setBottomPadding(1);
                cell.setTopPadding(3);
                cell = row.createCell(ROLE_WIDTH, roles.toString());
                cell.setAlign(HorizontalAlignment.CENTER);
                cell.setFont(PDType1Font.HELVETICA);
                cell.setBottomPadding(1);
                cell.setTopPadding(3);
            }
            table.draw();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return document;
    }
}
