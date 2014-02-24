package net.openhft.lang.sandbox.collection;

import net.openhft.lang.io.ByteBufferBytes;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Rob Austin
 */
@RunWith(value = Parameterized.class)
public class ATSDirectBitSetTest extends AbstractDirectBitSetTest {


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        int capacityInBytes = 256 / 8;
        return Arrays.asList(new Object[][]{{
                new ATSDirectBitSet(new ByteBufferBytes(
                        ByteBuffer.allocate(capacityInBytes)))
        }});
    }

    public ATSDirectBitSetTest(DirectBitSet bs) {
        super(bs);
    }
}




