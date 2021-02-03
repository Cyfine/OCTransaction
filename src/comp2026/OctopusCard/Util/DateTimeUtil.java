package comp2026.OctopusCard.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {
    private static final String DateFormat = "dd-MM-yyyy@HH:mm:ss";
    private static final String DisplayDateFormat = "MMM. dd, yyyy (EEE) 'at' HH:mm:ss";
    private static final String CmdLineInputDateFormat = "yyyy-MM-dd";

    // used by the save and add of the database
    public static String dateTime2Str(Date date) {
        return new SimpleDateFormat(DateFormat).format(date);
    }

    //usd by the save and add of the database
    public static Date str2DateTime(String dateTimeStr) throws ParseException {
        return new SimpleDateFormat(DateFormat).parse(dateTimeStr);
    }

    // used by the toString in each subclasses of the OCTransaction
    // format the Date obj to the String that if the list() output
    public static String dateTimeToDisplayString(Date date) {
        return new SimpleDateFormat(DisplayDateFormat).format(date);
    }

    // check if the input year month and day is legal calendar date
    // including leap year February 29 check, used by match is OCTransaction
    public static boolean chkDayLegal(int year, int month, int day) {
        if (month == 1 || month == 3 || month == 5 || month == 7 ||
                month == 8 || month == 10 || month == 12) {  // the month which has 31 days day <=31
            if (day <= 31) {
                return true;
            } else {
                return false;
            }
        } else if (month == 2) { // check if the leap year February
            if ((year - 2000) % 4 == 0 && day < 30) {
                return true;
            } else if (day < 29) {
                return true;
            } else {
                return false;
            }
        } else if (day < 31 && month < 13 && month > 0) {
            return true;
        } else {
            return false;
        }

    }
}
