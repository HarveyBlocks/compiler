package org.harvey.compiler.common.tree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.common.collecction.PairList;
import org.harvey.compiler.exception.self.CompilerException;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-13 16:37
 */
@Getter
public class DefaultMultipleTree<T> implements MultipleTree<T> {

    private final List<MultipleTree<T>> children = new ArrayList<>();
    @Setter
    private T value;
    @Setter
    private boolean readOnly;


    public DefaultMultipleTree(T value) {
        this.value = value;
    }

    public DefaultMultipleTree(T value, boolean readOnly) {
        this.value = value;
        this.readOnly = readOnly;
    }

    public static <T> DefaultMultipleTree<T> toTree(List<Pair<Integer, T>> sequence, boolean readOnly) {
        if (sequence == null || sequence.isEmpty()) {
            return null;
        }
        ListIterator<Pair<Integer, T>> iterator = sequence.listIterator();
        LinkedList<Pair<Integer, DefaultMultipleTree<T>>> queue = new LinkedList<>();
        Pair<Integer, T> first = iterator.next();
        int size0 = first.getKey();
        DefaultMultipleTree<T> type0 = new DefaultMultipleTree<>(first.getValue(), readOnly);
        if (size0 > 0) {
            queue.addLast(new Pair<>(size0, type0));
        } else {
            if (iterator.hasNext()) {
                throw new CompilerException("illegal sequence: superfluous");
            } else {
                return type0;
            }
        }

        while (iterator.hasNext()) {
            if (queue.isEmpty()) {
                throw new CompilerException("illegal sequence: superfluous");
            }
            Pair<Integer, DefaultMultipleTree<T>> each = queue.removeFirst();
            DefaultMultipleTree<T> type = each.getValue();
            Integer size = each.getKey();
            for (int i = 0; i < size; i++) {
                if (!iterator.hasNext()) {
                    throw new CompilerException("illegal sequence: superfluous");
                }
                Pair<Integer, T> next = iterator.next();
                int nextSize = next.getKey();
                DefaultMultipleTree<T> parameter = new DefaultMultipleTree<>(next.getValue());
                type.addChild(parameter);
                queue.addLast(new Pair<>(nextSize, parameter));
            }
        }
        if (!queue.isEmpty()) {
            throw new CompilerException("illegal sequence: superfluous");
        } else {
            return type0;
        }
    }

    public static <T> List<Pair<Integer, T>> toSequence(MultipleTree<T> type) {
        return type == null ? Collections.emptyList() : type.toSequence();
    }

    private static <T> void action(PairList<Integer, T> result, MultipleTree<T> first) {
        result.add(first.childrenSize(), first.getValue());
    }

    @Override
    public boolean readOnly() {
        return readOnly;
    }

    @Override
    public MultipleTree<T> getChild(int index) {
        return children.get(index);
    }


    @Override
    public T[] getChildrenValue(IntFunction<T[]> generator) {
        return children.stream().map(MultipleTree::getValue).toArray(generator);
    }

    @Override
    public int childrenSize() {
        return children.size();
    }

    @Override
    public void addChild(MultipleTree<T> child) {
        if (readOnly) {
            throw new CompilerException("read only", new UnsupportedOperationException());
        }
        this.children.add(child);
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    @Override
    public List<String> toStringList(String separator, String pre, String post) {
        return toStringList(separator, pre, post, Objects::toString);
    }


    @Deprecated
    private List<String> recursiveStringList(String separator, String pre, String post) {
        if (this.isNull()) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>();
        list.add(value.toString());
        if (children.isEmpty()) {
            return list;
        }
        list.add(pre);
        for (MultipleTree<T> child : children) {
            // 递归
            List<String> childList = ((DefaultMultipleTree<T>) child).recursiveStringList(separator, pre, post);
            list.addAll(childList);
            list.add(separator);
        }
        list.set(list.size() - 1, separator);
        return list;
    }

    @Override
    public List<String> toStringList(String separator, String pre, String post, Function<T, String> mapper) {
        if (this.isNull()) {
            return Collections.emptyList();
        }
        Stack<StackElement<T>> stack = new Stack<>();
        ArrayList<String> result = new ArrayList<>();
        stack.push(new StackElement<>(result, List.of(this), this, 0));
        while (!stack.empty()) {
            StackElement<T> top = stack.peek();
            MultipleTree<T> node = top.node;
            top.result.add(mapper.apply(node.getValue()));
            if (node.childrenSize() != 0) {
                top.result.add(pre);
                stack.push(new StackElement<>(new ArrayList<>(), node.getChildren(), node.getChild(0), 0));
                continue;
            }
            while (!stack.empty()) {
                StackElement<T> peek = stack.peek();
                if (peek.indexInParent + 1 < peek.brothers.size()) {
                    peek.result.add(separator);
                    peek.node = peek.brothers.get(++peek.indexInParent);
                    break;
                }
                // 遍历完毕brothers
                stack.pop();
                if (stack.empty()) {
                    break;
                }
                StackElement<T> parent = stack.peek();
                parent.result.addAll(peek.result);
                parent.result.add(post);
            }
        }
        return result;
    }

    /**
     * 由于会改变结构, 所以记得使用{@link #cloneThis()} 来避免结构的破坏
     */
    @Override
    public void forEach(ForEachConsumer<T> n) {
        if (this.isNull()) {
            return;
        }
        Stack<StackElement<T>> stack = new Stack<>();
        stack.push(new StackElement<>(null, List.of(this), this, 0));
        while (!stack.empty()) {
            StackElement<T> top = stack.peek();
            MultipleTree<T> node = top.node;
            // top.result.addIdentifier(mapper.apply(node.getValue()));

            List<MultipleTree<T>> brothers = readOnly ? Collections.unmodifiableList(top.brothers) : top.brothers;
            n.accept(brothers, top.indexInParent);
            if (node.childrenSize() != 0) {
                stack.push(new StackElement<>(null, node.getChildren(), node.getChild(0), 0));
                continue;
            }
            while (!stack.empty()) {
                StackElement<T> peek = stack.peek();
                if (peek.indexInParent + 1 < peek.brothers.size()) {
                    peek.node = peek.brothers.get(++peek.indexInParent);
                    break;
                }
                // 遍历完毕brothers
                stack.pop();
                if (stack.empty()) {
                    break;
                }
            }
        }
    }

    @Override
    public PairList<Integer, T> toSequence() {
        // 广度优先
        PairList<Integer, T> result = new PairList<>();
        LinkedList<Pair<Integer, MultipleTree<T>>> queue = new LinkedList<>();
        queue.push(new Pair<>(0, this));
        action(result, this);
        while (!queue.isEmpty()) {
            Pair<Integer, MultipleTree<T>> top = queue.removeFirst();
            MultipleTree<T> type = top.getValue();
            Integer index = top.getKey();
            if (index < type.childrenSize()) {
                queue.addLast(new Pair<>(index + 1, type));
                MultipleTree<T> parameter = type.getChild(index);
                action(result, parameter);
                queue.addLast(new Pair<>(0, parameter));
            }
        }
        return result;
    }

    @Override
    public MultipleTree<T> cloneThis() {
        return DefaultMultipleTree.toTree(this.toSequence(), false);
    }

    @AllArgsConstructor
    private static class StackElement<T> {
        private final List<String> result;
        private final List<MultipleTree<T>> brothers;
        private MultipleTree<T> node;
        private int indexInParent;
    }
}
