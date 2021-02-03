package comp2026.OctopusCard;

import comp2026.OctopusCard.Util.DateTimeUtil;
import comp2026.OctopusCard.Util.Tokenizer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class OCTransaction {
    private final Date date;
    private final String transactionID;
    private final double amount;
    private final String type;
    private Status status;

    public enum Status {
        MTR_COMPLETED, MTR_OUTSTANDING, COMPLETED
    }


    //============================================================
    // Constructors
    public OCTransaction(String type, String dateTimeStr, String transactionID, String amountStr) throws OCTransactionFormatException {
        this(type, parseDateTimeStr(dateTimeStr), transactionID, parseAmountStr(amountStr));
    }

    public OCTransaction(String type, Date date, String transactionID, double amount) throws OCTransactionFormatException {
        this.type = type;
        this.date = date;
        this.transactionID = transactionID;
        this.amount = amount;


        // chk type
        if (!typeIsValid(this.type)) {
            throw new OCTransactionFormatException("Invalid transaction type: " + this.type);
        }

        if (!isIdValid(transactionID)) {
            throw new OCTransactionFormatException("Invalid transactionID: " + transactionID);
        }
    }


    /**/
    public static boolean typeIsValid(String type) {
        switch (type.toLowerCase()) {
            case "mtr":
            case "busfare":
            case "topup":
            case "retail":
                return true;
            default:
                return false;
        }
    }

    //============================================================
    // parseTransaction: type dateTime transactionID amount...
    public static OCTransaction parseTransaction(String record) throws OCTransactionFormatException {
        String[] tokens = Tokenizer.getTokens(record);

        // chk for blank line (no tokens)
        if (tokens.length == 0) {
            return null;
        }

        // chk transaction type
        String transactionType = tokens[0];
        if (transactionType.equalsIgnoreCase(MTR.TypeHdrStr)) {
            return MTR.parseTransaction(record);
        } else if (transactionType.equalsIgnoreCase(BusFare.TypeHdrStr)) {
            return BusFare.parseTransaction(record);
        } else if (transactionType.equalsIgnoreCase(Retail.TypeHdrStr)) {
            return Retail.parseTransaction(record);
        } else if (transactionType.equalsIgnoreCase(TopUp.TypeHdrStr)) {
            return TopUp.parseTransaction(record);
        } else {
            throw new OCTransactionFormatException("parseTransaction: Invalid transaction type: " + tokens[0]);
        }

    }


    //============================================================
    // Helper Methods
    public String getType() {
        return type;
    }

    public Date getDate() {
        return date;
    }

    public String getDateStr() {
        return new SimpleDateFormat("MMM. d, yyyy (E)").format(date);
    }

    public String getTimeStr() {
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }

    public String getTransactionID() {
        return transactionID;
    }

    public double getAmount() {
        return amount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getRecordHdr() {
        return type + " " + DateTimeUtil.dateTime2Str(date) + " " + transactionID + " " + amount;
    }

    //===========================================================
    /*
     * check if the transaction is valid, if not return false, if valid, return true
     * */
    public static boolean isIdValid(String idStr) {
        if (idStr.length() != 5) {
            return false;
        }

        try {
            Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    //============================================================
    // Helper Method -- parseDateTimeStr
    private static Date parseDateTimeStr(String dateStr) throws OCTransactionFormatException {

        try {
            return DateTimeUtil.str2DateTime(dateStr);
        } catch (ParseException e) {
            throw new OCTransactionFormatException("parseDateTimeStr: Corrupted datetime: " + dateStr);
        }
    }


    //============================================================
    // Helper Method -- parseAmountStr
    private static double parseAmountStr(String amountStr) throws OCTransactionFormatException {
        try {
            return Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            throw new OCTransactionFormatException("parseAmountStr: Corrupted amount: " + amountStr);
        }
    }


    //============================================================
    // Helper Method -- matchDate
    // Advanced Feature added comparing to the demo program - calendar legal check
    // see DateTimeUtil.chkDayLegal and isSearchDaTimeValid
    protected boolean matchDate(String matchDateStr) throws ParseException {
        String dateFormat = "yyyy-MM-dd";

        // validate dateStr
        if (!isSearchDateTimeValid(matchDateStr)) {
            throw new ParseException("Invalid date format: ", 0);
        }
        new SimpleDateFormat(dateFormat).parse(matchDateStr);
        return new SimpleDateFormat(dateFormat).format(date).equals(matchDateStr);
    }

    //=========================== Date String valid check =================================
    // isSearchDateTimeValid - helper method for matchDate
    // check the date (yyyy-mm-dd)for the search method, if the date String
    // is format legal and calendar legal
    //// isSearchDateTimeValid() will split date into tokens and check digits of each date time token,
    // then parse each token into integer, to check illegal chars  (by catch ParseException)
    // determine whether the input date is legal calendar date (including the Feb of leap years)
    // this doing more than the demo program
    // the demo program receive the illegal date format like 2020-13-01 (as the api parse it as 2021-01-01)
    // so extra valid date check is added, it will check the Str including format and calendar legal
    private static boolean isSearchDateTimeValid(String matchDateStr) {
        String[] dateTimeTokens = Tokenizer.getTokens(matchDateStr.replaceAll("-", " "));
        int[] dateNum = new int[3];

        if (dateTimeTokens.length != 3 && matchDateStr.length()!=10) {
            return false;
        } else if (dateTimeTokens[0].length() != 4 || dateTimeTokens[1].length() != 2 || dateTimeTokens[2].length() != 2) {
            return false;
        }

        // parse date token into integers,
        // failing parsing the date tokens will pop ParseException
        // which indicates the DateTime invalid, thus return false
        try {
            dateNum[0] = Integer.parseInt(dateTimeTokens[0]);
            dateNum[1] = Integer.parseInt(dateTimeTokens[1]);
            dateNum[2] = Integer.parseInt(dateTimeTokens[2]);
        } catch (NumberFormatException e) {
            return false;
        }

        return DateTimeUtil.chkDayLegal(dateNum[0], dateNum[1], dateNum[2]);

    }



    //============================================================
    // match
    public abstract boolean match(String[] criteria) throws OCTransactionSearchException;

    //============================================================
    // toString
    @Override
    public String toString() {
        String str = "";
        str += "    TransactionID: " + transactionID + "\n";
        str += "    Date/Time: " + getDateStr() + " at " + getTimeStr() + "\n";
        str += "    Amount: " + amount + "\n";
        str += "    Status: " + status + "\n";
        return str;
    }

    public abstract String toRecord();



    //=================Inner Exception Classes================================
    // OCTransactionSearchException
    public static class OCTransactionSearchException extends Exception {
        public OCTransactionSearchException(String ocTransactionSearchExMsg) {
            super(ocTransactionSearchExMsg);
        }
    }


    //============================================================
    // OCTransactionFormatException
    public static class OCTransactionFormatException extends Exception {
        public OCTransactionFormatException(String ocTransactionFormatExMsg) {
            super(ocTransactionFormatExMsg);
        }
    }

}
