package com.example.comgroupcapmoney.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class DateUtilsTest {

    @Test
    public void testToDisplay() {
        assertEquals("22/10/2026 12:30", DateUtils.toDisplay("2026-10-22 12:30:00"));
    }

    @Test
    public void testMonthTitle() {
        assertEquals("Tháng 1/2027", DateUtils.monthTitle(1, 2027));
    }
}
