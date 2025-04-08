package org.harvey.compiler.type.basic.test5;

import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.exception.self.CompilerException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * 并非找一条, 而是把所有相关的路径都找出来
 *
 * @date 2025-03-31 22:05
 * @author  <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 */
@SuppressWarnings("DuplicatedCode")
class StructureUpperPathFideIterator implements Iterator<LinkedList<Pair<TempStructure, Integer>>> {
    private final TempStructure toRawType;
    private final LinkedList<LinkedList<Pair<TempStructure, Integer>>> pathQueue = new LinkedList<>();


    public StructureUpperPathFideIterator(TempStructure fromRawType, TempStructure toRawType) {
        this.toRawType = toRawType;
        pathQueue.addLast(new LinkedList<>());
        pathQueue.getLast().addLast(new Pair<>(fromRawType, null));
    }

    public static Parameterized upper(TempStructure structure, Integer upperReference) {
        Parameterized targetUpper = referenceMap(structure, upperReference);
        if (targetUpper.getRawType() instanceof TempStructure) {
            return targetUpper;
        } else {
            throw new CompilerException("Illegal type");
        }
    }

    private static Parameterized referenceMap(TempStructure structure, Integer upperReference) {
        if (upperReference == null) {
            throw new CompilerException("Upper reference can not be null");
        }
        if (upperReference == -1) {
            return structure.parent;
        } else {
            return structure.interfaces[upperReference];
        }
    }

    @Override
    public void remove() {
        throw new CompilerException(new UnsupportedOperationException());
    }

    @Override
    public void forEachRemaining(Consumer<? super LinkedList<Pair<TempStructure, Integer>>> action) {
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
     * @return 可能返回null, 那是真的没有了. 在list里不会有null,
     *      integer, -1 表示 parent,
     *      否则表示 interface in interfaces
     *      对于list可能是empty的, 说明, from 就是 to
     *      list 的 front 是 lower, back 是 upper
     */
    @Override
    public LinkedList<Pair<TempStructure, Integer>> next() {
        while (!pathQueue.isEmpty()) {
            LinkedList<Pair<TempStructure, Integer>> first = pathQueue.removeFirst();
            TempStructure last = first.getLast().getKey();
            if (last == toRawType) {
                return new LinkedList<>(first);
            }
            // 由于找的都是interfaces, 所以没有parents
            if (last.parent != null) {
                LinkedList<Pair<TempStructure, Integer>> path = new LinkedList<>(first);
                path.addLast(new Pair<>(getTempStructure(last.parent.getRawType()), -1));
                pathQueue.addLast(path);
            }
            Parameterized[] interfaces = last.interfaces;
            if (interfaces == null) {
                continue;
            }
            for (int i = 0; i < interfaces.length; i++) {
                LinkedList<Pair<TempStructure, Integer>> path = new LinkedList<>(first);
                path.addLast(new Pair<>(getTempStructure(interfaces[i].getRawType()), i));
                pathQueue.addLast(path);
            }
        }
        return null;
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
}
