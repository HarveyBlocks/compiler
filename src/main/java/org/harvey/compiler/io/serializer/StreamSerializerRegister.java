package org.harvey.compiler.io.serializer;

import org.harvey.compiler.exception.CompilerException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Register
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-01-29 15:03
 */
public class StreamSerializerRegister {
    private static final Map<String, StreamSerializer<?>> REGISTER = new HashMap<>();

    private StreamSerializerRegister() {
    }

    public static void register(StreamSerializer<?> serializer) {
        REGISTER.put(serializer.getClass().getName(), serializer);
    }

    public static <T extends StreamSerializer<?>> T get(Class<T> target) {
        String key = getSerializerKey(target);
        StreamSerializer<?> serializer = REGISTER.get(key);
        if (serializer != null) {
            return cast(serializer);
        }
        try {
            Class.forName(key);
        } catch (ClassNotFoundException e) {
            // target name 由 target 而来
            // throw new CompilerException("Unregistered stream serializer", e);
        }
        serializer = REGISTER.get(key);
        if (serializer == null) {
            throw new CompilerException("serializer of " + key + " do not be registered");
        }
        return cast(serializer);
    }

    private static <T extends StreamSerializer<?>> String getSerializerKey(Class<T> target) {
        return target.getName();
    }

    private static <T extends StreamSerializer<?>> String getCollectionSerializerKey(Class<T> elementType) {
        return "[]" + elementType.getName();
    }


    public static <T> StreamSerializer<Collection<T>> getCollectionSerializer(
            Class<? extends StreamSerializer<T>> elementSerializerType) {
        String collectionSerializerKey = getCollectionSerializerKey(elementSerializerType);
        StreamSerializer<?> collectionSerializer = REGISTER.get(collectionSerializerKey);
        if (collectionSerializer != null) {
            return castToCollection(collectionSerializer);
        }
        StreamSerializer<T> elementSerializer = get(elementSerializerType);
        if (elementSerializer == null) {
            throw new CompilerException(
                    "element serializer of " + elementSerializerType.getName() + " do not be registered");
        }
        collectionSerializer = newCollectionSerializer(elementSerializer);
        REGISTER.put(collectionSerializerKey, collectionSerializer);
        return castToCollection(collectionSerializer);
    }


    private static <E> StreamSerializer<Collection<E>> newCollectionSerializer(
            StreamSerializer<E> elementSerializer) {
        return new StreamSerializer<>() {
            @Override
            public Collection<E> in(InputStream is) {
                return StreamSerializerUtil.collectionIn(is, elementSerializer);
            }

            @Override
            public int out(OutputStream os, Collection<E> src) {
                return StreamSerializerUtil.collectionOut(os, src, elementSerializer);
            }
        };
    }

    private static <T extends StreamSerializer<?>> T cast(StreamSerializer<?> serializer) {
        return (T) serializer;
    }

    private static <T> StreamSerializer<Collection<T>> castToCollection(
            StreamSerializer<?> collectionSerializer) {
        return (StreamSerializer<Collection<T>>) collectionSerializer;
    }
}
