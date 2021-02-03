/*
 * Copyright (C) 2020  CHENG Yifeng

   The source code is for reference only. Any other usage except reference, include but not
   limited to distribute, copy will be considered as violation of academic integrity.

   You should make reasonable use of the source code of this program and abide
   by the rules of academic integrity. You should be held responsible for the punishment of plagiarism,
   dissemination of this code and violation of academic integrity rules.
   The author of the code does not assume any responsibility in connection with this punishment.
   
 * All the functions in the demo program as been implemented including exception handling  in this work
 * , so it is fully operational.
 *
 * There are some details to notify.
 * 1. when adding mtr check out, if there is not check in record found: an exception will be thrown
 *  then report error message "add: failed to add record -- mtrCheckOut: No outstanding MTR CheckIn transaction found!"
 * However, actually the record is added to the db, same as the demo program
 *
 * 2. exception handling. OCTransactionFormatException is the super class of
 * MTR/BusFare/Retail/TopUpTransactionFormatException
 *
 * 3. A method chkDayLegal is added to the DateTimeUtil, to check whether the day
 * is calendar legal,  including leap year's February like 2019-02-29. Used by OCTransaction.match()
 *
 * 4. A new Util class called StringUtil is added to comp2026.OctopusCard.Util, used to handle
 *    String relevant affairs
 *
 * 6. The most detailed documentation of subclasses mainly in MTR,
 *  as the implementation of methods in subclasses are similar to each other
 * */


package comp2026.OctopusCard;

import comp2026.OctopusCard.Util.Tokenizer;
import java.util.Scanner;

public class OctopusCard {
    private final OCTransactionDB ocTransactionDB;


    //============================================================
    // main
    public static void main(String[] args) {
        // chk number of command line arguments
        if (args.length != 1) {
            System.out.println("Invalid number of arguments.");
            return;
        }
        new OctopusCard().start(args[0]);
    }


    //============================================================
    // Constructor
    public OctopusCard() {
        ocTransactionDB = new OCTransactionDB();
    }


    //============================================================
    // start
    private void start(String ocTransactionDBFName) {
        // greeting
        System.out.println("+-------------------------------------+");
        System.out.println("|                                     |");
        System.out.println("|   Octopus Card Transaction System   |");
        System.out.println("|                                     |");
        System.out.println("+-------------------------------------+");
        System.out.println();
        System.out.println();

        // load our database
        loadCmd(ocTransactionDBFName);
        System.out.println();

        // prepare for command line
        Scanner in = new Scanner(System.in);
        boolean quit = false;

        // main loop
        while (!quit) {
            System.out.print("ready> ");
            String cmdLine = in.nextLine();
            String[] tokens = Tokenizer.getTokens(cmdLine);

            // chk number of tokens
            if (tokens.length < 1) {
                continue;
            }

            // chk our cmd
            String cmd = tokens[0];
            switch (cmd.toLowerCase()) {
                case "load":
                    loadCmd(tokens);
                    break;
                case "save":
                    saveCmd(tokens);
                    break;
                case "list":
                    listCmd(tokens);
                    break;
                case "add":
                    addCmd(tokens);
                    break;
                case "search":
                    searchCmd(tokens);
                    break;
                case "help":
                    helpCmd(tokens);
                    break;
                case "quit":
                    quit = quitCmd(tokens);
                    break;
                default:
                    System.out.println("Unknown command: " + cmd);
                    break;
            }
            System.out.println();
        }
        System.out.println("Goodbye!");
    }


    //============================================================
    // loadCmd
    private void loadCmd(String[] tokens) {
        // chk the number of arguments
        if (tokens.length != 2) {
            System.out.println("load: invalid number of arguments.");
            return;
        }
        loadCmd(tokens[1]);
    }

    private void loadCmd(String ocTransactionDBFName) {

        try {
            ocTransactionDB.loadDB(ocTransactionDBFName);
        } catch (OCTransactionDB.OCTransactionDBException e) {
            System.out.println("load: failed to load database (" + ocTransactionDBFName + ") -- " + e.getMessage());
        }

    }


    //============================================================
    // saveCmd
    private void saveCmd(String[] tokens) {
        // chk the number of arguments
        if (tokens.length != 2) {
            System.out.println("save: invalid number of arguments.");
            return;
        }


        // save the database
        try {
            ocTransactionDB.saveDB(tokens[1]);
        } catch (OCTransactionDB.OCTransactionDBException e) {
            System.out.println("save: failed to save database (" + tokens[0] + ") -- " + e.getMessage());
        }

    }


    //============================================================
    // listCmd
    private void listCmd(String[] tokens) {
        switch (tokens.length) {
            case 1:
                ocTransactionDB.list();
                break;

            case 2:
                switch (tokens[1].toLowerCase()) {
                    case "topup":
                    case "busfare":
                    case "retail":
                    case "mtr":
                        ocTransactionDB.list(tokens[1]);
                        break;
                    default:
                        System.out.println("list: invalid transaction type: " + tokens[1]);
                }
                break;

            default:
                System.out.println("list: invalid number of arguments.");
        }
    }


    //============================================================
    // addCmd
    private void addCmd(String[] tokens) {
        boolean exceptionHappen = false;
        String record = "";
        for (int i = 1; i < tokens.length; i++) {
            record += tokens[i];
            if (i + 1 < tokens.length) {
                record += " ";
            }
        }

        if (tokens.length == 1) {
            exceptionHappen = true;
            System.out.println("add: No record argument received.");
        }

        try {
            ocTransactionDB.addTransaction(record);
        } catch (OCTransactionDB.OCTransactionDBException | OCTransaction.OCTransactionFormatException e) {
            System.out.println("add: failed to add record -- " + e.getMessage());
            exceptionHappen = true;
        }
        if (!exceptionHappen) {
            System.out.println("add: record added.");
        }
    }


    //============================================================
    // searchCmd
    private void searchCmd(String[] tokens) {
        // chk the number of arguments
        if (tokens.length < 2) {
            System.out.println("search: invalid number of arguments.");
            return;
        }

        // prepare for the search
        String type = tokens[1].toLowerCase();
        String[] criteria = new String[tokens.length - 2];
        for (int i = 2; i < tokens.length; i++) {
            criteria[i - 2] = tokens[i];
        }


        //search the database
        OCTransaction[] searchResult = new OCTransaction[0];
        try {
            searchResult = ocTransactionDB.search(type, criteria);
        } catch (OCTransaction.OCTransactionSearchException e) {
            System.out.println("search: Error: " + e.getMessage());
            return;
        }

        // print the result
        System.out.println("Search result:");
        for (OCTransaction ocTransaction : searchResult) {
            System.out.println(ocTransaction);
        }
        System.out.println(searchResult.length + " record(s) found.");

    }


    //============================================================
    // helpCmd
    private void helpCmd(String[] tokens) {
        // chk the number arguments
        if (tokens.length > 2) {
            System.out.println("help: invalid number of arguments.");
            return;
        }

        // chk the number arguments
        if (tokens.length == 1) {
            System.out.println("Available commands:");
            System.out.println("  load");
            System.out.println("  save");
            System.out.println("  list");
            System.out.println("  add");
            System.out.println("  search");
            System.out.println("  help");
            System.out.println("  quit");
            return;
        }

        switch (tokens[1].toLowerCase()) {
            case "load":
                System.out.println("\"load\" -- loads transactions from file to the system.");
                System.out.println("Usage of \"load\":");
                System.out.println("  load fName");
                break;

            case "save":
                System.out.println("\"save\" -- saves transactions from the system to file.");
                System.out.println("Usage of \"save\":");
                System.out.println("  save fName");
                break;

            case "list":
                System.out.println("\"list\" -- lists transactions in the system.");
                System.out.println("Usage of \"list\":");
                System.out.println("  list         (list all transactions)");
                System.out.println("  list TopUp   (list all Top Up transactions)");
                System.out.println("  list BusFare (list all Bus Fare transactions)");
                System.out.println("  list Retail  (list all Retail transactions)");
                System.out.println("  list MTR     (list all MTR transactions)");
                break;

            case "add":
                System.out.println("\"add\" -- adds a new record to the system.");
                System.out.println("Usage of \"add\":");
                System.out.println("  add TopUp   dateTime transactionID amount topUpType agent...");
                System.out.println("  add BusFare dateTime transactionID amount route station to terminal...");
                System.out.println("  add MTR     dateTime transactionID amount mtrType station");
                System.out.println("  add Retail  dateTime transactionID amount retailer, description");
                System.out.println("");
                System.out.println("Format of dateTime: \"dd-MM-yyyy@HH:mm:ss\".");
                break;

            case "search":
                System.out.println("\"search\" -- searches for transactions in the system with the specified criteria.");
                System.out.println("Usage of \"search\":");
                System.out.println("  search TopUp cash");
                System.out.println("  search TopUp bank");
                System.out.println("  search TopUp bank bankName");
                System.out.println("  search TopUp date yyyy-mm-dd");
                System.out.println("  search BusFare route route");
                System.out.println("  search BusFare station station");
                System.out.println("  search BusFare terminal terminal");
                System.out.println("  search BusFare date yyyy-mm-dd");
                System.out.println("  search Retail retailer retailer");
                System.out.println("  search Retail description description");
                System.out.println("  search Retail date yyyy-mm-dd");
                System.out.println("  search MTR station station");
                System.out.println("  search MTR mtrType [ checkin | checkout ]");
                System.out.println("  search MTR status [ completed | outstanding ]");
                System.out.println("  search MTR date yyyy-mm-dd");
                break;

            case "help":
                System.out.println("\"help\" -- shows this help message.");
                System.out.println("Usage of \"help\":");
                System.out.println("  help");
                System.out.println("  help command");
                break;

            case "quit":
                System.out.println("\"quit\" -- quit from the Octopus Card Transaction System.");
                System.out.println("Usage of \"quit\":");
                System.out.println("  quit");
                break;

            default:
                System.out.println("Unknown command (" + tokens[1] + ")");
                break;
        }
    }


    //============================================================
    // quitCmd
    private boolean quitCmd(String[] tokens) {
        // chk number of arguments
        if (tokens.length != 1) {
            System.out.println("quit: too many arguments.");
            return false;
        }
        return true;
    }
}
