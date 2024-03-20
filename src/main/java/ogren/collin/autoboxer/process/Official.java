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

package ogren.collin.autoboxer.process;

import ogren.collin.autoboxer.control.MasterController;
import ogren.collin.autoboxer.pdf.EventSet;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Official {
    private String name;
    private ArrayList<EventSet> events = new ArrayList();

    public Official(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addDocument(EventSet eventSet) {
        events.add(eventSet);
    }

    public PDDocument merge() {
        PDDocument mergedDocument = new PDDocument();
        for (EventSet event : events) {
            for (PDPage page : event.mergeDocuments().getPages()) {
                mergedDocument.addPage(page);
            }
        }

        return mergedDocument;
    }

    public void save() {
        System.out.println("Printing for "+getName());
        if (!new File(MasterController.getBaseDir()+"/box/"+name).exists()) {
            new File(MasterController.getBaseDir()+"/box/"+name).mkdirs();
        }
        try {
            PDDocument merged = merge();
            merged.save(new File(MasterController.getBaseDir()+"/box/"+name+"/"+name+".pdf"));
            merged.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
