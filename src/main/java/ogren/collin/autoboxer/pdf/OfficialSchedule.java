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
    private static final float RINK_WIDTH = 10f;
    private static final float EVENT_NUMBER_WIDTH = 5f;
    private static final float EVENT_NAME_WIDTH = 51f;
    private static final float TIME_WIDTH = 12f;

    private static final float MARGIN = 14f;

    public static PDDocument generateSchedule(Official official) {
        PDPage page = new PDPage(PDRectangle.LETTER);
        PDDocument document = new PDDocument();
        String day = official.getScheduleElements().getFirst().scheduleElement().getDay();

        try {
            float pageWidth = page.getMediaBox().getWidth() - MARGIN * 2;
            Table.TableBuilder tableBuilder = Table.builder()
                    .addColumnsOfWidth(pageWidth * TIME_WIDTH / 100f,
                            pageWidth * TIME_WIDTH / 100f,
                            pageWidth * RINK_WIDTH / 100f,
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
                                            .colSpan(5)
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
                            .add(TextCell.builder().text("RINK").horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(1).build())
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
                try {
                    tableBuilder.addRow(
                            Row.builder()
                                    .add(TextCell.builder().text(scheduleElement.scheduleElement().getStartTime().trim().replaceAll("\t", " ")).horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(1).build())
                                    .add(TextCell.builder().text(scheduleElement.scheduleElement().getEndTime().trim().replaceAll("\t", " ")).horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(1).build())
                                    .add(TextCell.builder().text(scheduleElement.scheduleElement().getRink().trim().replaceAll("\t", " ")).horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(1).build())
                                    .add(TextCell.builder().text(scheduleElement.scheduleElement().getEventNumber().trim().replaceAll("\t", " ")).horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(1).build())
                                    .add(TextCell.builder().text(scheduleElement.scheduleElement().getEventName().split(PDFManipulator.getEventNameDelimiter(), 2)[1].trim().replaceAll("\t", " ")).horizontalAlignment(HorizontalAlignment.LEFT).borderWidth(1).build())
                                    .add(TextCell.builder().text(roles.toString().trim().replaceAll("\t", " ")).horizontalAlignment(HorizontalAlignment.CENTER).borderWidth(1).build())
                                    .backgroundColor(Color.WHITE)
                                    .textColor(Color.BLACK)
                                    .font(Standard14Fonts.FontName.HELVETICA)
                                    .fontSize(10)
                                    .horizontalAlignment(HorizontalAlignment.CENTER)
                                    .build());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException("Error processing event "+scheduleElement.scheduleElement().getEventNumber() + " " + scheduleElement.scheduleElement().getEventName());
                }
            }


            RepeatedHeaderTableDrawer.builder()
                    .numberOfRowsToRepeat(2)
                    .table(tableBuilder.build())
                    .startX(MARGIN)
                    .startY(page.getMediaBox().getUpperRightY() - MARGIN)
                    .endY(page.getMediaBox().getLowerLeftY() + MARGIN)
                    .build().draw(() -> document, () -> new PDPage(PDRectangle.LETTER), MARGIN);
        } catch (IOException e) {
            e.printStackTrace();
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
