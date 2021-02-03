/*
*A helper Util class to handle String
*/

package comp2026.OctopusCard.Util;

public class StringUtil {
    /*
     * merge the elements of an array form @startPos, to @endPos, including element at startPos and endPos
     * The merge result is separated by " ".
     * */
    public static String strMerge(String[] str, int startPos, int endPos) {
        String result = "";

        // bad startPos and endPos handling
        if (startPos > endPos) {
            return null;
        }
        if (endPos > str.length - 1) {
            return null;
        }

        for (int pos = startPos; pos < endPos + 1; pos++) {
            result += str[pos];

            if (pos == endPos) {
                break;
            }

            result += " ";
        }
        return result;
    }

    // Merge the elements in a token array with comma separated
    public static String strMerge(String[] str, int startPos) {
        return strMerge(str, startPos, str.length - 1);
    }

}
