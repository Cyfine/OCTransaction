package comp2026.OctopusCard;

import comp2026.OctopusCard.Util.*;

import java.text.ParseException;

public class BusFare extends OCTransaction {
    public final static String TypeHdrStr = "BusFare";
    private final String station;
    private final String terminal;
    private final String route;

    // constructor
    public BusFare(String dateTimeStr, String transactionID, String amountStr, String route, String station, String terminal) throws OCTransactionFormatException {
        super(TypeHdrStr, dateTimeStr, transactionID, amountStr);
        this.station = station;
        this.terminal = terminal;
        this.route = route;
        setStatus(Status.COMPLETED);
    }


    //=====================================================
    //parseBusFareTransaction
    public static OCTransaction parseTransaction(String record) throws OCTransactionFormatException {
        String[] tokens = Tokenizer.getTokens(record);
        String[] mergedTokens = toMerge(tokens, 5);
        if (tokens.length < 6) {
            throw new BusTransactionFormatException("parseBusFareTransaction: too few tokens");
        }
        return new BusFare(tokens[1], tokens[2], tokens[3], tokens[4], mergedTokens[0], mergedTokens[1]);
    }

    // merge the String the String by checking the token "to"
    //such as {"Kowloon" , "Tong", "to" ,"Mong", "Kok"} --> { "Kowloon Tong" , "Mong Kok"}
    private static String[] toMerge(String[] str, int startPos) {
        String[] result = {"", ""};

        for (int i = startPos; i < str.length; i++) {
            // if the record is separated by "to", merge the element from start pos to "to", and merger rest of the elements in the array.
            if (str[i].equals("to")) {
                StringUtil.strMerge(str, startPos, i - 1);
                result[0] = StringUtil.strMerge(str, startPos, i - 1);
                result[1] = StringUtil.strMerge(str, i + 1);
                return result;
            }
        }
        return result;
    }

    //====================================================
    //toString()
    // toSting, used by list();
    public String toString() {
        return "[Bus Fare]\n" +
                "    Route: " + route + "\n" +
                "    Terminal: " + terminal + "\n" +
                "    Station: " + station + "\n" +
                super.toString();
    }

    //====================================================
    //match()
    //Implementation of abstract method match() in the superclass, used by the
    // OCTransactionDB.search(String type, String[] criteria)
    public boolean match(String[] criteria) throws OCTransactionSearchException {
        if (criteria.length < 2) {
            throw new OCTransactionSearchException("Invalid number of arguments");
            // throw exception if the number of the argument are illegal
        }

        String userInputTag = StringUtil.strMerge(criteria, 1);// used to give out error message
        String tag = userInputTag.toLowerCase(); // used to compare ignore case
        switch (criteria[0].toLowerCase()) {
            case "station":
                return station.toLowerCase().contains(tag);

            case "terminal":
                return terminal.toLowerCase().contains(tag);

            case "route":
                return route.toLowerCase().equals(tag);
            // specially for the route, it do not make partial match, same as the demo program

            case "date":
                try {
                    return matchDate(tag);
                } catch (ParseException e) {
                    throw new OCTransactionSearchException(e.getMessage() + userInputTag);
                }


            default:
                throw new OCTransactionSearchException("Invalid BusFare search type: " + criteria[0]);
        }

    }


    //====================================================
    //toRecord()
    //used by the implementation of abstract method toRecord in superClass,
    // used by the OCTransactionDB.saveDB()
    public String toRecord() {
        return getRecordHdr() + " " + route + " " + station + " to " + terminal;
    }

    //=====================Inner Class: BusTransactionFormatException================
    public static class BusTransactionFormatException extends OCTransactionFormatException {
        public BusTransactionFormatException(String BusTransactionFormatExMsg) {
            super("BusTransactionFormatException: " + BusTransactionFormatExMsg);
        }
    }


}
