package ogren.collin.autoboxer.pdf;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.ArrayList;
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
        for (TextPosition textPosition : positions) {
            String text = textPosition.getUnicode();
            if (text.equals(",")) {
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
                    if (textPosition.getUnicode().equals(" ")) {
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
    }
}
