package com.example.comgroupcapmoney.ui.stats;

import com.example.comgroupcapmoney.data.local.TransactionEntity;
import com.example.comgroupcapmoney.model.CategoryStat;
import com.example.comgroupcapmoney.model.StatsSummary;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tinh thong ke tu giao dich da luu (khi mat mang), giong het GET /api/analytics/summary.
 * Khong dung class Android de unit test chay duoc tren may tinh.
 */
public final class StatsCalculator {

    private StatsCalculator() {}

    public static StatsSummary fromCache(List<TransactionEntity> items, int month, int year, String type) {
        String viewType = StatsSummary.TYPE_INCOME.equals(type) ? StatsSummary.TYPE_INCOME : StatsSummary.TYPE_EXPENSE;

        double totalIncome = 0;
        double totalExpense = 0;
        Map<Integer, Group> groups = new LinkedHashMap<>();

        if (items != null) {
            for (TransactionEntity e : items) {
                if (e == null) continue;
                if (StatsSummary.TYPE_INCOME.equals(e.type)) {
                    totalIncome += e.amount;
                } else if (StatsSummary.TYPE_EXPENSE.equals(e.type)) {
                    totalExpense += e.amount;
                }

                if (!viewType.equals(e.type)) continue;
                Group g = groups.get(e.categoryId);
                if (g == null) {
                    g = new Group(e.categoryId, e.categoryName, e.categoryIcon, e.categoryColor);
                    groups.put(e.categoryId, g);
                }
                g.amount += e.amount;
                g.count++;
            }
        }

        double total = StatsSummary.TYPE_INCOME.equals(viewType) ? totalIncome : totalExpense;

        List<Group> sorted = new ArrayList<>(groups.values());
        Collections.sort(sorted, (a, b) -> {
            int byAmount = Double.compare(b.amount, a.amount);
            return byAmount != 0 ? byAmount : Integer.compare(a.categoryId, b.categoryId);
        });

        List<CategoryStat> categories = new ArrayList<>();
        if (total > 0) {
            for (Group g : sorted) {
                double percent = Math.round(g.amount / total * 100 * 10) / 10.0;
                categories.add(new CategoryStat(g.categoryId, g.name, g.icon, g.color, g.amount, g.count, percent));
            }
        }

        return new StatsSummary(month, year, viewType, totalIncome, totalExpense, total, categories);
    }

    private static final class Group {
        final int categoryId;
        final String name;
        final String icon;
        final String color;
        double amount;
        int count;

        Group(int categoryId, String name, String icon, String color) {
            this.categoryId = categoryId;
            this.name = name;
            this.icon = icon;
            this.color = color;
        }
    }
}
