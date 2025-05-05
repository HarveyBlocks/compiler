package org.harvey.compiler.syntax;

import lombok.Getter;
import org.harvey.compiler.execute.calculate.Associativity;
import org.harvey.compiler.execute.calculate.OperandCount;
import org.harvey.compiler.execute.test.version1.element.CompileOperatorString;
import org.harvey.compiler.execute.test.version1.element.OperatorString;
import org.harvey.compiler.execute.test.version1.element.TypeString;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-03 23:57
 */
@Getter
public class CastOperator implements OperatorString {

    public static final CompileOperatorString.CompileOperator OPERATOR = CompileOperatorString.CompileOperator.CAST;
    private final TypeString type;


    public CastOperator(TypeString type) {

        this.type = type;
    }

    @Override
    public int getPriority() {
        return OPERATOR.getPriority();
    }

    @Override
    public Associativity getAssociativity() {
        return OPERATOR.getAssociativity();
    }

    @Override
    public OperandCount getOperandCount() {
        return OPERATOR.getOperandCount();
    }

    @Override
    public boolean isPost() {
        return false;
    }

    @Override
    public boolean isPre() {
        return false;
    }

    @Override
    public OperatorString pair() {
        return null;
    }

    @Override
    public boolean operatorEquals(OperatorString string) {
        return string instanceof CastOperator;
    }

    @Override
    public SourcePosition getPosition() {
        return type.getPosition();
    }
}
