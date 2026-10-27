package com.example.comgroupcapmoney.ui.budget;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BudgetAlertNotifierTest {

    @Test
    public void n1_vuaCham80() {
        assertEquals(BudgetAlertNotifier.WARNING, BudgetAlertNotifier.decide(1_000_000, 850_000, 100_000));
    }

    @Test
    public void n2_nhayQua80LenVuot100() {
        assertEquals(BudgetAlertNotifier.OVER, BudgetAlertNotifier.decide(1_000_000, 1_050_000, 300_000));
    }

    @Test
    public void n3_daVuotTuTruoc() {
        assertEquals(BudgetAlertNotifier.NONE, BudgetAlertNotifier.decide(1_000_000, 1_200_000, 100_000));
    }

    @Test
    public void n4_daQua80TuTruoc() {
        assertEquals(BudgetAlertNotifier.NONE, BudgetAlertNotifier.decide(1_000_000, 900_000, 50_000));
    }

    @Test
    public void n5_dung100LaVuot() {
        assertEquals(BudgetAlertNotifier.OVER, BudgetAlertNotifier.decide(1_000_000, 1_000_000, 100_000));
    }

    @Test
    public void n6_conXaNguong() {
        assertEquals(BudgetAlertNotifier.NONE, BudgetAlertNotifier.decide(1_000_000, 500_000, 100_000));
    }
}
