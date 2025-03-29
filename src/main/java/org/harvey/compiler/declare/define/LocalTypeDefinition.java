package org.harvey.compiler.declare.define;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.declare.analysis.Embellish;
import org.harvey.compiler.declare.analysis.EmbellishSource;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.execute.expression.ExpressionElement;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.type.generic.GenericFactory;

import java.util.ListIterator;

/**
 * 要转换成{@link org.harvey.compiler.execute.local.LocalType}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-13 16:07
 */
@AllArgsConstructor
@Getter
public class LocalTypeDefinition {
    private final SourcePosition markConst;
    private final SourcePosition markFinal;
    private ExpressionElement rawType;
    private SourceTextContext typeParameter;

    public boolean isMarkFinal() {
        return markFinal != null;
    }

    public SourcePosition getPosition() {
        return rawType.getPosition();
    }

    public static class Builder {
        private SourcePosition markConst;
        private SourcePosition markFinal;
        private ExpressionElement rawType;
        private SourceTextContext typeParameter;

        public Builder embellish(EmbellishSource embellish) {
            embellish.illegalOn("param", Embellish.EmbellishWord.STATIC, Embellish.EmbellishWord.SEALED,
                    Embellish.EmbellishWord.ABSTRACT
            );
            this.markConst = embellish.getConstMark();
            this.markFinal = embellish.getStaticMark();
            return this;
        }

        public Builder type(ListIterator<SourceString> typeIterator) {
            Pair<ExpressionElement, SourceTextContext> typePair = GenericFactory.skipSourceForUse(typeIterator);
            this.rawType = typePair.getKey();
            this.typeParameter = typePair.getValue();
            return this;
        }

        public LocalTypeDefinition build() {
            if (rawType == null) {
                throw new CompilerException("not complete");
            }
            return new LocalTypeDefinition(markConst, markFinal, rawType, typeParameter);
        }
    }
}
