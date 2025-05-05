package org.harvey.compiler.type.generic.register.entity;

import lombok.Getter;
import org.harvey.compiler.exception.analysis.AnalysisTypeException;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.LinkedList;
import java.util.List;

/**
 * Type & Type 这样的类型, 方便构建
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 15:35
 */
@Getter
public class UnionType implements TypeTreeNode {
    private final List<FullLinkType> types = new LinkedList<>();

    public UnionType(FullLinkType a, FullLinkType b) {
        types.add(a);
        types.add(b);
    }

    public static UnionType union(TypeTreeNode a, TypeTreeNode b) {
        if (a instanceof UnionType && b instanceof UnionType) {
            UnionType result = (UnionType) a;
            result.types.addAll(((UnionType) b).types);
            return result;
        } else if (a instanceof UnionType && b instanceof FullLinkType) {
            UnionType result = (UnionType) a;
            result.types.add(((FullLinkType) b).toOutermost());
            return result;
        } else if (a instanceof FullLinkType && b instanceof UnionType) {
            UnionType result = (UnionType) b;
            result.types.add(((FullLinkType) a).toOutermost());
            return result;
        } else if (a instanceof FullLinkType && b instanceof FullLinkType) {
            return new UnionType(((FullLinkType) a).toOutermost(), ((FullLinkType) b).toOutermost());
        } else {
            throw new AnalysisTypeException(a.getPosition(), "expect identifier type");
        }
    }

    @Override
    public SourcePosition getPosition() {
        return null;
    }
}
