package com.example.comgroupcapmoney.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WalletTest {

    @Test
    public void isBankWallet_uuTienTypeTuServer() {
        assertTrue(Wallet.isBankWallet("bank", "Tiền mặt"));
        assertFalse(Wallet.isBankWallet("cash", "Ngân hàng ACB"));
        assertFalse(Wallet.isBankWallet("saving", "Tiết kiệm"));
    }

    @Test
    public void isBankWallet_chuaCoType_doanTheoTen() {
        assertTrue(Wallet.isBankWallet(null, "Ngân hàng"));
        assertTrue(Wallet.isBankWallet(null, "Vietcombank"));
        assertTrue(Wallet.isBankWallet(null, "Tiền CK"));
        assertFalse(Wallet.isBankWallet(null, "Tiền mặt"));
        assertFalse(Wallet.isBankWallet(null, "Ví Momo"));
        assertFalse(Wallet.isBankWallet(null, "Quỹ đi chơi"));
        assertFalse(Wallet.isBankWallet(null, null));
    }
}
