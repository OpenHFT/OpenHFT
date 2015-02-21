package net.openhft.chronicle.bytes;

public interface ByteStringParser<B extends ByteStringParser<B>> extends StreamingDataInput<B> {
    default String parseUTF(StopCharTester stopCharTester) {
        return BytesUtil.parseUTF(this, stopCharTester);
    }

    default void parseUTF(StringBuilder sb, StopCharTester stopCharTester) {
        BytesUtil.parseUTF(this, sb, stopCharTester);
    }

    default long parseLong() {
        return BytesUtil.parseLong(this);
    }

    default double parseDouble() {
        return BytesUtil.parseDouble(this);
    }

    default boolean skipTo(StopCharTester tester) {
        return BytesUtil.skipTo(this, tester);
    }
}
