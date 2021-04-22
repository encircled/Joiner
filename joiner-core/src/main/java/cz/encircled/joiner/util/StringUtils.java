package cz.encircled.joiner.util;

public class StringUtils {

    public static String uncapitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

}
