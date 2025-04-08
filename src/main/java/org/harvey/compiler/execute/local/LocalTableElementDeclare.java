package org.harvey.compiler.execute.local;

import lombok.Getter;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-11 21:42
 */
@Getter
public class LocalTableElementDeclare extends ExpressionElement {

    private final int typeReference;
    private int start;

    public LocalTableElementDeclare(SourcePosition position, int start, int typeReference) {
        super(position);
        this.start = start;
        this.typeReference = typeReference;
    }

    public void resetStart(LambdaVariableManager lambdaVariableManager) {
        this.start = lambdaVariableManager.resetStart(start);
    }

    public LocalVariableManager.LocalVariableType getLocalVariableType() {
        if (typeReference <= LocalVariableManager.LocalVariableType.REFERENCE.ordinal()) {
            return LocalVariableManager.LocalVariableType.values()[typeReference];
        }
        return LocalVariableManager.LocalVariableType.REFERENCE;
    }

}
