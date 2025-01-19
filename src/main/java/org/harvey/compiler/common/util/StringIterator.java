package org.harvey.compiler.common.util;

import org.harvey.compiler.exception.CompilerException;

import java.util.Iterator;
import java.util.ListIterator;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-16 15:00
 */
public class StringIterator implements ListIterator<Character>, Cloneable, Iterable<Character> {
    private final String value;
    private final int length;
    // 指向下一个元素
    private int index;

    public StringIterator(String value) {
        this.value = value;
        this.index = 0;
        this.length = value.length();
    }

    public StringIterator toBegin() {
        index = 0;
        return this;
    }

    public StringIterator toEnd() {
        index = length;
        return this;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean hasNext() {
        return index < length;
    }


    @Override
    public Character next() {
        return value.charAt(index++);
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public Character previous() {
        return value.charAt(--index);
    }

    public Character previousWithoutMove() {
        return value.charAt(index - 1);
    }

    public Character nextWithoutMove() {
        return value.charAt(index);
    }

    @Override
    public int nextIndex() {
        return index;
    }

    @Override
    public int previousIndex() {
        return index - 1;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(Character character) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(Character character) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Object clone() {
        StringIterator newSit = new StringIterator(this.value);
        newSit.index = this.index;
        return newSit;
    }

    /**
     * 用`next`还没有遍历过的部分
     */
    public String stringAfter() {
        return value.substring(index);
    }

    public String stringPrevious() {
        return value.substring(0, index);
    }

    public StringIterator addIndex(int offset) {
        int newIndex = offset + index;
        if (newIndex > length || newIndex < 0) {
            throw new CompilerException("String Iterator out of bound", new IndexOutOfBoundsException());
        }
        index = newIndex;
        return this;
    }

    public int length() {
        return length;
    }

    @Override
    public Iterator<Character> iterator() {
        return new StringIterator(value);
    }
}
