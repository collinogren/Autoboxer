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

package ogren.collin.autoboxer.control;

import ogren.collin.autoboxer.process.Official;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Stream;

public class MasterController {

    private static final String COVERSHEET_DIR = "coversheets";
    private static final String JUDGE_SHEETS_DIR = "judges";
    private static final String TECH_PANEL_DIR = "tech";
    private static final String SIX0 = "60";

    private ArrayList officials = new ArrayList<Official>();

    private String baseDir;

    public MasterController(String baseDir) {
        this.baseDir = baseDir;
    }

    public void begin() {

    }

    private void renameFiles() {

    }

    private ArrayList<File> getAllFiles(String relativeDir) {
        ArrayList files = (ArrayList) FileUtils.listFiles(new File(baseDir + "/" + relativeDir), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        return  files;
    }
}
