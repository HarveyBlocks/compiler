package org.harvey.compiler.io;

import org.harvey.compiler.io.serializer.SerializableData;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-30 01:02
 */
public class CacheOutputStream extends OutputStream {
    private final OutputStream out;
    private final LinkedList<SerializableData> buffer = new LinkedList<>();

    public CacheOutputStream(OutputStream out) {
        super();
        this.out = out;
    }

    @Override
    public void write(int b) {
        buffer.add(SerializableData.create((byte) b));
    }

    @Override
    public void write(byte[] b) {
        buffer.add(new SerializableData(b));
    }

    @Override
    public void write(byte[] b, int off, int len) {
        buffer.add(new SerializableData(b, off, len));
    }

    @Override
    public void flush() throws IOException {
        out.write(SerializableData.concat(
                buffer.toArray(new SerializableData[]{})).data());
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public int hashCode() {
        return out.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CacheOutputStream)) {
            return false;
        }
        CacheOutputStream cos = (CacheOutputStream) obj;
        return out.equals(cos.out);
    }

    @Override
    public String toString() {
        return out.toString();
    }
}
