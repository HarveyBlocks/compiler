package org.harvey.compiler.common.collecction;

import org.harvey.compiler.exception.self.CompilerException;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-04-03 21:28
 */
public class SimpleLinkedList<E> {
    int size;
    Node<E> begin;
    Node<E> end;


    public SimpleLinkedList() {
        this.size = 0;
        this.begin = new Node<>(null);
        this.end = new Node<>(null);
        this.begin.next = this.end;
        this.begin.previous = this.end;
        this.end.next = this.begin;
        this.end.previous = this.begin;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void addLast(E element) {
        Node<E> newNode = new Node<>(element);
        newNode.previous = this.end.previous;
        newNode.next = this.end;
        this.end.previous = newNode;
        newNode.previous.next = newNode;
        this.size++;
    }

    public void removeLast() {
        if (isEmpty()) {
            throw new CompilerException("size == 0");
        }
        Node<E> last = end.previous;
        end.previous = last.previous;
        last.previous.next = begin;
        this.size--;
    }

    public void addFirst(E element) {
        Node<E> newNode = new Node<>(element);
        newNode.previous = this.begin;
        newNode.next = this.begin.next;
        this.begin.next = newNode;
        newNode.next.previous = newNode;
        this.size++;
    }

    public void removeFirst() {
        if (isEmpty()) {
            throw new CompilerException("size == 0");
        }
        Node<E> first = begin.next;
        begin.next = first.next;
        first.next.previous = begin;
        this.size--;
    }

    public E getFirst() {
        if (isEmpty()) {
            throw new CompilerException("size == 0");
        }
        // 消耗
        return begin.next.value;
    }

    public E getLast() {
        if (isEmpty()) {
            throw new CompilerException("size == 0");
        }
        return end.previous.value;
    }

    public Node<E> node(int index) {
        // assert isElementIndex(index);
        Node<E> x;
        if (index < (size >> 1)) {
            x = begin;
            for (int i = 0; i < index; i++) {
                x = x.next;
            }
        } else {
            x = end;
            for (int i = size - 1; i > index; i--) {
                x = x.previous;
            }
        }
        return x;
    }

    public static class Node<E> {
        Node<E> previous;
        E value;
        Node<E> next;

        public Node(E value) {
            this.value = value;
        }
    }

}
