package org.harvey.compiler.type.generic.register.entity;

import lombok.Getter;
import org.harvey.compiler.exception.analysis.AnalysisTypeException;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.LinkedList;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-04 15:35
 */
@Getter
public class BoundsType implements CanParameterType {
    private final SourcePosition position;
    // 有必要换成FullnameType, 因为上下界似乎不能在basic type上起效
    private final List<FullLinkType> uppers = new LinkedList<>();
    private FullLinkType lower;

    public BoundsType(SourcePosition position) {
        this.position = position;
    }

    public static TypeTreeNode boundLower(TypeTreeNode target, TypeTreeNode bound) {
        if (!(target instanceof BoundsType)) {
            throw new AnalysisTypeException(target.getPosition(), "expect ignore identifier for register bounds");
        }
        BoundsType boundsType = (BoundsType) target;
        if (bound instanceof UnionType) {
            throw new AnalysisTypeException(
                    bound.getPosition(), "unexpected type to be a bound: " + boundsType.getClass().getSimpleName());
        } else if (bound instanceof FullLinkType) {
            boundsType.setLower(((FullLinkType) bound).toOutermost());
        } else {
            throw new AnalysisTypeException(
                    bound.getPosition(), "unexpected type to be a bound: " + boundsType.getClass().getSimpleName());
        }

        return boundsType;
    }

    public static TypeTreeNode boundUpper(TypeTreeNode target, TypeTreeNode bound) {
        if (!(target instanceof BoundsType)) {
            throw new AnalysisTypeException(target.getPosition(), "expect ignore identifier for register bounds");
        }
        BoundsType boundsType = (BoundsType) target;
        if (bound instanceof UnionType) {
            for (FullLinkType type : ((UnionType) bound).getTypes()) {
                boundsType.addUpper(type);
            }
        } else if (bound instanceof FullLinkType) {
            boundsType.addUpper((FullLinkType) bound);
        } else {
            throw new AnalysisTypeException(
                    bound.getPosition(), "unexpected type to be a bound: " + boundsType.getClass().getSimpleName());
        }
        return boundsType;
    }

    public void setLower(FullLinkType lower) {
        if (this.lower != null) {
            throw new AnalysisTypeException(lower.getPosition(), "has set lower");
        }
        this.lower = lower;
    }

    public void addUpper(FullLinkType type) {
        uppers.add(type);
    }


    public void addAllUppers(LinkedList<FullLinkType> uppers) {
        this.uppers.addAll(uppers);
    }
}
