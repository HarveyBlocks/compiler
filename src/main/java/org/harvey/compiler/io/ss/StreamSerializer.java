package org.harvey.compiler.io.ss;


import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO  
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

    /**
     * @param is src
     * @return target
     */
    T in(InputStream is);

    /**
     * @param os target
     * @return size
     */
    int out(OutputStream os, T src);


}
