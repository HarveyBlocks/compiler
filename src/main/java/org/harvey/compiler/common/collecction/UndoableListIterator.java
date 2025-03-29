package org.harvey.compiler.common.collecction;

import java.util.ListIterator;

/**
 * 能够返回前一状态的iterator
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-02-27 10:54
 */
public class UndoableListIterator<E> implements ListIterator<E> {
    private final ListIterator<E> value;
    private int undo = 0;

    public UndoableListIterator(ListIterator<E> value) {
        this.value = value;
    }

    public UndoableListIterator<E> undo() {
        while (!inSitu()) {
            E ignore = undo > 0 ? previous() : next();
        }
        return this;
    }

    public boolean inSitu() {
        return undo == 0;
    }

    public UndoableListIterator<E> resetUndo() {
        undo = 0;
        return this;
    }

    @Override
    public boolean hasNext() {
        return value.hasNext();
    }

    @Override
    public E next() {
        undo++;
        return value.next();
    }

    @Override
    public boolean hasPrevious() {
        return value.hasPrevious();
    }

    @Override
    public E previous() {
        undo--;
        return value.previous();
    }

    @Override
    public int nextIndex() {
        return value.nextIndex();
    }

    @Override
    public int previousIndex() {
        return value.previousIndex();
    }

    @Override
    public void remove() {
        value.remove();
    }

    @Override
    public void set(E e) {
        value.set(e);
    }

    @Override
    public void add(E e) {
        value.add(e);
    }
}
