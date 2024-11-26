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

package ogren.collin.autoboxer.utilities.error_detection;

public class BoxError {

    private final String eventIdentifier;
    private final String official;
    private final ErrorType errorType;

    public BoxError(String eventIdentifier, String official, ErrorType errorType) {
        this.eventIdentifier = eventIdentifier;
        this.official = official;
        this.errorType = errorType;
    }

    public String createErrorMessage() {
        String errorMessage;
        switch (errorType.errorLevel()) {
            case ErrorLevel.ERROR -> errorMessage = "Error: ";
            case ErrorLevel.WARNING -> errorMessage = "Warning: ";
            default -> errorMessage = "Info: ";
        }

        String forOfficial;
        if (official != null) {
            forOfficial = " for " + official;
        } else {
            forOfficial = "";
        }

        String eventSpecifier;
        if (eventIdentifier == null) {
            eventSpecifier = "";
        } else {
            eventSpecifier = "event " + eventIdentifier;
        }

        return errorMessage + eventSpecifier + " " + errorType.description() + forOfficial;
    }
}
