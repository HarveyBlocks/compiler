package org.harvey.compiler.declare.define;

import org.harvey.compiler.common.collecction.CollectionUtil;
import org.harvey.compiler.common.collecction.Pair;
import org.harvey.compiler.declare.analysis.AccessControl;
import org.harvey.compiler.declare.analysis.Embellish;
import org.harvey.compiler.declare.identifier.IdentifierPoolFactory;
import org.harvey.compiler.exception.self.CompilerException;
import org.harvey.compiler.execute.calculate.Operator;
import org.harvey.compiler.execute.expression.IdentifierString;
import org.harvey.compiler.execute.expression.ReferenceElement;
import org.harvey.compiler.io.source.SourceString;
import org.harvey.compiler.io.source.SourceType;
import org.harvey.compiler.text.context.SourceTextContext;

import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * 主要用于完成对Declare的Identifier的引用转换
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2025-03-03 13:07
 */
public interface Definition {
    static List<Pair<ReferenceElement, SourceTextContext>> mapStructureGenericReference(
            List<Pair<IdentifierString, SourceTextContext>> pairs, IdentifierPoolFactory factory,
            Stack<ReferenceElement> referenceStack) {
        return pairs.stream().map(pair -> {
            IdentifierString identifier = pair.getKey();
            ReferenceElement reference = factory.addStructureGeneric(
                    referenceStack, identifier.getValue(), identifier.getPosition());
            return new Pair<>(reference, pair.getValue());
        }).collect(Collectors.toList());
    }

    static Stack<ReferenceElement> resetReferenceStack(
            Stack<ReferenceElement> outerReferenceStack, ReferenceElement thisDefinitionReference,
            boolean atFile, boolean markedStatic) {
        Stack<ReferenceElement> newStack;
        if (atFile || !markedStatic) {
            newStack = new Stack<>();
        } else {
            newStack = CollectionUtil.cloneStack(outerReferenceStack);
        }
        newStack.push(thisDefinitionReference);
        return newStack;
    }

    static boolean skipIf(ListIterator<SourceString> iterator, Operator operator) {
        return CollectionUtil.skipIf(
                iterator, s -> s.getType() == SourceType.OPERATOR && operator.nameEquals(s.getValue()));
    }

    static void notNullValid(Object obj, String name) {
        if (obj == null) {
            throw new CompilerException("not complete: " + name);
        }
    }

    static void notEqualsValid(int id, int illegal, String name) {
        if (id == illegal) {
            throw new CompilerException("not complete: " + name);
        }
    }

    AccessControl getPermissions();

    Embellish getEmbellish();

    ReferenceElement getIdentifierReference();
}
