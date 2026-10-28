package com.example.comgroupcapmoney.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class CurrencyUtilsTest {

    @Test
    public void testFormat() {
        assertEquals("50.000 đ", CurrencyUtils.format(50000));
        assertEquals("0 đ", CurrencyUtils.format(0));
        assertEquals("1.234.568 đ", CurrencyUtils.format(1234567.6));
    }

    @Test
    public void testFormatSigned() {
        assertEquals("-50.000 đ", CurrencyUtils.formatSigned(50000, true));
        assertEquals("+50.000 đ", CurrencyUtils.formatSigned(50000, false));
    }

    @Test
    public void testFormatShort() {
        assertEquals("500", CurrencyUtils.formatShort(500));
        assertEquals("50k", CurrencyUtils.formatShort(50000));
        assertEquals("1,2tr", CurrencyUtils.formatShort(1200000));
        assertEquals("2tr", CurrencyUtils.formatShort(2000000));
    }

    @Test
    public void testParseDigits() {
        assertEquals(1500000L, CurrencyUtils.parseDigits("1.500.000"));
        assertEquals(0L, CurrencyUtils.parseDigits(""));
        assertEquals(0L, CurrencyUtils.parseDigits("abc"));
        assertEquals(999999999999L, CurrencyUtils.parseDigits("9999999999999999"));
    }

    @Test
    public void testFormatInput() {
        assertEquals("1.500.000", CurrencyUtils.formatInput(1500000L));
    }
}
