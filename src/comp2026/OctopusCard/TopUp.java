// contains a enum class : TopUpType
//


package comp2026.OctopusCard;

import comp2026.OctopusCard.Util.*;

import java.text.ParseException;

public class TopUp extends OCTransaction {
    public final static String TypeHdrStr = "TopUp";
    private final TopUpType topUpType;
    private final String agent;


    //======================== constructor ===========================
    public TopUp(String dateTimeStr, String transactionID, String amountStr, String topUpType, String agent)
            throws OCTransactionFormatException {

        super(TypeHdrStr, dateTimeStr, transactionID, amountStr);
        switch (topUpType.toLowerCase()) {
            case "bank":
                this.topUpType = TopUpType.Bank;
                break;
            case "cash":
                this.topUpType = TopUpType.Cash;
                break;
            default:
                throw new OCTransactionFormatException("Invalid topUpType: " + topUpType);
        }

        this.agent = agent;
        setStatus(Status.COMPLETED);
    }

    // =============== parseTopUpTransaction ================
    public static OCTransaction parseTransaction(String record) throws OCTransactionFormatException {
        String[] tokens = Tokenizer.getTokens(record);
        String agent = StringUtil.strMerge(tokens, 5, tokens.length - 1);
        if (tokens.length < 6) {
            throw new TopUPTransactionFormatException("parseTopUpTransaction: too few tokens");
        }
        return new TopUp(tokens[1], tokens[2], tokens[3], tokens[4], agent);
    }


    // ******************** Helper Methods ***********************
    public String toRecord() {
        return getRecordHdr() + " " + topUpType + " " + agent;
    }

    // =============== toString ================
    public String toString() {
        return "[Top Up]\n" +
                "    Agent: " + agent + "\n" +
                "    Type: " + topUpType + "\n" +
                super.toString();
    }


    // =============== match ======================
    //used by the search method, true if fits the criteria

    public boolean match(String[] criteria) throws OCTransactionSearchException {
        boolean bankNameMatch = true;
        String userInputTag = "";
        String tag = "";


        // if input is one, the bankNameMatch is originally true regardless of comparing bank name
        if (criteria.length > 1) {
            // if the criteria is greater than one, merge the criteria array to search tag
            userInputTag = StringUtil.strMerge(criteria, 1);
            tag = userInputTag.toLowerCase();
            bankNameMatch = agent.toLowerCase().contains(tag);

        } else if (criteria.length < 1) {
            throw new OCTransactionSearchException("Invalid number of arguments");
        }


        //using switch to get the search type and partial search using String.contains()
        switch (criteria[0].toLowerCase()) {
            case "cash":
                if( criteria.length !=1  ){ // the cash option only receive one arguments, more than one will throw an exception
                    throw new OCTransactionSearchException("Invalid number of arguments.");
                }
                return topUpType.equals(TopUpType.Cash);

            case "bank":
                return topUpType.equals(TopUpType.Bank) && bankNameMatch;

            case "date":
                try {
                    return matchDate(tag);
                } catch (ParseException e) {
                    throw new OCTransactionSearchException(e.getMessage()  + userInputTag);
                }
            default:
                throw new OCTransactionSearchException("Invalid TopUp search type" + criteria[0]);
        }

    }

    //==================Inner Classes==================
    public enum TopUpType {
        Bank, Cash
    }

    public static class TopUPTransactionFormatException extends OCTransactionFormatException {
        public TopUPTransactionFormatException(String TopUpExceptionExMsg) {
            super("TopUpTransactionFormatException: " + TopUpExceptionExMsg);
        }
    }

}
