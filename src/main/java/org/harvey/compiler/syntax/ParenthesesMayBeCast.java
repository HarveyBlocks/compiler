package org.harvey.compiler.syntax;

import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Associativity;
import org.harvey.compiler.execute.calculate.OperandCount;
import org.harvey.compiler.execute.test.version1.element.CompileOperatorString;
import org.harvey.compiler.execute.test.version1.element.ItemString;
import org.harvey.compiler.execute.test.version1.element.OperatorString;
import org.harvey.compiler.io.source.SourcePosition;

/**
 * TODO
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-05-02 15:07
 */
@Deprecated
public class ParenthesesMayBeCast extends DefaultAbstractSyntaxTree implements MayBeOperatorWhileErrorItem {
    private boolean asOperator = false;

    public ParenthesesMayBeCast(SourcePosition position, ItemString type) {
        super(new CompileOperatorString(position, CompileOperatorString.CompileOperator.CAST));
        setType(type);
    }

    public void setType(ItemString type) {
        super.setLeft(type);
    }

    public ItemString getExpression() {
        return getLeft();
    }

    @Override
    public int getPriority() {
        return super.getOperator().getPriority();
    }

    @Override
    public Associativity getAssociativity() {
        return super.getOperator().getAssociativity();
    }

    @Override
    public OperandCount getOperandCount() {
        return super.getOperator().getOperandCount();
    }

    @Override
    public boolean isPost() {
        return super.getOperator().isPost();
    }

    @Override
    public boolean isPre() {
        return super.getOperator().isPre();
    }

    @Override
    public OperatorString pair() {
        return super.getOperator().pair();
    }

    @Override
    public boolean operatorEquals(OperatorString string) {
        return super.getOperator().operatorEquals(string);
    }

    @Override
    public DefaultAbstractSyntaxTree becomeTreeAsOperator() {
        this.asOperator = true;
        return this;
    }

    @Override
    public ItemString becomeItem() {
        if (this.asOperator) {
            throw new CompilerException("expect invoke becomeTreeAsOperator");
        }
        return getExpression();
    }

    @Override
    public boolean asOperator() {
        return asOperator;
    }


}
