package net.openhft.bytes;

public interface ByteStringParser {
    void parseUTF(CharSequence sb, StopCharTester stopCharTester);

    long parseLong();


    double parseDouble();
}
