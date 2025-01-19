package org.harvey.compiler.common.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * 集合工具
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-22 00:07
 */
public class CollectionUtil {

    private CollectionUtil() {
    }

    public static <K, V> Map<K, V> union(Map<K, V> map1, Map<K, V> map2) {
        Map<K, V> union = new HashMap<>(map1);
        union.putAll(map2);
        return union;
    }

    public static <E> Set<E> union(Set<E> set1, Set<E> set2) {
        Set<E> union = new HashSet<>(set1);
        union.addAll(set2);
        return union;
    }

    /**
     * 不忽略重复元素
     */
    public static <E> List<E> union(List<E> list1, List<E> list2) {
        List<E> union = new ArrayList<>(list1.size() + list2.size());
        union.addAll(list1);
        union.addAll(list2);
        return union;
    }

    public static <E> LinkedList<E> unmodifiableLinkedList(LinkedList<E> ll) {
        return new CollectionUtil.UnmodifiableLinkedList<>(ll);
    }

    public static <E> int indexOf(List<E> list, Predicate<E> predicate) {
        ListPoint<E> listPoint = find(list, predicate, 0);
        return listPoint == null ? -1 : listPoint.getIndex();
    }

    public static <E> E elementOf(List<E> list, Predicate<E> predicate) {
        ListPoint<E> listPoint = find(list, predicate, 0);
        return listPoint == null ? null : listPoint.getElement();
    }

    public static <E> ListPoint<E> find(List<E> list, Predicate<E> predicate) {
        return find(list, predicate, 0);
    }

    public static <E> ListPoint<E> find(List<E> list, Predicate<E> predicate, int fromIndex) {
        for (int i = fromIndex; i < list.size(); i++) {
            E element = list.get(i);
            if (predicate.test(element)) {
                return new ListPoint<>(i, element);
            }
        }
        return null;
    }

    public static <E> boolean contains(Collection<E> collection, Predicate<E> predicate) {
        for (E e : collection) {
            if (predicate.test(e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 迭代器不移动
     */
    public static <E> boolean nextIs(ListIterator<E> it, Predicate<E> predicate) {
        // 解析作用域 一定是关键字 一定是作用域关键字
        E next = getNext(it);
        return next != null && predicate.test(next);
    }

    public static <E> E getNext(ListIterator<E> it) {
        if (!it.hasNext()) {
            return null;
        }
        E next = it.next();
        it.previous();
        return next;
    }

    public static <T> EncirclePair<T> getEncirclePair(ArrayList<T> src, int index) {
        T pre = null;
        T post = null;
        if (index - 1 >= 0) {
            pre = src.get(index - 1);
        }
        if (index + 1 >= src.size()) {
            // 只有前了
            post = src.get(index + 1);
        }
        // 包围
        return new EncirclePair<>(pre, post);
    }

    private static class UnmodifiableLinkedList<E> extends LinkedList<E> {
        // COLLECTION
        final LinkedList<? extends E> list;

        UnmodifiableLinkedList(LinkedList<? extends E> list) {
            this.list = list;
        }

        public int size() {
            return list.size();
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }

        public boolean contains(Object o) {
            return list.contains(o);
        }

        public Object[] toArray() {
            return list.toArray();
        }

        public <T> T[] toArray(T[] a) {
            return list.toArray(a);
        }

        public <T> T[] toArray(IntFunction<T[]> f) {
            return list.toArray(f);
        }

        public String toString() {
            return list.toString();
        }

        public Iterator<E> iterator() {
            return new Iterator<E>() {
                private final Iterator<? extends E> i = list.iterator();

                public boolean hasNext() {
                    return i.hasNext();
                }

                public E next() {
                    return i.next();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void forEachRemaining(Consumer<? super E> action) {
                    // Use backing collection version
                    i.forEachRemaining(action);
                }
            };
        }

        public boolean add(E e) {
            throw new UnsupportedOperationException();
        }

        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        public boolean containsAll(Collection<?> coll) {
            return list.containsAll(coll);
        }

        public boolean addAll(Collection<? extends E> coll) {
            throw new UnsupportedOperationException();
        }

        public boolean removeAll(Collection<?> coll) {
            throw new UnsupportedOperationException();
        }

        public boolean retainAll(Collection<?> coll) {
            throw new UnsupportedOperationException();
        }

        public void clear() {
            throw new UnsupportedOperationException();
        }

        // Override default methods in Collection
        @Override
        public void forEach(Consumer<? super E> action) {
            list.forEach(action);
        }

        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Spliterator<E> spliterator() {
            return (Spliterator<E>) list.spliterator();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Stream<E> stream() {
            return (Stream<E>) list.stream();
        }

        @SuppressWarnings("unchecked")
        @Override
        public Stream<E> parallelStream() {
            return (Stream<E>) list.parallelStream();
        }

        // LIST
        public boolean equals(Object o) {
            return o == this || list.equals(o);
        }

        public int hashCode() {
            return list.hashCode();
        }

        public E get(int index) {
            return list.get(index);
        }

        public E set(int index, E element) {
            throw new UnsupportedOperationException();
        }

        public void add(int index, E element) {
            throw new UnsupportedOperationException();
        }

        public E remove(int index) {
            throw new UnsupportedOperationException();
        }

        public int indexOf(Object o) {
            return list.indexOf(o);
        }

        public int lastIndexOf(Object o) {
            return list.lastIndexOf(o);
        }

        public boolean addAll(int index, Collection<? extends E> list) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void replaceAll(UnaryOperator<E> operator) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sort(Comparator<? super E> list) {
            throw new UnsupportedOperationException();
        }

        public ListIterator<E> listIterator() {
            return listIterator(0);
        }

        public ListIterator<E> listIterator(final int index) {
            return new ListIterator<E>() {
                private final ListIterator<? extends E> i
                        = list.listIterator(index);

                public boolean hasNext() {
                    return i.hasNext();
                }

                public E next() {
                    return i.next();
                }

                public boolean hasPrevious() {
                    return i.hasPrevious();
                }

                public E previous() {
                    return i.previous();
                }

                public int nextIndex() {
                    return i.nextIndex();
                }

                public int previousIndex() {
                    return i.previousIndex();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }

                public void set(E e) {
                    throw new UnsupportedOperationException();
                }

                public void add(E e) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void forEachRemaining(Consumer<? super E> action) {
                    i.forEachRemaining(action);
                }
            };
        }

        public List<E> subList(int fromIndex, int toIndex) {
            return Collections.unmodifiableList(list.subList(fromIndex, toIndex));
        }

        private Object readResolve() {
            return this;
        }

        // LinkedList
        @Override
        public E getFirst() {
            return list.getFirst();
        }

        @Override
        public E getLast() {
            return list.getLast();
        }

        @Override
        public E removeFirst() {
            throw new UnsupportedOperationException();
        }

        @Override
        public E removeLast() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addFirst(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addLast(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public E peek() {
            return list.peek();
        }

        @Override
        public E element() {
            return list.element();
        }

        @Override
        public E poll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public E remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean offer(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean offerFirst(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean offerLast(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public E peekFirst() {
            return list.peekFirst();
        }

        @Override
        public E peekLast() {
            return list.peekLast();
        }

        @Override
        public E pollFirst() {
            throw new UnsupportedOperationException();
        }

        @Override
        public E pollLast() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void push(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public E pop() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeFirstOccurrence(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeLastOccurrence(Object o) {
            return list.removeLastOccurrence(o);
        }

        @Override
        public Iterator<E> descendingIterator() {
            return new Iterator<E>() {
                private final Iterator<? extends E> i
                        = list.descendingIterator();

                @Override
                public boolean hasNext() {
                    return i.hasNext();
                }

                @Override
                public E next() {
                    return i.next();
                }
            };
        }

        @Override
        public Object clone() {
            return list.clone();
        }
    }

}
