package org.harvey.compiler.io;

import org.harvey.compiler.exception.CompilerException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * 主要用于在文件的最前面写入东西而不导致所有的内容移动
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-23 14:22
 */
public class DequeueOutputStream extends OutputStream {
    private final OutputStream os;
    private final LinkedList<byte[]> datas = new LinkedList<>();

    public DequeueOutputStream(OutputStream os) {
        super();
        this.os = os;
    }

    @Override
    public void write(int b) {
        throw new CompilerException(new UnsupportedOperationException());
    }


    @Override
    public void write(byte[] b) {
        datas.add(b);
    }

    public void writeFirst(byte[] b) {
        datas.addFirst(b);
    }

    public void writeLast(byte[] b) {
        datas.addLast(b);
    }

    @Override
    public void write(byte[] b, int off, int len) {
        throw new CompilerException(new UnsupportedOperationException());
    }

    @Override
    public void flush() throws IOException {
        int len = 0;
        for (byte[] data : datas) {
            len += data.length;
        }
        byte[] all = new byte[len];
        for (int i = 0, j = 0; i < datas.size(); i++) {
            byte[] each = datas.get(i);
            System.arraycopy(each, 0, all, j, each.length);
            j += each.length;
        }
        os.write(all);
        os.flush();
    }

    @Override
    public void close() throws IOException {
        os.close();
    }

    @Override
    public int hashCode() {
        return os.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return os.equals(obj);
    }

    @Override
    public String toString() {
        return os.toString();
    }

}
