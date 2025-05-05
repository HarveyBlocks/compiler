package org.harvey.compiler.syntax;

import lombok.Getter;
import org.harvey.compiler.exception.self.UnsupportedOperationException;
import org.harvey.compiler.execute.calculate.Associativity;
import org.harvey.compiler.execute.calculate.OperandCount;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.test.version1.element.OperatorString;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-01 16:16
 */
@Getter
public class UncertainOperatorString implements OperatorString {
    private final Operator[] operators;
    private final SourcePosition position;

    public UncertainOperatorString(SourcePosition position, Operator[] operators) {
        this.operators = operators;
        this.position = position;
    }

    @Override
    public int getPriority() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Associativity getAssociativity() {
        throw new UnsupportedOperationException();
    }

    @Override
    public OperandCount getOperandCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String show() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isPost() {
        return false;
    }

    @Override
    public boolean isPre() {
        throw new UnsupportedOperationException();
    }

    @Override
    public OperatorString pair() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean operatorEquals(OperatorString string) {
        throw new UnsupportedOperationException();
    }
}
