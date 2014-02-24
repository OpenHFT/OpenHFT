package net.openhft.lang.sandbox.collection;

import net.openhft.lang.io.ByteBufferBytes;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Rob Austin
 */
@RunWith(value = Parameterized.class)
public class SingleThreadedDirectBitSetTest extends AbstractDirectBitSetTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        int capacityInBytes = 256 / 8;
        return Arrays.asList(new Object[][]{{
                new SingleThreadedDirectBitSet(new ByteBufferBytes(
                        ByteBuffer.allocate(capacityInBytes)))
        }});
    }


    public SingleThreadedDirectBitSetTest(DirectBitSet bs) {
        super(bs);
    }

    @Test
    @Ignore
    public void testNextClearBit() {
        super.testNextClearBit();
    }


    @Test
    @Ignore
    public void testRangeOpsSpanLongCase() {
        super.testRangeOpsSpanLongCase();
    }


    @Test
    @Ignore
    public void testPreviousClearBit() {
        super.testPreviousClearBit();
    }


}
