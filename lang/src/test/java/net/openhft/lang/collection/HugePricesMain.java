/*
 * Copyright 2013 Peter Lawrey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.lang.collection;

import net.openhft.lang.model.constraints.MaxSize;
import net.openhft.lang.model.constraints.Range;

import static org.junit.Assert.assertEquals;

interface Price {
    String getInstrument();

    void setInstrument(@MaxSize CharSequence instrument);

    TimedQuote getAsk();

    void setAsk(TimedQuote ask);

    TimedQuote getBid();

    void setBid(TimedQuote bid);
}

interface QuoteHeader {
    int getMajor();

    // @Start
    void setMajor(@Range(min = 0, max = (1 << 24) - 1) int major);

    void setType(char ch);

    char getType();
}

interface Quote extends QuoteHeader {
    double getPrice();

    void setPrice(double price);

    int getAmount();

    void setAmount(int amount);
}

interface TimedQuote extends Quote {
    long getTimestamp();

    void setTimestamp(long price);
}

public class HugePricesMain {
    public static void main(String[] args) {
        int length = 1000;
        final HugeArray<Price> prices =
                HugeCollections.newArray(Price.class, length);
        for (int i = 0; i < length; i++) {
            final Price price = prices.get(i);
            price.setInstrument("ID" + i);
            price.getAsk().setPrice(100.1);
            price.getAsk().setAmount(1000);
            price.getBid().setPrice(99.8);
            price.getBid().setAmount(2000);
            prices.recycle(price);
        }
        for (int i = 0; i < length; i++) {
            final Price price = prices.get(i);
            assertEquals("ID" + i, price.getInstrument());
            assertEquals(100.1, price.getAsk().getPrice(), 0.0);
            assertEquals(1000, price.getAsk().getAmount());
            assertEquals(99.8, price.getBid().getPrice(), 0.0);
            assertEquals(2000, price.getBid().getAmount());
            prices.recycle(price);
        }
    }
}

