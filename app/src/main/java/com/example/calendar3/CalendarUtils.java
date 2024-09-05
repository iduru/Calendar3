package com.example.calendar3;

import java.util.Calendar;

public class CalendarUtils {

    public static long getDateInMillis(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0); // 時間を 00:00:00 に設定
        calendar.set(Calendar.MILLISECOND, 0); // ミリ秒を 0 に設定
        return calendar.getTimeInMillis();
    }

    public static int getYear(long dateInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        return calendar.get(Calendar.YEAR);
    }

    public static int getMonth(long dateInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        return calendar.get(Calendar.MONTH);
    }

    public static int getDayOfMonth(long dateInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
}
