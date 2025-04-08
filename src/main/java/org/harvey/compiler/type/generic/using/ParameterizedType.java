package org.harvey.compiler.type.generic.using;

import lombok.Getter;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.common.tree.DefaultMultipleTree;
import org.harvey.compiler.common.tree.MultipleTree;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.io.serializer.StreamSerializer;
import org.harvey.compiler.io.serializer.StreamSerializerUtil;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourcePositionSupplier;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-02 00:04
 */
@Getter
public class ParameterizedType<T extends SourcePositionSupplier> {
    /**
     * nullable
     */
    private final MultipleTree<T> tree;

    public ParameterizedType(MultipleTree<T> tree) {
        this.tree = tree;
    }

    public ParameterizedType(T rawType) {
        this.tree = new DefaultMultipleTree<>(rawType, false);
    }

    public void setReadOnly(boolean readOnly) {
        this.tree.setReadOnly(readOnly);
    }


    public static <T extends SourcePositionSupplier> ParameterizedType<T> toTree(
            List<Pair<Integer, T>> sequence) {
        return new ParameterizedType<>(DefaultMultipleTree.toTree(sequence, true));
    }

    public static <T extends SourcePositionSupplier> List<Pair<Integer, T>> toSequence(ParameterizedType<T> type) {
        return type == null ? Collections.emptyList() : type.toSequence();
    }

    public static <T extends SourcePositionSupplier> ParameterizedType<T> empty() {
        return toTree(Collections.emptyList());
    }

    public boolean isNull() {
        return tree == null || tree.isNull();
    }

    public void addParameter(ParameterizedType<T> parameter) {
        if (this.tree != null) {
            this.tree.addChild(parameter.tree);
        } else {
            throw new CompilerException("can not add child", new NullPointerException());
        }
    }

    public List<Pair<Integer, T>> toSequence() {
        return tree == null ? Collections.emptyList() : tree.toSequence();
    }

    public SourcePosition getPosition() {
        return tree == null ? SourcePosition.UNKNOWN : tree.getValue().getPosition();
    }

    public T getRawType() {
        return tree == null ? null : tree.getValue();
    }

    public T[] getChildrenValue(IntFunction<T[]> generator) {
        return tree.getChildrenValue(generator);
    }

    @SuppressWarnings("unchecked")
    public ParameterizedType<T>[] getChildren() {
        return tree.getChildren().stream().map(ParameterizedType::new).toArray(ParameterizedType[]::new);
    }

    public ParameterizedType<T> getChild(int index) {
        return new ParameterizedType<>(tree.getChild(index));
    }

    /**
     * 对资源的消耗大, 尽量少用
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : toStringList()) {
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }


    /**
     * 及其消耗资源
     *
     * @see org.harvey.compiler.execute.calculate.Operator#GENERIC_LIST_PRE
     * @see org.harvey.compiler.execute.calculate.Operator#GENERIC_LIST_POST
     * @see org.harvey.compiler.execute.calculate.Operator#COMMA
     */
    public List<String> toStringList() {
        return tree.toStringList(
                Operator.COMMA.getName(), Operator.GENERIC_LIST_PRE.getName(), Operator.GENERIC_LIST_POST.getName());
    }

    public List<String> toStringList(Function<T, String> toString) {
        return tree.toStringList(Operator.COMMA.getName(), Operator.GENERIC_LIST_PRE.getName(),
                Operator.GENERIC_LIST_POST.getName(), toString
        );
    }

    /**
     * @return 克隆结构, 而不是克隆值
     */
    public ParameterizedType<T> cloneThis() {
        return new ParameterizedType<>(this.tree.cloneThis());
    }


    public abstract static class Serializer<T extends SourcePositionSupplier> implements
            StreamSerializer<ParameterizedType<T>> {
        protected abstract StreamSerializer<T> getRawTypeSerializer();

        @Override
        public ParameterizedType<T> in(InputStream is) {
            int size = (int) StreamSerializerUtil.readNumber(is, 8, false);
            List<Pair<Integer, T>> sequence = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                int n = (int) StreamSerializerUtil.readNumber(is, 8, false);
                T reference = getRawTypeSerializer().in(is);
                sequence.add(new Pair<>(n, reference));
            }
            return ParameterizedType.toTree(sequence);
        }

        @Override
        public int out(OutputStream os, ParameterizedType<T> src) {
            List<Pair<Integer, T>> sequence = src == null ? Collections.emptyList() : src.toSequence();
            int length = StreamSerializerUtil.writeNumber(os, sequence.size(), 8, false);
            for (Pair<Integer, T> pair : sequence) {
                length += StreamSerializerUtil.writeNumber(os, pair.getKey(), 8, false) +
                          getRawTypeSerializer().out(os, pair.getValue());
            }
            return length;
        }
    }
}
