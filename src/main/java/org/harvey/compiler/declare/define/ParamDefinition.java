package org.harvey.compiler.declare.define;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.declare.analysis.Declarable;
import org.harvey.compiler.declare.analysis.DeclarableFactory;
import org.harvey.compiler.declare.analysis.EmbellishSource;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * 要转化成{@link org.harvey.compiler.declare.context.ParamContext}
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-06 21:24
 */
@Getter
@AllArgsConstructor
public class ParamDefinition {
    private final LocalTypeDefinition localTypeDefinition;
    private final boolean multipleType; // 只有在参数上才有意义
    private final IdentifierString identifier;
    /**
     * can not nullable,形如 a = 1, 就不需要有`a=`的部分, empty表示没有init
     */
    private final SourceTextContext initAssign;

    public boolean hasAssign() {
        return !initAssign.isEmpty();
    }

    public SourcePosition getPosition() {
        return identifier.getPosition();
    }

    public static class Builder {

        private final LocalTypeDefinition.Builder typeBuilder = new LocalTypeDefinition.Builder();
        private boolean multipleType; // 只有在参数上才有意义
        private IdentifierString identifier;
        private SourceTextContext initAssign;

        public Builder embellish(EmbellishSource embellish) {
            typeBuilder.embellish(embellish);
            return this;
        }

        public Builder type(SourceTextContext sourceType) {
            ListIterator<SourceString> typeIterator = sourceType.listIterator();
            typeBuilder.type(typeIterator);
            this.multipleType = Definition.skipIf(typeIterator, Operator.MULTIPLE_TYPE);
            return this;
        }

        public Builder identifier(SourceString identifierSource, SourcePosition forNull) {
            if (identifierSource == null) {
                throw new AnalysisExpressionException(forNull, "expected identifier");
            }
            this.identifier = new IdentifierString(identifierSource);
            return this;
        }

        public Builder initAssign(SourceTextContext initAssign) {
            this.initAssign = initAssign;
            return this;
        }

        public ParamDefinition build() {
            if (identifier == null) {
                throw new CompilerException("genericDefine of param definition is not complete.");
            }
            LocalTypeDefinition localType = typeBuilder.build();
            return new ParamDefinition(localType, multipleType, identifier, initAssign);
        }
    }

    public static class Factory {
        private final List<ParamDefinition> params = new ArrayList<>();

        private static void permissions(SourceTextContext permissions) {
            if (permissions != null && !permissions.isEmpty()) {
                throw new AnalysisExpressionException(permissions.getFirst().getPosition(),
                        permissions.getLast().getPosition(), "permissions is illegal on param"
                );
            }
        }

        private static SourceTextContext paramInit(ListIterator<SourceString> attachmentIterator) {
            if (!Definition.skipIf(attachmentIterator, Operator.ASSIGN)) {
                return SourceTextContext.empty();
            }
            // 不认为会有什么
            SourceTextContext paramInit = SourceTextContext.skipUntilComma(attachmentIterator);
            if (paramInit.isEmpty()) {
                throw new AnalysisExpressionException(
                        attachmentIterator.previous().getPosition(), "param init can not be empty");
            }
            return paramInit;
        }

        public final List<ParamDefinition> getParams() {
            // valid
            valid();
            return params;
        }

        private void valid() {
            if (params.isEmpty()) {
                return;
            }
            ParamDefinition last = params.get(params.size() - 1);
            if (last.isMultipleType() && last.hasAssign()) {
                throw new AnalysisExpressionException(last.getPosition(), "multiple type can not init");
            }
            boolean afterFirstAssign = true;
            for (int i = params.size() - 2; i >= 0; i--) {
                ParamDefinition param = params.get(i);
                if (param.isMultipleType()) {
                    throw new AnalysisExpressionException(last.getPosition(), "only last can be multiply type");
                } else if (afterFirstAssign && !param.hasAssign()) {
                    afterFirstAssign = false;
                } else if (!afterFirstAssign && param.hasAssign()) {
                    throw new AnalysisExpressionException(param.getPosition(), "default assign should be at last");
                }

            }
        }

        /**
         * @param paramListIterator iterator.next()=='embellish'
         * @return paramListIterator, 移动到下一个, iterator.next()==','
         */
        public ListIterator<SourceString> create(ListIterator<SourceString> paramListIterator) {
            Declarable declarable = DeclarableFactory.statementBasic(paramListIterator, SourcePosition.UNKNOWN);
            // permission
            permissions(declarable.getPermissions());
            // assign
            ListIterator<SourceString> attachmentIterator = declarable.getAttachment().listIterator();
            // 只有init 表达式, 没有 a = ?
            SourceTextContext paramInit = paramInit(attachmentIterator);
            // genericDefine
            SourceTextContext sourceType = declarable.getType();
            SourcePosition forNull = sourceType.getLast().getPosition();
            ParamDefinition variableDefinition = new Builder().embellish(declarable.getEmbellish())
                    .type(sourceType)
                    .identifier(declarable.getIdentifier(), forNull)
                    .initAssign(paramInit)
                    .build();
            params.add(variableDefinition);
            return attachmentIterator;
        }
    }


}
