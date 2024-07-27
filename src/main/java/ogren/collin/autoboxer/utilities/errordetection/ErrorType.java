package ogren.collin.autoboxer.utilities.errordetection;

public record ErrorType(String description, ErrorLevel errorLevel) {
    public static final ErrorType MISSING_JUDGE_PAPERS = new ErrorType("missing judge papers", ErrorLevel.ERROR);
    public static final ErrorType MISSING_TECHNICAL_PANEL_PAPERS = new ErrorType("missing technical panel papers", ErrorLevel.ERROR);
    public static final ErrorType MISSING_REFEREE_PAPERS = new ErrorType("missing referee papers", ErrorLevel.ERROR);
    public static final ErrorType MISSING_CORRECT_DELIMITER = new ErrorType("missing correct delimiter", ErrorLevel.ERROR);
    public static final ErrorType MISSING_PAPERS_FOR_SCHEDULED_EVENT = new ErrorType("missing papers for scheduled event", ErrorLevel.ERROR);
}
