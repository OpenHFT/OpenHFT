package net.openhft.lang.data;

import java.util.Collection;

/**
 * Created by peter on 12/01/15.
 */
public abstract class AbstractWire implements Wire {
    @Override
    public void writeSequenceStart() {
        writeSequenceStart(null);
    }

    @Override
    public void writeSequenceStart(WireKey key) {
        writeSequenceStart(null, key);
    }

    @Override
    public void readSequenceStart() {
        readSequenceStart(null);
    }

    @Override
    public void readSequenceStart(WireKey key) {
        readSequenceStart(null, key);
    }

    @Override
    public void writeSequenceLength(int length) {
        writeSequenceLength(null, length);
    }

    @Override
    public void writeSequenceLength(WireKey key, int length) {
        writeSequenceLength(null, key, length);
    }

    @Override
    public int readSequenceLength() {
        return readSequenceLength(null);
    }

    @Override
    public int readSequenceLength(WireKey key) {
        return readSequenceLength(null, key);
    }


    @Override
    public void writeSequence(WireKey key, Object... array) {
        writeSequence(null, key, array);
    }

    @Override
    public void writeSequence(Iterable array) {
        writeSequence(null, array);
    }

    @Override
    public void writeSequence(WireKey key, Iterable array) {
        writeSequence(null, key, array);
    }

    @Override
    public <T> int readSequence(Collection<T> collection, Class<T> aClass) {
        return readSequence(null, collection, aClass);
    }

    @Override
    public <T> int readSequence(WireKey key, Collection<T> collection, Class<T> aClass) {
        return readSequence(null, key, collection, aClass);
    }

    @Override
    public long startLength(int bytes) {
        return startLength(null, bytes);
    }

    @Override
    public long startLength(WireKey key, int bytes) {
        return startLength(null, key, bytes);
    }

    @Override
    public int readLength() {
        return readLength(null);
    }

    @Override
    public int readLength(WireKey key) {
        return readLength(null, key);
    }

    @Override
    public void writeUTF(CharSequence s) {
        writeText(null, s);
    }

    @Override
    public void writeText(WireKey key, CharSequence s) {
        writeUTF(null, key, s);
    }

    @Override
    public String readUTF() {
        return readUTF((WireKey) null);
    }

    @Override
    public String readUTF(WireKey key) {
        return readUTF(null, key);
    }

    @Override
    public CharSequence readUTF(StringBuilder s) {
        return readUTF(null, s);
    }

    @Override
    public CharSequence readUTF(WireKey key, StringBuilder s) {
        return readUTF(null, key, s);
    }

    @Override
    public void writeInt(int i) {
        writeInt(null, i);
    }

    @Override
    public void writeInt(WireKey key, int i) {
        writeInt(null, key, i);
    }

    @Override
    public int readInt() {
        return readInt(null);
    }

    @Override
    public int readInt(WireKey key) {
        return readInt(null, key);
    }

    @Override
    public void writeDouble(double v) {
        writeDouble(null, v);
    }

    @Override
    public void writeDouble(WireKey key, double v) {
        writeDouble(null, key, v);
    }

    @Override
    public double readDouble() {
        return readDouble(null);
    }

    @Override
    public double readDouble(WireKey key) {
        return readDouble(null, key);
    }

    @Override
    public void writeLong(long i) {
        writeLong(null, i);
    }

    @Override
    public void writeLong(WireKey key, long i) {
        writeLong(null, key, i);
    }

    @Override
    public long readLong() {
        return readLong(null);
    }

    @Override
    public long readLong(WireKey key) {
        return readLong(null, key);
    }

    @Override
    public void writeMappingStart() {
        writeMappingStart(null);
    }

    @Override
    public void writeMappingStart(WireKey key) {
        writeMappingStart(null, key);
    }

    @Override
    public void readMappingStart() {
        readMappingStart(null);
    }

    @Override
    public void readMappingStart(WireKey key) {
        readMappingStart(null, key);
    }
}
