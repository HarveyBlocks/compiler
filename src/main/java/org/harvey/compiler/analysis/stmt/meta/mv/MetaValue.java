package org.harvey.compiler.analysis.stmt.meta.mv;

import lombok.Getter;
import lombok.ToString;
import org.harvey.compiler.analysis.core.AccessControl;
import org.harvey.compiler.analysis.core.Keyword;
import org.harvey.compiler.analysis.core.Keywords;
import org.harvey.compiler.analysis.stmt.meta.MetaIdentifier;
import org.harvey.compiler.analysis.text.type.*;
import org.harvey.compiler.common.entity.SourceString;
import org.harvey.compiler.common.entity.SourceStringType;
import org.harvey.compiler.exception.CompilerException;

/**
 * 不显示作用域, 作用域取决于这个对象存在哪里, 存在局部变量表就是在方法里, 存在全局上下文就是全局范围
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-21 15:45
 */
@Getter
@ToString(callSuper = true)
public abstract class MetaValue extends MetaIdentifier {
    protected SourceType type;
    /**
     * 包含变量名和赋值符号
     */

    protected boolean embellishConst;
    protected boolean embellishFinal;
    protected AccessControl accessControl;


    protected MetaValue() {
        super();
        this.type = null;
        this.accessControl = null;
        this.embellishConst = false;
        this.embellishFinal = false;
    }


    protected abstract static class Builder<Product extends MetaValue, Child extends Builder> {
        protected final Product product;
        private final Child child;

        protected Builder(Product product) {
            this.product = product;
            child = (Child) this;
        }

        public Child identifier(SourceString identifier) {
            if (identifier.getType() != SourceStringType.IDENTIFIER) {
                throw new CompilerException("Identifier is needed, but not: " + identifier.getType());
            }
            product.identifier = identifier.getValue();
            product.declare = identifier.getPosition();
            return child;
        }


        public void type(SourceType type) {
            product.type = type;
        }

        public Child accessControl(AccessControl accessControl) {
            product.accessControl = accessControl;
            return child;
        }


        public Child embellishConst() {
            product.embellishConst = true;
            return child;
        }

        public Child embellishFinal() {
            product.embellishFinal = true;
            return child;
        }

        public Product build() {
            assertProductCompleted();
            return product;
        }

        private void assertProductCompleted() {
            if (product.identifier == null || product.identifier.isEmpty()) {
                throw new CompilerException("Identifier shouldn't be null or empty");
            }
            if (product.declare == null) {
                throw new CompilerException("The declare position of identifier is needed");
            }
            if (product.type == null) {
                throw new CompilerException("Need declare product type");
            }
            if (product.accessControl == null) {
                throw new CompilerException("Need declare product access control");
            }
        }
    }

}
