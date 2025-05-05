package org.harvey.compiler.type.transform.test.version4;

import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.common.tree.MultipleTree;
import org.harvey.compiler.common.util.ArrayUtil;
import org.harvey.compiler.exception.self.CompilerException;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Stack;
import java.util.function.BiConsumer;

/**
 * 考虑了多继承
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-30 23:02
 */
@SuppressWarnings("DuplicatedCode")
class AssignableParameterized implements Assignable {
    final Parameterized to;

    AssignableParameterized(Parameterized parameterized) {
        to = parameterized;
        if (to.getRawType() instanceof GenericDefineReference) {
            throw new CompilerException("不能处理此parameterized, 请使用: " + AssignableGenericDefine.class.getName());
        }
    }

    private static String toStringValue(MultipleTree<RawTypeCanInParameter> tree) {
        return new Parameterized(tree).toStringValue();
    }

    private static TempStructure getUpper(TempStructure fromStructure, Integer reference) {
        return StructureUpperPathFideIterator.upper(fromStructure, reference);
    }

    private static void rebuildParameterized(
            MultipleTree<RawTypeCanInParameter> mappedParameterized,
            TempStructure finalOriginCur,
            MultipleTree<RawTypeCanInParameter> source) {
        mappedParameterized.forEach((brothers, indexOfThisNode) -> {
            // 有一个坏处, brother加辈分, 就会无限循环下去
            MultipleTree<RawTypeCanInParameter> node = brothers.get(indexOfThisNode);
            int indexOfGenericDefine = ArrayUtil.indexOf(finalOriginCur.genericDefines, gd -> {
                // Name 的 值的比较是不好的,
                return Objects.equals(gd.name, node.getValue().getName());
            });
            if (indexOfGenericDefine == -1) {
                return;
            }
            MultipleTree<RawTypeCanInParameter> childToMap = source.getChild(indexOfGenericDefine);
            brothers.set(indexOfThisNode, childToMap);
        });
    }

    @Override
    public void assign(Parameterized from) {
        if (to == null) {
            // 总是正确, 即认为没有要求
            return;
        }
        if (from == null) {
            throw FakeCanNotAssignUtil.throwExp("can not ensure from name for null", "can not 匹配 for from 是 null");
        }
        if (from.getRawType() != to.getRawType()) {
            System.out.printf("[%02d]assign: from '%s' to '%s'%n", Thread.currentThread().getStackTrace().length,
                    from.toStringValue(), to.toStringValue()
            );
        }
        if (to.getRawType() instanceof GenericDefineReference) {
            // 循环(逻辑), 递归(实际), 一直引导到type是TempRawType为止
            throw new CompilerException("由factory制造的, 就不应该有这种问题");
        } else if (from.getRawType() instanceof GenericDefineReference) {
            throw new CompilerException("参数错误, 这个参数不应该在这里出现");
        } else if (bothStructure(from)) {
            TempStructure fromRawType = (TempStructure) from.getRawType();
            StructureUpperPathFideIterator pathFideIterator = new StructureUpperPathFideIterator(
                    fromRawType, (TempStructure) to.getRawType());
            boolean successful = false;
            while (pathFideIterator.hasNext()) {
                Stack<Integer> next = pathFideIterator.next();
                if (next == null) {
                    throw FakeCanNotAssignUtil.throwExp(fromRawType.name, "没有找到");
                }
                Level levelUsing = Level.decide(from.level, to.level);
                // to.raw 到 from.raw 的 一条 path 出来了
                //  遍历所有path, 吗? 找一个什么出来? 找一个? 找全?
                // 答: 找全, 因为找的时候是rawType找的, 不知道parameter怎么样只能全部找
                // 不过可以找到一个之后, 马上进行检查, 然后马上break, 这样就不会出现效率低的问题了
                // 但是这样不解耦, 不分层了
                Parameterized mappedParameterized = map(fromRawType, next, levelUsing);
                successful = FakeCanNotAssignUtil.catchExp(
                        () -> mappedStrictAssignWithDefine(from, mappedParameterized),
                        (BiConsumer<String, String>) null
                );
                if (successful) {
                    break;
                }
            }
            if (!successful) {
                throw FakeCanNotAssignUtil.throwExp(fromRawType.name, "can not assign");
            }
        } else {
            throw new CompilerException("Unknown type");
        }
    }

    /**
     * 对映射的parametrized严格比较
     */
    private void mappedStrictAssignWithDefine(Parameterized fromType, Parameterized mappedParameterized) {
        Level levelUsing = mappedParameterized.level;
        // mappedTree = fromTree
        LinkedList<TypeTreePair> queue = new LinkedList<>();
        queue.add(new TypeTreePair(0, mappedParameterized.getType(), fromType.getType()));
        while (!queue.isEmpty()) {
            TypeTreePair first = queue.removeFirst();
            // raw type 比较
            if (!first.typeSame()) {
                throw FakeCanNotAssignUtil.throwExp(
                        first.getFrom().getValue().getName(),
                        String.format("不匹配: from %s to %sat %d. msg: from sequence'%s', mapped sequence '%s'",
                                toStringValue(first.getFrom()), to.toStringValue(), first.indexOfFrom, first.getFrom(),
                                first.getMapped()
                        )
                );
            }
            matchQueueTask(first, queue);
        }

    }

    private void matchQueueTask(TypeTreePair first, LinkedList<TypeTreePair> queue) {
        MultipleTree<RawTypeCanInParameter> mapped = first.getMapped();
        MultipleTree<RawTypeCanInParameter> from = first.getFrom();
        for (int i = 0; i < mapped.childrenSize(); i++) {
            MultipleTree<RawTypeCanInParameter> mappedChild = mapped.getChild(i);
            if (i == from.childrenSize()) {
                // from 拿不出来了
                // 说明 from 不足了, 只有mappedChild是multiple才行
                if (isMultipleGeneric(mappedChild)) {
                    if (i + 1 != mapped.childrenSize()) {
                        // 不是最后一个
                        throw new CompilerException("multiple在generic define中必须是最后一个");
                    }
                    // 是最后一个, 正常通过
                } else {
                    throw FakeCanNotAssignUtil.throwExp(
                            from.getChild(from.childrenSize() - 1).getValue().getName(),
                            "except more to match parameterized"
                    );
                }
            } else if (i + 1 == mapped.childrenSize()) {
                // 是最后一个了
                matchLastMayMultiple(mappedChild, i, from, queue);
            } else {
                queue.add(new TypeTreePair(i, mappedChild, from.getChild(i)));
            }
        }
    }

    private void matchLastMayMultiple(
            MultipleTree<RawTypeCanInParameter> mappedChild,
            int mappedChildIndex,
            MultipleTree<RawTypeCanInParameter> from,
            LinkedList<TypeTreePair> queue) {

        if (isMultipleGeneric(mappedChild)) {
            // 后面0个或多个fromType统统匹配到mappedChild上
            for (int j = mappedChildIndex; j < from.childrenSize(); j++) {
                queue.add(new TypeTreePair(j, mappedChild, from.getChild(j)));
            }
            return;
        }
        // 不是... 那就只能严格匹配
        if (mappedChildIndex + 1 == from.childrenSize()) {
            // 长度刚好
            queue.add(new TypeTreePair(mappedChildIndex, mappedChild, from.getChild(mappedChildIndex)));
            return;
        }
        // 长度不对
        // 过大? 过小?
        if (mappedChildIndex + 1 > from.childrenSize()) {
            // from 没有了, 不足
            throw FakeCanNotAssignUtil.throwExp(
                    from.getChild(from.childrenSize() - 1).getValue().getName(),
                    "except more to match parameterized"
            );
        } else {
            // from 还剩不止一个, 太多了
            throw FakeCanNotAssignUtil.throwExp(
                    from.getChild(mappedChildIndex).getValue().getName(),
                    "too much to match parameterized"
            );
        }
    }

    private boolean isMultipleGeneric(MultipleTree<RawTypeCanInParameter> tree) {
        RawTypeCanInParameter value = tree.getValue();
        if (value instanceof GenericDefineReference) {
            return ((GenericDefineReference) value).getGeneric().multiple;
        }
        return false;
    }

    private boolean bothStructure(Parameterized from) {
        return to.getRawType() instanceof TempStructure && from.getRawType() instanceof TempStructure;
    }

    private Parameterized map(TempStructure fromStructure, Stack<Integer> path, Level levelUsing) {
        // path不可能为空
        if (path.empty()) {
            throw new CompilerException("不可能为空");
        }
        if (path.size() == 1) {
            return getUpper(fromStructure, path.pop()).defaultParameter();
        }
        TempStructure originCur = getUpper(fromStructure, path.pop());
        // mappedParameterized. raw 一定是from
        MultipleTree<RawTypeCanInParameter> mappedParameterized = null;
        Level level = Level.decide(levelUsing, originCur.level);
        while (!path.empty()) {
            TempStructure top = getUpper(fromStructure, path.pop());
            level = Level.decide(level, top.level);
            if (top.parent == null) {
                throw new CompilerException("if null, then can not find path");
            } else if (top.parent.getRawType() == originCur) {
                originCur = (TempStructure) top.parent.getRawType();
                if (mappedParameterized == null) {
                    mappedParameterized = top.parent.getType().cloneThis();
                    continue;
                }
                // mappedParameterized 遍历, 找到GenericDefine in originCur.GenericDefines的, 用 top 的 parent的Parameterized替换
                MultipleTree<RawTypeCanInParameter> source = top.parent.getType();
                TempStructure finalOriginCur = originCur;
                rebuildParameterized(mappedParameterized, finalOriginCur, source);
            } else {
                throw new CompilerException("找不到匹配路径的父类");
            }
            // origin 的 所有 涉及 top 的 generic define 的统统
        }
        // TODO 是比较合适的吗?
        Parameterized parameterized = new Parameterized(mappedParameterized);
        parameterized.level = level;
        return parameterized;
    }

    @Deprecated
    private Parameterized map(Stack<TempStructure> path, Level levelUsing) {
        // path不可能为空
        if (path.empty()) {
            throw new CompilerException("不可能为空");
        }
        if (path.size() == 1) {
            return path.pop().defaultParameter();
        }
        TempStructure originCur = path.pop();
        // mappedParameterized. raw 一定是from
        MultipleTree<RawTypeCanInParameter> mappedParameterized = null;
        Level level = Level.decide(levelUsing, originCur.level);
        while (!path.empty()) {
            TempStructure top = path.pop();
            level = Level.decide(level, top.level);
            if (top.parent == null) {
                throw new CompilerException("if null, then can not find path");
            } else if (top.parent.getRawType() == originCur) {
                originCur = (TempStructure) top.parent.getRawType();
                if (mappedParameterized == null) {
                    mappedParameterized = top.parent.getType().cloneThis();
                    continue;
                }
                // mappedParameterized 遍历, 找到GenericDefine in originCur.GenericDefines的, 用 top 的 parent的Parameterized替换
                MultipleTree<RawTypeCanInParameter> source = top.parent.getType();
                TempStructure finalOriginCur = originCur;
                rebuildParameterized(mappedParameterized, finalOriginCur, source);
            } else {
                throw new CompilerException("找不到匹配路径的父类");
            }
            // origin 的 所有 涉及 top 的 generic define 的统统
        }
        // TODO 是比较合适的吗?
        Parameterized parameterized = new Parameterized(mappedParameterized);
        parameterized.level = level;
        return parameterized;
    }

    @Override
    public void assign(TempGenericDefine from) {
        if (to == null) {
            // 总是正确, 即认为没有要求
            return;
        }
        if (from == null) {
            throw FakeCanNotAssignUtil.throwExp("from name is null", "can not assign");
        }
        if (from.multiple) {
            throw FakeCanNotAssignUtil.throwExp(
                    from.name, String.format("can't assign a value because '%s' is indefinite, " +
                                             "and '%s' is parameterized and fixed-length, " +
                                             "which doesn't fit each other", from.name, to.getRawType().getName()));
        }
        // 存在一个from的upper使得能够assign
        // generic define assign 到 parameterized type 上, 不需要考虑 from.lower
        boolean successful = false;
        if (from.parent != null) {
            successful = FakeCanNotAssignUtil.catchExp(() -> assign(from.parent), (BiConsumer<String, String>) null);
        }
        if (successful) {
            return;
        }
        if (from.interfaces == null) {
            throw FakeCanNotAssignUtil.throwExp(from.name, "can not assign");
        }
        for (Parameterized each : from.interfaces) {
            successful = FakeCanNotAssignUtil.catchExp(() -> {
                assign(each);
            }, (BiConsumer<String, String>) null);

            if (successful) {
                break;
            }
        }
        if (!successful) {
            throw FakeCanNotAssignUtil.throwExp(from.name, "can not assign");
        }
    }

    private static class TypeTreePair extends
            Pair<MultipleTree<RawTypeCanInParameter>, MultipleTree<RawTypeCanInParameter>> {

        public final int indexOfFrom;

        public TypeTreePair(
                int indexOfFrom, MultipleTree<RawTypeCanInParameter> mapped, MultipleTree<RawTypeCanInParameter> from) {
            super(mapped, from);
            this.indexOfFrom = indexOfFrom;
        }

        public MultipleTree<RawTypeCanInParameter> getMapped() {
            return getKey();
        }

        public MultipleTree<RawTypeCanInParameter> getFrom() {
            return getValue();
        }

        public boolean typeSame() {
            RawTypeCanInParameter to = getMapped().getValue();
            RawTypeCanInParameter from = getFrom().getValue();
            Class<? extends RawTypeCanInParameter> sameClass = to.getClass();
            if (sameClass != from.getClass()) {
                return false;
            }
            if (to instanceof TempStructure) {
                return to == from;
            }
            if (to instanceof GenericDefineReference) {
                TempGenericDefine toGeneric = ((GenericDefineReference) to).getGeneric();
                TempGenericDefine fromGeneric = ((GenericDefineReference) from).getGeneric();
                return FakeCanNotAssignUtil.catchExp(
                        () -> AssignableFactory.create(toGeneric).assign(fromGeneric),
                        (BiConsumer<String, String>) null
                );
            } else {
                throw new CompilerException("Unknown type");
            }
        }
    }
}