package org.harvey.compiler.io.serializer;

import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.common.util.ArrayUtil;
import org.harvey.compiler.exception.io.CompilerFileWriteException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 序列化工具类, 主要用于提高集合序列化的有关代码的复用性
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-27 16:16
 */
public class StreamSerializerUtil {
    public static <E> int collectionOut(OutputStream os, Collection<E> collection, StreamSerializer<E> serializer) {
        return writeNumber(os, collection.size(), 16, false) + writeElements(os, collection, serializer);
    }

    public static <E> ArrayList<E> collectionIn(InputStream is, StreamSerializer<E> serializer) {
        return readElements(is, readNumber(is, 16, false), serializer);
    }

    public static <T> ArrayList<? extends List<T>> doubleNestCollectionIn(
            InputStream is,
            StreamSerializer<T> serializer) {
        long size = StreamSerializerUtil.readNumber(is, 16, false);
        ArrayList<ArrayList<T>> list = new ArrayList<>();
        for (long l = 0; l < size; l++) {
            list.add(collectionIn(is, serializer));
        }
        return list;
    }

    public static <T> int doubleNestCollectionOut(
            OutputStream os, Collection<? extends List<T>> collection,
            StreamSerializer<T> serializer) {
        int size = collection.size();
        int length = writeNumber(os, size, 16, false);
        for (List<T> ls : collection) {
            length += collectionOut(os, ls, serializer);
        }
        return length;
    }

    public static <T> ArrayList<T> readElements(InputStream is, long size, StreamSerializer<T> serializer) {
        ArrayList<T> list = new ArrayList<>();
        for (long l = 0; l < size; l++) {
            list.add(serializer.in(is));
        }
        return list;
    }

    public static <E> E[] readArray(InputStream is, E[] result, StreamSerializer<E> serializer) {
        for (int l = 0; l < result.length; l++) {
            result[l] = (serializer.in(is));
        }
        return result;
    }

    public static <T> int writeElements(OutputStream os, T[] arr, StreamSerializer<T> serializer) {
        return writeElements(os, Arrays.stream(arr), serializer);
    }

    public static int writeHeads(OutputStream os, HeadMap... maps) {
        SerializableData head = Serializes.makeHead(maps);
        try {
            os.write(head.data());
        } catch (IOException e) {
            throw new CompilerFileWriteException(e);
        }
        return head.length();
    }

    public static HeadMap[] readHeads(InputStream is, int bytesLen, int... headsBitCounts) {
        byte[] headData;
        try {
            headData = is.readNBytes(bytesLen);
        } catch (IOException e) {
            throw new CompilerFileWriteException(e);
        }
        return new SerializableData(headData).phaseHeader(headsBitCounts);
    }

    public static <T> int writeElements(
            OutputStream os, Collection<? extends T> collection,
            StreamSerializer<T> serializer) {
        return writeElements(os, collection.stream(), serializer);
    }

    private static <T> int writeElements(OutputStream os, Stream<? extends T> stream, StreamSerializer<T> serializer) {
        return (int) stream.map(ele -> serializer.out(os, ele)).collect(Collectors.summarizingInt(m -> m)).getSum();
    }


    public static int writeElements(OutputStream os, byte[] data) {
        try {
            os.write(data);
        } catch (IOException e) {
            throw new CompilerFileWriteException(e);
        }
        return data.length;
    }

    public static long readNumber(InputStream is, int bitCount, boolean signed) {
        try {
            byte[] data = is.readNBytes(Serializes.bitCountToByteCount(bitCount));
            HeadMap[] headMaps = new SerializableData(data).phaseHeader(bitCount);
            return signed ? headMaps[0].getSignedValue() : headMaps[0].getUnsignedValue();
        } catch (IOException e) {
            throw new CompilerFileWriteException(e);
        }
    }

    public static int writeNumber(OutputStream os, long value, int bitCount, boolean signed) {
        try {
            byte[] data = Serializes.makeHead(new HeadMap(value, bitCount).inRange(!signed, "number")).data();
            os.write(data);
            return data.length;
        } catch (IOException e) {
            throw new CompilerFileWriteException(e);
        }
    }

    public static ArrayList<Integer> readNumbers(InputStream is, int size, int eachBitCount, boolean signed) {
        ArrayList<Integer> list = new ArrayList<>();
        for (long l = 0; l < size; l++) {
            list.add((int) readNumber(is, eachBitCount, signed));
        }
        return list;
    }

    public static int writeNumbers(OutputStream os, Collection<Integer> numbers, int eachBitCount, boolean signed) {
        return (int) numbers.stream()
                .map(ele -> writeNumber(os, ele, eachBitCount, signed))
                .collect(Collectors.summarizingInt(m -> m))
                .getSum();
    }

    public static int writeOneByte(OutputStream os, byte ordinal) {
        try {
            os.write(new byte[]{ordinal});
        } catch (IOException e) {
            throw new CompilerFileWriteException(e);
        }
        return 1;
    }

    public static <T> StreamSerializer<T> create(
            Function<InputStream, T> in,
            BiFunction<OutputStream, T, Integer> out) {
        return new StreamSerializer<>() {
            @Override
            public T in(InputStream is) {
                return in.apply(is);
            }

            @Override
            public int out(OutputStream os, T src) {
                return out.apply(os, src);
            }
        };
    }

    public static int headByte(int[] headLengthBits) {
        return Serializes.bitCountToByteCount(ArrayUtil.sum(headLengthBits));
    }
}
