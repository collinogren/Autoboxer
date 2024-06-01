package ogren.collin.autoboxer.process;

public class StringUtils {

    public static String toLastFirst(String name) {
        String[] split = name.split(" ");
        String first = "";
        int numSplits = split.length;
        for (int i = 0; i < numSplits - 1; i++) {
            first += split[i];
        }

        String last = split[numSplits - 1];

        return last + ", " + first;
    }
}
