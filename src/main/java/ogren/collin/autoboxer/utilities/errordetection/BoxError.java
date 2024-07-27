package ogren.collin.autoboxer.utilities.errordetection;

public class BoxError {

    private String eventIdentifier;
    private String official;
    private ErrorType errorType;

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
           forOfficial = "\nFor " + official;
       } else {
           forOfficial = "";
       }

       return errorMessage + errorType.description() + "\nat " + eventIdentifier + forOfficial;
    }
}
