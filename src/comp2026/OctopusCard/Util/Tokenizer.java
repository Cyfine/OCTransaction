package comp2026.OctopusCard.Util;

public class Tokenizer {
    // ============================================================
    // getTokens
    public static String[] getTokens(String str) {
        String[] tokenArray = new String[0];

        for (int pos = 0; pos < str.length(); ) {
            pos = skipUnwanted(str, pos);
            String token = nextToken(str, pos);
            tokenArray = addToTokenArray(tokenArray, token);
            pos += token.length();
        }

        return tokenArray;
    }

    // ============================================================
    // skipUnwanted
    private static int skipUnwanted(String str, int begin) {
        int pos = begin;

        for (; pos < str.length(); pos++) {
            char c = str.charAt(pos);
            if (c != ' ') {
                break;
            }
        }
        return pos;
    }

    // ============================================================
    // nextToken
    private static String nextToken(String str, int begin) {
        int pos = begin;
        String token = "";

        for (; pos < str.length(); pos++) {
            char c = str.charAt(pos);

            if (c == ' ') {
                break;
            }

            token += str.charAt(pos);
        }
        return token;
    }

    // ============================================================
    // addToTokenArray
    private static String[] addToTokenArray(String[] tokenArray, String token) {
        // chk the length of token
        if (token.length() == 0) {
            return tokenArray;
        }

        // create the new token array
        String[] newTokenArray = new String[tokenArray.length + 1];

        // copy tokenArray
        for (int i = 0; i < tokenArray.length; i++) {
            newTokenArray[i] = tokenArray[i];
        }

        // get the new token and return the new token array
        newTokenArray[newTokenArray.length - 1] = token;
        return newTokenArray;
    }

}

