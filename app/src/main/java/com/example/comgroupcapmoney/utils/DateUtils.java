package com.example.comgroupcapmoney.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    public static String toServer(Calendar c) {
        if (c == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(c.getTime());
    }

    public static String toDisplay(String serverDate) {
        if (serverDate == null || serverDate.trim().isEmpty()) return "";
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            parser.setTimeZone(TimeZone.getDefault());
            Date d = parser.parse(serverDate);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
            formatter.setTimeZone(TimeZone.getDefault());
            return formatter.format(d);
        } catch (ParseException e) {
            return serverDate;
        }
    }

    public static String toDisplayDay(String serverDate) {
        if (serverDate == null || serverDate.trim().isEmpty()) return "";
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            parser.setTimeZone(TimeZone.getDefault());
            Date d = parser.parse(serverDate);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            formatter.setTimeZone(TimeZone.getDefault());
            return formatter.format(d);
        } catch (ParseException e) {
            if (serverDate.length() >= 10) {
                String ymd = serverDate.substring(0, 10);
                String[] parts = ymd.split("-");
                if (parts.length == 3) {
                    return parts[2] + "/" + parts[1] + "/" + parts[0];
                }
            }
            return serverDate;
        }
    }

    public static String monthTitle(int month, int year) {
        return "Tháng " + month + "/" + year;
    }
}
