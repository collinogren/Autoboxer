package ogren.collin.autoboxer.utilities.errordetection;

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

        return errorMessage + "event " + eventIdentifier + " " + errorType.description() + forOfficial;
    }
}
