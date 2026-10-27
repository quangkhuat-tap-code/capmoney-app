package com.example.comgroupcapmoney.ui.stats;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.comgroupcapmoney.data.local.TransactionEntity;
import com.example.comgroupcapmoney.model.CategoryStat;
import com.example.comgroupcapmoney.model.StatsSummary;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class StatsCalculatorTest {

    private static final double DELTA = 0.0001;

    private static int nextId = 1;

    private static TransactionEntity tx(int categoryId, String name, String type, double amount) {
        TransactionEntity e = new TransactionEntity();
        e.id = nextId++;
        e.userId = 1;
        e.month = 10;
        e.year = 2026;
        e.amount = amount;
        e.type = type;
        e.categoryId = categoryId;
        e.categoryName = name;
        e.categoryIcon = "";
        e.categoryColor = "#FF9F0A";
        e.transactionDate = "2026-10-05 12:00:00";
        return e;
    }

    /** Du lieu mau giong "Chuan bi du lieu test" cua BE-1. */
    private static List<TransactionEntity> sampleData() {
        List<TransactionEntity> items = new ArrayList<>();
        items.add(tx(1, "Ăn uống", "expense", 300000));
        items.add(tx(1, "Ăn uống", "expense", 250000));
        items.add(tx(1, "Ăn uống", "expense", 300000));
        items.add(tx(2, "Di chuyển", "expense", 500000));
        items.add(tx(9, "Lương", "income", 8000000));
        return items;
    }

    @Test
    public void u1_expense_groupsByCategoryWithPercent() {
        StatsSummary s = StatsCalculator.fromCache(sampleData(), 10, 2026, "expense");

        assertEquals(10, s.getMonth());
        assertEquals(2026, s.getYear());
        assertEquals("expense", s.getType());
        assertEquals(1350000, s.getTotal(), DELTA);
        assertEquals(1350000, s.getTotalExpense(), DELTA);
        assertEquals(8000000, s.getTotalIncome(), DELTA);

        List<CategoryStat> cats = s.getCategories();
        assertEquals(2, cats.size());

        CategoryStat food = cats.get(0);
        assertEquals(1, food.getCategoryId());
        assertEquals(850000, food.getAmount(), DELTA);
        assertEquals(3, food.getCount());
        assertEquals(63.0, food.getPercent(), DELTA);

        CategoryStat move = cats.get(1);
        assertEquals(2, move.getCategoryId());
        assertEquals(500000, move.getAmount(), DELTA);
        assertEquals(1, move.getCount());
        assertEquals(37.0, move.getPercent(), DELTA);
    }

    @Test
    public void u2_income_onlySalary() {
        StatsSummary s = StatsCalculator.fromCache(sampleData(), 10, 2026, "income");

        assertEquals("income", s.getType());
        assertEquals(8000000, s.getTotal(), DELTA);
        assertEquals(1350000, s.getTotalExpense(), DELTA);
        assertEquals(1, s.getCategories().size());

        CategoryStat salary = s.getCategories().get(0);
        assertEquals(9, salary.getCategoryId());
        assertEquals(8000000, salary.getAmount(), DELTA);
        assertEquals(100.0, salary.getPercent(), DELTA);
    }

    @Test
    public void u3_empty_allZero() {
        StatsSummary s = StatsCalculator.fromCache(new ArrayList<>(), 10, 2026, "expense");

        assertEquals(0, s.getTotal(), DELTA);
        assertEquals(0, s.getTotalIncome(), DELTA);
        assertEquals(0, s.getTotalExpense(), DELTA);
        assertTrue(s.getCategories().isEmpty());
    }

    @Test
    public void u4_sameAmount_smallerCategoryIdFirst() {
        List<TransactionEntity> items = new ArrayList<>();
        items.add(tx(5, "Hóa đơn", "expense", 200000));
        items.add(tx(3, "Mua sắm", "expense", 200000));

        StatsSummary s = StatsCalculator.fromCache(items, 10, 2026, "expense");

        assertEquals(2, s.getCategories().size());
        assertEquals(3, s.getCategories().get(0).getCategoryId());
        assertEquals(5, s.getCategories().get(1).getCategoryId());
        assertEquals(50.0, s.getCategories().get(0).getPercent(), DELTA);
    }
}
