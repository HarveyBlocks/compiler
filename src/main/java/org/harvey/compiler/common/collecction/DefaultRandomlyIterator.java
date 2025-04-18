package org.harvey.compiler.common.collecction;

import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.exception.self.ReadOnlyException;

import java.util.Iterator;
import java.util.Stack;
import java.util.function.Function;

/**
 * 随机访问的iterator的默认实现
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-02-25 23:46
 */
public class DefaultRandomlyIterator<E> implements RandomlyIterator<E>, Cloneable, Iterable<E> {

    protected final RandomlyAccessAble<E> randomlyAccessAble;
    private final int length;
    private final Stack<Integer> randomlyAccessMark = new Stack<>();
    // 指向下一个元素
    protected int index;

    public DefaultRandomlyIterator(RandomlyAccessAble<E> sourceList, int initIndex) {
        // 能够随机访问
        this.index = initIndex;
        randomlyAccessMark.push(initIndex);
        this.length = sourceList.length();
        this.randomlyAccessAble = sourceList;
    }


    public DefaultRandomlyIterator(RandomlyAccessAble<E> randomlyAccessAble) {
        this(randomlyAccessAble, 0);
    }


    public DefaultRandomlyIterator<E> toBegin() {
        index = 0;
        return this;
    }

    public DefaultRandomlyIterator<E> toEnd() {
        index = length;
        return this;
    }

    @Override
    public String toString() {
        int iMax = length - 1;
        if (iMax == -1) {
            return "[]";
        }
        StringBuilder b = new StringBuilder();
        b.append('[');
        for (int i = 0; ; i++) {
            E at = randomlyAccessAble.at(i);
            b.append(at);
            if (i == iMax) {
                return b.append(']').toString();
            }
            b.append(", ");
        }
    }

    @Override
    public boolean hasNext() {
        return index < length;
    }

    @Override
    public E next() {
        return randomlyAccessAble.at(index++);
    }

    public E previousWithoutMove() {
        return randomlyAccessAble.at(index - 1);
    }

    public E nextWithoutMove() {
        return randomlyAccessAble.at(index);
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public E previous() {
        return randomlyAccessAble.at(--index);
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
        throw new ReadOnlyException();
    }

    @Override
    public void set(E character) {
        throw new ReadOnlyException();
    }

    @Override
    public void add(E character) {
        throw new ReadOnlyException();
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Object clone() {
        DefaultRandomlyIterator<?> newSit = new DefaultRandomlyIterator<>(this.randomlyAccessAble);
        newSit.index = this.index;
        return newSit;
    }

    public DefaultRandomlyIterator<E> addIndex(int offset) {
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
    public Iterator<E> iterator() {
        return new DefaultRandomlyIterator<>(randomlyAccessAble);
    }

    @Override
    public RandomlyIterator<E> skipTo(int index) {
        this.index = index;
        return this;
    }

    @Override
    public RandomlyIterator<E> skipTo(Function<Integer, Integer> mapper) {
        return skipTo(mapper.apply(index));
    }

    /**
     * @see Stack#push(Object)
     */
    @Override
    public void mark() {
        this.randomlyAccessMark.push(index);
    }

    /**
     * @see Stack#peek()
     */
    @Override
    public void returnToMark() {
        this.index = randomlyAccessMark.peek();
    }

    /**
     * @see Stack#pop()
     */
    @Override
    public void returnToAndRemoveMark() {
        this.index = randomlyAccessMark.pop();
    }

    @Override
    public void removeMark() {
        randomlyAccessMark.pop();
    }
}
