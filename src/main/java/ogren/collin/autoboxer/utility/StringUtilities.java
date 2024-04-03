package ogren.collin.autoboxer.utility;

public class StringUtilities {

    public static String truncateWithEllipses(String s, int length) {
        if (s.length() > length) {
            s = truncate(s, length - 3);
            s += ". . .";
        }

        return s;
    }

    public static String truncate(String s, int length) {
        if (s.length() > length) {
            return s.substring(0, length);
        } else {
            return s;
        }
    }
}
