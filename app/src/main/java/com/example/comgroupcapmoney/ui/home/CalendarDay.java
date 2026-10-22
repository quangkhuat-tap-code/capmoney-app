package com.example.comgroupcapmoney.ui.home;

import com.example.comgroupcapmoney.model.Transaction;
import java.util.ArrayList;
import java.util.List;

public class CalendarDay {
    private final int day;
    private final int month;
    private final int year;
    private final boolean inMonth;
    private final boolean isToday;
    private final List<Transaction> items = new ArrayList<>();
    private String thumbUrl;
    private double totalExpense = 0;
    private double totalIncome = 0;
    private final String dayKey;

    public CalendarDay(int day, int month, int year, boolean inMonth, boolean isToday, String dayKey) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.inMonth = inMonth;
        this.isToday = isToday;
        this.dayKey = dayKey;
    }

    public int getDay() { return day; }
    public int getMonth() { return month; }
    public int getYear() { return year; }
    public boolean isInMonth() { return inMonth; }
    public boolean isToday() { return isToday; }
    public List<Transaction> getItems() { return items; }
    public String getThumbUrl() { return thumbUrl; }
    public double getTotalExpense() { return totalExpense; }
    public double getTotalIncome() { return totalIncome; }
    public String getDayKey() { return dayKey; }

    public void addTransactions(List<Transaction> txList) {
        if (txList == null) return;
        items.addAll(txList);
        for (Transaction tx : txList) {
            if (tx.isExpense()) {
                totalExpense += tx.getAmount();
            } else {
                totalIncome += tx.getAmount();
            }
            if (thumbUrl == null && tx.getImageUrl() != null && !tx.getImageUrl().trim().isEmpty()) {
                thumbUrl = tx.getFullImageUrl();
            }
        }
    }
}
