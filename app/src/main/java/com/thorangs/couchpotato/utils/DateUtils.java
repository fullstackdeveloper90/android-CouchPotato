package com.thorangs.couchpotato.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

/**
 * Created by balman on 11/18/17.
 */

public class DateUtils {

    public static String getEnglishDateString(Calendar cal) {
        return getStringFromTwoDigitNumber(cal.get(Calendar.YEAR)) + "-" + getStringFromTwoDigitNumber(cal.get(Calendar.MONTH) + 1) + "-" + getStringFromTwoDigitNumber(cal.get(Calendar.DAY_OF_MONTH));
    }

    public static String getStringFromTwoDigitNumber(int number) {
        return number > 9 ? "" + number : "0" + number;
    }

    public static int getDayOfWeek(Long timestamp){

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        return c.get(Calendar.DAY_OF_WEEK);
    }


    public static int getDayOfMonth(Long timestamp){

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        return c.get(Calendar.DAY_OF_MONTH);
    }


    public static int getMonthOfYear(Long timestamp){

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        return c.get(Calendar.MONTH) + 1;
    }


    public static long getThisSundayTimeMills(Calendar c){
        c.add( Calendar.DAY_OF_WEEK, -(c.get(Calendar.DAY_OF_WEEK)-1));
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        return c.getTimeInMillis();
    }

    public static long getStartOfTheMonthMillis(){
        Calendar c=Calendar.getInstance();
        c.set(Calendar.DATE, c.getActualMinimum(Calendar.DAY_OF_MONTH));
        c.set(Calendar.HOUR_OF_DAY,0);
        c.set(Calendar.MINUTE,0);
        c.set(Calendar.SECOND,0);
        c.set(Calendar.MILLISECOND,0);
        return c.getTimeInMillis();
    }


    @NotNull
    public static Calendar getCalendarFromString (@NotNull String dateString/*YYYY-mm-DD*/) {
        if (dateString.length() == 10) {
            int year = Integer.parseInt(dateString.substring(0, 4));
            int month = (Integer.parseInt(dateString.substring(5, 7))-1);
            int day = Integer.parseInt(dateString.substring(8, dateString.length()));
            Calendar c = UtilKt.flooredCalendar(UtilKt.getToday());
            c.set(Calendar.YEAR,year);
            c.set(Calendar.MONTH,month);
            c.set(Calendar.DAY_OF_MONTH,day);
            return c;
        } else {
            return Calendar.getInstance();
        }
    }
}
