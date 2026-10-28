package com.example.comgroupcapmoney.ui.capture;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Doan so tien tong tu cac dong chu doc duoc tren hoa don (4A-3). Thuan Java de unit test.
public final class ReceiptAmountParser {

    public static final long NOT_FOUND = -1;
    private static final long MAX_AMOUNT = 999_999_999_999L;

    // So co dau phan cach nghin (125.000, 1,250,000) hoac so lien 4-9 chu so (125000)
    private static final Pattern AMOUNT = Pattern.compile("(?<![\\d.,])(\\d{1,3}(?:[.,]\\d{3})+|\\d{4,9})(?![\\d])");
    private static final Pattern DATE = Pattern.compile("\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}");
    private static final Pattern TIME = Pattern.compile("\\d{1,2}:\\d{2}");
    private static final Pattern LONG_DIGITS = Pattern.compile("\\d{10,}");
    private static final Pattern MARKS = Pattern.compile("\\p{M}+");

    private static final String[] KEYWORDS = {
            "TONG", "TOTAL", "THANH TOAN", "CONG", "PHAI TRA", "KHACH TRA"
    };

    private ReceiptAmountParser() {}

    public static long parse(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return NOT_FOUND;
        }
        List<String> norm = new ArrayList<>();
        for (String line : lines) {
            norm.add(normalize(line));
        }

        // Uu tien dong co tu khoa "Tong / Thanh toan"; nhieu dong thi lay dong cuoi (tong sau giam gia)
        for (int i = norm.size() - 1; i >= 0; i--) {
            if (!hasKeyword(norm.get(i))) continue;
            long onLine = maxAmount(norm.get(i));
            if (onLine == NOT_FOUND && i + 1 < norm.size()) {
                onLine = maxAmount(norm.get(i + 1));
            }
            if (onLine != NOT_FOUND) {
                return onLine;
            }
        }

        // Khong co tu khoa: lay so lon nhat ca hoa don
        long best = NOT_FOUND;
        for (String line : norm) {
            best = Math.max(best, maxAmount(line));
        }
        return best;
    }

    // Bo dau tieng Viet, doi d/D, viet hoa
    static String normalize(String s) {
        if (s == null) return "";
        String noMarks = MARKS.matcher(Normalizer.normalize(s, Normalizer.Form.NFD)).replaceAll("");
        return noMarks.replace('đ', 'd').replace('Đ', 'D').toUpperCase(Locale.ROOT);
    }

    private static boolean hasKeyword(String line) {
        for (String k : KEYWORDS) {
            if (line.contains(k)) return true;
        }
        return false;
    }

    // So tien lon nhat tren 1 dong, bo ngay/gio, so dien thoai/ma so thue, so < 1.000
    private static long maxAmount(String line) {
        String cleaned = DATE.matcher(line).replaceAll(" ");
        cleaned = TIME.matcher(cleaned).replaceAll(" ");
        cleaned = LONG_DIGITS.matcher(cleaned).replaceAll(" ");

        long best = NOT_FOUND;
        Matcher m = AMOUNT.matcher(cleaned);
        while (m.find()) {
            String digits = m.group(1).replace(".", "").replace(",", "");
            long v;
            try {
                v = Long.parseLong(digits);
            } catch (NumberFormatException e) {
                continue;
            }
            if (v >= 1000 && v <= MAX_AMOUNT && v > best) {
                best = v;
            }
        }
        return best;
    }
}
