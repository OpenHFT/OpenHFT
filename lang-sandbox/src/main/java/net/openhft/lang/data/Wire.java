package net.openhft.lang.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * The defines the stand interface for writing and reading sequentially to/from a Bytes stream.
 *
 * Created by peter on 12/01/15.
 */
public interface Wire {
    /*
     * Sequence handling code.
     */
    void writeSequenceStart();

    void writeSequenceStart(WireKey key);

    void writeSequenceStart(CharSequence name, WireKey key);

    void writeSequenceEnd();

    void readSequenceStart();

    void readSequenceStart(WireKey key);

    void readSequenceStart(CharSequence name, WireKey key);

    boolean hasNextSequenceItem();

    void readSequenceEnd();

    void writeSequenceLength(int length);

    void writeSequenceLength(WireKey key, int length);

    void writeSequenceLength(CharSequence name, WireKey key, int length);

    int readSequenceLength();

    int readSequenceLength(WireKey key);

    int readSequenceLength(StringBuilder name, WireKey key);

    void writeSequence(WireKey key, Object... array);

    void writeSequence(CharSequence name, WireKey key, Object... array);

    void writeSequence(Iterable array);

    void writeSequence(WireKey key, Iterable array);

    void writeSequence(CharSequence name, WireKey key, Iterable array);

    <T> int readSequence(Collection<T> collection, Class<T> aClass);

    <T> int readSequence(WireKey key, Collection<T> collection, Class<T> aClass);

    <T> int readSequence(StringBuilder name, WireKey key, Collection<T> collection, Class<T> aClass);

    /*
     * length type.
     */
    long startLength(int bytes);

    long startLength(WireKey key, int bytes);

    long startLength(CharSequence name, WireKey key, int bytes);

    void endLength(long startPosition);

    int readLength();

    int readLength(WireKey key);

    int readLength(CharSequence name, WireKey key);

    /*
     * Text / Strings.
     */
    void writeText(CharSequence s);

    void writeText(WireKey key, CharSequence s);

    void writeText(CharSequence name, WireKey key, CharSequence s);

    String readText();

    String readText(WireKey key);

    String readText(StringBuilder name, WireKey key);

    CharSequence readText(StringBuilder s);

    CharSequence readText(WireKey key, StringBuilder s);

    CharSequence readText(StringBuilder name, WireKey key, StringBuilder s);

    void writeInt(int i);

    void writeInt(WireKey key, int i);

    void writeInt(CharSequence name, WireKey key, int i);

    int readInt();

    int readInt(WireKey key);

    int readInt(StringBuilder name, WireKey key);

    void writeDouble(double v);

    void writeDouble(WireKey key, double v);

    void writeDouble(CharSequence name, WireKey key, double v);

    double readDouble();

    double readDouble(WireKey key);

    double readDouble(StringBuilder name, WireKey key);

    void writeLong(long i);

    void writeLong(WireKey key, long i);

    void writeLong(CharSequence name, WireKey key, long i);

    long readLong();

    long readLong(WireKey key);

    long readLong(StringBuilder name, WireKey key);

    /*
     * read and write comments.
     */
    void writeComment(CharSequence s);

    String readComment();

    void readComment(StringBuilder sb);

    boolean hasMapping();

    void writeMappingStart();

    void writeMappingStart(WireKey key);

    void writeMappingStart(CharSequence name, WireKey key);

    void writeMappingEnd();

    void readMappingStart();

    void readMappingStart(WireKey key);

    void readMappingStart(StringBuilder name, WireKey key);

    void readMappingEnd();

    void writeTime(LocalTime localTime);

    void writeTime(WireKey time, LocalTime localTime);

    void writeTime(CharSequence name, WireKey time, LocalTime localTime);

    LocalTime readTime();

    LocalTime readTime(WireKey time);

    LocalTime readTime(StringBuilder name, WireKey time);

    void writeDocumentStart();

    void writeDocumentEnd();

    boolean hasDocument();

    void readDocumentStart();

    void readDocumentEnd();

    void flip();

    void clear();

    void writeZonedDateTime(ZonedDateTime zonedDateTime);

    void writeZonedDateTime(WireKey key, ZonedDateTime zonedDateTime);

    void writeZonedDateTime(CharSequence name, WireKey key, ZonedDateTime zonedDateTime);

    ZonedDateTime readZonedDateTime();

    ZonedDateTime readZonedDateTime(WireKey key);

    ZonedDateTime readZonedDateTime(StringBuilder name, WireKey key);

    void writeDate(LocalDate zonedDateTime);

    void writeDate(WireKey key, LocalDate zonedDateTime);

    void writeDate(CharSequence name, WireKey key, LocalDate zonedDateTime);

    LocalDate readDate();

    LocalDate readDate(WireKey key);

    LocalDate readDate(StringBuilder name, WireKey key);

    void writeMarshallable(Marshallable type);

    void writeMarshallable(WireKey key, Marshallable type);

    void writeMarshallable(CharSequence name, WireKey key, Marshallable type);

    void readMarshallable(Marshallable type);

    void readMarshallable(WireKey key, Marshallable type);

    void readMarshallable(StringBuilder name, WireKey key, Marshallable type);

    Marshallable readMarshallable();

    Marshallable readMarshallable(WireKey key);

    Marshallable readMarshallable(StringBuilder name, WireKey key);
}
