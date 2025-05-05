package org.harvey.compiler.common.collecction;

import org.harvey.compiler.common.util.EncirclePair;
import org.harvey.compiler.exception.self.CompilerException;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    /**
     * 迭代器不移动
     */
    public static <E> boolean previousIs(ListIterator<E> it, Predicate<E> predicate) {
        // 解析作用域 一定是关键字 一定是作用域关键字
        E previous = getPrevious(it);
        return previous != null && predicate.test(previous);
    }


    public static <E> E getNext(ListIterator<E> it) {
        if (!it.hasNext()) {
            return null;
        }
        E next = it.next();
        it.previous();
        return next;
    }

    public static <E> E getPrevious(ListIterator<E> it) {
        if (!it.hasPrevious()) {
            return null;
        }
        E previous = it.previous();
        it.next();
        return previous;
    }

    public static <T> EncirclePair<T> getEncirclePair(List<T> src, int index) {
        T pre = null;
        T post = null;
        if (index - 1 >= 0) {
            pre = src.get(index - 1);
        }
        if (index + 1 < src.size()) {
            // 只有前了
            post = src.get(index + 1);
        }
        // 包围
        return new EncirclePair<>(pre, post);
    }

    /**
     * @return iterator.previous, iterator.next.next
     */
    public static <T> EncirclePair<T> getEncirclePair(ListIterator<T> iterator) {
        T pre = CollectionUtil.getPrevious(iterator);
        if (!iterator.hasNext()) {
            throw new CompilerException("encircle must have mid");
        }
        iterator.next();
        T post = CollectionUtil.getNext(iterator);
        iterator.previous();
        // 包围
        return new EncirclePair<>(pre, post);
    }

    public static <T> RandomlyIterator<T> randomlyIterator(List<T> sourceList, int index) {
        // 能随机访问的
        return new DefaultRandomlyIterator<>(RandomlyAccessAble.forList(sourceList), index);
    }

    /**
     * @param it 指向end的元素
     * @return 最后的元素
     */
    public static <E> List<E> skipTo(ListIterator<E> it, Predicate<E> endPredicate, boolean includingEnd) {
        List<E> skipped = new ArrayList<>();
        while (it.hasNext()) {
            E next = it.next();
            if (!endPredicate.test(next)) {
                skipped.add(next);
                continue;
            }
            if (includingEnd) {
                skipped.add(next);
            } else {
                it.previous();
            }
            break;
        }
        return skipped;
    }

    public static <E> boolean skipIf(ListIterator<E> iterator, Predicate<E> predicate) {
        if (!iterator.hasNext()) {
            return false;
        }
        E next = iterator.next();
        boolean test = predicate.test(next);
        if (!test) {
            iterator.previous();
        }
        return test;
    }

    public static <T> void restCopy(Iterator<T> iterator, List<T> restPart) {
        while (iterator.hasNext()) {
            restPart.add(iterator.next());
        }
    }


    /**
     * @see CollectionUtil#skipNest(ListIterator, Predicate, Predicate, Supplier)
     */
    public static <E> void skipNest(ListIterator<E> it, Predicate<E> isPre, Predicate<E> isPost) {
        skipNest(it, isPre, isPost, null);
    }

    /**
     * @return have pre and post
     */
    public static <E, R extends List<E>> R skipNest(
            ListIterator<E> it, Predicate<E> isPre, Predicate<E> isPost,
            Supplier<R> generator) {
        R result = generator == null ? null : generator.get();
        if (!it.hasNext()) {
            return result;
        }
        if (!CollectionUtil.nextIs(it, isPre)) {
            return result;
        }
        E first = it.next();// 跳过第一个
        if (result != null) {
            result.add(first);
        }
        int inNest = 1;
        while (it.hasNext()) {
            E next = it.next();
            if (result != null) {
                result.add(next);
            }
            if (isPre.test(next)) {
                inNest++;
            } else if (isPost.test(next)) {
                inNest--;
                if (inNest < 0) {
                    throw new CompilerException("Illegal matching");
                }
                if (inNest == 0) {
                    return result;
                }
            }
        }
        throw new CompilerException("expect more...");
    }

    public static <T> Stack<T> cloneStack(Stack<T> referenceStack) {
        Stack<T> clone = new Stack<>();
        clone.addAll(referenceStack);
        return clone;
    }

    public static <P, R> Stack<R> cloneStack(Stack<P> referenceStack, Function<P, R> mapper) {
        Stack<R> clone = new Stack<>();
        clone.addAll(referenceStack.stream().map(mapper).collect(Collectors.toList()));
        return clone;
    }

    public static <E> E one(
            Collection<E> collect,
            Predicate<E> filter,
            RuntimeException tooMany,
            RuntimeException empty) {
        return one(collect.stream().filter(filter).collect(Collectors.toList()), tooMany, empty);
    }

    public static <E> E one(
            Collection<E> collect,
            RuntimeException tooMany,
            RuntimeException empty) {
        if (collect.isEmpty()) {
            if (empty == null) {
                return null;
            } else {
                throw empty;
            }
        }
        if (collect.size() > 1) {
            if (tooMany == null) {
                return null;
            } else {
                throw tooMany;
            }
        }
        for (E e : collect) {
            return e;
        }
        return null;
    }

    public static <T> LinkedList<T> toLinkedList(T[] src) {
        if (src == null) {
            return new LinkedList<>();
        }
        LinkedList<T> result = new LinkedList<>();
        Collections.addAll(result, src);
        return result;
    }

}
