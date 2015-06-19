/*
 *     Copyright (C) 2015  higherfrequencytrading.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.openhft.lang.collection;

import net.openhft.lang.model.constraints.MaxSize;
import net.openhft.lang.model.constraints.Range;
import org.junit.Test;

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

    @Test
    public void test() {
        int length = 1000;
        final HugeArray<Price> prices =
                HugeCollections.newArray(Price.class, length);
        for (int i = 0; i < length; i++) {
            final Price price = prices.get(i);
            price.setInstrument("ID" + i);
            price.getAsk().setPrice(100.1);
            price.getAsk().setAmount(1000);
            price.getBid().setPrice(99.8123456789);
            price.getBid().setAmount(2000);
            prices.recycle(price);
        }
        for (int i = 0; i < length; i++) {
            final Price price = prices.get(i);
            assertEquals("ID" + i, price.getInstrument());
            assertEquals(100.1, price.getAsk().getPrice(), 0.0);
            assertEquals(1000, price.getAsk().getAmount());
            assertEquals(99.8123456789, price.getBid().getPrice(), 0.0);
            assertEquals(2000, price.getBid().getAmount());
            prices.recycle(price);
        }
    }
}

