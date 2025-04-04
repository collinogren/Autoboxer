package ogren.collin.autoboxer;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.io.File;
import java.io.IOException;

public class PrinterUtils {

    // Work in progress
    private static String defaultPhysicalPrinter = ""; // This will be set by the user.
    private static String clawPDF = "clawPDF"; // This will likely not change, but just in case.
    // End work in progress

    public static String getDefaultPrinter() {
        return PrintServiceLookup.lookupDefaultPrintService().getName();
    }

    public static void setDefaultPrinterToClawPDF() {
        setDefaultPrinter(clawPDF);
    }

    public static void setDefaultPrinter(String printerName) {
        String command = "rundll32 printui.dll,PrintUIEntry /y /n \"" + printerName + "\"";

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
            processBuilder.start();
        } catch (IOException ioe) {
            Logging.logger.error(ioe.getStackTrace());
        }
    }

    public static void openPrintersScannersUtility() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", "start", "ms-settings:printers");
            processBuilder.start();
        } catch (IOException e) {
            Logging.logger.error(e);
            throw new RuntimeException(e);
        }
    }

    // Function to set the output directory for ClawPDF by editing the Windows registry.
    public static void setClawPDFRegVariable(String endDir, File boxDirectory) {
        String directory = boxDirectory.getPath().replace("/", "\\").replace("\\", "\\\\") + "\\\\" + endDir;
        try {
            String[] regCommand = {"REG", "ADD", "HKEY_CURRENT_USER\\Software\\clawSoft\\clawPDF\\Settings\\ConversionProfiles\\0\\AutoSave", "/v", "TargetDirectory", "/d", "\"" + directory + "\"", "/f"};
            Runtime.getRuntime().exec(regCommand);
        } catch (IOException e) {
            Logging.logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public static PrintService[] getAllPrinters() {
        return PrintServiceLookup.lookupPrintServices(null, null);
    }

    // Work in progress
    public static void setClawPDFName(String clawPDFName) {
        clawPDF = clawPDFName;
    }

    // Work in progress
    public static void setDefaultPhysicalPrinter(String printerName) {
        defaultPhysicalPrinter = printerName;
    }
}
