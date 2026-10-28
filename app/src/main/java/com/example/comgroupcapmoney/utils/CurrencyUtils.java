package com.example.comgroupcapmoney.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyUtils {
    public static final long MAX_ALLOWED_AMOUNT = 999_999_999_999L;

    private static DecimalFormat getFormatter() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", symbols);
    }

    public static String format(double amount) {
        long rounded = Math.round(amount);
        return getFormatter().format(rounded) + " đ";
    }

    public static String formatSigned(double amount, boolean isExpense) {
        long rounded = Math.round(amount);
        String prefix = isExpense ? "-" : "+";
        return prefix + getFormatter().format(rounded) + " đ";
    }

    public static String formatShort(double amount) {
        long rounded = Math.round(amount);
        if (rounded < 1000) {
            return String.valueOf(rounded);
        } else if (rounded < 1_000_000) {
            long k = rounded / 1000;
            return k + "k";
        } else {
            double tr = rounded / 1_000_000.0;
            double trRounded = Math.round(tr * 10.0) / 10.0;
            if (trRounded == Math.floor(trRounded)) {
                return ((long) trRounded) + "tr";
            } else {
                return String.format(Locale.US, "%.1f", trRounded).replace('.', ',') + "tr";
            }
        }
    }

    public static long parseDigits(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0L;
        }
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return 0L;
        }
        try {
            if (digits.length() > 12) {
                return MAX_ALLOWED_AMOUNT;
            }
            long val = Long.parseLong(digits);
            return Math.min(val, MAX_ALLOWED_AMOUNT);
        } catch (NumberFormatException e) {
            return MAX_ALLOWED_AMOUNT;
        }
    }

    public static String formatInput(long value) {
        return getFormatter().format(value);
    }
}
