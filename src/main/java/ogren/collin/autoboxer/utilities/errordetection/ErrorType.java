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

package ogren.collin.autoboxer.utilities.errordetection;

public record ErrorType(String description, ErrorLevel errorLevel) {
    // Errors
    public static final ErrorType MISSING_JUDGE_PAPERS = new ErrorType("missing judge papers", ErrorLevel.ERROR);
    public static final ErrorType MISSING_TECHNICAL_PANEL_PAPERS = new ErrorType("missing technical panel papers", ErrorLevel.ERROR);
    public static final ErrorType MISSING_REFEREE_PAPERS = new ErrorType("missing referee papers", ErrorLevel.ERROR);
    public static final ErrorType WRONG_FILE_POSITION = new ErrorType("file is in the wrong place", ErrorLevel.ERROR);
    public static final ErrorType LIKELY_WRONG_DELIMITER = new ErrorType("likely has wrong delimiter or the wrong delimiter was set in Autoboxer", ErrorLevel.ERROR);
    public static final ErrorType MISSING_START_TIME = new ErrorType("missing start time in the schedule", ErrorLevel.ERROR);
    public static final ErrorType DUPLICATE_SCHEDULE_ENTRY = new ErrorType("duplicate schedule entry", ErrorLevel.ERROR);
    public static final ErrorType FILE_SAVE_ERROR = new ErrorType("could not save be saved to PDF. This may be due to use of a character disallowed in Windows file names such as '*' and similar.", ErrorLevel.ERROR);
    public static final ErrorType TS_FILE_READ_ERROR = new ErrorType("Failed to read TS1 or TS2 technical panel sheets during a sorting check.", ErrorLevel.ERROR);

    // Warnings
    public static final ErrorType MISSING_PAPERS_FOR_SCHEDULED_EVENT = new ErrorType("missing papers for scheduled event", ErrorLevel.WARNING);
}
