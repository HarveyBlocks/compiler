package org.harvey.compiler.common.tree;

import org.harvey.compiler.common.collecction.PairList;

import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * 一个多叉树的数据结构, 主要用于序列化
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-13 16:43
 */
public interface MultipleTree<T> {
    boolean readOnly();

    MultipleTree<T> getChild(int index);

    List<MultipleTree<T>> getChildren();

    T getValue();

    void setValue(T value);

    T[] getChildrenValue(IntFunction<T[]> generator);

    int childrenSize();

    void addChild(MultipleTree<T> child);

    List<String> toStringList(String separator, String pre, String post, Function<T, String> toString);

    void setReadOnly(boolean readOnly);

    /**
     * 有一个坏处, 一直向brother加辈孩子 就会无限循环下去
     */
    void forEach(ForEachConsumer<T> n);

    PairList<Integer, T> toSequence();

    boolean isNull();

    List<String> toStringList(
            String separator,
            String pre,
            String post);

    /**
     * @return 克隆结构, 而不是克隆值
     */
    MultipleTree<T> cloneThis();

    interface ForEachConsumer<T> {
        void accept(List<MultipleTree<T>> brothers, int indexOfThisNode);
    }
}
