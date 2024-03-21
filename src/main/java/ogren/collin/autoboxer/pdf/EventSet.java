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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.IOException;
import java.util.ArrayList;

public class EventSet {
    ArrayList<PDDocument> documents = new ArrayList<>();
    public void push(PDDocument document) {
        documents.add(document);
    }

    public PDDocument mergeDocuments() {
        PDDocument mergedDocument = new PDDocument();
        for (PDDocument document : documents) {
            for (PDPage page : document.getPages()) {
                mergedDocument.addPage(page);
            }
        }

        return mergedDocument;
    }

    public void close() {
        for (PDDocument document : documents) {
            try {
                document.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
