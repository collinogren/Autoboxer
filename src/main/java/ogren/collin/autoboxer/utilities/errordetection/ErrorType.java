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

    // Warnings
    public static final ErrorType MISSING_PAPERS_FOR_SCHEDULED_EVENT = new ErrorType("missing papers for scheduled event", ErrorLevel.WARNING);
}
