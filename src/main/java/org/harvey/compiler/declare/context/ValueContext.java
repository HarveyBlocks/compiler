package org.harvey.compiler.declare.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.harvey.compiler.analysis.core.AccessControl;
import org.harvey.compiler.common.Pair;
import org.harvey.compiler.declare.Embellish;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.execute.expression.Expression;
import org.harvey.compiler.io.source.SourcePosition;

import java.util.ArrayList;
import java.util.List;

import static org.harvey.compiler.declare.phaser.FileStatementContextBuilder.NOT_HAVE_IDENTIFIER;

/**
 * 不显示作用域, 作用域取决于这个对象存在哪里, 存在局部变量表就是在方法里, 存在全局上下文就是全局范围
 * context的成员可以为Empty, 但不应该为null, 否则不好序列化区分null和Empty
 * 不对! 这个一定是字段, 不可能是局部变量或者全局变量, 全局变量已经被删除了, 局部变量不会在这里被解析
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ValueContext extends DeclaredContext {
    private final List<Pair<Integer, Expression>> assignList = new ArrayList<>();
    private Expression type;

    @Override
    public int getIdentifierReference() {
        // TODO throw new CompilerException(new UnsupportedOperationException());
        System.err.println(new UnsupportedOperationException("get identifier reference from value context"));
        return NOT_HAVE_IDENTIFIER;
    }

    @Override
    public void setIdentifierReference(int identifierReference) {
        // TODO throw new CompilerException(new UnsupportedOperationException());
        System.err.println(new UnsupportedOperationException(
                "set " + identifierReference + " as identifier reference on value context"));
    }

    public void putAssign(Integer identifier, Expression assign) {
        assignList.add(new Pair<>(identifier, assign));
    }

    public int getIdentifierReference(int index) {
        return assignList.get(index).getKey();
    }

    public Expression getAssign(int index) {
        return assignList.get(index).getValue();
    }

    public static class Builder {
        private final ValueContext product;

        public Builder() {
            product = new ValueContext();
            product.identifierReference = NOT_HAVE_IDENTIFIER;
        }

        public Builder accessControl(AccessControl accessControl) {
            product.accessControl = accessControl;
            return this;
        }

        public Builder embellish(Embellish embellish) {
            product.embellish = embellish;
            return this;
        }

        public Builder type(Expression type) {
            product.type = type;
            return this;
        }

        public ValueContext build(SourcePosition position) {
            assertValid(position);
            return product;
        }

        private void assertValid(SourcePosition position) {
            DeclaredContext.assertNotNull(position, product.accessControl, "accessControl");
            DeclaredContext.assertNotNull(position, product.embellish, "embellish");
            DeclaredContext.assertNotNull(position, product.type, "type");
            if (product.identifierReference != NOT_HAVE_IDENTIFIER) {
                throw new CompilerException("Illegal Identifier reference: " + product.identifierReference);
            }
        }

    }
}
