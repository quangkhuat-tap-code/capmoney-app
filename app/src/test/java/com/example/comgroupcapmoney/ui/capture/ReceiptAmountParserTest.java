package com.example.comgroupcapmoney.ui.capture;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class ReceiptAmountParserTest {

    @Test
    public void r1_dongTongCong() {
        assertEquals(90000, ReceiptAmountParser.parse(Arrays.asList("Bún bò Huế", "SL 2 x 45.000", "Tổng cộng: 90.000")));
    }

    @Test
    public void r2_soNamODongSauTuKhoa() {
        assertEquals(125000, ReceiptAmountParser.parse(Arrays.asList("TOTAL", "125,000")));
    }

    @Test
    public void r3_layDongTuKhoaCuoi() {
        assertEquals(180000, ReceiptAmountParser.parse(Arrays.asList(
                "Tổng tiền hàng 200.000", "Giảm giá 20.000", "Thanh toán 180.000")));
    }

    @Test
    public void r4_boNgayGio() {
        assertEquals(35000, ReceiptAmountParser.parse(Arrays.asList("Ngày 20/10/2026 12:30", "Cà phê 35000")));
    }

    @Test
    public void r5_boSoDienThoai() {
        assertEquals(45000, ReceiptAmountParser.parse(Arrays.asList("Hotline 0901234567", "Trà sữa 45.000")));
    }

    @Test
    public void r6_khongCoSo() {
        assertEquals(ReceiptAmountParser.NOT_FOUND, ReceiptAmountParser.parse(Collections.singletonList("Cảm ơn quý khách")));
    }

    @Test
    public void r7_khongDau() {
        assertEquals(1250000, ReceiptAmountParser.parse(Collections.singletonList("TONG CONG 1.250.000 VND")));
    }
}
