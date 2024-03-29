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

package ogren.collin.autoboxer.process;

import ogren.collin.autoboxer.control.MasterController;
import ogren.collin.autoboxer.pdf.EventSet;
import ogren.collin.autoboxer.pdf.OfficialSchedule;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class Official {
    private final String name;
    private final ArrayList<EventSet> events = new ArrayList<>();
    private final ArrayList<OfficialScheduleBundle> scheduleElements = new ArrayList<>();

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
        PDDocument mergedDocument = OfficialSchedule.generateSchedule(this);
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
            boolean success = new File(MasterController.getBaseDir()+"/box/"+name).mkdirs();
            if (!success) {
                throw new RuntimeException("Failed to create directory /box/"+name);
            }
        }
        try {
            PDDocument merged = merge();
            merged.save(new File(MasterController.getBaseDir()+"/box/"+name+"/"+name+".pdf"));
            merged.close();
            for (EventSet eventSet : events) {
                eventSet.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<OfficialScheduleBundle> getScheduleElements() {
        return scheduleElements;
    }

    public void tryAddScheduleBundle(ScheduleElement scheduleElement, Role role) {
        for (OfficialScheduleBundle officialScheduleBundle : scheduleElements) {
            if (officialScheduleBundle.scheduleElement().equals(scheduleElement)) {
                officialScheduleBundle.role().add(role.toString());
                return;
            }
        }
        scheduleElements.add(new OfficialScheduleBundle(scheduleElement, new ArrayList<>(Collections.singletonList(role.name()))));
    }
}
