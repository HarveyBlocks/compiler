package org.harvey.compiler.common.collecction;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

/**
 * 能随机访问的集合
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-02-26 00:02
 */
public abstract class RandomlyAccessAble<E> {
    public static RandomlyAccessAble<Character> forString(String s) {
        return new CharSequenceRA<>(s);
    }

    public static <E> RandomlyAccessAble<E> forList(List<E> l) {
        return new ListRA<>(l);
    }


    public static <E> RandomlyAccessAble<E> forArray(E[] l) {
        return new ArrayRA<>(l);
    }

    public abstract E at(int index);

    public abstract int length();

    @AllArgsConstructor
    private static class ArrayRA<E> extends RandomlyAccessAble<E> {
        private final E[] value;

        @Override
        public E at(int index) {
            return value[index];
        }

        @Override
        public int length() {
            return value.length;
        }
    }

    private static class ListRA<E> extends RandomlyAccessAble<E> {
        private final List<E> value;

        private ListRA(List<E> value) {
            this.value = value instanceof RandomAccess ? value : new ArrayList<>(value);
        }

        @Override
        public E at(int index) {
            return value.get(index);
        }

        @Override
        public int length() {
            return value.size();
        }
    }

    @AllArgsConstructor
    private static class CharSequenceRA<E> extends RandomlyAccessAble<Character> {
        private final CharSequence value;

        @Override
        public Character at(int index) {
            return value.charAt(index);
        }

        @Override
        public int length() {
            return value.length();
        }
    }
}
