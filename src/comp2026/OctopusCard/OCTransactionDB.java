package comp2026.OctopusCard;

import comp2026.OctopusCard.Util.Tokenizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class OCTransactionDB {
    private List<OCTransaction> transactionList = new ArrayList<OCTransaction>();

    // ============================================================
    // constructor
    public OCTransactionDB() {
    }


    // ============================================================
    // loadDB
    public void loadDB(String fName) throws OCTransactionDBException {
        int cnt = 0;
        int lineNo = 0;

        try {
            System.out.println("Loading transaction db from " + fName + "...");
            Scanner in = new Scanner(new File(fName));

            // the number of records read, and the line number of corrupted records
            // are not reported corrected!
            while (in.hasNext()) {
                lineNo++;
                String line = in.nextLine();
                try {
                    if (addTransaction(line) != null) {
                        cnt++;
                    }
                } catch (OCTransactionDBException | OCTransaction.OCTransactionFormatException e) {
                    System.out.println("OCTransactionDB.loadDB: error loading record from line " + lineNo + " of "
                            + fName + " -- " + e.getMessage());
                }

            }
        } catch (FileNotFoundException e) {
            throw new OCTransactionDBException("loadDB failed: File not found (" + fName + ")!");
        }
        System.out.println(cnt + " Octopus card transactions loaded.");
    }

    // ============================================================
    // saveDB
    public void saveDB(String fName) throws OCTransactionDBException {
        int cnt = 0;


        try {
            PrintWriter out = new PrintWriter(fName);
            for (OCTransaction ocTransaction : transactionList) {
                out.println(ocTransaction.toRecord());
                cnt++;
            }
            out.close();
        } catch (FileNotFoundException e) {
            throw new OCTransactionDBException("File not found: " + fName);
            // if file not found, through file not found exception
        }
        System.out.println(cnt + " Octopus card transactions saved to " + fName + ".");
    }

    // ============================================================
    // list
    public void list(String type) {
        int cnt = 0;

        //  go through transactionList, and print transactions
        // with matching type (or print all if type is an empty string)
        // Note: (1) should ignore letter case for the type; and
        // (2) should count the number of records correctly.
        for (OCTransaction ocTransaction : transactionList) {
            if (ocTransaction.getType().equalsIgnoreCase(type) || type.equals("")) {
                System.out.println(ocTransaction);
                cnt++;
            }

        }
        System.out.println(cnt + " record(s) found.");
    }

    public void list() {
        list("");
    }

    // ============================================================
    // addTransaction
    public void addTransaction(OCTransaction newTransaction) throws OCTransactionDBException {
        //  revised this so that (1) new transactions will be added to
        // transactionList in a chronological manner, with older transactions
        // listed first; and (2) duplicated transactions will not be added to
        // transactionList (duplicated transaction = the same transaction type,
        // with the same transaction date & time, and the same transaction
        // number).
        if (searchIdx(newTransaction.getType(), newTransaction.getDate(), newTransaction.getTransactionID()) == -1) {
            int index = transactionList.size();
            Date newTransactionDate = newTransaction.getDate();

            for (int i = 0; i < transactionList.size(); i++) {
                Date transactionRecDate = transactionList.get(i).getDate();
                if (transactionRecDate.after(newTransactionDate)) {
                    index = i;
                    break;
                }
            }
            transactionList.add(index, newTransaction);
        } else {
            throw new OCTransactionDBException("Ignoring duplicated record"); // fixme
        }
    }

    public OCTransaction addTransaction(String record)
            throws OCTransactionDBException, OCTransaction.OCTransactionFormatException {
        OCTransaction transaction = OCTransaction.parseTransaction(record);

        // skip blank lines
        if (transaction == null) {
            return null;
        }

        addTransaction(transaction);

        //  Do this last when we try to match MTR checkout transactions
        // with their corresponding checkin transaction.
        if (transaction.getType().equalsIgnoreCase("MTR")
                && ((MTR) transaction).getMtrType().equals(MTR.MTRType.CheckOut)) {
            mtrCheckOut((MTR) transaction);
        }

        return transaction;
    }

    // ============================================================
    // mtrCheckOut
    private void mtrCheckOut(MTR chkOutTransaction) throws OCTransactionDBException {
        // from the newest to the oldest transactions, search for
        // an outstanding MTR checkin transaction.

        // if the checkin transaction is found, match them so that
        // they keep a reference pointing to each other.

        // if no outstanding checkin transaction can be found, throw
        // a OCTransactionDBException.
        boolean match = false;
        for (int i = transactionList.size() - 1; i >= 0; i--) {
            OCTransaction ocTransaction = transactionList.get(i);
            if (ocTransaction.getType().equalsIgnoreCase("mtr")
                    && ((MTR) ocTransaction).getMtrType().equals(MTR.MTRType.CheckIn)
                    && ocTransaction.getStatus().equals(OCTransaction.Status.MTR_OUTSTANDING)) {

                ((MTR) ocTransaction).setMatchedRecord(chkOutTransaction); // making reference of two matched record
                chkOutTransaction.setMatchedRecord((MTR) ocTransaction);
                match = true;
                break;
            }
        }
        if (!match) {
            throw new OCTransactionDBException("mtrCheckOut: No outstanding MTR CheckIn transaction found!");
        }

    }

    // ============================================================
    // search
    public OCTransaction[] search(String type, String[] criteria) throws OCTransaction.OCTransactionSearchException {
        OCTransaction[] searchResult = new OCTransaction[0];

        if (!OCTransaction.typeIsValid(type)) {
            throw new OCTransaction.OCTransactionSearchException("Invalid search type:" + type);
        }
        // should check and confirm that the specified type is valid.
        // If not valid, throw the OCTransactionSearchException.
        // Hint: use "typeIsValid" of OCTransaction.

        // search through the transactions now
        for (OCTransaction transaction : transactionList) {
            if (transaction.getType().equalsIgnoreCase(type) && transaction.match(criteria)) {
                // the transaction matches the criteria.
                // put the transaction into the searchResult array.
                OCTransaction[] temp = new OCTransaction[searchResult.length + 1];
                for (int i = 0; i < searchResult.length; i++) {
                    temp[i] = searchResult[i];
                }
                temp[temp.length - 1] = transaction;
                searchResult = temp;
            }
        }

        return searchResult;
    }

    // ============================================================
    // searchIdx
    private int searchIdx(String type, Date date, String transactionID) {

        for (int i = 0; i < transactionList.size(); i++) {
            OCTransaction obj = transactionList.get(i);

            if (obj.getType().equals(type) && obj.getDate().equals(date)
                    && obj.getTransactionID().equals(transactionID)) {
                return i;  //if match the criteria, return index
            }
        }

        return -1; // no record found, return -1
    }

    // ============================================================
    // OCTransactionDBException
    public static class OCTransactionDBException extends Exception {
        public OCTransactionDBException(String ocTransactionDBExMsg) {
            super(ocTransactionDBExMsg);
        }
    }
}
