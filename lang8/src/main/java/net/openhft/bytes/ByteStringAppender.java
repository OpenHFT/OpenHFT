package net.openhft.bytes;

public interface ByteStringAppender<B extends ByteStringAppender<B>> {
    B append(char ch);

    default B append(CharSequence cs) {
        BytesUtil.appendUTF((StreamingDataOutput) this, cs);
        return (B) this;
    }

    B append(long value);

    B append(float f);

    B append(double d);
}
