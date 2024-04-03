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

import ogren.collin.autoboxer.process.Official;
import ogren.collin.autoboxer.process.OfficialScheduleBundle;
import ogren.collin.autoboxer.utility.StringUtilities;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.vandeseer.easytable.RepeatedHeaderTableDrawer;
import org.vandeseer.easytable.settings.HorizontalAlignment;
import org.vandeseer.easytable.structure.Row;
import org.vandeseer.easytable.structure.Table;
import org.vandeseer.easytable.structure.cell.TextCell;

import java.awt.*;
import java.io.IOException;

public class OfficialSchedule {

    private static final float ROLE_WIDTH = 10f;
    private static final float EVENT_NUMBER_WIDTH = 5f;
    private static final float EVENT_NAME_WIDTH = 61f;
    private static final float TIME_WIDTH = 12f;

    private static final float MARGIN = 14f;

    public static PDDocument generateSchedule(Official official) {
        PDPage page = new PDPage(PDRectangle.LETTER);
        PDDocument document = new PDDocument();
        document.addPage(page);
        String day = official.getScheduleElements().getFirst().scheduleElement().getDay();

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float pageWidth = page.getMediaBox().getWidth() - MARGIN * 2;
            Table.TableBuilder tableBuilder = Table.builder()
                    .addColumnsOfWidth(pageWidth * TIME_WIDTH / 100f,
                            pageWidth * TIME_WIDTH / 100f,
                            pageWidth * EVENT_NUMBER_WIDTH / 100f,
                            pageWidth * EVENT_NAME_WIDTH / 100f,
                            pageWidth * ROLE_WIDTH / 100f)
                    .fontSize(10)
                    .font(Standard14Fonts.FontName.HELVETICA)
                    .horizontalAlignment(HorizontalAlignment.CENTER)
                    .borderColor(Color.BLACK);
            tableBuilder.addRow(
                    Row.builder()
                            .add(
                                    TextCell.builder()
                                            .text(official.getName())
                                            .horizontalAlignment(HorizontalAlignment.LEFT)
                                            //.borderWidth(1)
                                            .colSpan(4)
                                            //.borderWidth(0)
                                            .font(Standard14Fonts.FontName.HELVETICA_BOLD)
                                            .fontSize(14)
                                            .padding(10)
                                            .build())
                            .add(
                                    TextCell.builder()
                                            .text(day.replaceAll("\t", " "))
                                            .horizontalAlignment(HorizontalAlignment.RIGHT)
                                            //.borderWidth(1)
                                            .colSpan(1)
                                            //.borderWidthLeft(0)
                                            .font(Standard14Fonts.FontName.HELVETICA_BOLD)
                                            .fontSize(14)
                                            .padding(10)
                                            .build()
                            ).build());
            tableBuilder.wordBreak(false);
            tableBuilder.addRow(
                    Row.builder()
                            .add(TextCell.builder().text("START TIME").horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(1).build())
                            .add(TextCell.builder().text("END TIME").horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(1).build())
                            .add(TextCell.builder().text("#").horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(1).build())
                            .add(TextCell.builder().text("EVENT NAME").horizontalAlignment(HorizontalAlignment.LEFT).borderWidth(1).build())
                            .add(TextCell.builder().text("ROLE").horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(1).build())
                            .backgroundColor(Color.WHITE)
                            .textColor(Color.BLACK)
                            .font(Standard14Fonts.FontName.HELVETICA_BOLD)
                            .fontSize(10)
                            .horizontalAlignment(HorizontalAlignment.CENTER)
                            .build());

            for (OfficialScheduleBundle scheduleElement : official.getScheduleElements()) {
                StringBuilder roles = new StringBuilder();
                scheduleElement.role().sort(String::compareToIgnoreCase);
                for (int i = 0; i < scheduleElement.role().size(); i++) {
                    if (scheduleElement.role().size() > 1) {
                        roles.append(truncateRole(scheduleElement.role().get(i)));
                    } else {
                        roles.append(scheduleElement.role().get(i));
                    }
                    if (i < scheduleElement.role().size() - 1) {
                        roles.append(", ");
                    }
                }
                tableBuilder.addRow(
                        Row.builder()
                                .add(TextCell.builder().text(scheduleElement.scheduleElement().getStartTime().trim().replaceAll("\t", " ")).horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(1).build())
                                .add(TextCell.builder().text(scheduleElement.scheduleElement().getEndTime().trim().replaceAll("\t", " ")).horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(1).build())
                                .add(TextCell.builder().text(scheduleElement.scheduleElement().getEventNumber().trim().replaceAll("\t", " ")).horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(1).build())
                                .add(TextCell.builder().text(scheduleElement.scheduleElement().getEventName().split(PDFManipulator.getEventNameDelimiter())[1].trim().replaceAll("\t", " ")).horizontalAlignment(HorizontalAlignment.LEFT).borderWidth(1).build())
                                .add(TextCell.builder().text(roles.toString().trim().replaceAll("\t", " ")).horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(1).build())
                                .backgroundColor(Color.WHITE)
                                .textColor(Color.BLACK)
                                .font(Standard14Fonts.FontName.HELVETICA)
                                .fontSize(10)
                                .horizontalAlignment(HorizontalAlignment.CENTER)
                                .build());
            }


            RepeatedHeaderTableDrawer.builder()
                    .numberOfRowsToRepeat(2)
                    .contentStream(contentStream)
                    .startX(MARGIN)
                    .startY(page.getMediaBox().getUpperRightY() - MARGIN)
                    .table(tableBuilder.build())
                    .build().draw(() -> document, () -> page, MARGIN);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return document;
    }

    private static String truncateRole(String role) {
        if (role.equals("JUDGE")) {
            return "JDG";
        }

        if (role.equals("REFEREE")) {
            return "REF";
        }

        if (role.equals("VIDEO")) {
            return "VID";
        }

        return role;
    }
}
