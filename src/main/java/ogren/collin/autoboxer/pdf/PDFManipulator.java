/*
    Autoboxer to make creating "boxes" for Figure Skating competitions easier.
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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

public class PDFManipulator {
    PDDocument document;
    public PDFManipulator(File file) throws IOException {
        document = PDDocument.load(file);
    }

    public void rename() {

    }

    private String parseEventNumber(FileType fileType) {
        String eventNumber = "";
        switch (fileType) {
            case FileType.IJS_COVERSHEET:
                //TODO: Make parser function.
                break;
            case FileType.IJS_JUDGE_SHEET:
                //TODO: Make parser function.
                break;
            case FileType.IJS_REFEREE_SHEET:
                //TODO: Make parser function.
                break;
            case FileType.IJS_TC_SHEET:
                //TODO: Make parser function.
                break;
            case FileType.IJS_TS2_SHEET:
                //TODO: Make parser function.
                break;
            case FileType.SIX0_JUDGE_SHEET:
                //TODO: Make parser function.
                break;
            case FileType.SIX0_WORKSHEET:
                //TODO: Make parser function.
                break;
            default:
        }

        return eventNumber;
    }

    public String parseToString() throws IOException {
        return new PDFTextStripper().getText(document);
    }
}
