package net.openhft.lang.io.serialization.impl;

import net.openhft.lang.io.Bytes;
import net.openhft.lang.io.serialization.BytesMarshaller;
import net.openhft.lang.io.serialization.CompactBytesMarshaller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListMarshaller<E> extends CollectionMarshaller<E, List<E>> implements CompactBytesMarshaller<List<E>> {
    ListMarshaller(BytesMarshaller<E> eBytesMarshaller) {
        super(eBytesMarshaller);
    }

    public static <E> BytesMarshaller<List<E>> of(BytesMarshaller<E> eBytesMarshaller) {
        return new ListMarshaller<>(eBytesMarshaller);
    }

    @Override
    public byte code() {
        return 'L' & 31;
    }

    @Override
    List<E> newCollection() {
        return new ArrayList<>();
    }

    @Override
    List<E> readCollection(Bytes bytes, List<E> es, int length) {
        List<E> ret = newCollection();
        if (es == null || (es.size() != length)) {
            if (es == null)
                es = Collections.emptyList();
            List<E> es2 = newCollection();
            for (int i = 0; i < length; i++)
                if (i < es.size()) {
                    ret.add(eBytesMarshaller.read(bytes, es.get(i)));
                } else {
                    ret.add(eBytesMarshaller.read(bytes));
                }
            return es2;
        }
        for (int i = 0; i < length; i++)
            eBytesMarshaller.read(bytes, es.get(i));
        return ret;
    }
}
