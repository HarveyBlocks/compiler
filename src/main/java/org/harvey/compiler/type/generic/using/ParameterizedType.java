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
@Deprecated
@Getter
public class ParameterizedType<RAW extends SourcePositionSupplier> {
    /**
     * nullable
     */
    private final MultipleTree<RAW> tree;

    public ParameterizedType(MultipleTree<RAW> tree) {
        this.tree = tree;
    }

    public ParameterizedType(RAW rawType) {
        this.tree = new DefaultMultipleTree<>(rawType, false);
    }

    public static <RAW extends SourcePositionSupplier> ParameterizedType<RAW> toTree(
            List<Pair<Integer, RAW>> sequence) {
        return new ParameterizedType<>(DefaultMultipleTree.toTree(sequence, true));
    }

    public static <RAW extends SourcePositionSupplier> List<Pair<Integer, RAW>> toSequence(ParameterizedType<RAW> type) {
        return type == null ? Collections.emptyList() : type.toSequence();
    }

    public static <RAW extends SourcePositionSupplier> ParameterizedType<RAW> empty() {
        return toTree(Collections.emptyList());
    }

    public void setReadOnly(boolean readOnly) {
        this.tree.setReadOnly(readOnly);
    }

    public boolean isNull() {
        return tree == null || tree.isNull();
    }

    public void addParameter(ParameterizedType<RAW> parameter) {
        if (this.tree != null) {
            this.tree.addChild(parameter.tree);
        } else {
            throw new CompilerException("can not addIdentifier child", new NullPointerException());
        }
    }

    public List<Pair<Integer, RAW>> toSequence() {
        return tree == null ? Collections.emptyList() : tree.toSequence();
    }

    public SourcePosition getPosition() {
        return tree == null ? SourcePosition.UNKNOWN : tree.getValue().getPosition();
    }

    public RAW getRawType() {
        return tree == null ? null : tree.getValue();
    }

    public RAW[] getChildrenValue(IntFunction<RAW[]> generator) {
        return tree.getChildrenValue(generator);
    }

    @SuppressWarnings("unchecked")
    public ParameterizedType<RAW>[] getChildren() {
        return tree.getChildren().stream().map(ParameterizedType::new).toArray(ParameterizedType[]::new);
    }

    public ParameterizedType<RAW> getChild(int index) {
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

    public List<String> toStringList(Function<RAW, String> toString) {
        return tree.toStringList(Operator.COMMA.getName(), Operator.GENERIC_LIST_PRE.getName(),
                Operator.GENERIC_LIST_POST.getName(), toString
        );
    }

    /**
     * @return 克隆结构, 而不是克隆值
     */
    public ParameterizedType<RAW> cloneThis() {
        return new ParameterizedType<>(this.tree.cloneThis());
    }


    public abstract static class Serializer<RAW extends SourcePositionSupplier> implements
            StreamSerializer<ParameterizedType<RAW>> {
        protected abstract StreamSerializer<RAW> getRawTypeSerializer();

        @Override
        public ParameterizedType<RAW> in(InputStream is) {
            int size = (int) StreamSerializerUtil.readNumber(is, 8, false);
            List<Pair<Integer, RAW>> sequence = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                int n = (int) StreamSerializerUtil.readNumber(is, 8, false);
                RAW reference = getRawTypeSerializer().in(is);
                sequence.add(new Pair<>(n, reference));
            }
            return ParameterizedType.toTree(sequence);
        }

        @Override
        public int out(OutputStream os, ParameterizedType<RAW> src) {
            List<Pair<Integer, RAW>> sequence = src == null ? Collections.emptyList() : src.toSequence();
            int length = StreamSerializerUtil.writeNumber(os, sequence.size(), 8, false);
            for (Pair<Integer, RAW> pair : sequence) {
                length += StreamSerializerUtil.writeNumber(os, pair.getKey(), 8, false) +
                          getRawTypeSerializer().out(os, pair.getValue());
            }
            return length;
        }
    }
}
