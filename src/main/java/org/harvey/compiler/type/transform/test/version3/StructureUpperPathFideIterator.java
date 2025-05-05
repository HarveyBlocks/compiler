package org.harvey.compiler.type.transform.test.version3;

import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.exception.self.CompilerException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import java.util.function.Consumer;

/**
 * 并非找一条, 而是把所有相关的路径都找出来
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-31 22:05
 */
class StructureUpperPathFideIterator implements Iterator<Stack<Integer>> {
    private final TempStructure toRawType;
    private final LinkedList<Stack<Pair<TempStructure, Integer>>> pathQueue = new LinkedList<>();

    public StructureUpperPathFideIterator(TempStructure fromRawType, TempStructure toRawType) {
        this.toRawType = toRawType;
        pathQueue.addLast(new Stack<>());
        pathQueue.getFirst().push(new Pair<>(fromRawType, 0));
    }

    public static TempStructure upper(TempStructure structure, Integer upperReference) {
        RawTypeCanInParameter targetUpper = referenceMap(structure, upperReference);
        if (targetUpper instanceof TempStructure) {
            return (TempStructure) targetUpper;
        } else {
            throw new CompilerException("Illegal type");
        }
    }

    private static RawTypeCanInParameter referenceMap(TempStructure structure, Integer upperReference) {
        if (upperReference == null) {
            throw new CompilerException("Upper reference can not be null");
        }
        if (upperReference == -1) {
            return structure.parent.getRawType();
        } else {
            return structure.interfaces[upperReference].getRawType();
        }
    }

    private static TempStructure getTempStructure(RawTypeCanInParameter parameter) {
        if (parameter instanceof GenericDefineReference) {
            throw new CompilerException("不允许 generic define 在 parent 作为 raw type");
        } else if (parameter instanceof TempStructure) {
            return (TempStructure) parameter;
        } else {
            throw new CompilerException("Unknown type");
        }
    }

    @Override
    public void remove() {
        throw new CompilerException(new UnsupportedOperationException());
    }

    @Override
    public void forEachRemaining(Consumer<? super Stack<Integer>> action) {
        throw new CompilerException(new UnsupportedOperationException());
    }

    /**
     * @return 并不是真正的hasNext, 返回true不一定有, 返回false是一定没有了
     */
    @Override
    public boolean hasNext() {
        return !pathQueue.isEmpty();
    }

    /**
     * @return 可能返回null, 那是真的没有了. 在stack里不会有null,
     * integer, -1 表示 parent,
     * 否则表示 interface in interfaces
     */
    @Override
    public Stack<Integer> next() {
        while (!pathQueue.isEmpty()) {
            Stack<Pair<TempStructure, Integer>> first = pathQueue.removeFirst();
            TempStructure top = first.peek().getKey();
            if (top == toRawType) {
                return CollectionUtil.cloneStack(first, Pair::getValue);
            }
            // 由于找的都是interfaces, 所以没有parents
            if (top.parent != null) {
                Stack<Pair<TempStructure, Integer>> path = CollectionUtil.cloneStack(first);
                path.push(new Pair<>(getTempStructure(top.parent.getRawType()), -1));
                pathQueue.addLast(path);
            }
            Parameterized[] interfaces = top.interfaces;
            if (interfaces == null) {
                continue;
            }
            for (int i = 0; i < interfaces.length; i++) {
                Stack<Pair<TempStructure, Integer>> path = CollectionUtil.cloneStack(first);
                path.push(new Pair<>(getTempStructure(interfaces[i].getRawType()), i));
                pathQueue.addLast(path);
            }
        }
        return null;
    }
}
