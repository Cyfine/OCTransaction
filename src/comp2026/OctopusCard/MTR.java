package comp2026.OctopusCard;

import comp2026.OctopusCard.Util.*;
import java.text.ParseException;


public class MTR extends OCTransaction {
    public static final String TypeHdrStr = "MTR";
    private final String station;
    private final MTRType mtrType;
    private MTR matchedRecord;

    //constructor
    public MTR(String dateTimeStr, String transactionID, String amount, String mtrType, String station) throws OCTransactionFormatException {
        super(TypeHdrStr, dateTimeStr, transactionID, amount);
        this.station = station;
        switch (mtrType.toLowerCase()) {
            case "checkin":             // receive string, match to enum MTRType
                this.mtrType = MTRType.CheckIn;
                break;
            case "checkout":
                this.mtrType = MTRType.CheckOut;
                break;
            default:
                throw new OCTransactionFormatException("Invalid CheckIn/CheckOut type: " + mtrType);

        }
        setStatus(Status.MTR_OUTSTANDING);
        if (this.getMtrType().equals(MTRType.CheckIn) && this.getAmount() != 0.0) {
            throw new MTRTransactionFormatException("Invalid amount for CheckIn transaction: " + this.getAmount());
        }
    }

    //=================Inner Class enum MTRType===================
    public enum MTRType {
        CheckIn, CheckOut
    }

    //================= parseMTRTransaction ======================
    // used by superClass parseTransaction()
    public static OCTransaction parseTransaction(String record) throws OCTransactionFormatException {
        String[] tokens = Tokenizer.getTokens(record);
        String station = StringUtil.strMerge(tokens, 5);

        if (tokens.length < 6) { // if the number of tokens are too few, throw an exception
            throw new MTRTransactionFormatException("parseMTRTransaction: too few tokens.");
        }
        return new MTR(tokens[1], tokens[2], tokens[3], tokens[4], station);
    }

    //==================== getters/setter =======================
    public String getStation() {
        return station;
    }

    public MTRType getMtrType() {
        return mtrType;
    }

    public void setMatchedRecord(MTR mtrRec) {
        matchedRecord = mtrRec;
        setStatus(Status.MTR_COMPLETED);
    }

    //==================== helper methods  =======================
    public String toRecord() {
        return getRecordHdr() + " " + mtrType + " " + station;
    }


    /*
    * toString()
    * For each toString in the MTR class, there are two situation,
    * if the MTR the transaction record is completed, it will print the chose to print without details of
    * reference record
    */
    public String toString() {
        String referenceRecInfo = "    Matching TransactionID: OUTSTANDING!\n";
        //the record are all originally set as outstanding , print as no reference

        //if the record got a reference, it will the referenceInfo will be the part that contains
        // Matching info
        if (getStatus().equals(Status.MTR_COMPLETED)) {
            referenceRecInfo = "    Matching TransactionID: " + matchedRecord.getTransactionID() + "\n" +
                    "      Date/Time: " + DateTimeUtil.dateTimeToDisplayString(matchedRecord.getDate()) + "\n" +
                    "      Station: " + matchedRecord.getStation() + "\n";
        }

        return "[MTR Transaction]\n" +
                "    MTR Type: " + mtrType + "\n" +
                referenceRecInfo +
                "    Station: " + station + "\n" +
                super.toString();
    }

    //========================= match ===========================
    //using String.contains(CharSequence chars) to implement the partial match
    public boolean match(String[] criteria) throws OCTransactionSearchException {

        if (criteria.length != 2) {
            throw new OCTransactionSearchException("Invalid number of arguments");
        }

        //original tag, used to report error message with original case letter
        String userInputTag = StringUtil.strMerge(criteria, 1);

        //the lower case tag info, used to compare, with case ignored
        String tag = userInputTag.toLowerCase();
        switch (criteria[0].toLowerCase()) {
            case "station":
                return station.toLowerCase().contains(tag);
            case "mtrtype":
                return ("" + mtrType).toLowerCase().contains(tag);
            case "status":
                return ("" + getStatus()).toLowerCase().contains(tag);

            case "date":
                try {
                    return matchDate(tag);
                } catch (ParseException e) {
                    throw new OCTransactionSearchException(e.getMessage() + userInputTag);
                }

            default:
                throw new OCTransactionSearchException("Invalid MTR search type" + criteria[0]);
        }

    }

    //================== Inner Exception class=================
    public static class MTRTransactionFormatException extends OCTransactionFormatException {
        public MTRTransactionFormatException(String mtrTransactionExMsg) {
            super("MTRTransactionFormatException: " + mtrTransactionExMsg);
        }
    }


}
