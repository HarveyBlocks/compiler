package org.harvey.compiler.type.generic.register.entity;

import lombok.Getter;
import lombok.Setter;
import org.harvey.compiler.exception.analysis.AnalysisTypeException;
import org.harvey.compiler.exception.self.UnsupportedOperationException;
import org.harvey.compiler.execute.expression.ReferenceElement;
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
@Setter
public class FullLinkType implements EndType {
    private final ReferenceElement reference;
    private final List<CanParameterType> params ;
    private FullLinkType innerType = null;
    private FullLinkType outerType = null;

    public FullLinkType(ReferenceElement reference) {
        this.reference = reference;
        this.params = new LinkedList<>();
        this.innerType = null;
        this.outerType = null;
    }

    public static FullLinkType link(TypeTreeNode outer, TypeTreeNode inner) {
        if (!(outer instanceof FullLinkType)) {
            throw new AnalysisTypeException(outer.getPosition(), "expect be a identifier type");
        }
        if (!(inner instanceof FullLinkType)) {
            throw new AnalysisTypeException(inner.getPosition(), "expect be a identifier type");
        }
        FullLinkType fullLinkOuter = (FullLinkType) outer;
        FullLinkType fullLinkInner = (FullLinkType) inner;
        if (!fullLinkOuter.innermost()) {
            throw new AnalysisTypeException(outer.getPosition(), "expect no inner type");
        }
        FullLinkType outermostOfInner = fullLinkInner.toOutermost();
        fullLinkOuter.innerType = outermostOfInner;
        outermostOfInner.outerType = fullLinkOuter;
        return fullLinkInner;

    }

    public static TypeTreeNode addParams(TypeTreeNode target, List<CanParameterType> params) {
        if (target instanceof FullLinkType) {
            FullLinkType fullnameTarget = (FullLinkType) target;
            fullnameTarget.addAllParam(params);
            return fullnameTarget;
        } else {
            throw new AnalysisTypeException(target.getPosition(), "expect be a identifier type");
        }
    }


    public ReferenceElement getReference() {
        if (reference == null) {
            throw new UnsupportedOperationException("hasn't referred");
        }
        return reference;
    }

    @Override
    public SourcePosition getPosition() {
        return reference.getPosition();
    }

    public void addAllParam(List<CanParameterType> param) {
        params.addAll(param);
    }

    public boolean outermost() {
        return outerType == null;
    }

    public boolean innermost() {
        return innerType == null;
    }

    public FullLinkType toOutermost() {
        FullLinkType cur = this;
        while (!cur.outermost()) {
            cur = cur.getOuterType();
        }
        return cur;
    }
}
