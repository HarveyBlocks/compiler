package org.harvey.compiler.declare.define;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.common.util.ExceptionUtil;
import org.harvey.compiler.declare.analysis.*;
import org.harvey.compiler.declare.identifier.DIdentifierPoolFactory;
import org.harvey.compiler.exception.analysis.AnalysisDeclareException;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.type.generic.GenericFactory;
import org.harvey.compiler.type.generic.RawType;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * 用于完成对Declare的字段的引用转换
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-03 13:06
 */
@Getter
@AllArgsConstructor
public class FieldDefinition implements Definition {

    private final AccessControl permissions;
    private final Embellish embellish;
    private final Pair<RawType, SourceTextContext> type;
    private final List<Pair<ReferenceElement, SourceTextContext>> identifierMap;

    @Override
    public ReferenceElement getIdentifierReference() {
        throw new CompilerException("call getIdentifierMap instead", new UnsupportedOperationException());
    }

    public static class Builder {
        private final DIdentifierPoolFactory identifierPoolFactory;
        private final Environment environment;
        private AccessControl permissions;
        private Embellish embellish;
        private Pair<RawType, SourceTextContext> type;
        private List<Pair<ReferenceElement, SourceTextContext>> identifierMap;

        public Builder(DIdentifierPoolFactory identifierPoolFactory, Environment environment) {
            this.identifierPoolFactory = identifierPoolFactory;
            this.environment = environment;
            if (environment == Environment.FILE) {
                throw new CompilerException("environment can not be file");
            }
        }

        public Builder permission(SourceTextContext source) {
            this.permissions = AccessControls.buildAccessControl(environment, source);
            return this;
        }

        public Builder embellish(EmbellishSource source) {
            DetailedDeclarationType onDefault = DetailedDeclarationType.onDefaultForMember(2, environment);
            DetailedDeclarationType onIllegal = DetailedDeclarationType.onIllegalInStructure(1);
            embellish = Embellish.create(onDefault, onIllegal, source, false, false);
            return this;
        }

        public Builder type(SourceTextContext type) {
            ListIterator<SourceString> iterator = type.listIterator();
            this.type = GenericFactory.skipSourceForUse(iterator);
            if (iterator.hasNext()) {
                throw new AnalysisDeclareException(iterator.next().getPosition(), "expected identifier");
            }
            return this;
        }

        public Builder assignMaps(ListIterator<SourceString> iterator) {
            ExceptionUtil.iteratorHasNext(iterator, "assignMaps can not be empty");
            this.identifierMap = new ArrayList<>();
            while (iterator.hasNext()) {
                SourceTextContext assignMap = SourceTextContext.skipUntilComma(iterator);
                if (assignMap.isEmpty()) {
                    throw new AnalysisDeclareException(iterator.next().getPosition(), "can not be empty");
                }
                SourceString mayIdentifier = assignMap.get(0);
                if (mayIdentifier.getType() != SourceType.IDENTIFIER) {
                    throw new AnalysisDeclareException(mayIdentifier.getPosition(), "expect identifier");
                }
                ReferenceElement reference = identifierPoolFactory.addIdentifier(
                        DetailedDeclarationType.FIELD,
                        mayIdentifier.getValue(),
                        mayIdentifier.getPosition()
                );
                identifierMap.add(new Pair<>(reference, assignMap));
                if (!iterator.hasNext()) {
                    break;
                }
                if (!Definition.skipIf(iterator, Operator.COMMA)) {
                    throw new AnalysisDeclareException(iterator.next().getPosition(), "expected ,");
                }
                if (!iterator.hasNext()) {
                    throw new AnalysisDeclareException(iterator.previous().getPosition(), "not expected empty");
                }
            }
            return this;
        }

        public Builder noMore(ListIterator<SourceString> iterator) {
            if (iterator.hasNext()) {
                throw new AnalysisDeclareException(iterator.next().getPosition(), "expect ;");
            }
            return this;
        }

        public FieldDefinition build() {
            valid();
            return new FieldDefinition(permissions, embellish, type, identifierMap);
        }

        private void valid() {
            Definition.notNullValid(permissions, "permissions");
            Definition.notNullValid(embellish, "embellish");
            Definition.notNullValid(type, "type");
            Definition.notNullValid(identifierMap, "identifierMap");
        }


    }
}
