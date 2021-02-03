package comp2026.OctopusCard;

import comp2026.OctopusCard.Util.*;

import java.text.ParseException;

public class Retail extends OCTransaction {
    public final static String TypeHdrStr = "Retail";
    private String retailer;
    private String description;

    public Retail(String dateTimeStr, String transactionID, String amountStr, String retailer, String description)
            throws OCTransactionFormatException {
        super(TypeHdrStr, dateTimeStr, transactionID, amountStr);
        setStatus(Status.COMPLETED);
        this.retailer = retailer;
        this.description = description;
    }

    //=================== parseRetailTransaction ================
    public static OCTransaction parseTransaction(String record) throws OCTransactionFormatException {
        String[] tokens = Tokenizer.getTokens(record);
        String[] mergedToken = commaMerge(tokens, 4);

        if (tokens.length < 5) {
            throw new RetailTransactionFormatException("parseRetailTransaction: two few tokens");
        }

        return new Retail(tokens[1], tokens[2], tokens[3], mergedToken[0], mergedToken[1]);
    }

    // merge tokens by finding the comma
    // {"Paper", "&", “Coffee,", "Cappuccino"} --> {"Paper & Coffee", "Cappuccino"}
    private static String[] commaMerge(String[] str, int startPos) {
        String[] result = {"", ""};

        for (int i = startPos; i < str.length; i++) {
            //if the record is separated by ",", merge the element from start pos to the element with"," and merge rest of the elements.
            if (str[i].indexOf(",") > 0) {
                String temp = "";

                for (int j = 0; j < str[i].length() - 1; j++) {
                    temp += str[i].charAt(j);

                }

                str[i] = temp;
                result[0] = StringUtil.strMerge(str, startPos, i);
                result[1] = StringUtil.strMerge(str, i + 1);

                return result;
            }
        }
        return result;
    }

    // ===============helper Methods================
    public String toRecord() {
        return getRecordHdr() + " " + retailer + ", " + description;
    }

    public String getRetailer() {
        return retailer;
    }

    public String getDescription() {
        return description;
    }

    public String toString() {
        return "[Retail]\n" +
                "    Retailer:" + retailer + "\n" +
                "    Description: " + description + "\n" +
                super.toString(); // call super class constructor to print rest of the String
    }


    public boolean match(String[] criteria) throws OCTransactionSearchException {
        if (criteria.length < 2) { // throw exception if the number of arguments is invalid
            throw new OCTransactionSearchException("Invalid number of arguments");
        }

        String userInputTag = StringUtil.strMerge(criteria, 1);
        String tag = userInputTag.toLowerCase();

        switch (criteria[0].toLowerCase()) {
            case "retailer":
                return retailer.toLowerCase().contains(tag);

            case "description":
                return description.toLowerCase().contains(tag);

            case "date":
                try {
                    return matchDate(tag);
                } catch (ParseException e) {
                    throw new OCTransactionSearchException(e.getMessage()  + userInputTag);
                }

            default:
                throw new OCTransactionSearchException("Invalid Retail search type: " + criteria[0]);
        }
    }


    public static class RetailTransactionFormatException extends OCTransactionFormatException {
        public RetailTransactionFormatException(String retailTransactionFormatExMsg) {
            super("RetailTransactionFormatException: " + retailTransactionFormatExMsg);
        }
    }


}



