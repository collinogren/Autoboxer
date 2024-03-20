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

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.List;

public class TextLocator extends PDFTextStripper {

    private String name;

    private int occurrenceToBox;
    private StringLocationBundle locationBundle;

    public TextLocator(String name, int occurrenceToBox) throws IOException {
        super.setSortByPosition(true);
        super.setStartPage(0);
        super.setEndPage(1);
        this.name = name;
        this.occurrenceToBox = occurrenceToBox;
    }

    public StringLocationBundle getLocationBundle() {
        return locationBundle;
    }

    @Override
    protected void writeString(String string, List<TextPosition> positions) {
        double startingX = 0;
        double startingY = 0;
        double width = 0;
        double height = 0;
        int occurrences = 0;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < positions.size(); i++) {
            String text = positions.get(i).getUnicode();
            builder.append(text);
            if (builder.toString().endsWith(name)) {
                occurrences++;
                startingX = positions.get(i - (name.length() - 1)).getXDirAdj();
                startingY = positions.get(i - (name.length() - 1)).getYDirAdj();
                width = 0;
                height = 0;
                for (int j = i - (name.length() - 1); j <= i; j++) {
                    width += positions.get(j).getWidthDirAdj();
                    if (height < positions.get(j).getHeightDir()) {
                        height = positions.get(j).getHeightDir();
                    }
                }

                if (occurrences >= occurrenceToBox) {
                    locationBundle = new StringLocationBundle(startingX, startingY, width, height);
                    return;
                }
            }
        }
    }

    /*@Override
    protected void writeString(String string, List<TextPosition> positions) {
        double startingX = 0;
        double startingY = 0;
        double width = 0;
        double height = 0;
        int occurrences = 0;
        StringBuilder builder = new StringBuilder();
        for (TextPosition textPosition : positions) {
            String text = textPosition.getUnicode();
            if (text.equals(",")
                    || text.equals("0")
                    || text.equals("1")
                    || text.equals("2")
                    || text.equals("3")
                    || text.equals("4")
                    || text.equals("5")
                    || text.equals("6")
                    || text.equals("7")
                    || text.equals("8")
                    || text.equals("9")
                    || text.equals(".")) {
                if (builder.toString().endsWith(name)) {
                    occurrences += 1;
                    System.out.println(builder);
                    if (occurrences >= occurrenceToBox) {
                        locationBundle = new StringLocationBundle(startingX, startingY, width, height);
                        return;
                    }
                }
            } else {
                if (builder.isEmpty()) {
                    if (textPosition.getUnicode().equals(" ")
                            || textPosition.getUnicode().equals("0")
                            || textPosition.getUnicode().equals("1")
                            || textPosition.getUnicode().equals("2")
                            || textPosition.getUnicode().equals("3")
                            || textPosition.getUnicode().equals("4")
                            || textPosition.getUnicode().equals("5")
                            || textPosition.getUnicode().equals("6")
                            || textPosition.getUnicode().equals("7")
                            || textPosition.getUnicode().equals("8")
                            || textPosition.getUnicode().equals("9")
                            || textPosition.getUnicode().equals(".")) {
                        continue;
                    }
                    startingX = textPosition.getXDirAdj();
                    startingY = textPosition.getYDirAdj();
                    width = 0;
                    height = 0;
                }

                builder.append(text);
                width += textPosition.getWidthDirAdj();
                if (height < textPosition.getHeightDir()) {
                    height = textPosition.getHeightDir();
                }
            }
        }
    }*/
}
