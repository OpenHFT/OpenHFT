/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.thirdparty.smoke;

import org.junit.jupiter.api.Test;
import quickfix.Message;
import quickfix.field.*;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Smoke test for QuickFIX/J bundle.
 */
class QuickFixSmokeTest {

    @Test
    void canBuildNewOrderSingleMessage() throws Exception {
        quickfix.fix44.NewOrderSingle order =
                new quickfix.fix44.NewOrderSingle(
                        new ClOrdID("123"),
                        new Side(Side.BUY),
                        new TransactTime(new Date()),
                        new OrdType(OrdType.MARKET)
                );
        order.set(new HandlInst(
                HandlInst.AUTOMATED_EXECUTION_ORDER_PRIVATE));
        order.set(new Symbol("AAPL"));

        Message msg = order;

        assertEquals("123", msg.getString(ClOrdID.FIELD), "QuickFIX message should carry ClOrdID");
        assertEquals("AAPL", msg.getString(Symbol.FIELD), "QuickFIX message should carry Symbol");
    }
}
