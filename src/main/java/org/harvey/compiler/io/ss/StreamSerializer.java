package org.harvey.compiler.io.ss;


import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.exception.io.CompilerFileWriterException;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.execute.expression.LocalVariableDeclare;
import org.harvey.compiler.execute.expression.SourceVariableDeclare;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link LocalVariableDeclare.Serializer}
 * {@link ExpressionElement.Serializer}
 * {@link SourceVariableDeclare.LocalType.Serializer}
 *
 * @date 2025-01-19 16:59
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
public interface StreamSerializer<T> {


    Map<String, StreamSerializer<?>> REGISTER = new HashMap<>();

    static void register(StreamSerializer<?> serializer) {
        REGISTER.put(serializer.getClass().getName(), serializer);
    }

    static <T extends StreamSerializer<?>> T get(Class<T> target) {
        return (T) REGISTER.get(target.getName());
    }


    public static <T> ArrayList<T> readElements(InputStream is, long size, StreamSerializer<T> serializer) {
        ArrayList<T> list = new ArrayList<>();
        for (long l = 0; l < size; l++) {
            list.add(serializer.in(is));
        }
        return list;
    }

    static <T> int writeElements(OutputStream os, T[] arr,
                                 StreamSerializer<T> serializer) {
        return writeElements(os, Arrays.stream(arr), serializer);
    }

    static <T> int writeElements(OutputStream os, Collection<T> collection,
                                 StreamSerializer<T> serializer) {
        return writeElements(os, collection.stream(), serializer);
    }

    private static <T> int writeElements(OutputStream os, Stream<T> stream, StreamSerializer<T> serializer) {
        return (int) stream.map(ele -> serializer.out(os, ele))
                .collect(Collectors.summarizingInt(m -> m)).getSum();
    }

    static int writeElements(OutputStream os, byte[] data) {
        try {
            os.write(data);
        } catch (IOException e) {
            throw new CompilerFileWriterException(e);
        }
        return data.length;
    }
    static long readNumber(InputStream is, int bitCount) {
        try {
            byte[] data = is.readNBytes(Serializes.bitCountToByteCount(bitCount));
            HeadMap[] headMaps = new SerializableData(data[0]).phaseHeader(bitCount);
            return headMaps[0].getValue();
        } catch (IOException e) {
            throw new CompilerFileWriterException(e);
        }
    }

     static int writeNumber(OutputStream os, long value, int bitCount) {
        try {
            byte[] data = Serializes.makeHead(new HeadMap(value, bitCount)).data();
            int size = data.length;
            os.write(data);
            return size;
        } catch (IOException e) {
            throw new CompilerFileWriterException(e);
        }
    }

    static int writeOneByte(OutputStream os, byte ordinal) {
        try {
            os.write(new byte[]{ordinal});
        } catch (IOException e) {
            throw new CompilerFileWriterException(e);
        }
        return 1;
    }
    /**
     * @param is src
     * @return target
     */
    T in(InputStream is);

    /**
     * @param os target
     * @return byte array, write to os, size
     */
    int out(OutputStream os, T src);


}
