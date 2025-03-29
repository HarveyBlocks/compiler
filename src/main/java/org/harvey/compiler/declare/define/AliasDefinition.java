package org.harvey.compiler.declare.define;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.declare.analysis.*;
import org.harvey.compiler.declare.identifier.IdentifierPoolFactory;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.analysis.AnalysisExpressionException;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.source.SourcePosition;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.text.context.SourceTextContext;
import org.harvey.compiler.type.generic.GenericFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

/**
 * 完成Alias的Declare Identifier的引用
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-03 15:22
 */
@AllArgsConstructor
@Getter
public class AliasDefinition implements Definition {
    private final AccessControl permissions;
    private final ReferenceElement identifierReference;
    private final List<Pair<ReferenceElement, SourceTextContext>> genericDefine;
    private final SourceTextContext origin;
    private final boolean staticAlias;

    @Override
    public Embellish getEmbellish() {
        return new Embellish(staticAlias ? Embellish.EmbellishWord.STATIC.getCode() : 0);
    }

    public static class Builder {
        private final IdentifierPoolFactory factory;
        private final Environment environment;

        private AccessControl permissions;
        private ReferenceElement identifierReference;
        private List<Pair<ReferenceElement, SourceTextContext>> genericDefine = new ArrayList<>();
        private SourceTextContext origin;
        private Boolean staticAlias;


        public Builder(IdentifierPoolFactory factory, Environment environment) {
            this.factory = factory;
            this.environment = environment;

        }


        public Builder permissions(Environment environment, SourceTextContext permissions) {
            this.permissions = AccessControls.buildAccessControl(environment, permissions);
            return this;
        }

        public Builder identifierReference(IdentifierString identifier) {
            this.identifierReference = factory.add(DetailedDeclarationType.STRUCTURE, identifier.getValue(),
                    identifier.getPosition()
            );
            return this;
        }

        public Builder origin(SourceTextContext origin) {
            this.origin = origin;
            return this;
        }

        public Builder staticAlias(SourcePosition staticPosition) {
            if (environment == Environment.FILE && staticPosition != null) {
                throw new AnalysisExpressionException(staticPosition, "alias on file can not embellish static");
            }
            this.staticAlias = staticPosition != null;
            return this;
        }

        // class A<T>{
        //  alias AliasGeneric<T> = Generic<T> <-此T向前索取AliasGeneric的
        //  alias AliasGeneric2<A> = Generic2<A,T> <- 不被允许
        // public static     alias AliasGeneric2<A,B> = Generic2<A,B> <- 改进
        //
        // }

        /**
         * @param outerReferenceStack {@link Definition#mapStructureGenericReference(List, IdentifierPoolFactory, Stack)}
         *                            注意要除去自身, 自身由本Builder提供
         */
        public Builder genericDefine(SourceTextContext genericMessage, Stack<ReferenceElement> outerReferenceStack) {
            Definition.notNullValid(identifierReference, "identifier reference");
            if (environment != Environment.FILE && staticAlias == null) {
                throw new CompilerException("set 'static alias' before invoke this generic define");
            }
            ListIterator<SourceString> genericMessageIterator = genericMessage.listIterator();
            if (!GenericFactory.genericPreCheck(genericMessageIterator)) {
                return this;
            }
            List<Pair<IdentifierString, SourceTextContext>> pairs = GenericFactory.defineSourceDepart(
                    genericMessageIterator);
            Stack<ReferenceElement> referenceStack = Definition.resetReferenceStack(
                    outerReferenceStack, identifierReference, environment == Environment.FILE, staticAlias);
            this.genericDefine = Definition.mapStructureGenericReference(pairs, factory, referenceStack);
            return this;
        }

        public AliasDefinition build() {
            valid();
            return new AliasDefinition(
                    permissions, identifierReference, genericDefine, origin, Boolean.TRUE.equals(staticAlias));
        }

        private void valid() {
            Definition.notNullValid(identifierReference, "identifier reference");
            Definition.notNullValid(permissions, "permissions");
            Definition.notNullValid(genericDefine, "generic define");
            Definition.notNullValid(origin, "origin");
            Definition.notNullValid(staticAlias, "static");
        }
    }
}
