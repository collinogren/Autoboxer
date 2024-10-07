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

import ogren.collin.autoboxer.pdf.FileType;

import java.util.ArrayList;

public class IdentityBundle {

    String name;
    Role role;
    int occurrenceToBox;
    int judgeNumber = 0;

    public IdentityBundle(String name, Role role, int occurrenceToBox) {
        this.name = name;
        this.role = role;
        this.occurrenceToBox = occurrenceToBox;
    }

    public IdentityBundle(String name, Role role, int judgeNumber, int occurrenceToBox) {
        this(name, role, occurrenceToBox);
        this.judgeNumber = judgeNumber;
    }

    public String name() {
        return name;
    }

    public Role role() {
        return role;
    }

    public int occurrenceToBox() {
        return occurrenceToBox;
    }

    public int getJudgeNumber() {
        return judgeNumber;
    }

    public ArrayList<FileType> matchRoleToFileTypeIJS() {
        ArrayList<FileType> types = new ArrayList<>();
        types.add(FileType.IJS_COVERSHEET);
        switch (role) {
            case REFEREE -> {
                types.add(FileType.IJS_REFEREE_SHEET);
                types.add(FileType.IJS_JUDGE_SHEET);
                return types;
            }
            case JUDGE, TS1 -> {
                types.add(FileType.IJS_TS1_SHEET);
                types.add(FileType.IJS_JUDGE_SHEET);
                return types;
            }
            case TC -> {
                types.add(FileType.IJS_TC_SHEET);
                return types;
            }
            case TS2 -> {
                types.add(FileType.IJS_TS2_SHEET);
                types.add(FileType.IJS_JUDGE_SHEET); // TS2 can get judge sheet because of solo dance.
                return types;
            }
            case VIDEO, DEO, AR -> {
                return types;
            }
        }

        return types;
    }
}
